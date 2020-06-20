package com.test.uphubfragmentarch.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Repository(
    @PrimaryKey val id: Int,
    val name: String,
    val ownerId: Int,
    val isPrivate: Boolean,
    val description: String? = null,
    val isFork: Boolean,
    val createdAt: Long
) {

    fun merge(newRepo: Repository) = Repository(
        id,
        name,
        ownerId,
        isPrivate,
        description ?: newRepo.description ?: "",
        isFork,
        createdAt
    )
}