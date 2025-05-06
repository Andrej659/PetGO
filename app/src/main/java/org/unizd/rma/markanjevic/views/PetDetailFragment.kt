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
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Fragment za prikaz i uređivanje detalja o odabranoj životinji.
 * Omogućuje prikaz, ažuriranje, brisanje i dodavanje slike te promjenu spola.
 */
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
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>


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

        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Kamera dozvola odbijena", Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }

    /**
     * Učitavanje podataka o životinji i prikaz u UI.
     */
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

    /**
     * Briše životinju iz baze podataka
     *
     * @param animal Životinja koja se briše
     */
    private fun deleteAnimal(animal: Animal) {
        animalViewModel.deleteAnimal(animal)
    }

    /**
     * Otvara fragment za odabir nove životinje
     */
    private fun openSelectionFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, SelectionFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    /**
     * Otvara listu svih životinja
     */
    private fun openListFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, PetListFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    /**
     * Postavlja polja kao uređivačka ili samo za čitanje
     */
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

    /**
     * Uključuje/isključuje način uređivanja i sprema promjene
     */
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
            val genderArray = resources.getStringArray(R.array.gender_array)
            val genderIndex = genderArray.indexOf(currentAnimal.gender)
            if (genderIndex >= 0) {
                genderSpinner.setSelection(genderIndex)
            }
        } else {
            btnEdit.text = "Edit"
            animalGender.visibility = View.VISIBLE
            genderSpinner.visibility = View.GONE
            saveAnimalDetails()
        }
    }

    /**
     * Sprema uređene podatke o životinji u bazu.
     */
    private fun saveAnimalDetails() {

        currentAnimal.name = animalName.text.toString()
        currentAnimal.type = animalType.text.toString()
        currentAnimal.age = animalAge.text.toString().toInt()
        currentAnimal.dateAdded = animalDateAdded.text.toString()
        currentAnimal.gender = genderSpinner.selectedItem.toString()

        animalViewModel.updateAnimal(currentAnimal)
    }

    // Pokreće galeriju
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            animalImage.setImageURI(imageUri)
            currentAnimal.image = imageUri.toString()
        }
    }

    /**
     * Otvara dijalog za izbor slike (kamera ili galerija).
     */
    private fun showImagePickerDialog() {
        val options = arrayOf("Slikaj kamerom", "Odaberi iz galerije")
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

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    /**
     * Otvara dijalog za odabir datuma.
     */
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

    /**
     * Postavlja adapter za spinner spola
     */
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

    private val cameraIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Slika spremljena", Toast.LENGTH_SHORT).show()
            currentAnimal.image = saveImageFromUri(imageUri)

            if (currentAnimal.image.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(currentAnimal.image)
                    .into(animalImage)
            }

        } else {
            Toast.makeText(requireContext(), "Snimanje nije uspjelo", Toast.LENGTH_SHORT).show()
        }
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
