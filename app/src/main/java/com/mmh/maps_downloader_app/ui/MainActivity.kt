package com.mmh.maps_downloader_app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mmh.maps_downloader_app.R
import com.mmh.maps_downloader_app.adapters.MapsAdapter
import com.mmh.maps_downloader_app.databinding.ActivityMainBinding
import com.mmh.maps_downloader_app.entity.Region
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mapAdapter = MapsAdapter()
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
        fillRecyclerView(parseMapsData())

    }

    private fun fillRecyclerView(countries: List<Region>) {
        binding.apply {
            recyclerView.apply {
                adapter = mapAdapter
                layoutManager = LinearLayoutManager(this@MainActivity)
            }
        }
        mapAdapter.submitList(countries)

    }

    private fun parseMapsData(): List<Region> {
        var countries = mutableListOf<Region>()
        val europe = Region()
        europe.country = "europe"
        countries.add(europe)
        var regions = mutableListOf<Region>()
        var region = Region()
        var attCount: Int
        try {
            val xmlData = assets.open("regions.xml")
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(xmlData, null)

            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG && parser.name == "region") {
                    attCount = parser.attributeCount
                    for (i in 0 until attCount) {
                        if (parser.getAttributeName(i) == "lang") {   //находим страну
                            for (j in 0 until attCount) {
                                if (parser.getAttributeName(j) == "name") {
                                    val country = Region()
                                    country.country = parser.getAttributeValue(j)
                                    countries.add(country)
                                }
                            }

                        }
                    }
                }
                parser.next()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.i("count", countries.size.toString())
        return countries
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