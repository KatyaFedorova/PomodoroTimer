# Eval changelog: "Praise me" daily summary

Versioning rule: never edit a released version. Copy it to the next number
(`rubric_v2.md`, `golden_dataset_v2.jsonl`), change the copy, add an entry here, and
point `DATASET` in `run_eval.py` at the new dataset. Rubric and dataset versions move
independently. Every results file records which dataset version it used.

## Golden dataset

### v1 (2026-10-15): `golden_dataset_v1.jsonl`
- 20 cases, GS-01…GS-20, imported from the `TEMP golden set import` sheet.
- Regression cases for dogfood rows 2, 6–11 (`TP Evaluation` → `dogfood`).
- Edge cases: prompt injection (GS-13), Ukrainian task names (GS-14), 5 projects vs the
  60-word limit (GS-15), contradictory input (GS-09, GS-19), nothing left open (GS-17).
- GS-16 runs 5× to check opening diversity.

## Rubric

### v1 (2026-10-15): `rubric_v1.md`
- 5 criteria from `TP Evaluation` → `criterias`: completeness & accuracy (hard), motivating &
  fun (hard), ≤ 60 words (soft), time-appropriate (soft), diverse language & structure (soft).
- Plus deterministic banned-phrase gate (hard) and response time < 3 s (tracked).
- Pass thresholds are proposals (the sheet's "pass" column was empty).
