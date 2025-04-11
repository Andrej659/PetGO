package org.unizd.rma.markanjevic.views

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.bumptech.glide.Glide
import org.unizd.rma.markanjevic.R
import org.unizd.rma.markanjevic.database.AppDatabase
import org.unizd.rma.markanjevic.models.Animal
import org.unizd.rma.markanjevic.viewmodels.AnimalViewModel
import org.unizd.rma.markanjevic.viewmodels.factory.AnimalViewModelFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PetDetailFragment : Fragment() {

    private var animalId: Int = 0
    private lateinit var animalViewModel: AnimalViewModel
    private lateinit var currentAnimal: Animal
    private lateinit var imageUri: Uri

    private lateinit var animalName: EditText
    private lateinit var animalType: EditText
    private lateinit var animalAge: EditText
    private lateinit var animalDateAdded: EditText
    private lateinit var animalGender: TextView
    private lateinit var animalImage: ImageView
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var btnList: Button
    private lateinit var genderSpinner: Spinner

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_pet_detail, container, false)

        animalName = rootView.findViewById(R.id.animalName)
        animalType = rootView.findViewById(R.id.animalType)
        animalAge = rootView.findViewById(R.id.animalAge)
        animalDateAdded = rootView.findViewById(R.id.animalDateAdded)
        animalGender = rootView.findViewById(R.id.animalGender)
        animalImage = rootView.findViewById(R.id.animalImage)
        btnEdit = rootView.findViewById(R.id.editButton)
        btnDelete = rootView.findViewById(R.id.deleteButton)
        genderSpinner = rootView.findViewById<Spinner>(R.id.genderSpinner)
        btnList = rootView.findViewById(R.id.listButton)

        setFieldsEditable(false)

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "animals"
        ).build()

        val animalDao = db.animalDao()
        val factory = AnimalViewModelFactory(animalDao)
        animalViewModel = ViewModelProvider(this, factory)[AnimalViewModel::class.java]

        animalId = arguments?.getInt("PET_ID") ?: 0

        loadAnimalDetails(animalId)
        spinnerSetup()

        btnEdit.setOnClickListener {
            toggleEditMode()
        }

        btnDelete.setOnClickListener {
            deleteAnimal(currentAnimal)
            openSelectionFragment()
        }

        animalImage.setOnClickListener {
            showImagePickerDialog()
        }

        animalDateAdded.setOnClickListener {
            showDatePickerDialog()
        }

        btnList.setOnClickListener {
            openListFragment()
        }

        return rootView
    }

    private fun loadAnimalDetails(animalId: Int) {
        animalViewModel.getAnimalById(animalId).observe(viewLifecycleOwner) { animal ->
            animal?.let {
                animalName.setText(animal.name)
                animalType.setText(animal.type)
                animalAge.setText(animal.age.toString())
                animalDateAdded.setText(animal.dateAdded)
                animalGender.text = animal.gender

                if (animal.image.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(animal.image)
                        .into(animalImage)
                }
                currentAnimal = animal
            }
        }
    }

    private fun deleteAnimal(animal: Animal) {
        animalViewModel.deleteAnimal(animal)
    }

    private fun openSelectionFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, SelectionFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun openListFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, PetListFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun setFieldsEditable(editable: Boolean) {

        animalImage.isEnabled = editable
        animalName.isEnabled = editable
        animalType.isEnabled = editable
        animalAge.isEnabled = editable
        animalDateAdded.isEnabled = editable

        animalImage.isFocusable = editable
        animalName.isFocusable = editable
        animalType.isFocusable = editable
        animalAge.isFocusable = editable
        animalDateAdded.isFocusable = editable

        animalName.isFocusableInTouchMode = editable
        animalType.isFocusableInTouchMode = editable
        animalAge.isFocusableInTouchMode = editable

    }

    private fun toggleEditMode() {

        val isEditable = animalName.isEnabled

        if (animalName.text.toString().isEmpty() || animalType.text.toString().isEmpty() || animalDateAdded.text.toString().isEmpty() || animalGender.text.toString().isEmpty() || animalAge.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), "Molimo unesite sve podatke", Toast.LENGTH_SHORT).show()
            return
        }

        setFieldsEditable(!isEditable)

        if (!isEditable) {
            btnEdit.text = "Save"
            animalGender.visibility = View.GONE
            genderSpinner.visibility = View.VISIBLE
        } else {
            btnEdit.text = "Edit"
            animalGender.visibility = View.VISIBLE
            genderSpinner.visibility = View.GONE
            saveAnimalDetails()
        }
    }

    private fun saveAnimalDetails() {

        currentAnimal.name = animalName.text.toString()
        currentAnimal.type = animalType.text.toString()
        currentAnimal.age = animalAge.text.toString().toInt()
        currentAnimal.dateAdded = animalDateAdded.text.toString()
        currentAnimal.gender = genderSpinner.selectedItem.toString()

        animalViewModel.updateAnimal(currentAnimal)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            animalImage.setImageURI(imageUri)
            currentAnimal.image = imageUri.toString()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            animalImage.setImageURI(imageUri)
            currentAnimal.image = imageUri.toString()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Slikaj kamerom", "Odaberi iz galerije")
        AlertDialog.Builder(requireContext())
            .setTitle("Odaberite opciju")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val imageFile = File(requireContext().filesDir, "pet_image_${System.currentTimeMillis()}.jpg")
        imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", imageFile)
        cameraLauncher.launch(imageUri)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar[year, month] = dayOfMonth
                animalDateAdded.setText(dateFormat.format(calendar.time))
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        datePicker.show()
    }

    private fun spinnerSetup(){

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderSpinner.adapter = adapter
        }
    }
}
