package org.timer.main.domain.summary

interface GenerateDailySummaryUseCase {
    suspend operator fun invoke(activity: DailyActivity): String
}

class DefaultGenerateDailySummaryUseCase(
    private val summaryGateway: SummaryGateway,
) : GenerateDailySummaryUseCase {

    override suspend fun invoke(activity: DailyActivity): String {
        val raw = summaryGateway.generate(SYSTEM_PROMPT, buildUserPrompt(activity))
        return takeSentences(raw, SENTENCES)
    }

    companion object {
        const val SENTENCES = 5
        const val MAX_DONE_TASKS_PER_PROJECT = 5

        val SYSTEM_PROMPT = """
            You are a warm, upbeat coach inside "TomatoPaws", a cat-themed Pomodoro focus app.
            Write exactly $SENTENCES short sentences that celebrate what the person did today.
            Mention their focus sessions, finished tasks and projects, and the physical or mindful
            activities they did during breaks. Use the real numbers from the stats.
            Only praise things listed under "Done today". Never say the person did something listed
            under "Not done yet today"; gently encourage one of those for next time instead.
            Plain text only: no lists, no numbering, no headings, no quotes around the answer.
        """.trimIndent()

        fun buildUserPrompt(activity: DailyActivity): String = buildString {
            // Small local models invent activity when shown "0" counts, so zeros are listed separately.
            val stats = listOf(
                "focus sessions (pomodoros) completed" to activity.pomodorosCompleted,
                "tasks completed" to activity.tasksCompleted,
                "high-energy exercises during breaks" to activity.highEnergyExercises,
                "low-energy exercises during breaks" to activity.lowEnergyExercises,
                "dances during breaks" to activity.dances,
                "clear-your-mind sessions during breaks" to activity.clearYourMindSessions,
            )
            val (done, notDone) = stats.partition { it.second > 0 }
            appendLine("Done today:")
            if (done.isEmpty()) appendLine("- nothing tracked yet, the day is just getting started")
            done.forEach { (label, count) -> appendLine("- $count $label") }
            if (notDone.isNotEmpty()) {
                appendLine("Not done yet today: ${notDone.joinToString(", ") { it.first }}")
            }
            if (activity.projects.isNotEmpty()) {
                appendLine("Projects:")
                activity.projects.forEach { project ->
                    val done = project.doneTasks.take(MAX_DONE_TASKS_PER_PROJECT)
                    val doneText = if (done.isEmpty()) "none yet" else done.joinToString("; ")
                    appendLine("- ${project.name}: done tasks: $doneText; open tasks: ${project.openTasks}")
                }
            }
            append("Write the $SENTENCES-sentence encouraging summary now.")
        }

        fun takeSentences(text: String, count: Int): String {
            val sentences = Regex("""[^.!?]+[.!?]+["')\]]*""")
                .findAll(text.trim())
                .map { it.value.trim() }
                .filter { it.isNotEmpty() }
                .toList()
            return if (sentences.size > count) sentences.take(count).joinToString(" ") else text.trim()
        }
    }
}
