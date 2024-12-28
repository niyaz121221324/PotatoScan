package com.example.potatoscan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    private lateinit var predictionManager: PredictionManager
    private val requestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!arePermissionsGranted()) {
            requestPermissions()
        }

        predictionManager = PredictionManager()

        setContent {
            PotatoScanApp { file, onSuccess ->
                predict(file, onSuccess)
            }
        }
    }

    private fun arePermissionsGranted(): Boolean {
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(this, requiredPermissions, requestCode)
    }

    private fun predict(file: MultipartBody.Part, onSuccess: (PredictionResponse) -> Unit) {
        predictionManager.predict(file, object : PredictionManager.PredictionCallback {
            override fun onSuccess(predictionResponse: PredictionResponse) {
                onSuccess(predictionResponse)
            }

            override fun onFailure(t: Throwable) {
                println("Failed to make prediction: ${t.message}")
            }
        })
    }
}

@Composable
fun PotatoScanApp(
    predict: (MultipartBody.Part, (PredictionResponse) -> Unit) -> Unit) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmapImage by remember { mutableStateOf<Bitmap?>(null) }
    var predictionResult by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmapImage = bitmap
        imageUri = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            imageUri != null -> {
                imageUri?.let { uri ->
                    val bitmap = loadBitmapFromUri(context, uri)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        )
                    }
                }
            } bitmapImage != null -> {
                Image(
                    bitmap = bitmapImage!!.asImageBitmap(),
                    contentDescription = "Сделанный снимок",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            } else -> {
                Text("Нет изображения для отображения", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Выбрать изображение")
        }

        Button(onClick = { takePhotoLauncher.launch(null) }) {
            Text("Сделать снимок")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            when{
                imageUri != null -> {
                    imageUri?.let { uri ->
                        val file = createMultipartBodyPart(context, uri)
                        predict(file) { response ->
                            predictionResult = response.toString()
                        }
                    }
                } bitmapImage != null -> {
                    val file = createMultipartBodyPartFromBitmap(context, bitmapImage!!)
                    predict(file) { response ->
                        predictionResult = response.toString()
                    }
                }
            }

        }) {
            Text("Сделать предсказание")
        }

        Spacer(modifier = Modifier.height(16.dp))

        predictionResult?.let {
            Text(
                text = "Результат предсказания: $it",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        null
    }
}

fun createMultipartBodyPart(context: Context, uri: Uri): MultipartBody.Part {
    val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalArgumentException("Cannot open file descriptor")
    val file = File(context.cacheDir, "temp_image").apply {
        fileDescriptor.use {
            val inputStream = FileInputStream(it.fileDescriptor)
            val outputStream = FileOutputStream(this)
            inputStream.copyTo(outputStream)
        }
    }

    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("file", file.name, requestBody)
}

fun createMultipartBodyPartFromBitmap(context: Context, bitmap: Bitmap): MultipartBody.Part {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()

    val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())

    return MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
}

