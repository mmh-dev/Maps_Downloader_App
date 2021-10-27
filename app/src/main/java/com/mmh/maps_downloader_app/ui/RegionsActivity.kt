package com.mmh.maps_downloader_app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mmh.maps_downloader_app.adapters.MapsAdapter
import com.mmh.maps_downloader_app.databinding.ActivityRegionsBinding
import com.mmh.maps_downloader_app.entity.Region

class RegionsActivity : AppCompatActivity(), MapsAdapter.MapClickListener {

    private lateinit var binding: ActivityRegionsBinding
    private var regions = mutableListOf<Region>()
    private var mapAdapter = MapsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            setSupportActionBar(toolbarRegions)
            title = intent.getStringExtra("title")
            toolbarRegions.setTitleTextColor(android.graphics.Color.WHITE)
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
        mapAdapter.submitList(regions.sortedBy { it.country })
    }

    override fun onItemClick(position: Int) {

    }

    override fun onDownloadClick(position: Int) {
        try {
            val link = mapAdapter.getItem(position).link
            val fileName = mapAdapter.getItem(position).country + "_europe_2.obf.zip"
            downloadMaps(link, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downloadMaps(link: String, fileName: String) {


    }
}