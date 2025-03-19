package org.unizd.rma.markanjevic.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.bumptech.glide.Glide
import org.unizd.rma.markanjevic.R
import org.unizd.rma.markanjevic.database.AppDatabase
import org.unizd.rma.markanjevic.viewmodels.AnimalViewModel
import org.unizd.rma.markanjevic.viewmodels.factory.AnimalViewModelFactory


class PetDetailFragment : Fragment() {

    private var animalId: Int = 0
    private lateinit var animalViewModel: AnimalViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_pet_detail, container, false)

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "animals"
        ).build()

        val animalDao = db.animalDao()
        val factory = AnimalViewModelFactory(animalDao)
        animalViewModel = ViewModelProvider(this, factory).get(AnimalViewModel::class.java)


        animalId = arguments?.getInt("PET_ID") ?: 0


        loadAnimalDetails(animalId, rootView)

        return rootView
    }

    private fun loadAnimalDetails(animalId: Int, rootView: View) {

        animalViewModel.getAnimalById(animalId).observe(viewLifecycleOwner) { animal ->
            animal?.let {


                val animalName = rootView.findViewById<TextView>(R.id.animalName)
                val animalType = rootView.findViewById<TextView>(R.id.animalType)
                val animalAge = rootView.findViewById<TextView>(R.id.animalAge)
                val animalDateAdded = rootView.findViewById<TextView>(R.id.animalDateAdded)
                val animalGender = rootView.findViewById<TextView>(R.id.animalGender)
                val animalImage = rootView.findViewById<ImageView>(R.id.animalImage)

                animalName.text = animal.name
                animalType.text = animal.type
                animalAge.text = animal.age.toString()
                animalDateAdded.text = animal.dateAdded
                animalGender.text = animal.gender

                if (animal.image.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(animal.image)
                        .into(animalImage)
                }
            }
        }
    }
}