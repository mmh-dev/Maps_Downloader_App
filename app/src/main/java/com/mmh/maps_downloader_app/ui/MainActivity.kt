package com.mmh.maps_downloader_app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
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

class MainActivity : AppCompatActivity(), MapsAdapter.MapClickListener {

    private var mapAdapter = MapsAdapter(this)
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
        var regions = mutableListOf<Region>()
        try {
            val xmlData = assets.open("regions.xml")
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(xmlData, null)

            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG && parser.name == "region") {
                    var attCount = parser.attributeCount
                    for (i in 0 until attCount) {
                        if (parser.getAttributeName(i) == "lang") {   //находим страну
                            val country = Region()
                            country.country = parser.getAttributeValue(0)
                            for (j in 0 until attCount) {
                                if (parser.getAttributeName(j) == "map" && parser.getAttributeValue(
                                        j
                                    ) == "no"
                                ) {
                                    country.isDownloadable = false
                                }
                            }
                            countries.add(country)
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

    override fun onItemClick(position: Int) {
        askPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
        ) { granted ->
            if (granted) {
                try {
                    val link = mapAdapter.getItem(position).link
                    downloadMaps(link)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else{
                Toast.makeText(this, getString(R.string.need_permissions), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadMaps(link: String) {


    }

    private fun askPermission(vararg permissions: String, callback: (Boolean) -> Unit) {
        Dexter.withContext(this)
            .withPermissions(*permissions)
            .withListener(object : BaseMultiplePermissionsListener() {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport) {
                    callback(p0.areAllPermissionsGranted())
                }
            }).check()
    }
}