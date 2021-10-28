package com.mmh.maps_downloader_app.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.mmh.maps_downloader_app.R
import com.mmh.maps_downloader_app.adapters.HeaderAdapter
import com.mmh.maps_downloader_app.adapters.MapsAdapter
import com.mmh.maps_downloader_app.databinding.ActivityMainBinding
import com.mmh.maps_downloader_app.entity.Region
import com.mmh.maps_downloader_app.utils.MyWorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity(), MapsAdapter.MapClickListener {

    private val headerAdapter = HeaderAdapter()
    private var mapAdapter = MapsAdapter(this)
    private lateinit var binding: ActivityMainBinding
    private var countries = mutableListOf<Region>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            setSupportActionBar(toolbarMain)
            title = getString(R.string.download_maps)
        }

        setProgressBar()
        fillRecyclerView()
    }

    private fun fillRecyclerView() {
        lifecycleScope.launch(Dispatchers.IO) {
            countries = parseData()
        }
        countries = parseData()
        val concatAdapter = ConcatAdapter(headerAdapter, mapAdapter)
        binding.apply {
            recyclerView.apply {
                adapter = concatAdapter
                layoutManager = LinearLayoutManager(this@MainActivity)
            }
        }
        mapAdapter.submitList(countries.sortedBy { it.country })
    }

    private fun parseData(): MutableList<Region> {
        val countries = mutableListOf<Region>()
        var regions = mutableListOf<Region>()
        var currentCountry = Region()
        var currentRegion = Region()
        try {
            val xmlData = applicationContext.assets.open("regions.xml")
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(xmlData, null)

            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    if (parser.name == "region" && parser.depth == 3) {  // is a country
                        if (currentCountry.hasRegions) {
                            currentCountry.regions = regions
                            countries.add(currentCountry)
                            regions = mutableListOf()
                            currentCountry = Region()
                            currentRegion = Region()
                        }
                        val attCount = parser.attributeCount
                        for (j in 0 until attCount) {
                            if (parser.getAttributeName(j) == "name") {
                                currentCountry.country = parser.getAttributeValue(j)
                            }
                            if (parser.getAttributeName(j) == "map" && parser.getAttributeValue(
                                    j
                                ) == "no"
                            ) {
                                currentCountry.isDownloadable = false
                            }
                            if (parser.getAttributeName(j) == "join_map_files" || parser.getAttributeName(
                                    j
                                ) == "join_road_files"
                            ) {  // define if country has regions or not
                                currentCountry.hasRegions = true
                            }
                        }
                    } else if (parser.name == "region" && parser.depth > 3) {  // is a region
                        currentRegion.country = currentCountry.country
                        val attCount = parser.attributeCount
                        for (j in 0 until attCount) {
                            if (parser.getAttributeName(j) == "name") {
                                currentRegion.region = parser.getAttributeValue(j)
                            }
                            if (parser.getAttributeName(j) == "map" && parser.getAttributeValue(
                                    j
                                ) == "no"
                            ) {
                                currentRegion.isDownloadable = false
                            }
                        }
                    }
                } else if (parser.eventType == XmlPullParser.END_TAG) {
                    if (parser.name == "region" && parser.depth == 3) {
                        if (!currentCountry.hasRegions) {
                            countries.add(currentCountry)
                            currentCountry = Region()
                            currentRegion = Region()
                        }
                    } else if (parser.name == "region" && parser.depth > 3) {
                        regions.add(currentRegion)
                        currentRegion = Region()
                    }
                }

                parser.next()
            }

        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        }
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

    private fun updateProgress(downloadedSize: Int, totalSize: Int) {

        Toast.makeText(this, downloadedSize.toString(), Toast.LENGTH_SHORT).show()

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

    override fun onDownloadClick(position: Int) {
        askPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
        ) { granted ->
            if (granted) {
                try {
                    if (!mapAdapter.getItem(position).hasRegions) {
                        val workManager = WorkManager.getInstance(this)
                        val jsonMap = Gson().toJson(mapAdapter.getItem(position))
                        val data = Data.Builder().putString("tag", jsonMap).build()
                        val parseWorker =
                            OneTimeWorkRequestBuilder<MyWorkManager>().setInputData(data).build()
                        workManager.enqueue(parseWorker)
                    } else {
                        onItemClick(position)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, getString(R.string.permissions_required), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onItemClick(position: Int) {
        if (mapAdapter.getItem(position).hasRegions) {
            val regions = mapAdapter.getItem(position).regions
            val regionsJson = Gson().toJson(regions)
            val intent = Intent(this, RegionsActivity::class.java)
            intent.putExtra("title", mapAdapter.getItem(position).country)
            intent.putExtra("regions", regionsJson)
            startActivity(intent)
        }

    }
}