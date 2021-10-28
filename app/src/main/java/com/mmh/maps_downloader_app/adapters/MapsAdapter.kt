package com.mmh.maps_downloader_app.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mmh.maps_downloader_app.databinding.MapItemBinding
import com.mmh.maps_downloader_app.entity.Region

class MapsAdapter(var listener: MapClickListener, val tag: String) :
    ListAdapter<Region, MapsAdapter.MapsViewHolder>(DiffCallBack()) {

    interface MapClickListener {
        fun onItemClick(position: Int)
        fun onDownloadClick(position: Int)
    }

    public override fun getItem(position: Int): Region {
        return super.getItem(position)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapsViewHolder {

        val binding = MapItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MapsViewHolder(binding, listener, tag)
    }

    override fun onBindViewHolder(holder: MapsViewHolder, position: Int) {
        val currentRegion = getItem(position)
        holder.bind(currentRegion)
    }

    class MapsViewHolder(private val binding: MapItemBinding, var listener: MapClickListener, val tag: String) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.downloadBtn.setOnClickListener {
                listener.onDownloadClick(adapterPosition)
            }

            binding.bodyLayout.setOnClickListener {
                if (tag == "main") listener.onItemClick(adapterPosition)
            }
        }

        fun bind(region: Region) {
            binding.apply {
                if (tag == "main") locationName.text = region.country
                else locationName.text = region.region?.capitalize()
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