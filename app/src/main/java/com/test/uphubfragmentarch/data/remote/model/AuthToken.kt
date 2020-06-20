package com.test.uphubfragmentarch.data.remote.model

data class AuthToken(
        val app: App,
        val created_at: String,
        val fingerprint: String,
        val hashed_token: String,
        val id: Int,
        val note: String,
        val note_url: String,
        val scopes: List<String>,
        val token: String,
        val token_last_eight: String,
        val updated_at: String,
        val url: String
)

data class App(
        val client_id: String,
        val name: String,
        val url: String
)
