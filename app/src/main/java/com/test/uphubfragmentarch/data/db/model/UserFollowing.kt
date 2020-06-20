package com.test.uphubfragmentarch.data.db.model

import androidx.room.Entity

@Entity(primaryKeys = ["userId", "followerId"])
data class UserFollowing(
        val userId: Int,
        val followerId: Int
)