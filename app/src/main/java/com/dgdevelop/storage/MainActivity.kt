package com.dgdevelop.storage

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var storageReference: StorageReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storageReference = FirebaseStorage.getInstance().reference

        ivImage.setOnClickListener {
            val i = Intent()
            i.type = "image/*"
            i.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(i, "Selecciona una imagen"), CHOOSER_IMAGES)
        }

        btnUpload.setOnClickListener {
            uploadImage()
        }

        btnDownload.setOnClickListener {
            downloadImage()
        }
    }

    private fun downloadImage() {
        try {
            val file = File.createTempFile("56356", "jpg")
            storageReference.child("56356.jpg").getFile(file)
                    .addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        ivImage.setImageBitmap(bitmap)
                    }
                    .addOnFailureListener{
                        Log.e(TAG, "Ocurrio un error al mostrar la imagen ${it.message}")
                    }

        }catch (e: Exception){
            Log.e(TAG, "Ocurrio un error en la descarga de imagenes ${e.message}")
        }
    }

    private fun uploadImage() {
        val girl = storageReference.child("image_girl.png")

        val imgBtm = ivImage.drawable as BitmapDrawable
        val imgCompress = imgBtm.bitmap

        val byteArrayOutputStream = ByteArrayOutputStream()
        imgCompress.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

        val imageByte = byteArrayOutputStream.toByteArray()

        val uploadTask = girl.putBytes(imageByte).addOnFailureListener {
            Log.e(TAG, "Ocurrio un error en la subida ${it.message}")
        }.addOnSuccessListener {
            Log.i(TAG, "Image upload success")
        }

        uploadTask.addOnProgressListener {task ->
            val progress = (100*task.bytesTransferred/task.totalByteCount).toInt()
            Log.i(TAG, "Progress: $progress")
        }.continueWithTask {task ->
            if (task.isSuccessful){
                task.exception?.let{
                    throw  it
                }
            }
            girl.downloadUrl
        }.addOnCompleteListener {task ->
            Log.w(TAG, "Path: ${task.result}")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHOOSER_IMAGES) {
            val imageUri = data?.data
            if (imageUri != null) {
                ivImage.setImageURI(imageUri)
            }
        }
    }

    companion object {
        private const val CHOOSER_IMAGES = 1
        private const val TAG = "MainActivity"
    }
}
