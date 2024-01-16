package com.jeju_tour.visit_jeju_app.gpt.model

data class Message(val message: String, val sentBy: String) {
    companion object {
        const val SENT_BY_ME = "me"
        const val SENT_BY_BOT = "bot"
    }
}
