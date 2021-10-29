package com.mmh.maps_downloader_app.ui

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mmh.maps_downloader_app.R
import com.mmh.maps_downloader_app.adapters.MapsAdapter
import com.mmh.maps_downloader_app.databinding.ActivityRegionsBinding
import com.mmh.maps_downloader_app.entity.Region
import com.mmh.maps_downloader_app.utils.MyWorkManager
import com.mmh.maps_downloader_app.utils.PROGRESS
import com.mmh.maps_downloader_app.utils.askPermission
import com.mmh.maps_downloader_app.utils.showSnackBar

class RegionsActivity : AppCompatActivity(), MapsAdapter.MapClickListener {

    private lateinit var binding: ActivityRegionsBinding
    private var regions = mutableListOf<Region>()
    private var mapAdapter = MapsAdapter(this, "second")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            setSupportActionBar(toolbarRegions)
            title = intent.getStringExtra("title")
            toolbarRegions.setTitleTextColor(android.graphics.Color.WHITE)

            val regionsJson = intent.getStringExtra("regions")
            val type = object : TypeToken<List<Region>?>() {}.type
            regions = Gson().fromJson(regionsJson, type)

            back.setOnClickListener {
                finish()
            }
        }

        fillRecyclerView()
    }

    private fun fillRecyclerView() {
        binding.apply {
            regionsRecyclerView.apply {
                adapter = mapAdapter
                layoutManager = LinearLayoutManager(this@RegionsActivity)
            }
        }
        mapAdapter.submitList(regions.sortedBy { it.region })
    }

    override fun onItemClick(position: Int) {

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
                            .setConstraints(
                                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresStorageNotLow(true).build()).build()
                        workManager.enqueueUniqueWork("download", ExistingWorkPolicy.APPEND_OR_REPLACE, downloadWorker)

                        workManager.getWorkInfoByIdLiveData(downloadWorker.id).observe(this, Observer{
                            if (it.state == WorkInfo.State.RUNNING){
                                mapAdapter.getItem(position).progress = it.progress.getInt(PROGRESS, 0)
                            }
                            else if (it.state == WorkInfo.State.SUCCEEDED){
                                binding.root.showSnackBar(mapAdapter.getItem(position).country + "_" + mapAdapter.getItem(position).region + " map is downloaded!")
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
}