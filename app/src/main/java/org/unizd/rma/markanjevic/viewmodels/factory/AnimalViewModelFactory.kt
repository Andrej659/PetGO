package org.unizd.rma.markanjevic.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.unizd.rma.markanjevic.dao.AnimalDao
import org.unizd.rma.markanjevic.viewmodels.AnimalViewModel

class AnimalViewModelFactory(private val animalDao: AnimalDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnimalViewModel::class.java)) {
            return AnimalViewModel(animalDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}