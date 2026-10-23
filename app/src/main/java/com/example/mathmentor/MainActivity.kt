package com.example.mathmentor

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var equationInput: EditText
    private lateinit var solveButton: Button
    private lateinit var exampleButton: Button
    private lateinit var clearButton: Button
    private lateinit var takePhotoButton: Button
    private lateinit var resultText: TextView
    private lateinit var photoPreview: ImageView

    private var photoUri: Uri? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to use the photo solver.", Toast.LENGTH_LONG).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            photoPreview.setImageURI(photoUri)
            processPhoto(photoUri!!)
        } else {
            Toast.makeText(this, "The photo could not be captured.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        equationInput = findViewById(R.id.equationInput)
        solveButton = findViewById(R.id.solveButton)
        exampleButton = findViewById(R.id.exampleButton)
        clearButton = findViewById(R.id.clearButton)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        resultText = findViewById(R.id.resultText)
        photoPreview = findViewById(R.id.photoPreview)

        solveButton.setOnClickListener {
            val raw = equationInput.text?.toString()?.trim().orEmpty()
            if (raw.isEmpty()) {
                resultText.text = "Please type or scan a math problem first."
                return@setOnClickListener
            }
            val result = EquationSolver.solve(raw)
            resultText.text = result.formatForDisplay()
        }

        exampleButton.setOnClickListener {
            equationInput.setText("3x^2 - 5x + 2 = 0")
            val result = EquationSolver.solve("3x^2 - 5x + 2 = 0")
            resultText.text = result.formatForDisplay()
        }

        clearButton.setOnClickListener {
            equationInput.setText("")
            resultText.text = getString(R.string.result_default)
            photoPreview.setImageDrawable(null)
        }

        takePhotoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "math_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        takePictureLauncher.launch(photoUri)
    }

    private fun processPhoto(imageUri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val detectedText = visionText.text
                        .replace("\n", " ")
                        .replace("\\s+".toRegex(), " ")
                        .trim()

                    if (detectedText.isNotBlank()) {
                        equationInput.setText(detectedText)
                        val result = EquationSolver.solve(detectedText)
                        resultText.text = result.formatForDisplay()
                    } else {
                        Toast.makeText(this, "No math text was detected.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "OCR failed. You can type the equation manually.", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open the photo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
