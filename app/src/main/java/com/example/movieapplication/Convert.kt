package com.example.movieapplication
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun uploadImageToImgBB(
    imageBase64: String,
    apiKey: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val client = OkHttpClient()

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("image", imageBase64)
        .build()

    val request = Request.Builder()
        .url("https://api.imgbb.com/1/upload?key=$apiKey")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError(e.message ?: "Upload failed")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val json = response.body?.string() ?: ""
                try {
                    val jsonObject = JSONObject(json)
                    val data = jsonObject.getJSONObject("data")
                    val url = data.getString("url")
                    onSuccess(url)
                } catch (e: Exception) {
                    onError(e.message ?: "Error parsing response")
                }
            } else {
                onError("Upload failed with code ${response.code}")
            }
        }
    })
}