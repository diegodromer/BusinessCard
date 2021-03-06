package com.diegolima.businesscard.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import com.diegolima.businesscard.BuildConfig
import com.diegolima.businesscard.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class Image {

    companion object {

        fun share(context: Context, card: View) {
            val bitmap = getScreenShotFromView(card)

            bitmap?.let {
                saveMediaToStorage(context, bitmap)
            }
        }

        private fun saveMediaToStorage(context: Context, bitmap: Bitmap) {
            val filename = "${System.currentTimeMillis()}.jpg"

            var fos: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver?.also { resolver ->

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }

                    val imageUri: Uri? =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                    fos = imageUri?.let {
                        shareIntent(context, imageUri)
                        resolver.openOutputStream(it)
                    }
                }
            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)

                val photoUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", image)
                shareIntent(context, photoUri)

                fos = FileOutputStream(image)
            }else { //abaixo da N (SDK 24) -> n??o testado
                val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)

                shareIntent(context, Uri.fromFile(image))

                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Toast.makeText(context, "Imagem capturada com sucesso!", Toast.LENGTH_SHORT).show()
            }

        }

        private fun shareIntent(context: Context, imageUri: Uri) {
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/jpeg"
            }
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.resources.getText(R.string.label_share)
                )
            )
        }

        private fun getScreenShotFromView(card: View): Bitmap? {
            var screeshot: Bitmap? = null
            try {
                screeshot = Bitmap.createBitmap(
                    card.measuredWidth,
                    card.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(screeshot)
                card.draw(canvas)
            } catch (e: Exception) {
                Log.e("ERROR", "Falha ao capturar imagem ${e.message}")
            }
            return screeshot
        }
    }
}