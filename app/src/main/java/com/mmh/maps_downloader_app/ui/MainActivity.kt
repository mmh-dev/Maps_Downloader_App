package com.mmh.maps_downloader_app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mmh.maps_downloader_app.R
import com.mmh.maps_downloader_app.databinding.ActivityMainBinding
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

        setProgressBar()
        parseMapsData()

        binding.apply {
            setSupportActionBar(toolbarMain)
            title = getString(R.string.download_maps)
        }
    }

    private fun parseMapsData() {
        try {
            val inputStream = assets.open("regions.xml")
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
            val regList = doc.getElementsByTagName("region")
            for (i in 0 until regList.length){

            }

        } catch (e: Exception){
            e.printStackTrace()
        }

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