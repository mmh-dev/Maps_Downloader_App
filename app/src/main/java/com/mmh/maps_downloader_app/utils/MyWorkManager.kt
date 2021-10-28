package com.mmh.maps_downloader_app.utils

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.mmh.maps_downloader_app.entity.Region
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.net.MalformedURLException


class MyWorkManager(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        var countries = mutableListOf<Region>()
        var regions = mutableListOf<Region>()
        var currentCountry = Region()
        var currentRegion = Region()
        val tag = inputData.getString("tag")
        if (tag == "parse") {
            try {
                val xmlData = applicationContext.assets.open("regions.xml")
                val parser = XmlPullParserFactory.newInstance().newPullParser()
                parser.setInput(xmlData, null)

                while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG) {
                        if (parser.name == "region" && parser.depth == 3){  // is a country
                            if (currentCountry.hasRegions){
                                currentCountry.regions = regions
                                countries.add(currentCountry)
                                regions = mutableListOf<Region>()
                                currentCountry = Region()
                                currentRegion = Region()
                            }
                            val attCount = parser.attributeCount
                            for (j in 0 until attCount) {
                                if (parser.getAttributeName(j) == "name") {
                                    currentCountry.country = parser.getAttributeValue(j)
                                }
                                if(parser.getAttributeName(j) == "map" && parser.getAttributeValue(j) == "no"){
                                    currentCountry.isDownloadable = false
                                }
                                if (parser.getAttributeName(j) == "join_map_files" || parser.getAttributeName(j) == "join_road_files" ){  // define if country has regions or not
                                    currentCountry.hasRegions = true
                                }
                            }
                        } else if (parser.name == "region" && parser.depth > 3){  // is a region
                            currentRegion.country = currentCountry.country
                            val attCount = parser.attributeCount
                            for (j in 0 until attCount) {
                                if (parser.getAttributeName(j) == "name") {
                                    currentRegion.region = parser.getAttributeValue(j)
                                }
                                if(parser.getAttributeName(j) == "map" && parser.getAttributeValue(j) == "no"){
                                    currentRegion.isDownloadable = false
                                }
                            }
                        }
                    } else if (parser.eventType == XmlPullParser.END_TAG){
                        if (parser.name == "region" && parser.depth == 3){
                            if (!currentCountry.hasRegions){
                                countries.add(currentCountry)
                                currentCountry = Region()
                                currentRegion = Region()
                            }
                        } else if (parser.name == "region" && parser.depth > 3){
                            regions.add(currentRegion)
                            currentRegion = Region()
                        }
                    }
                    parser.next()
                }

            } catch (e: XmlPullParserException) {
                e.printStackTrace()
                return Result.failure()
            }
            val countriesJson = Gson().toJson(countries)
            val outputData = Data.Builder().putString("countries", countriesJson).build()
            return Result.success(outputData)
        } else {
            val region = Gson().fromJson(tag, Region::class.java)
            downloadMaps(region)
            val downloadProgress = String()
            val outputData = Data.Builder().putString("progress", downloadProgress).build()
            return Result.success(outputData)
        }
    }
}

fun downloadMaps(region: Region) {
    try {
//        val url = URL(link)
//        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
//        urlConnection.requestMethod = "GET"
//        urlConnection.doOutput = true
//        urlConnection.connect()
//        val sdcard = Environment.getExternalStorageDirectory()
//        val file = File(sdcard, fileName)
//        val fileOutput = FileOutputStream(file)
//        val inputStream: InputStream = urlConnection.inputStream
//        val totalSize = urlConnection.contentLength
//        var downloadedSize = 0
//        val buffer = ByteArray(1024)
//        var bufferLength = 0
//        while (inputStream.read(buffer) > 0) {
//            bufferLength = inputStream.read(buffer)
//            fileOutput.write(buffer, 0, bufferLength);
//            downloadedSize += bufferLength;
//            updateProgress(downloadedSize, totalSize);
//        }
//        fileOutput.close()
    } catch (e: MalformedURLException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}