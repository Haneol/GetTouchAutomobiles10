package com.example.gta.view.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gta.R
import com.example.gta.data.model.PhotoItem
import com.example.gta.data.repository.FirebaseRepository
import com.example.gta.databinding.ActivityPhotoBinding
import com.example.gta.view.adapters.PhotoAdapter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoBinding
    private lateinit var photoAdapter: PhotoAdapter
    private val repository = FirebaseRepository()
    private var currentCarId: String = ""
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2
    private val CAMERA_PERMISSION_CODE = 100
    private val STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentCarId = intent.getStringExtra("carId") ?: run {
            Toast.makeText(this, "차량 정보가 없습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupFab()
        loadPhotos()
    }

    private fun loadPhotos() {
        repository.getPhotos(
            carId = currentCarId,
            onSuccess = { photos ->
                photoAdapter.updatePhotos(photos)
            },
            onFailure = { exception ->
                Toast.makeText(this,
                    "사진을 불러오는데 실패했습니다: ${exception.message}",
                    Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun addPhotoToList(uri: Uri) {
        repository.addPhoto(
            carId = currentCarId,
            uri = uri,
            onSuccess = { photoItem ->
                photoAdapter.addPhoto(photoItem)
            },
            onFailure = { exception ->
                Toast.makeText(this,
                    "사진 업로드 실패: ${exception.message}",
                    Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupRecyclerView() {
        photoAdapter = PhotoAdapter()
        binding.photoRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PhotoActivity)
            adapter = photoAdapter
        }

        photoAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                updateEmptyView()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                updateEmptyView()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                updateEmptyView()
            }
        })

        updateEmptyView()
    }

    private fun updateEmptyView() {
        if (photoAdapter.itemCount == 0) {
            binding.emptyView.visibility = View.VISIBLE
            binding.photoRecyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.photoRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupFab() {
        binding.addPhotoFab.setOnClickListener {
            showImagePickerDialog()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("카메라로 촬영", "갤러리에서 선택")
        AlertDialog.Builder(this)
            .setTitle("사진 추가")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkAndRequestCameraPermission()
                    1 -> checkAndRequestStoragePermission()
                }
            }
            .show()
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun checkAndRequestStoragePermission() {
        // Android 13 이상
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
        // Android 13 미만
        else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.also {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { intent ->
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_PICK_IMAGE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "저장소 접근 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap?
                    imageBitmap?.let { handleCapturedPhoto(it) }
                }
                REQUEST_PICK_IMAGE -> {
                    data?.data?.let { handleSelectedPhoto(it) }
                }
            }
        }
    }

    private fun handleCapturedPhoto(bitmap: Bitmap) {
        val uri = saveBitmapToFile(bitmap)
        addPhotoToList(uri)
    }

    private fun handleSelectedPhoto(uri: Uri) {
        addPhotoToList(uri)
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri {
        val fileName = "PHOTO_${System.currentTimeMillis()}.jpg"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            file
        )
    }
}