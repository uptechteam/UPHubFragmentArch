package com.test.uphubfragmentarch.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.test.uphubfragmentarch.data.db.model.Repository
import com.test.uphubfragmentarch.data.db.model.User
import com.test.uphubfragmentarch.data.db.model.UserFollowing

@Database(entities = [
    Repository::class,
    User::class,
    UserFollowing::class
], version = 1)
abstract class Database : RoomDatabase() {

    abstract fun repositoryDao(): RepositoryDao

    abstract fun userDao(): UserDao
}