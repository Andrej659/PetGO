package org.unizd.rma.markanjevic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.unizd.rma.markanjevic.dao.AnimalDao
import org.unizd.rma.markanjevic.models.Animal

@Database(entities = [Animal::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "animals"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
