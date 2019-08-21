package xyz.ismailnurudeen.numberfacts

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*

class AppUtils(private val context: Activity) {

    fun saveBitmapToExternalStorage(bitmap: Bitmap) {
        val savePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "saved_fact_card${UUID.randomUUID()}.png")
        try {
            val outputStream = FileOutputStream(savePath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(context, "Saved to gallery successfully...", Toast.LENGTH_SHORT).show()
        } catch (fnfe: FileNotFoundException) {
            Log.e("SAVE ERROR", fnfe.localizedMessage)
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun getTempImageToShare(bitmap: Bitmap): File {
        val savePath = File(Environment.getExternalStorageDirectory(), "temp_share_fact.png")
        try {
            val outputStream = FileOutputStream(savePath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (fnfe: FileNotFoundException) {
            Log.e("SAVE ERROR", fnfe.localizedMessage)
        }
        return savePath
    }

    fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background

        if (bgDrawable != null) bgDrawable.draw(canvas)
        else canvas.drawColor(Color.WHITE)

        view.draw(canvas)
        return bitmap
    }

    fun checkPermission(permission: String, requestCode: Int): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            return if (ContextCompat.checkSelfPermission(context,
                            permission) == PackageManager.PERMISSION_GRANTED) {
                Log.v("Permission Request", "Permission is granted")
                true
            } else {
                ActivityCompat.requestPermissions(context, arrayOf(permission), requestCode)

                Log.v("Permission Request", "Permission is revoked")
                false
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v("Permission Request", "Permission is granted")
            return true
        }
    }

    fun checkNetworkState(): Boolean {
        val conman = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = conman.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun isWhiteText(color: Int): Boolean {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        // https://en.wikipedia.org/wiki/YIQ
        // https://24ways.org/2010/calculating-color-contrast/
        val yiq = (red * 299 + green * 587 + blue * 114) / 1000
        return yiq < 192
    }
}