package com.mmh.maps_downloader_app.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.mmh.maps_downloader_app.R
import com.mmh.maps_downloader_app.adapters.HeaderAdapter
import com.mmh.maps_downloader_app.adapters.MapsAdapter
import com.mmh.maps_downloader_app.databinding.ActivityMainBinding
import com.mmh.maps_downloader_app.entity.Region
import com.mmh.maps_downloader_app.utils.MyWorkManager
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
        val workManager = WorkManager.getInstance(this)
        val data = Data.Builder().putString("tag", "parse").build()
        val parseWorker = OneTimeWorkRequestBuilder<MyWorkManager>().setInputData(data).build()
        workManager.enqueue(parseWorker)
        workManager.getWorkInfoByIdLiveData(parseWorker.id)
            .observe(this, { workInfo ->
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    val jsonString = workInfo.outputData.getString("countries")
                    val type = object : TypeToken<List<Region>?>() {}.type
                    countries = Gson().fromJson(jsonString, type)
                    val concatAdapter = ConcatAdapter(headerAdapter, mapAdapter)
                    binding.apply {
                        recyclerView.apply {
                            adapter = concatAdapter
                            layoutManager = LinearLayoutManager(this@MainActivity)
                        }
                    }
                    mapAdapter.submitList(countries.sortedBy { it.country })
                }
            })
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
//                    if (!mapAdapter.getItem(position).hasRegions) {
//                        val workManager = WorkManager.getInstance(this)
//                        val jsonMap = Gson().toJson(mapAdapter.getItem(position))
//                        val data = Data.Builder().putString("tag", jsonMap).build()
//                        val parseWorker = OneTimeWorkRequestBuilder<MyWorkManager>().setInputData(data).build()
//                        workManager.enqueue(parseWorker)
//                    } else {
//                        onItemClick(position)
//                    }
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
        val regions = mapAdapter.getItem(position).regions
        val regionsJson = Gson().toJson(regions)
        val intent = Intent(this, RegionsActivity::class.java)
        intent.putExtra("title", mapAdapter.getItem(position).country)
        intent.putExtra("regions", regionsJson)
        startActivity(intent)
    }
}