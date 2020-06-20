package com.test.uphubfragmentarch.data.remote.model

import com.google.gson.annotations.SerializedName
import com.test.uphubfragmentarch.data.db.model.Repository
import java.text.SimpleDateFormat
import java.util.*

data class ApiRepository(
    val id: Int,
    val name: String,
    val owner: ApiUser,
    val private: Boolean,
    val description: String,
    val fork: Boolean,
    @SerializedName("created_at") val createdAt: String?
) {

    fun asGeneralModel() = Repository(
        id,
        name,
        owner.id,
        private,
        description,
        fork,
        parseGithubTime(createdAt ?: "").time
    )

    private fun parseGithubTime(tm: String): Date = try {
        tm.takeIf {
            it.isNotBlank()
        }?.let {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(it)
        } ?: Date(0)
    } catch (ex: Exception) {
        Date(0)
    }
}
