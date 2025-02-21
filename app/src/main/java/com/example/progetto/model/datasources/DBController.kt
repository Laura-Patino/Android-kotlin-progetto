package com.example.progetto.model.datasources

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.progetto.model.dataclasses.MenuImageWithVersion

@Dao
interface MenuDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuImage(menuImage: MenuImageWithVersion)

    @Query("SELECT * FROM MenuImageWithVersion")
    suspend fun getAllMenusImages(): List<MenuImageWithVersion>

    @Query("SELECT * FROM MenuImageWithVersion WHERE mid = :mid AND imageVersion = :imageVersion LIMIT 1")
    suspend fun getMenuImageByVersion(mid: Int, imageVersion: Int) : MenuImageWithVersion?
}

@Database(entities = [MenuImageWithVersion::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao() : MenuDao //serve come punto di accesso al DAO
}

class DBController(
    context : Context
) {
    private val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "images-database"
    ).build()

    val dao = database.menuDao()
}