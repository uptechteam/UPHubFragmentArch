package com.test.uphubfragmentarch.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.test.uphubfragmentarch.data.db.model.Repository

@Dao
abstract class RepositoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(repos: Repository)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(repos: List<Repository>)

    @Query("SELECT * FROM repository")
    abstract fun getAll(): List<Repository>

    @Query("SELECT * FROM repository WHERE (name LIKE '%' || :search || '%') OR (description LIKE '%' || :search || '%') ORDER BY createdAt DESC")
    abstract fun searchPattern(search: String): List<Repository>

    @Query("SELECT * FROM repository WHERE id = :id")
    abstract fun getRepos(id: Int): Repository?

    @Query("SELECT * FROM repository WHERE ownerId = :id ORDER BY createdAt DESC")
    abstract fun listenUser(id: Int): LiveData<List<Repository>>

    fun searchRepos(search: String): List<Repository> = if (search.isNotEmpty()) searchPattern(search) else getAll()

    fun updateRepository(newRepo: Repository) =
            insert(getRepos(newRepo.id)?.let {
                newRepo.merge(it)
            } ?: newRepo)
}