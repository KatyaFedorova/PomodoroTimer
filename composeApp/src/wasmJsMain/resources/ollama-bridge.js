// Local open-source LLM bridge for Kotlin/Wasm. Talks to Ollama on this machine (no cloud).
const OLLAMA_URL = "http://localhost:11434/api/chat";

window.ollamaBridge = {
    generate: async (model, systemPrompt, userPrompt) => {
        try {
            const response = await fetch(OLLAMA_URL, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    model,
                    stream: false,
                    messages: [
                        { role: "system", content: systemPrompt },
                        { role: "user", content: userPrompt }
                    ],
                    options: { temperature: 0.7 }
                })
            });
            if (!response.ok) {
                return { success: false, text: null, error: `Ollama HTTP ${response.status}: ${await response.text()}` };
            }
            const data = await response.json();
            return { success: true, text: data?.message?.content ?? "", error: null };
        } catch (error) {
            console.error("[Ollama] generate error:", error);
            return { success: false, text: null, error: error.message || "Unknown error" };
        }
    }
};
