package edu.nd.pmcburne.hwapp.one

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() { // creating database to store game information for instances where the app may go offline, etc.
    abstract fun gameDao(): GameDAO

    companion object {

        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "basketball_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}