# "Praise me" daily summary: eval rubric v1

**Version:** v1 (2026-10-15) · see `CHANGELOG.md`

Source: `TP Evaluation` → `criterias` sheet (Google Drive). Golden cases: `golden_dataset_v1.jsonl`
(from `TEMP golden set import`, GS-01…GS-20). Runner: `run_eval.py`.

## Criteria

| # | Criterion | Gate | Method | Pass |
|---|---|---|---|---|
| 1 | Completeness & accuracy | **hard** | LLM-as-a-judge | score ≥ 4 *(proposed)* |
| 2 | Motivating, fun & encouraging | **hard** | LLM-as-a-judge | score ≥ 4 *(proposed)* |
| 3 | Not more than 60 words | soft | deterministic | `words ≤ 60` |
| 4 | Time-appropriate | soft | LLM-as-a-judge | score ≥ 3 *(proposed)* |
| 5 | Diverse language & structure | soft | deterministic (across runs) | GS-16 run 5×: no two runs share the same first 3 words |
| — | Banned phrases | **hard** | deterministic | none of the case's `banned_phrases` appear |
| — | Response time | tracked | deterministic | target < 3 s (from `scenarious` sheet; not gated yet) |

A case **passes** when every hard gate passes. Soft criteria are reported, not gated.
The "pass" column in the sheet is empty, so the thresholds marked *(proposed)* are my suggestion.

## Scoring anchors (1 / 3 / 5)

### 1. Completeness & accuracy (hard)
- **1:** less than 50% of the data used; invents data; doesn't mention focus time in minutes.
- **3:** about 70% of the data used; everything stated is correct; mentions focus time in minutes.
- **5:** all data used correctly, no hallucination; asks a question; mentions focus time in minutes.
- **Failure example (dogfood row 2):** input had 0 focus sessions, 1 task, 2 dances; output said
  "a great number of focus sessions", "two challenging tasks… for Google", "tackle tomorrow".

### 2. Motivating, fun & encouraging (hard)
- **1:** no jokes; doesn't encourage finishing remaining tasks; no praise for done tasks; no question.
- **3:** no jokes; no encouragement to finish; lists done tasks; no question.
- **5:** names what still remains; uses jokes; praises done tasks; asks a question.
- **Failure example:** "you did this, you did that, tomorrow you will do that again. good luck"

### 3. Not more than 60 words (soft)
- **Pass:** ≤ 60 words. **Fail:** > 60 words (dogfood rows 2 and 10 were ~90–110 words).

### 4. Time-appropriate (soft)
- **1:** doesn't reflect the time of day.
- **3:** reflects it, but not explicitly.
- **5:** mentions the time of day and adjusts the motivation to it (morning: plenty of day left;
  late night: stop and rest).
- **Failure example:** "you can do it tomorrow" said at noon.

### 5. Diverse language & structure (soft)
- **1:** same wording and structure every time. **3:** repetitive, similar structure.
  **5:** mostly unique wording, varied structure.
- **Failure example:** every output opens with "You crushed" / "Wow". "You crushed" is banned in most cases.

## Known failures from dogfooding (llama3.2:3b, 2026-10-14)
From `TP Evaluation` → `dogfood` "What's wrong", each with a golden regression case:

| Dogfood row | Problem | Golden case |
|---|---|---|
| 2 | 0 focus → "great number"; 1 task → "2"; too long; too slow; no focus time; "tomorrow" mid-day | GS-06 |
| 6 | Didn't mention time spent focused; invented high-energy breaks | GS-03 |
| 7 | "You crushed" in every output; same structure every time; motivating but not fun | GS-04, GS-16 |
| 8 | Invented breaks that didn't happen; talked as if the day was over | GS-05 |
| 9 | (no written note) small day, singular counts | GS-07 |
| 10 | Over 60 words | GS-08 |
| 11 | Heavy data; must fit 60 words without inventing tasks | GS-09 |

## Gaps between the app and this rubric (to fix in the app, not the eval)
- The app's prompt (`GenerateDailySummaryUseCase.kt`) does **not** send `Local time` or `Focus minutes`,
  so criterion 4 and the "focus time in minutes" part of criterion 1 will fail until they are added.
- The app's system prompt asks for **exactly 5 sentences**; the rubric asks for **≤ 60 words** and a
  question. The prompt should be updated to match the rubric.
- The app's prompt says nothing about avoiding "You crushed" or "tomorrow" mid-day.
