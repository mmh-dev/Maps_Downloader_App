package com.mmh.maps_downloader_app.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.mmh.maps_downloader_app.entity.Region
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MyWorkManager(context: Context, workerParams: WorkerParameters) :
    Worker (context, workerParams) {

    override fun doWork(): Result {

        var currentProgress = 0
        val region = Gson().fromJson(inputData.getString("tag"), Region::class.java)
        setProgressAsync(workDataOf(PROGRESS to currentProgress))

        try {
            val url = URL(region.link)
            val fileName = region.link.subSequence(58, region.link.length) as String
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.doOutput = true
            urlConnection.connect()

            val file = File((applicationContext.getExternalFilesDir("")), fileName)
            val fileOutput = FileOutputStream(file)
            val inputStream: InputStream = urlConnection.inputStream
            val totalSize = urlConnection.contentLength
            var downloadedSize = 0
            val buffer = ByteArray(1024)
            var bufferLength = 0
            while (inputStream.read(buffer) > 0) {
                bufferLength = inputStream.read(buffer)
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength
                currentProgress = (downloadedSize / totalSize) * 100
                setProgressAsync(workDataOf(PROGRESS to currentProgress))

            }
            fileOutput.close()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Result.success()
    }
}