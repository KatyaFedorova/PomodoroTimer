# Eval changelog: "Praise me" daily summary

Versioning rules:
- Rubric: one file, `rubric.md`. Edit it in place, bump **Version** at its top and
  `RUBRIC_VERSION` in `run_eval.py` to `vN · YYYY-MM-DD HH:MM +TZ` (time of the change), and add
  an entry here. Old versions live in git history.
- Golden dataset: one file, `golden_dataset.jsonl`. Edit it in place, set `dataset_version`
  in every case to `vN · YYYY-MM-DD HH:MM +TZ` (time of the change), and add an entry here.
  Old versions live in git history. `run_eval.py` reads the version from the file.
Every results file records which rubric and dataset version it used.

## Golden dataset

### v1 · 2026-10-15 11:16 +08:00
- 20 cases, GS-01…GS-20, imported from the `TEMP golden set import` sheet.
- Regression cases for dogfood rows 2, 6–11 (`TP Evaluation` → `dogfood`).
- Edge cases: prompt injection (GS-13), Ukrainian task names (GS-14), 5 projects vs the
  60-word limit (GS-15), contradictory input (GS-09, GS-19), nothing left open (GS-17).
- GS-16 runs 5× to check opening diversity.

## Rubric

### v3 · 2026-10-15 18:24 +08:00
- One judge for everything: gpt-oss-120b on Groq free tier (was split across gpt-oss-20b and
  qwen3.8-27b, whose scores disagreed: accuracy 1.4 vs 2.12 on the same baseline).
- Diversity is judged after the per-case criteria, not in parallel; a judge reply saying it saw
  no response is treated as an error, not a score of 1.

### v2 · 2026-10-15 14:34 +08:00
- Diverse language & structure is now judged by the LLM (once per run, over all summaries),
  as in the `criterias` sheet, instead of a "first 3 words" Python check.
- Removed the banned-phrase gate; the accuracy judge covers wrong claims.
- Only ≤ 60 words stays in Python. Hard gates: accuracy, motivating & fun.
- Judges: gpt-oss-20b + qwen3.8-27b on Groq free tier, split for speed (was gpt-oss-120b).

### v1 · 2026-10-15 11:16 +08:00
- 5 criteria from `TP Evaluation` → `criterias`: completeness & accuracy (hard), motivating &
  fun (hard), ≤ 60 words (soft), time-appropriate (soft), diverse language & structure (soft).
- Plus deterministic banned-phrase gate (hard) and response time < 3 s (tracked).
- Pass thresholds are proposals (the sheet's "pass" column was empty).
