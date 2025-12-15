
package com.example.a20251215.MypageFragment

import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

data class HolidayItem(
    val yyyymmdd: String, // "20251225"
    val name: String
)

object HolidayXmlParser {

    fun parse(xml: String): List<HolidayItem> {
        val list = mutableListOf<HolidayItem>()

        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(StringReader(xml))

        var locdate: String? = null
        var dateName: String? = null
        var isHoliday: String? = null

        var eventType = parser.eventType
        while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                org.xmlpull.v1.XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "item" -> {
                            locdate = null
                            dateName = null
                            isHoliday = null
                        }
                        "locdate" -> locdate = parser.nextText()
                        "dateName" -> dateName = parser.nextText()
                        "isHoliday" -> isHoliday = parser.nextText()
                    }
                }

                org.xmlpull.v1.XmlPullParser.END_TAG -> {
                    if (parser.name == "item") {
                        if (isHoliday == "Y" && !locdate.isNullOrBlank()) {
                            list.add(
                                HolidayItem(
                                    yyyymmdd = locdate!!,
                                    name = dateName ?: ""
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return list
    }
}