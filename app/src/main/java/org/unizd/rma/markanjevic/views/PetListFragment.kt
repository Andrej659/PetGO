package org.unizd.rma.markanjevic.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import org.unizd.rma.markanjevic.R
import org.unizd.rma.markanjevic.models.Animal
import org.unizd.rma.markanjevic.adapters.PetAdapter
import org.unizd.rma.markanjevic.database.AppDatabase
import org.unizd.rma.markanjevic.viewmodels.AnimalViewModel
import org.unizd.rma.markanjevic.viewmodels.factory.AnimalViewModelFactory

/**
 * Fragment koji prikazuje listu svih ljubimaca iz baze podataka.
 * Omogućuje pregled detalja svakog ljubimca klikom na pojedini red u RecyclerView-u.
 */
class PetListFragment : Fragment() {

    private lateinit var petRecyclerView: RecyclerView
    private lateinit var petAdapter: PetAdapter
    private lateinit var animalViewModel: AnimalViewModel
    private lateinit var btnBack: Button

    /**
     * Kreira view za fragment i postavlja osnovne UI komponente.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_pet_list, container, false)

        petRecyclerView = rootView.findViewById(R.id.petRecyclerView)
        btnBack = rootView.findViewById(R.id.backButton)
        petRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Učitavanje podataka iz baze
        loadPetData()

        btnBack.setOnClickListener {
            openSelectionFragment()
        }

        return rootView
    }

    /**
     * Učitava podatke o ljubimcima iz baze putem ViewModel-a i prikazuje ih u RecyclerView-u.
     */
    private fun loadPetData() {

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "animals"
        ).build()

        val animalDao = db.animalDao()
        val factory = AnimalViewModelFactory(animalDao)
        animalViewModel = ViewModelProvider(this, factory)[AnimalViewModel::class.java]

        animalViewModel.animals.observe(viewLifecycleOwner, Observer { animals ->
            setUpAdapter(animals)
            petRecyclerView.adapter = petAdapter
        })
    }

    /**
     * Postavlja adapter za RecyclerView s dohvaćenim životinjama.
     * Također se definira ponašanje na klik pojedinog ljubimca.
     */
    private fun setUpAdapter(animals: List<Animal>){

        petAdapter = PetAdapter(animals, object : PetAdapter.OnPetClickListener {
            override fun onPetClick(petId: Int) {

                val bundle = Bundle()
                bundle.putInt("PET_ID", petId)

                val petDetailFragment = PetDetailFragment()
                petDetailFragment.arguments = bundle

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, petDetailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        })
    }

    /**
     * Otvara početni fragment s opcijama (izbornik).
     */
    private fun openSelectionFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, SelectionFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}