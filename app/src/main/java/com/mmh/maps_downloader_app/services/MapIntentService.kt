package com.mmh.maps_downloader_app.services

import android.app.IntentService
import android.content.Intent
import com.mmh.maps_downloader_app.entity.Region
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException

class MapIntentService (name: String = "MapIntentService"): IntentService(name) {
    override fun onHandleIntent(intent: Intent?) {
        val link = intent?.getStringExtra("link")
        if (link == "parse"){
            var countries = mutableListOf<Region>()
            var regions = mutableListOf<Region>()
            try {
                val xmlData = assets.open("regions.xml")
                val parser = XmlPullParserFactory.newInstance().newPullParser()
                parser.setInput(xmlData, null)

                while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "region") {
                        var attCount = parser.attributeCount
                        for (i in 0 until attCount) {
                            if (parser.getAttributeName(i) == "lang") {   //находим страну
                                for (j in 0 until attCount) {
                                    if (parser.getAttributeName(j) == "name") {
                                        val country = Region()
                                        country.country = parser.getAttributeValue(j)
                                        countries.add(country)
                                    }
                                }

                            }
                        }
                    }
                    parser.next()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

//            val responseIntent = Intent()
//            responseIntent.action = ACTION_MYINTENTSERVICE
//            responseIntent.putExtra("countries", countries as Serializable)
//            responseIntent.putExtra("regions", regions as Serializable)
//            sendBroadcast(responseIntent)

        } else{
            // TODO: downloading maps
        }
    }
}