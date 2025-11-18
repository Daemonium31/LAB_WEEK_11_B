package com.example.lab_week_11_b

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

class FileHelper(private val context: Context) {

    // Menghasilkan URI untuk mengakses file
    // URI akan bersifat sementara untuk Membatasi akses dari aplikasi lain
    fun getUriFromFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context, "com.example.lab_week_11_b.camera", file
        )
    }

    // Mendapatkan nama folder untuk gambar
    // Nama didefinisikan di file_provider_paths.xml
    fun getPicturesFolder(): String =
        Environment.DIRECTORY_PICTURES

    // Mendapatkan nama folder untuk video
    // Nama didefinisikan di file_provider_paths.xml
    fun getVideosFolder(): String =
        Environment.DIRECTORY_MOVIES
}