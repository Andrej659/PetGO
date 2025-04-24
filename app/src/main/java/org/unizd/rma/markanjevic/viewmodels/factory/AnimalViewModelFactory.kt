package org.unizd.rma.markanjevic.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.unizd.rma.markanjevic.dao.AnimalDao
import org.unizd.rma.markanjevic.viewmodels.AnimalViewModel

/**
 * Factory klasa koja se koristi za kreiranje instance [AnimalViewModel].
 *
 * Ova klasa omogućuje pravilno stvaranje [AnimalViewModel] objekta, koristeći [AnimalDao]
 * koji je potreban za pristup podacima u bazi. Koristi se s [ViewModelProvider] za kreiranje
 * ViewModel instanci u aplikaciji.
 *
 * @param animalDao DAO za pristup podacima o ljubimcima u bazi podataka.
 */
class AnimalViewModelFactory(private val animalDao: AnimalDao) : ViewModelProvider.Factory {

    /**
     * Kreira instancu [AnimalViewModel] klase.
     *
     * Ova metoda provjerava je li traženi ViewModel tip [AnimalViewModel], a zatim
     * kreira njegovu instancu koristeći [animalDao] za pristup podacima o ljubimcima.
     *
     * @param modelClass Klasa ViewModel-a koji treba biti instanciran.
     * @return Kreirani ViewModel objekat tipa [AnimalViewModel].
     * @throws IllegalArgumentException Ako modelClass nije [AnimalViewModel], baca iznimku.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnimalViewModel::class.java)) {
            return AnimalViewModel(animalDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}