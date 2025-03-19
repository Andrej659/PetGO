package org.unizd.rma.markanjevic.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class PetListFragment : Fragment() {

    private lateinit var petRecyclerView: RecyclerView
    private lateinit var petAdapter: PetAdapter
    private lateinit var animalViewModel: AnimalViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pet_list, container, false)

        petRecyclerView = view.findViewById(R.id.petRecyclerView)
        petRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadPetData()

        return view
    }

    private fun loadPetData() {

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "animals"
        ).build()

        val animalDao = db.animalDao()
        val factory = AnimalViewModelFactory(animalDao)
        animalViewModel = ViewModelProvider(this, factory).get(AnimalViewModel::class.java)

        animalViewModel.animals.observe(viewLifecycleOwner, Observer { animals ->
            setUpAdapter(animals)
            petRecyclerView.adapter = petAdapter
        })
    }

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
}