package org.unizd.rma.markanjevic.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.unizd.rma.markanjevic.models.Animal
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.unizd.rma.markanjevic.dao.AnimalDao

/**
 * ViewModel koji upravlja podacima o ljubimcima.
 *
 * Ovaj ViewModel pruža funkcionalnost za dohvat, dodavanje, brisanje i ažuriranje podataka o ljubimcima.
 * Komunicira s [AnimalDao] kako bi obavio operacije u bazi podataka, koristeći asinkroni pristup putem
 * [viewModelScope] za pokretanje korutina.
 *
 * @param animalDao DAO za pristup podacima o ljubimcima.
 */
class AnimalViewModel(private val animalDao: AnimalDao) : ViewModel() {

    /**
     * Lista svih ljubimaca u bazi podataka, praćena u formatu [LiveData].
     *
     * LiveData omogućava promjene u podacima da budu automatski reflektirane na UI-u,
     * pa će se UI ažurirati svaki puta kada se podaci o ljubimcima promijene.
     */
    val animals: LiveData<List<Animal>> = animalDao.getAllAnimals()

    /**
     * Dohvaća jednog ljubimca iz baze podataka prema njegovom identifikatoru.
     *
     * @param animalId Identifikator ljubimca koji se traži.
     * @return [LiveData] objekt koji sadrži ljubimca s traženim identifikatorom.
     */
    fun getAnimalById(animalId: Int): LiveData<Animal> {
        return animalDao.getAnimalById(animalId)
    }

    /**
     * Dodaje novog ljubimca u bazu podataka.
     *
     * Ova funkcija koristi korutine unutar [viewModelScope] za pokretanje asinkronog unosa
     * u bazu podataka. To znači da neće blokirati glavni UI thread.
     *
     * @param animal Objekt tipa [Animal] koji se dodaje u bazu podataka.
     */
    fun insertAnimal(animal: Animal) {
        viewModelScope.launch {
            animalDao.insertAnimal(animal)
        }
    }

    /**
     * Briše ljubimca iz baze podataka.
     *
     * Ova funkcija koristi korutine za asinkrono brisanje ljubimca iz baze podataka.
     *
     * @param animal Objekt tipa [Animal] koji se briše iz baze.
     */
    fun deleteAnimal(animal: Animal) {
        viewModelScope.launch {
            animalDao.deleteAnimal(animal)
        }
    }

    /**
     * Ažurira postojeće podatke o ljubimcu u bazi podataka.
     *
     * Ova funkcija koristi korutine za asinkrono ažuriranje podataka o ljubimcu.
     *
     * @param animal Objekt tipa [Animal] koji sadrži nove podatke za ljubimca.
     */
    fun updateAnimal(animal: Animal) {
        viewModelScope.launch {
            animalDao.updateAnimal(animal)
        }
    }
}