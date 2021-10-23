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

class MapsAdapter (var listener: MapClickListener) : ListAdapter<Region, MapsAdapter.MapsViewHolder>(DiffCallBack()) {

    interface MapClickListener {
        fun onItemClick(position: Int)
    }
    public override fun getItem(position: Int): Region {
        return super.getItem(position)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapsViewHolder {

        val binding = MapItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MapsViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: MapsViewHolder, position: Int) {
        val currentRegion = getItem(position)
        holder.bind(currentRegion)
    }

    class MapsViewHolder(private val binding: MapItemBinding, var listener: MapClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.downloadBtn.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }

        fun bind(region: Region) {
            binding.apply {
                if (region.hasRegions) {
                    locationName.text = region.country
                } else {
                    locationName.text = region.region
                }
            }
        }
    }
}

class DiffCallBack : DiffUtil.ItemCallback<Region>() {
    override fun areItemsTheSame(oldItem: Region, newItem: Region) =
        oldItem == newItem

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Region, newItem: Region) =
        oldItem == newItem
}