"""Eval for the "Praise me" daily summary. See rubric.md for the criteria.

Generates a summary for every case in the golden dataset (DATASET) the same way production does
(system prompt from functions/prompts, user prompt mirrored from GenerateDailySummaryUseCase.kt,
model and prompt layout from functions/src/index.ts), then scores it: word count in Python, the
other four criteria with deepEval LLM-as-a-judge. All models are open source and free-tier.

One judge model (JUDGE) scores everything, so scores are comparable across cases and runs.

Setup:
    python3.12 -m venv .venv && source .venv/bin/activate && pip install deepeval litellm
    export GOOGLE_API_KEY=...   # Google AI Studio key, project WITHOUT billing (free tier): generator
    export GROQ_API_KEY=...     # Groq free tier: judges

Usage:
    python run_eval.py                          # full run
    python run_eval.py --no-judge               # generation + word count only (no Groq quota used)
    python run_eval.py --summaries results/<stamp>-summaries.jsonl   # judge saved summaries, no generation
    python run_eval.py --cases GS-03,GS-06      # subset
    python run_eval.py --prompt payload         # send the golden set's payload (adds local time + focus minutes)
"""
import argparse
import json
import os
import re
import sys
import threading
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from pathlib import Path

HERE = Path(__file__).parent
GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models"
SUMMARY_MODEL = "gemma-4-26b-a4b-it"   # functions/src/index.ts SUMMARY_MODEL (production)
# LiteLLM names, Groq free tier. Different families from Gemma; each has its own rate-limit quota.
JUDGE = "groq/openai/gpt-oss-120b"     # LiteLLM name, Groq free tier; different family from Gemma
GENERATION_WORKERS = 3
PROMPT_FILE = HERE.parent.parent / "functions" / "prompts" / "daily_summary_system_v1.txt"
DATASET = "golden_dataset.jsonl"       # version + timestamp live in each case ("dataset_version")
MAX_WORDS = 60
RESPONSE_TIME_TARGET_S = 3.0
RUBRIC_VERSION = "v3 · 2026-10-15 18:24 +08:00"  # keep in sync with **Version** in rubric.md
HARD = ("accuracy", "motivating_fun")  # rubric.md: hard gates

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


def generate(user_prompt, attempts=5):
    # Same layout as buildPrompt() in functions/src/index.ts: system prompt first in the user turn.
    body = json.dumps({
        "contents": [{"role": "user", "parts": [{"text": f"{SYSTEM_PROMPT}\n\n{user_prompt}"}]}],
        # minimal thinking: otherwise Gemma 4 can spend all output tokens on hidden reasoning and return nothing
        "generationConfig": {"temperature": 0.7, "maxOutputTokens": 400, "thinkingConfig": {"thinkingLevel": "minimal"}},
    }).encode()
    for attempt in range(attempts):
        req = urllib.request.Request(f"{GEMINI_URL}/{SUMMARY_MODEL}:generateContent", body,
                                     {"Content-Type": "application/json", "x-goog-api-key": os.environ["GOOGLE_API_KEY"]})
        started = time.perf_counter()
        try:
            with urllib.request.urlopen(req, timeout=120) as response:
                parts = json.load(response)["candidates"][0]["content"]["parts"]
            content = "".join(p.get("text", "") for p in parts if not p.get("thought"))
            return take_sentences(content), round(time.perf_counter() - started, 2)
        except urllib.error.HTTPError as e:
            if e.code not in (429, 500, 503) or attempt == attempts - 1:
                raise
            time.sleep(5 * (attempt + 1))  # free-tier rate limit: back off and retry


def word_checks(output, seconds):
    words = len(output.split())
    return {"word_count": words, "max_60_words": words <= MAX_WORDS,
            "response_time_s": seconds, "response_time_ok": seconds < RESPONSE_TIME_TARGET_S}


def build_judge(judge_model):
    """One judge model with its own metric objects (metrics aren't shared between threads)."""
    from deepeval.metrics import GEval
    from deepeval.models import LiteLLMModel
    from deepeval.test_case import SingleTurnParams

    class GroqJudge(LiteLLMModel):
        """Groq doesn't support logprobs; raising AttributeError makes GEval use plain score + reason."""
        def generate_raw_response(self, *args, **kwargs):
            raise AttributeError("logprobs unsupported on Groq")

        async def a_generate_raw_response(self, *args, **kwargs):
            raise AttributeError("logprobs unsupported on Groq")

    # deepEval doesn't pass GROQ_API_KEY on by itself; retries with backoff when the free-tier limit returns 429.
    model = GroqJudge(model=judge_model, api_key=os.environ["GROQ_API_KEY"], temperature=0,
                      generation_kwargs={"num_retries": 8})
    params = [SingleTurnParams.INPUT, SingleTurnParams.ACTUAL_OUTPUT, SingleTurnParams.EXPECTED_OUTPUT]

    def metric(name, threshold, steps, evaluation_params=params):
        # threshold is on deepEval's 0..1 scale; score_1_to_5 = 1 + 4 * score
        return GEval(name=name, model=model, evaluation_params=evaluation_params, threshold=threshold,
                     evaluation_steps=steps, async_mode=False)

    per_case = {
        "accuracy": metric("Completeness & accuracy", 0.75, [
            "The input is the person's real activity data for today. The expected output is a reference answer.",
            "Check every number and task name in the actual output against the input. Any invented, wrong or "
            "inflated count, task, break or project is a severe failure (score 1).",
            "Check how much of the input data is used: all counts, focus minutes, done tasks and open tasks.",
            "Score 1: under 50% of the data used, or invented data, or no focus time in minutes. "
            "Score 3: about 70% used, all correct, focus minutes mentioned. "
            "Score 5: all data used correctly, focus minutes mentioned, and it asks the person a question.",
        ]),
        "motivating_fun": metric("Motivating, fun & encouraging", 0.75, [
            "Judge the tone of the actual output for a cat-themed focus app.",
            "Score 1: no jokes, no praise for done tasks, no encouragement to finish what remains, no question. "
            "Score 3: lists done tasks but no jokes, no encouragement to finish, no question. "
            "Score 5: praises done tasks, names what still remains, uses humour, and asks a question.",
            "Guilt-tripping or pressure to keep working late lowers the score.",
        ]),
        "time_appropriate": metric("Time-appropriate", 0.5, [
            "The input includes the local time. Judge whether the actual output fits that time of day.",
            "Saying 'tomorrow' or talking as if the day is over before evening is a failure.",
            "Late at night it should suggest resting, not more work.",
            "Score 1: ignores the time of day. Score 3: reflects it implicitly. "
            "Score 5: mentions the time of day and adjusts the motivation to it.",
        ]),
    }
    diversity = metric("Diverse language & structure", 0.5, [
        "The actual output is a numbered list of summaries the app wrote for different days.",
        "Compare their openings, recurring phrases (e.g. 'You crushed', 'Wow') and sentence structure.",
        "Score 1: same style of language and the same structure every time. "
        "Score 3: repetitive, similar structure. Score 5: mostly unique language and varied structure.",
    ], evaluation_params=[SingleTurnParams.ACTUAL_OUTPUT])
    return per_case, diversity


def score(metric_obj, test_case, attempts=8):
    for attempt in range(attempts):
        try:
            metric_obj.measure(test_case)
            break
        except Exception as e:  # Groq free tier: tokens-per-minute limit -> wait as told, then retry
            if "rate_limit" not in str(e) and "RateLimit" not in type(e).__name__ or attempt == attempts - 1:
                raise
            wait = re.search(r"try again in ([\d.]+)s", str(e))
            time.sleep((float(wait[1]) if wait else 10) + 1)
    # A judge that says it saw no response has failed, not scored 1: don't save that as a score.
    if re.search(r"no (response|output)", metric_obj.reason or "", re.IGNORECASE):
        raise RuntimeError(f"{metric_obj.name}: judge reported an empty input: {metric_obj.reason}")
    return {"score_1_to_5": round(1 + 4 * metric_obj.score, 1), "passed": metric_obj.is_successful(),
            "reason": metric_obj.reason}


def judge_runs(judge_model, jobs):
    """Scores (case, run) jobs sequentially on one judge model."""
    from deepeval.test_case import LLMTestCase

    per_case, _ = build_judge(judge_model)
    for case, run in jobs:
        # The judge always sees the full real data (incl. local time), even if the app's prompt omits some of it.
        test_case = LLMTestCase(input=case["input_payload"], actual_output=run["output"],
                                expected_output=case["golden_output"])
        run["judge_model"] = judge_model
        run["judge"] = {key: score(m, test_case) for key, m in per_case.items()}
        print(f"  judged {case['id']} with {judge_model.split('/')[-1]}", flush=True)


def judge_diversity(judge_model, outputs):
    from deepeval.test_case import LLMTestCase

    _, diversity = build_judge(judge_model)
    listing = "\n".join(f"{n}. {text}" for n, text in enumerate(outputs, 1))
    return score(diversity, LLMTestCase(input="Summaries for different days", actual_output=listing))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--cases", help="comma-separated case ids, e.g. GS-03,GS-06")
    parser.add_argument("--no-judge", action="store_true", help="skip deepEval judges (word count only)")
    parser.add_argument("--summaries", help="judge summaries saved by an earlier run instead of generating")
    parser.add_argument("--prompt", choices=["app", "payload"], default="app",
                        help="app: exact prompt the app sends today; payload: the golden set's payload text")
    args = parser.parse_args()
    started = time.perf_counter()

    cases = [json.loads(line) for line in (HERE / DATASET).read_text(encoding="utf-8").splitlines()]
    if args.cases:
        wanted = set(args.cases.split(","))
        cases = [c for c in cases if c["id"] in wanted]

    # 1. Generate one summary per case, in parallel. Diversity is judged across this run's summaries,
    #    so no case is repeated.
    jobs = []
    for case in cases:
        user_prompt = build_app_user_prompt(case["input"]) if args.prompt == "app" else case["input_payload"]
        case["user_prompt"] = user_prompt
        case["runs"] = [{}]
        jobs += [(case, run) for run in case["runs"]]

    # Every summary is kept in `summaries` and appended to a file as soon as it exists, so a crash
    # later in the run (e.g. while judging) doesn't lose them.
    out_dir = HERE / "results"
    out_dir.mkdir(exist_ok=True)
    stamp = f"{datetime.now():%Y%m%d-%H%M%S}-{args.prompt}"
    summaries_file = out_dir / f"{stamp}-summaries.jsonl"
    summaries = []
    lock = threading.Lock()

    def generate_job(job):
        case, run = job
        output, seconds = generate(case["user_prompt"])
        run.update({"output": output, **word_checks(output, seconds)})
        entry = {"id": case["id"], "output": output, "response_time_s": seconds}
        with lock:
            summaries.append(entry)
            with summaries_file.open("a", encoding="utf-8") as f:
                f.write(json.dumps(entry, ensure_ascii=False) + "\n")

    if args.summaries:
        saved = {e["id"]: e for e in map(json.loads, Path(args.summaries).read_text(encoding="utf-8").splitlines())}
        for case, run in jobs:
            entry = saved.get(case["id"], {"output": "", "response_time_s": 0.0})
            run.update({"output": entry["output"], **word_checks(entry["output"], entry["response_time_s"])})
        print(f"Loaded {len(saved)} saved summaries from {args.summaries}", flush=True)
    else:
        with ThreadPoolExecutor(GENERATION_WORKERS) as pool:
            list(pool.map(generate_job, jobs))
    empty = [case["id"] for case, run in jobs if not run["output"]]
    print(f"{len(jobs)} summaries ready in {time.perf_counter() - started:.0f}s"
          + (f"  (empty: {', '.join(empty)})" if empty else ""), flush=True)
    jobs = [(case, run) for case, run in jobs if run["output"]]  # an empty summary can't be judged; it fails

    # 2. Judge with one model. Diversity runs after the per-case judging, never in parallel with it:
    #    concurrent calls on the same rate-limited quota made it see an empty input before.
    diversity = None
    if not args.no_judge:
        judge_runs(JUDGE, jobs)
        diversity = judge_diversity(JUDGE, [run["output"] for _, run in jobs])

    # 3. Report.
    results = []
    for case in cases:
        passed = all(run["output"] and (args.no_judge or run["judge"][k]["passed"])
                     for run in case["runs"] for k in HARD)
        results.append({"id": case["id"], "scenario": case["scenario"], "prompt_mode": args.prompt,
                        "user_prompt": case["user_prompt"], "runs": case["runs"], "passed": passed})
        first = case["runs"][0]
        scores = " ".join(f"{k[:4]}={v['score_1_to_5']}" for k, v in first.get("judge", {}).items())
        print(f"{case['id']:6} {'PASS' if passed else 'FAIL'}  {first['word_count']:3}w  "
              f"{first['response_time_s']:5.1f}s  {scores}  {case['scenario']}")

    all_runs = [run for case in cases for run in case["runs"]]
    judged = [r for r in all_runs if "judge" in r]
    averages = {k: round(sum(r["judge"][k]["score_1_to_5"] for r in judged) / len(judged), 2)
                for k in ("accuracy", "motivating_fun", "time_appropriate")} if judged else {}
    out_file = out_dir / f"{stamp}.json"
    out_file.write_text(json.dumps({
        "dataset": DATASET, "dataset_version": cases[0]["dataset_version"] if cases else None,
        "rubric_version": RUBRIC_VERSION, "prompt_file": PROMPT_FILE.name, "model": SUMMARY_MODEL,
        "judge": None if args.no_judge else JUDGE,
        "summaries_from": args.summaries, "passed": sum(r["passed"] for r in results),
        "total": len(results), "average_scores": averages, "diversity": diversity, "cases": results,
    }, ensure_ascii=False, indent=2))

    print(f"\nHard-gate pass: {sum(r['passed'] for r in results)}/{len(results)}  |  "
          f"<=60 words: {sum(r['max_60_words'] for r in all_runs)}/{len(all_runs)}  |  "
          f"<3s: {sum(r['response_time_ok'] for r in all_runs)}/{len(all_runs)}")
    if averages:
        print("Average (1-5): " + "  ".join(f"{k}={v}" for k, v in averages.items())
              + f"  diversity={diversity['score_1_to_5']}")
    print(f"Saved {out_file.relative_to(HERE)}  ({time.perf_counter() - started:.0f}s)")
    sys.exit(0 if all(r["passed"] for r in results) else 1)  # CI fails when any hard gate fails


if __name__ == "__main__":
    main()
