package com.test.uphubfragmentarch.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.test.uphubfragmentarch.data.db.model.User
import com.test.uphubfragmentarch.data.db.model.UserFollowing

@Dao
abstract class UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(user: List<User>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertFollowing(following: UserFollowing)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertFollowing(following: List<UserFollowing>)

    @Query("SELECT * FROM user")
    abstract fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE id = :id")
    abstract fun getUser(id: Int): User?

    @Query("SELECT * FROM user WHERE (name LIKE '%' || :search || '%') OR (login LIKE '%' || :search || '%') ORDER BY followers DESC")
    abstract fun searchPattern(search: String): List<User>

    @Query("SELECT * FROM user WHERE id = :id")
    abstract fun listen(id: Int): LiveData<User>

    @Query("SELECT login FROM user WHERE id = :id")
    abstract fun getLogin(id: Int): String

    @Query("SELECT * FROM user WHERE id IN (SELECT followerId FROM userFollowing WHERE userId = :id)")
    abstract fun listenFollowers(id: Int): LiveData<List<User>>

    @Query("SELECT * FROM user WHERE id IN (SELECT userId FROM userFollowing WHERE followerId = :id)")
    abstract fun listenSubscription(id: Int): LiveData<List<User>>

    fun searchUser(search: String): List<User> = if (search.isNotEmpty()) searchPattern(search) else getAll()

    fun updateUser(newUser: User) {
        insert(getUser(newUser.id)?.let {
            newUser.merge(it)
        } ?: newUser)
    }
}