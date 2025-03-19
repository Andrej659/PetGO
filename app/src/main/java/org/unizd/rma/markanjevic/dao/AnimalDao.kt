package org.unizd.rma.markanjevic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import org.unizd.rma.markanjevic.models.Animal
@Dao
interface AnimalDao {

    // Insert a new animal
    @Insert
    suspend fun insertAnimal(animal: Animal)

    // Get all animals from the database
    @Query("SELECT * FROM animals")
    fun getAllAnimals(): LiveData<List<Animal>>

    // Get an animal by its ID
    @Query("SELECT * FROM animals WHERE id = :id")
    fun getAnimalById(id: Int): LiveData<Animal>

    @Delete
    suspend fun deleteAnimal(animal: Animal)
}
