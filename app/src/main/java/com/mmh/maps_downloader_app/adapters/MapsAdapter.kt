package com.mmh.maps_downloader_app.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.opengl.Visibility
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mmh.maps_downloader_app.databinding.MapItemBinding
import com.mmh.maps_downloader_app.entity.Region

class MapsAdapter : ListAdapter<Region, MapsAdapter.MapsViewHolder>(DiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapsViewHolder {

        val binding = MapItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MapsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MapsViewHolder, position: Int) {
        val currentRegion = getItem(position)
        holder.bind(currentRegion)
    }

    class MapsViewHolder(private val binding: MapItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(region: Region) {
            binding.apply {
//                if (region.region == "Europe") {
//                    mapIcon.visibility = View.GONE
//                    downloadBtn.visibility = View.GONE
//                    divider.visibility = View.GONE
//                    locationName.typeface = Typeface.DEFAULT_BOLD
//                    (locationName.layoutParams as ConstraintLayout.LayoutParams).apply {
//                        marginStart = 16.dpToPixels(mapIcon.context)
//                    }
//                }
                if (region.hasRegions) {
                    locationName.text = region.country
                } else {
                    locationName.text = region.region
                }
            }
        }
    }
}

fun Int.dpToPixels(context: Context): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
).toInt()

class DiffCallBack : DiffUtil.ItemCallback<Region>() {
    override fun areItemsTheSame(oldItem: Region, newItem: Region) =
        oldItem == newItem

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Region, newItem: Region) =
        oldItem == newItem
}