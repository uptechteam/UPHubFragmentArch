package com.test.uphubfragmentarch.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.max

@Entity
data class User(
    @PrimaryKey val id: Int,
    val login: String,
    val name: String,
    val company: String,
    val location: String,
    val email: String,
    val bio: String,
    val followers: Int,
    val following: Int,
    val publicRepos: Int,
    val avatarUrl: String,
    val isGlobal: Boolean = false
) {

    fun merge(user: User) = User(
        id,
        login,
        name,
        company,
        location,
        email,
        bio,
        max(followers, user.followers),
        max(following, user.following),
        max(publicRepos, user.publicRepos),
        avatarUrl,
        isGlobal or user.isGlobal
    )
}