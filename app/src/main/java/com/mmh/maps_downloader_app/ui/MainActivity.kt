package com.mmh.maps_downloader_app.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.google.gson.Gson
import com.mmh.maps_downloader_app.R
import com.mmh.maps_downloader_app.adapters.HeaderAdapter
import com.mmh.maps_downloader_app.adapters.MapsAdapter
import com.mmh.maps_downloader_app.databinding.ActivityMainBinding
import com.mmh.maps_downloader_app.entity.Region
import com.mmh.maps_downloader_app.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity(), MapsAdapter.MapClickListener {

    private val headerAdapter = HeaderAdapter()
    private var mapAdapter = MapsAdapter(this, "main")
    private lateinit var binding: ActivityMainBinding
    private var countriesLiveData = MutableLiveData<MutableList<Region>>()
    private var countries: MutableList<Region>? = null

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch(Dispatchers.IO) {
            countries = parseData()
            withContext(Dispatchers.Main) {
                countriesLiveData.value = countries
            }
        }
    }

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
        val concatAdapter = ConcatAdapter(headerAdapter, mapAdapter)
        binding.apply {
            recyclerView.apply {
                adapter = concatAdapter
                layoutManager = LinearLayoutManager(this@MainActivity)
            }
        }
        countriesLiveData.observe(this) { list ->
            mapAdapter.submitList(list.sortedBy { it.country })
        }
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
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when {
                            parser.name == "region" && parser.depth == 3 -> {  // is a country
                                val attCount = parser.attributeCount
                                for (j in 0 until attCount) {
                                    if (parser.getAttributeName(j) == "name") currentCountry.country =
                                        parser.getAttributeValue(j)
                                    if (parser.getAttributeName(j) == "map" && parser.getAttributeValue(
                                            j
                                        ) == "no"
                                    ) currentCountry.isDownloadable = false
                                }
                            }
                            parser.name == "region" && parser.depth > 3 -> {  // is a region
                                currentRegion.country = currentCountry.country
                                currentCountry.hasRegions = true
                                val attCount = parser.attributeCount
                                for (j in 0 until attCount) {
                                    if (parser.getAttributeName(j) == "name") currentRegion.region =
                                        parser.getAttributeValue(j)
                                    if (parser.getAttributeName(j) == "map" && parser.getAttributeValue(
                                            j
                                        ) == "no"
                                    ) currentRegion.isDownloadable = false
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "region" && parser.depth == 3) {
                            if (currentCountry.hasRegions) {
                                regions.removeIf { it.country == null }
                                currentCountry.regions = regions
                                regions = mutableListOf()
                            }
                            countries.add(currentCountry)
                            currentCountry = Region()
                            currentRegion = Region()
                        } else if (parser.name == "region" && parser.depth > 3) {
                            regions.add(currentRegion)
                            currentRegion = Region()
                        }
                    }
                }
                parser.nextTag()
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
                        val downloadWorker = OneTimeWorkRequestBuilder<MyWorkManager>()
                            .setInputData(data)
                            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                                    .setRequiresStorageNotLow(true).build()).build()
                        workManager.enqueueUniqueWork("download", ExistingWorkPolicy.APPEND_OR_REPLACE, downloadWorker)

                        workManager.getWorkInfoByIdLiveData(downloadWorker.id).observe(this, Observer{
                            if (it.state == WorkInfo.State.RUNNING){
                                mapAdapter.getItem(position).progress = it.progress.getInt(PROGRESS, 0)
                            }
                            else if (it.state == WorkInfo.State.SUCCEEDED){
                                binding.root.showSnackBar(mapAdapter.getItem(position).country + " map is downloaded!")
                            }
                        })
                    } else {
                        onItemClick(position)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                binding.root.showSnackBar(getString(R.string.permissions_required))
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