package com.mmh.maps_downloader_app.entity

data class Region(
    var name: String? = null,
    var isDownloadable: Boolean = true,
    var hasRegions: Boolean = true
){
//    var link: String? = "http://download.osmand.net/download.php?standard=yes&file=" + name.replaceFirstChar {  } "_europe_2.obf.zip"
    var regions: List<Region>? = null
}