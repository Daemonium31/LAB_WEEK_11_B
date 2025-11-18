package com.example.lab_week_11_b

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    // Kode permintaan untuk permintaan izin ke penyimpanan eksternal
    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 3
    }

    // Kelas helper untuk mengelola file di MediaStore
    private lateinit var providerFileManager: ProviderFileManager

    // Model data untuk file
    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null

    // Flag untuk menunjukkan apakah pengguna sedang mengambil foto atau video
    private var isCapturingVideo = false

    // Activity result Launcher untuk mengambil gambar dan video
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi ProviderFileManager
        providerFileManager =
            ProviderFileManager(
                applicationContext,
                FileHelper(applicationContext),
                contentResolver,
                Executors.newSingleThreadExecutor(),
                MediaContentHelper()
            )

        // Inisialisasi activity result launcher
        // .TakePicture() dan .CaptureVideo() adalah kontrak bawaan
        // Mereka digunakan untuk mengambil gambar dan video
        // Hasilnya akan disimpan di URI yang diteruskan ke Launcher
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) {
                providerFileManager.insertImageToStore(photoInfo)
            }
        takeVideoLauncher =
            registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
                providerFileManager.insertVideoToStore(videoInfo)
            }

        findViewById<Button>(R.id.photo_button).setOnClickListener {
            // Setel flag untuk menunjukkan bahwa pengguna sedang mengambil foto
            isCapturingVideo = false
            // Periksa izin penyimpanan
            // Jika izin diberikan, buka kamera
            // Jika tidak, minta izin
            checkStoragePermission {
                openImageCapture()
            }
        }

        findViewById<Button>(R.id.video_button).setOnClickListener {
            // Setel flag untuk menunjukkan bahwa pengguna sedang mengambil video
            isCapturingVideo = true
            // Periksa izin penyimpanan
            // Jika izin diberikan, buka kamera
            // Jika tidak, minta izin
            checkStoragePermission {
                openVideoCapture()
            }
        }
    }

    // Buka kamera untuk mengambil gambar
    private fun openImageCapture() {
        photoInfo =
            providerFileManager.generatePhotoUri(System.currentTimeMillis())
        takePictureLauncher.launch(photoInfo!!.uri)
    }

    // Buka kamera untuk mengambil video
    private fun openVideoCapture() {
        videoInfo =
            providerFileManager.generateVideoUri(System.currentTimeMillis())
        takeVideoLauncher.launch(videoInfo!!.uri)
    }

    // Periksa izin penyimpanan
    // Untuk Android 10 ke atas, izin tidak diperlukan
    // Untuk Android 9 ke bawah, izin diperlukan
    private fun checkStoragePermission(onPermissionGranted: () -> Unit) {
        if (android.os.Build.VERSION.SDK_INT <
            android.os.Build.VERSION_CODES.Q) {
            // Periksa izin WRITE_EXTERNAL_STORAGE
            when (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
                // Jika izin diberikan
                PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
                // jika izin tidak diberikan, minta izin
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        } else {
            onPermissionGranted()
        }
    }

    // Untuk android 9 ke bawah
    // Tangani hasil permintaan izin
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions,
            grantResults)

        when (requestCode) {
            // Periksa apakah requestCode untuk izin Penyimpanan Eksternal atau tidak
            REQUEST_EXTERNAL_STORAGE -> {
                // Jika diberikan, buka kamera
                if ((grantResults.isNotEmpty() && grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED)) {
                    if (isCapturingVideo) {
                        openVideoCapture()
                    } else {
                        openImageCapture()
                    }
                }
                return
            }
            // untuk kode permintaan lain, jangan lakukan apa-apa
            else -> {
            }
        }
    }
}