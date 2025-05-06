package org.unizd.rma.markanjevic.views

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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

/**
 * Fragment za dodavanje novog ljubimca u aplikaciju.
 *
 * Ovaj fragment omogućuje korisnicima unos informacija o ljubimcima, kao što su ime, tip, dob, spol, datum dodavanja
 * i slika ljubimca. Također omogućava odabir slike pomoću kamere ili galerije, a unesene podatke sprema u bazu podataka.
 */
class AddNewPetFragment : Fragment() {

    private lateinit var petName: EditText
    private lateinit var petType: EditText
    private lateinit var petAge: EditText
    private lateinit var petGenderSpinner: Spinner
    private lateinit var petDateAdded: EditText
    private lateinit var selectImageButton: Button
    private lateinit var savePetButton: Button
    private var imageUri: Uri? = null
    private lateinit var animalViewModel: AnimalViewModel
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_new_pet, container, false)

        // Postavljanje Room baze podataka i AnimalViewModel-a
        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "animals"
        ).build()

        val animalDao = db.animalDao()
        val factory = AnimalViewModelFactory(animalDao)
        animalViewModel = ViewModelProvider(this, factory)[AnimalViewModel::class.java]

        // Inicijalizacija UI komponenti
        petName = view.findViewById(R.id.petName)
        petType = view.findViewById(R.id.petType)
        petAge = view.findViewById(R.id.petAge)
        petGenderSpinner = view.findViewById(R.id.petGenderSpinner)
        petDateAdded = view.findViewById(R.id.petDateAdded)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        savePetButton = view.findViewById(R.id.savePetButton)

        // Postavljanje adaptera za spinner (spol ljubimca)
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
        }

        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Kamera dozvola odbijena", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }


    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            Toast.makeText(requireContext(), "Slika odabrana", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Prikazuje dijalog za odabir izvora slike (kamera ili galerija).
     */
    private fun showImagePickerDialog() {
        val options = arrayOf("Slikaj kamerom", "Odaberi iz galerije", "Odustani")
        AlertDialog.Builder(requireContext())
            .setTitle("Odaberite opciju")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpenCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    /**
     * Pokreće kameru za snimanje slike.
     */
    private fun openCamera() {
        val imageFile = File(
            requireContext().getExternalFilesDir(null),
            "pet_image_${System.currentTimeMillis()}.jpg"
        )
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        cameraIntentLauncher.launch(cameraIntent)
    }

    private val cameraIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Slika spremljena", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Snimanje nije uspjelo", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Otvara galeriju za odabir slike.
     */
    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    /**
     * Spremanje podataka o ljubimcu u bazu podataka.
     */
    private fun savePet() {

        val name = petName.text.toString().trim()
        val type = petType.text.toString().trim()
        val age = petAge.text.toString().toIntOrNull() ?: 0
        val gender = petGenderSpinner.selectedItem.toString()
        val dateAdded = petDateAdded.text.toString().trim()
        val imagePath = saveImageFromUri(imageUri)

        if (name.isEmpty() || type.isEmpty() || dateAdded.isEmpty() || imagePath.isEmpty()) {
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
        openSelectionFragment()
    }

    /**
     * Spremanje ljubimca u bazu podataka.
     */
    private fun savePetToDatabase(pet: Animal) {
        Toast.makeText(requireContext(), "Životinja spremljena: ${pet.name}", Toast.LENGTH_SHORT).show()
        animalViewModel.insertAnimal(pet)
    }


    /**
     * Prikazuje DatePicker dijalog za odabir datuma.
     */
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar[year, month] = dayOfMonth
                petDateAdded.setText(dateFormat.format(calendar.time))
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        datePicker.show()
    }


    /**
     * Otvara fragment za odabir ljubimaca nakon što je novi ljubimac spremljen.
     */
    private fun openSelectionFragment() {

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, SelectionFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    /**
     * Spremanje slike s URI-ja u internu pohranu.
     */
    private fun saveImageFromUri(uri: Uri?): String {

        var newUrl: Uri

        if (uri != null){
            newUrl = uri
        }else{
            return ""
        }

        val context = requireContext()
        try {
            val inputStream = context.contentResolver.openInputStream(newUrl)
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

    /**
     * Spremanje slike u internu pohranu.
     */
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

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}