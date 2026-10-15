"""Turns a run_eval.py results file into baseline.json, the reference every later run is compared to.

Usage:
    python make_baseline.py results/<stamp>-app.json
    python make_baseline.py --latest            # newest results file
"""
import argparse
import json
import statistics
from datetime import datetime, timezone
from pathlib import Path

HERE = Path(__file__).parent
CRITERIA = ("accuracy", "motivating_fun", "time_appropriate")
TARGETS = {"accuracy": 4, "motivating_fun": 4, "time_appropriate": 3, "diversity": 3}


def latest_results():
    files = sorted(p for p in (HERE / "results").glob("*.json"))
    if not files:
        raise SystemExit("No results/*.json found. Run run_eval.py first.")
    return files[-1]


def summarize(d, results_file):
    """Headline numbers of one results file; shared with compare_baseline.py."""
    runs = [(c["id"], c["scenario"], c["passed"], r) for c in d["cases"] for r in c["runs"]]
    judged = [r for *_, r in runs if "judge" in r]
    return {
        "run": {
            "results_file": str(results_file),
            "summaries_file": d.get("summaries_from"),
            "rubric_version": d["rubric_version"],
            "dataset_version": d["dataset_version"],
            "summary_model": d["model"],
            "judge": d["judge"],
            "prompt_file": d["prompt_file"],
        },
        "scores_1_to_5": {**{k: d["average_scores"][k] for k in CRITERIA},
                          "diversity": (d.get("diversity") or {}).get("score_1_to_5")},
        "targets_1_to_5": TARGETS,
        "pass_counts": {
            "hard_gates": d["passed"],
            **{k: sum(r["judge"][k]["passed"] for r in judged) for k in CRITERIA},
            "max_60_words": sum(r["max_60_words"] for *_, r in runs),
            "under_3s": sum(r["response_time_ok"] for *_, r in runs),
            "total": len(runs),
        },
        "word_count": {"median": statistics.median(r["word_count"] for *_, r in runs),
                       "max": max(r["word_count"] for *_, r in runs)},
        "response_time_s": {"median": statistics.median(r["response_time_s"] for *_, r in runs),
                            "max": max(r["response_time_s"] for *_, r in runs)},
        "diversity_reason": (d.get("diversity") or {}).get("reason"),
        "cases": [{"id": cid, "scenario": sc, "passed": ok, "words": r["word_count"],
                   **{k: r["judge"][k]["score_1_to_5"] for k in CRITERIA if "judge" in r}}
                  for cid, sc, ok, r in runs],
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("results", nargs="?", help="results/<stamp>-app.json")
    parser.add_argument("--latest", action="store_true", help="use the newest results file")
    args = parser.parse_args()
    results_file = latest_results() if args.latest or not args.results else Path(args.results)

    data = json.loads(results_file.read_text(encoding="utf-8"))
    if not data.get("judge"):
        raise SystemExit("This results file has no judge scores (run without --no-judge).")
    baseline = {"created": datetime.now(timezone.utc).astimezone().strftime("%Y-%m-%d %H:%M %z"),
                **summarize(data, results_file.relative_to(HERE) if results_file.is_absolute() else results_file)}
    (HERE / "baseline.json").write_text(json.dumps(baseline, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"baseline.json updated from {results_file}")


if __name__ == "__main__":
    main()
