package org.unizd.rma.markanjevic.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.unizd.rma.markanjevic.R

class SelectionFragment : Fragment(R.layout.fragment_selection) {

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

    private fun openCreateNewAnimalFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, AddNewPetFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun openListOfPetsFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, PetListFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}