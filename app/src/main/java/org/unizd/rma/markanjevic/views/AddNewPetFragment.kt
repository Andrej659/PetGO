package org.unizd.rma.markanjevic.views

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import org.unizd.rma.markanjevic.R
import org.unizd.rma.markanjevic.database.AppDatabase
import org.unizd.rma.markanjevic.models.Animal
import org.unizd.rma.markanjevic.viewmodels.AnimalViewModel
import org.unizd.rma.markanjevic.viewmodels.factory.AnimalViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddNewPetFragment : Fragment() {

    private lateinit var petName: EditText
    private lateinit var petType: EditText
    private lateinit var petAge: EditText
    private lateinit var petGenderSpinner: Spinner
    private lateinit var petDateAdded: EditText
    private lateinit var selectImageButton: Button
    private lateinit var savePetButton: Button
    private lateinit var imageUri: Uri
    private lateinit var animalViewModel: AnimalViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_new_pet, container, false)

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "animals"
        ).build()

        val animalDao = db.animalDao()
        val factory = AnimalViewModelFactory(animalDao)
        animalViewModel = ViewModelProvider(this, factory).get(AnimalViewModel::class.java)

        petName = view.findViewById(R.id.petName)
        petType = view.findViewById(R.id.petType)
        petAge = view.findViewById(R.id.petAge)
        petGenderSpinner = view.findViewById(R.id.petGenderSpinner)
        petDateAdded = view.findViewById(R.id.petDateAdded)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        savePetButton = view.findViewById(R.id.savePetButton)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            petGenderSpinner.adapter = adapter
        }

        petDateAdded.setOnClickListener {
            showDatePickerDialog()
        }

        selectImageButton.setOnClickListener {
            showImagePickerDialog()
        }

        savePetButton.setOnClickListener {
            savePet()
            openSelectionFragment()
        }

        return view
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Toast.makeText(requireContext(), "Slika spremljena", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            Toast.makeText(requireContext(), "Slika odabrana", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Slikaj kamerom", "Odaberi iz galerije", "Odustani")
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

    private fun savePet() {
        val name = petName.text.toString().trim()
        val type = petType.text.toString().trim()
        val age = petAge.text.toString().toIntOrNull() ?: 0
        val gender = petGenderSpinner.selectedItem.toString()
        val dateAdded = petDateAdded.text.toString().trim()
        val imagePath = saveImageFromUri(imageUri)

        if (name.isEmpty() || type.isEmpty() || dateAdded.isEmpty() || gender.isEmpty() || imagePath.isEmpty()) {
            Toast.makeText(requireContext(), "Molimo unesite sve podatke", Toast.LENGTH_SHORT).show()
            return
        }

        val newPet = Animal(
            id = 0,
            name = name,
            type = type,
            age = age,
            gender = gender,
            dateAdded = dateAdded,
            image = imagePath
        )

        savePetToDatabase(newPet)
    }

    private fun savePetToDatabase(pet: Animal) {
        Toast.makeText(requireContext(), "Životinja spremljena: ${pet.name}", Toast.LENGTH_SHORT).show()
        animalViewModel.insertAnimal(pet)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                petDateAdded.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun openSelectionFragment() {

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, SelectionFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun saveImageFromUri(uri: Uri): String {

        val context = requireContext()
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val fileName = "animal_${System.currentTimeMillis()}"
            val imagePath = saveImageToInternalStorage(context, bitmap, fileName)

            return imagePath

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }


    private fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, fileName: String): String {
        val directory = File(context.filesDir, "pet_images")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, fileName)
        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }
}