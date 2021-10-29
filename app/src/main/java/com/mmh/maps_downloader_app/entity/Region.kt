package com.mmh.maps_downloader_app.entity

import java.io.Serializable

data class Region(
    private var _country: String? = null,
    var region: String? = null,
    var isDownloadable: Boolean = true,
    var hasRegions: Boolean = false,
    var regions: List<Region>? = null,
    var progress: Int = 0
) : Serializable {

    var country: String? = _country
        get() {
            return field?.capitalize()
        }

    val link: String
        get() = if (hasRegions) "http://download.osmand.net/download.php?standard=yes&file=" + country?.capitalize() + "_" + region + "_europe_2.obf.zip"
        else "http://download.osmand.net/download.php?standard=yes&file=" + country?.capitalize() + "_europe_2.obf.zip"

}