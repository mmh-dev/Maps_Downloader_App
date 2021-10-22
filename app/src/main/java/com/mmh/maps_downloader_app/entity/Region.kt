package com.mmh.maps_downloader_app.entity

data class Region(
    private var _country: String? = null,
    var region: String? = null,
    var isDownloadable: Boolean = true,
    var hasRegions: Boolean = true,
    var regions: List<Region>? = null,
    private var _link: String? = null
) {

    var country: String? = _country
        get() {
            return field?.capitalize()
        }

    val link: String
        get() = if (hasRegions) "http://download.osmand.net/download.php?standard=yes&file=" + country?.capitalize() + "_" + region + "_europe_2.obf.zip"
        else "http://download.osmand.net/download.php?standard=yes&file=" + country?.capitalize() + "_europe_2.obf.zip"

}