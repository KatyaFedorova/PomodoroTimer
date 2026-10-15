"""Compares an eval run with baseline.json and decides whether a pull request may merge.

Writes a Markdown table (for the PR comment / job summary) and exits:
    0  no regression, or rubric/dataset version changed (new baseline proposal, not blocking)
    1  regression: a judged average dropped more than TOLERANCE, or fewer cases pass the hard gates

Usage:
    python compare_baseline.py --latest --out comparison.md
    python compare_baseline.py results/<stamp>-app.json
"""
import argparse
import json
import sys
from pathlib import Path

from make_baseline import HERE, latest_results, summarize

TOLERANCE = 0.3  # the judge's scores vary a little between runs; smaller drops are noise
SCORES = ("accuracy", "motivating_fun", "time_appropriate", "diversity")
LABELS = {"accuracy": "Accuracy (hard)", "motivating_fun": "Motivating & fun (hard)",
          "time_appropriate": "Time-appropriate", "diversity": "Diversity"}


def arrow(delta, higher_is_better=True):
    if delta == 0:
        return "="
    return "▲" if (delta > 0) == higher_is_better else "▼"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("results", nargs="?")
    parser.add_argument("--latest", action="store_true")
    parser.add_argument("--out", help="write the Markdown report here")
    args = parser.parse_args()
    results_file = latest_results() if args.latest or not args.results else Path(args.results)

    base = json.loads((HERE / "baseline.json").read_text(encoding="utf-8"))
    new = summarize(json.loads(results_file.read_text(encoding="utf-8")), results_file)
    b_run, n_run = base["run"], new["run"]
    same_exam = (b_run["rubric_version"], b_run["dataset_version"]) == (n_run["rubric_version"], n_run["dataset_version"])

    rows, regressions = [], []
    for k in SCORES:
        b, n = base["scores_1_to_5"].get(k), new["scores_1_to_5"].get(k)
        if b is None or n is None:
            continue
        delta = round(n - b, 2)
        if delta < -TOLERANCE:
            regressions.append(f"{LABELS[k]} dropped {b} → {n}")
        rows.append(f"| {LABELS[k]} | {b} | {n} | {arrow(delta)} {delta:+} |")
    bp, np_ = base["pass_counts"], new["pass_counts"]
    total = np_["total"]
    for key, label in (("hard_gates", "Hard-gate passes"), ("max_60_words", "≤ 60 words"), ("under_3s", "Under 3 s")):
        b_count = bp[key] if isinstance(bp[key], int) else int(str(bp[key]).split("/")[0])
        delta = np_[key] - b_count
        rows.append(f"| {label} | {b_count}/{total} | {np_[key]}/{total} | {arrow(delta)} {delta:+} |")
        if key == "hard_gates" and delta < 0:
            regressions.append(f"hard-gate passes dropped {b_count} → {np_[key]}")

    if not same_exam:
        verdict = ("🟡 **New baseline proposal.** The rubric or golden set version changed, so this run "
                   "isn't compared for regressions. If the numbers look right, run the *Update eval "
                   "baseline* workflow on this branch.")
        failed = False
    elif regressions:
        verdict = "🔴 **Regression, merge blocked:** " + "; ".join(regressions) + "."
        failed = True
    else:
        verdict = f"🟢 **No regression** (tolerance ±{TOLERANCE})."
        failed = False

    report = "\n".join([
        "## Golden set eval vs baseline", "", verdict, "",
        "| Criterion | Baseline | This PR | Change |", "|---|---|---|---|", *rows, "",
        f"- Rubric: baseline `{b_run['rubric_version']}` → this PR `{n_run['rubric_version']}`",
        f"- Golden set: baseline `{b_run['dataset_version']}` → this PR `{n_run['dataset_version']}`",
        f"- Model `{n_run['summary_model']}`, judge `{n_run['judge']}`, prompt `{n_run['prompt_file']}`",
        f"- Words: median {new['word_count']['median']} (baseline {base['word_count']['median']})",
        "",
    ])
    print(report)
    if args.out:
        Path(args.out).write_text(report, encoding="utf-8")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
