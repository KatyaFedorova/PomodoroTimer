// Production "Praise me": calls the generateDailySummary Cloud Function, which holds the model key.
import { app } from "./firebase-init.js";
import { getFunctions, httpsCallable } from "https://www.gstatic.com/firebasejs/11.10.0/firebase-functions.js";

const generateDailySummary = httpsCallable(getFunctions(app, "us-central1"), "generateDailySummary");

window.aiSummaryBridge = {
    generate: async (userPrompt) => {
        try {
            const result = await generateDailySummary({ userPrompt });
            return { success: true, text: result.data?.text ?? "", error: null };
        } catch (error) {
            console.error("[AiSummary] generate error:", error);
            return { success: false, text: null, error: error.message || "Unknown error" };
        }
    }
};
