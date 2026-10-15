"""Eval for the "Praise me" daily summary. See rubric_v1.md for the criteria.

Generates a summary for every case in the golden dataset (DATASET) the same way production does
(system prompt from functions/prompts, user prompt mirrored from GenerateDailySummaryUseCase.kt,
model and prompt layout from functions/src/index.ts), then scores it with deterministic checks
and deepEval LLM-as-a-judge metrics. All models are open source and free-tier.

Setup:
    pip install deepeval litellm
    export GOOGLE_API_KEY=...   # Google AI Studio key, project WITHOUT billing (free tier): generator
    export GROQ_API_KEY=...     # Groq free tier: judge

Usage:
    python run_eval.py                          # Gemma generator + Groq gpt-oss-120b judge
    python run_eval.py --no-judge               # deterministic checks only (no deepEval/Groq needed)
    python run_eval.py --cases GS-03,GS-06      # subset
    python run_eval.py --prompt payload         # send the golden set's payload (adds local time + focus minutes)
"""
import argparse
import json
import os
import re
import sys
import time
import urllib.request
from datetime import datetime
from pathlib import Path

HERE = Path(__file__).parent
GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models"
SUMMARY_MODEL = "gemma-4-26b-a4b-it"           # functions/src/index.ts SUMMARY_MODEL (production)
DEFAULT_JUDGE = "groq/openai/gpt-oss-120b"     # LiteLLM name for Groq free tier; different family from Gemma
JUDGE_PAUSE_S = float(os.environ.get("JUDGE_PAUSE_S", "20"))  # Groq free tier: ~8k tokens/minute
PROMPT_FILE = HERE.parent.parent / "functions" / "prompts" / "daily_summary_system_v1.txt"
DATASET = "golden_dataset_v1.jsonl"  # bump when a new dataset version is added
MAX_WORDS = 60
RESPONSE_TIME_TARGET_S = 3.0

SYSTEM_PROMPT = PROMPT_FILE.read_text(encoding="utf-8").strip()  # same file the Cloud Function uses

# ---- Mirror of GenerateDailySummaryUseCase.kt: keep in sync when the app prompt changes ----
SENTENCES = 5
MAX_DONE_TASKS_PER_PROJECT = 5


def build_app_user_prompt(i):
    b = i["breaks"]
    stats = [
        ("focus sessions (pomodoros) completed", i["focus_sessions"]),
        ("tasks completed", i["tasks_completed"]),
        ("high-energy exercises during breaks", b["high_energy"]),
        ("low-energy exercises during breaks", b["low_energy"]),
        ("dances during breaks", b["dance"]),
        ("clear-your-mind sessions during breaks", b["clear_mind"]),
    ]
    done = [s for s in stats if s[1] > 0]
    not_done = [s for s in stats if s[1] <= 0]
    lines = ["Done today:"]
    if not done:
        lines.append("- nothing tracked yet, the day is just getting started")
    lines += [f"- {count} {label}" for label, count in done]
    if not_done:
        lines.append("Not done yet today: " + ", ".join(label for label, _ in not_done))
    if i["projects"]:
        lines.append("Projects:")
        for p in i["projects"]:
            tasks = p["done"][:MAX_DONE_TASKS_PER_PROJECT]
            done_text = "; ".join(tasks) if tasks else "none yet"
            lines.append(f"- {p['name']}: done tasks: {done_text}; open tasks: {p['open']}")
    lines.append(f"Write the {SENTENCES}-sentence encouraging summary now.")
    return "\n".join(lines)


def take_sentences(text, count=SENTENCES):
    sentences = [s.strip() for s in re.findall(r"""[^.!?]+[.!?]+["')\]]*""", text.strip()) if s.strip()]
    return " ".join(sentences[:count]) if len(sentences) > count else text.strip()
# ---- end mirror ----


def generate(user_prompt):
    # Same layout as buildPrompt() in functions/src/index.ts: system prompt first in the user turn.
    body = json.dumps({
        "contents": [{"role": "user", "parts": [{"text": f"{SYSTEM_PROMPT}\n\n{user_prompt}"}]}],
        "generationConfig": {"temperature": 0.7, "maxOutputTokens": 400},
    }).encode()
    req = urllib.request.Request(f"{GEMINI_URL}/{SUMMARY_MODEL}:generateContent", body,
                                 {"Content-Type": "application/json", "x-goog-api-key": os.environ["GOOGLE_API_KEY"]})
    started = time.perf_counter()
    with urllib.request.urlopen(req, timeout=120) as response:
        parts = json.load(response)["candidates"][0]["content"]["parts"]
    content = "".join(p.get("text", "") for p in parts if not p.get("thought"))
    return take_sentences(content), round(time.perf_counter() - started, 2)


def deterministic_checks(case, output, seconds):
    words = len(output.split())
    banned = [p for p in case["banned_phrases"] if p.lower() in output.lower()]
    return {
        "word_count": words,
        "max_60_words": words <= MAX_WORDS,
        "banned_phrases_found": banned,
        "no_banned_phrases": not banned,
        "response_time_s": seconds,
        "response_time_ok": seconds < RESPONSE_TIME_TARGET_S,
    }


def build_judge_metrics(judge_model):
    from deepeval.metrics import GEval
    from deepeval.models import LiteLLMModel
    from deepeval.test_case import LLMTestCaseParams

    judge = LiteLLMModel(model=judge_model, temperature=0)  # LiteLLM reads GROQ_API_KEY
    params = [LLMTestCaseParams.INPUT, LLMTestCaseParams.ACTUAL_OUTPUT, LLMTestCaseParams.EXPECTED_OUTPUT]
    # threshold is on deepEval's 0..1 scale; score_1_to_5 = 1 + 4 * score
    return {
        "accuracy": GEval(
            name="Completeness & accuracy", model=judge, evaluation_params=params, threshold=0.75,
            evaluation_steps=[
                "The input is the person's real activity data for today. The expected output is a reference answer.",
                "Check every number and task name in the actual output against the input. Any invented, wrong or "
                "inflated count, task, break or project is a severe failure (score 1).",
                "Check how much of the input data is used: all counts, focus minutes, done tasks and open tasks.",
                "Score 1: under 50% of the data used, or invented data, or no focus time in minutes. "
                "Score 3: about 70% used, all correct, focus minutes mentioned. "
                "Score 5: all data used correctly, focus minutes mentioned, and it asks the person a question.",
            ]),
        "motivating_fun": GEval(
            name="Motivating, fun & encouraging", model=judge, evaluation_params=params, threshold=0.75,
            evaluation_steps=[
                "Judge the tone of the actual output for a cat-themed focus app.",
                "Score 1: no jokes, no praise for done tasks, no encouragement to finish what remains, no question. "
                "Score 3: lists done tasks but no jokes, no encouragement to finish, no question. "
                "Score 5: praises done tasks, names what still remains, uses humour, and asks a question.",
                "Guilt-tripping or pressure to keep working late lowers the score.",
            ]),
        "time_appropriate": GEval(
            name="Time-appropriate", model=judge, evaluation_params=params, threshold=0.5,
            evaluation_steps=[
                "The input may include the local time. Judge whether the actual output fits that time of day.",
                "Saying 'tomorrow' or talking as if the day is over before evening is a failure.",
                "Late at night it should suggest resting, not more work.",
                "Score 1: ignores the time of day. Score 3: reflects it implicitly. "
                "Score 5: mentions the time of day and adjusts the motivation to it.",
            ]),
    }


def judge(metrics, case, output):
    from deepeval.test_case import LLMTestCase

    # The judge always sees the full real data (incl. local time), even if the app's prompt omits some of it.
    test_case = LLMTestCase(input=case["input_payload"], actual_output=output, expected_output=case["golden_output"])
    results = {}
    for key, metric in metrics.items():
        metric.measure(test_case)
        results[key] = {"score_1_to_5": round(1 + 4 * metric.score, 1), "passed": metric.is_successful(),
                        "reason": metric.reason}
    return results


def first_words(text, n=3):
    return " ".join(re.findall(r"[\w']+", text.lower())[:n])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--cases", help="comma-separated case ids, e.g. GS-03,GS-06")
    parser.add_argument("--no-judge", action="store_true", help="skip deepEval LLM-as-a-judge metrics")
    parser.add_argument("--judge-model", default=DEFAULT_JUDGE, help="LiteLLM model name of the judge")
    parser.add_argument("--prompt", choices=["app", "payload"], default="app",
                        help="app: exact prompt the app sends today; payload: the golden set's payload text")
    args = parser.parse_args()

    cases = [json.loads(line) for line in (HERE / DATASET).read_text(encoding="utf-8").splitlines()]
    if args.cases:
        wanted = set(args.cases.split(","))
        cases = [c for c in cases if c["id"] in wanted]
    metrics = None if args.no_judge else build_judge_metrics(args.judge_model)

    results = []
    for case in cases:
        user_prompt = build_app_user_prompt(case["input"]) if args.prompt == "app" else case["input_payload"]
        runs = []
        for _ in range(case.get("repeat", 1)):
            output, seconds = generate(user_prompt)
            run = {"output": output, **deterministic_checks(case, output, seconds)}
            if metrics:
                run["judge"] = judge(metrics, case, output)
                time.sleep(JUDGE_PAUSE_S)  # stay under Groq's free-tier tokens-per-minute limit
            runs.append(run)
        openings = [first_words(r["output"]) for r in runs]
        diverse = len(set(openings)) == len(openings) if len(runs) > 1 else None
        hard_pass = all(r["no_banned_phrases"] and all(
            r.get("judge", {}).get(k, {}).get("passed", True) for k in ("accuracy", "motivating_fun")) for r in runs)
        results.append({"id": case["id"], "scenario": case["scenario"], "prompt_mode": args.prompt,
                        "user_prompt": user_prompt, "runs": runs, "diverse_openings": diverse, "passed": hard_pass})
        print(f"{case['id']:6} {'PASS' if hard_pass else 'FAIL'}  {runs[0]['word_count']:3}w  "
              f"{runs[0]['response_time_s']:5.1f}s  banned={runs[0]['banned_phrases_found'] or '-'}  {case['scenario']}",
              flush=True)

    out_dir = HERE / "results"
    out_dir.mkdir(exist_ok=True)
    out_file = out_dir / f"{datetime.now():%Y%m%d-%H%M%S}-{args.prompt}.json"
    out_file.write_text(json.dumps({
        "dataset": DATASET, "prompt_file": PROMPT_FILE.name, "model": SUMMARY_MODEL,
        "judge": None if args.no_judge else args.judge_model,
        "passed": sum(r["passed"] for r in results), "total": len(results), "cases": results,
    }, ensure_ascii=False, indent=2))
    all_runs = [run for r in results for run in r["runs"]]
    print(f"\nHard-gate pass: {sum(r['passed'] for r in results)}/{len(results)}  |  "
          f"<=60 words: {sum(run['max_60_words'] for run in all_runs)}/{len(all_runs)}  |  "
          f"<3s: {sum(run['response_time_ok'] for run in all_runs)}/{len(all_runs)}")
    print(f"Saved {out_file.relative_to(HERE)}")
    sys.exit(0 if all(r["passed"] for r in results) else 1)  # CI fails when any hard gate fails


if __name__ == "__main__":
    main()
