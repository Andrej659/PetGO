package org.unizd.rma.markanjevic.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.unizd.rma.markanjevic.R

/**
 * Fragment koji predstavlja početni izbornik aplikacije.
 * Korisnik ovdje može birati između dodavanja novog ljubimca ili pregleda postojećih.
 */
class SelectionFragment : Fragment(R.layout.fragment_selection) {

    /**
     * Metoda koja se poziva kada je view fragmenta kreiran.
     * Postavlja akcije na gumbe za otvaranje odgovarajućih fragmenata.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fabCreateNewAnimal: FloatingActionButton = view.findViewById(R.id.fabCreateNewAnimal)
        val buttonOpenListOfPets: Button = view.findViewById(R.id.buttonOpenListOfPets)

        fabCreateNewAnimal.setOnClickListener {
            openCreateNewAnimalFragment()
        }

        buttonOpenListOfPets.setOnClickListener {
            openListOfPetsFragment()
        }
    }

    /**
     * Otvara fragment za dodavanje novog ljubimca.
     */
    private fun openCreateNewAnimalFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, AddNewPetFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    /**
     * Otvara fragment s listom postojećih ljubimaca.
     */
    private fun openListOfPetsFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, PetListFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}