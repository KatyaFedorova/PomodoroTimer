package org.timer.main.domain.feedback

import dev.gitlive.firebase.*
import dev.gitlive.firebase.auth.*
import dev.gitlive.firebase.firestore.*

actual class FirebaseFeedbackGateway actual constructor() : FeedbackGateway {

    override suspend fun sendEmail(stars: Int, message: String) {
        val user = Firebase.auth.currentUser ?: throw RuntimeException("User not authenticated")
        val emailDoc = hashMapOf(
            "to" to listOf("tomato.paws4@gmail.com","bigfatmonkey1992@gmail.com"),
            "message" to mapOf(
                "subject" to "New feedback from ${user.email ?: "Anonymous"}",
                "text" to "Stars: $stars\nMessage: $message",
                "html" to "<p>⭐ $stars</p><p>$message</p>"
            ),
            "stars" to stars,
            "userId" to user.uid,
            "email" to (user.email ?: ""),
            "createdAt" to FieldValue.serverTimestamp
        )

        Firebase.firestore.collection("mail").add(emailDoc)
    }
}