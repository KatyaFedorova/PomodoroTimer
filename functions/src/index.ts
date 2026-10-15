import { readFileSync } from "fs";
import { join } from "path";
import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { defineSecret } from "firebase-functions/params";
import { HttpsError, onCall } from "firebase-functions/v2/https";

initializeApp();

// Free-tier Google AI Studio key from a separate project with NO billing (billing switches it to paid rates).
const geminiApiKey = defineSecret("GEMINI_API_KEY");

export const SUMMARY_MODEL = "gemma-4-26b-a4b-it";
const PROMPT_VERSION = "v1";
const SYSTEM_PROMPT = readFileSync(join(__dirname, `../prompts/daily_summary_system_${PROMPT_VERSION}.txt`), "utf8").trim();
const MAX_USER_PROMPT_CHARS = 3000;
const DAILY_LIMIT_PER_USER = 20;

/** Called by the web app's "Praise me" button. Input: { userPrompt }, output: { text }. */
export const generateDailySummary = onCall(
  { secrets: [geminiApiKey], region: "us-central1", maxInstances: 5, timeoutSeconds: 60 },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "Sign in to get your summary.");

    const userPrompt = request.data?.userPrompt;
    if (typeof userPrompt !== "string" || userPrompt.length === 0 || userPrompt.length > MAX_USER_PROMPT_CHARS) {
      throw new HttpsError("invalid-argument", "userPrompt must be a non-empty string.");
    }

    await countUsage(uid);
    const text = await callGemma(buildPrompt(userPrompt), geminiApiKey.value());
    return { text, model: SUMMARY_MODEL, promptVersion: PROMPT_VERSION };
  },
);

/** Gemma on the Gemini API has no separate system role, so the system prompt goes first in the user turn.
 *  evals/daily-summary/run_eval.py builds the prompt the same way: keep them in sync. */
export function buildPrompt(userPrompt: string): string {
  return `${SYSTEM_PROMPT}\n\n${userPrompt}`;
}

/** Per-user daily cap so one account can't use up the shared free-tier quota. */
async function countUsage(uid: string): Promise<void> {
  const day = new Date().toISOString().slice(0, 10);
  const ref = getFirestore().collection("aiUsage").doc(`${uid}_${day}`);
  await getFirestore().runTransaction(async (tx) => {
    const count: number = (await tx.get(ref)).get("count") ?? 0;
    if (count >= DAILY_LIMIT_PER_USER) {
      throw new HttpsError("resource-exhausted", "You've reached today's summary limit. Come back tomorrow!");
    }
    tx.set(ref, { uid, day, count: count + 1 }, { merge: true });
  });
}

async function callGemma(prompt: string, apiKey: string): Promise<string> {
  const response = await fetch(
    `https://generativelanguage.googleapis.com/v1beta/models/${SUMMARY_MODEL}:generateContent`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json", "x-goog-api-key": apiKey },
      body: JSON.stringify({
        contents: [{ role: "user", parts: [{ text: prompt }] }],
        // minimal thinking: otherwise Gemma 4 can spend all output tokens on hidden reasoning and return nothing
        generationConfig: { temperature: 0.7, maxOutputTokens: 400, thinkingConfig: { thinkingLevel: "minimal" } },
      }),
    },
  );
  if (response.status === 429) throw new HttpsError("resource-exhausted", "The AI is busy right now. Try again in a minute.");
  if (!response.ok) throw new HttpsError("internal", `Model request failed (${response.status}).`);

  const data = await response.json();
  const parts: Array<{ text?: string; thought?: boolean }> = data?.candidates?.[0]?.content?.parts ?? [];
  const text = parts.filter((p) => !p.thought && p.text).map((p) => p.text).join("").trim();
  if (!text) throw new HttpsError("internal", "The model returned an empty answer.");
  return text;
}
