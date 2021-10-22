package com.mmh.maps_downloader_app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mmh.maps_downloader_app.R
import com.mmh.maps_downloader_app.databinding.ActivityMainBinding
import com.mmh.maps_downloader_app.entity.Region
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            setSupportActionBar(toolbarMain)
            title = getString(R.string.download_maps)
        }

        setProgressBar()
        parseMapsData()
    }

    private fun parseMapsData() {
        val countries = mutableListOf<String>()
        val regions = mutableListOf<String>()
        var attCount = 0
        try {
            val xmlData = assets.open("regions.xml")
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(xmlData, null)

            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG && parser.name == "region") {
                    attCount = parser.attributeCount
                     for (i in 0 until attCount){
                         if (parser.getAttributeName(i) == "lang"){   //находим страну
                             for (j in 0 until attCount){
                                 if (parser.getAttributeName(j) == "name"){

                                 }
                             }

                         }
                     }
                }
                parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.i("count", countries.size.toString())
    }

    private fun setProgressBar() {
        val freeBytesExternal: Long = File(getExternalFilesDir(null).toString()).freeSpace
        val freeSpaceInGb = (freeBytesExternal.toDouble() / (1024 * 1024 * 1024)).round(2)

        val totalSize: Long = File(getExternalFilesDir(null).toString()).totalSpace
        val totalSpaceInGb = (totalSize.toDouble() / (1024 * 1024 * 1024)).round(2)

        binding.apply {
            freeSpace.text = freeSpaceInGb.toString()
            val freeSpacePercentage: Int = ((freeSpaceInGb / totalSpaceInGb) * 100).toInt()
            linearProgressIndicator.progress = freeSpacePercentage
        }
    }

    private fun Double.round(decimals: Int = 2): Double =
        "%.${decimals}f".format(Locale.US, this).toDouble()
}