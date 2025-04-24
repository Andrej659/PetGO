package org.unizd.rma.markanjevic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.unizd.rma.markanjevic.dao.AnimalDao
import org.unizd.rma.markanjevic.models.Animal

/**
 * Glavna baza podataka aplikacije koja koristi Room biblioteku za pohranu podataka o ljubimcima.
 *
 * Ova klasa predstavlja Room bazu podataka koja sadrži entitet [Animal]. Baza podataka koristi
 * DAO (Data Access Object) za pristup podacima vezanim uz ljubimce. Ovdje je implementirana singleton
 * instanca baze podataka koja se koristi za sprječavanje više instanci baze u isto vrijeme.
 */
@Database(entities = [Animal::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Vraća DAO za pristup podacima o ljubimcima.
     *
     * @return Objekt [AnimalDao] koji omogućuje rad s tabelom ljubimaca.
     */
    abstract fun animalDao(): AnimalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Vraća instancu baze podataka. Ako baza već postoji, vraća postojeću instancu.
         * Ako ne postoji, kreira novu instancu baze podataka koristeći Room.
         *
         * @param context Context aplikacije koji je potreban za kreiranje baze podataka.
         * @return Singleton instanca baze podataka [AppDatabase].
         */
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
