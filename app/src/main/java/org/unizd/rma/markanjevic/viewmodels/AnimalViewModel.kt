package org.unizd.rma.markanjevic.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.unizd.rma.markanjevic.models.Animal
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.unizd.rma.markanjevic.dao.AnimalDao

class AnimalViewModel(private val animalDao: AnimalDao) : ViewModel() {

    val animals: LiveData<List<Animal>> = animalDao.getAllAnimals()

    fun getAnimalById(animalId: Int): LiveData<Animal> {
        return animalDao.getAnimalById(animalId)
    }

    fun insertAnimal(animal: Animal) {
        viewModelScope.launch() {
            animalDao.insertAnimal(animal)
        }
    }

    fun deleteAnimal(animal: Animal) {
        viewModelScope.launch() {
            animalDao.deleteAnimal(animal)
        }
    }

    fun updateAnimal(animal: Animal) {
        viewModelScope.launch() {
            animalDao.updateAnimal(animal)
        }
    }
}