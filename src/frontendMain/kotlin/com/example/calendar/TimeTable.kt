package com.example.calendar

import io.kvision.core.JustifyItems
import io.kvision.html.span
import io.kvision.panel.GridPanel
import io.kvision.table.*
import io.kvision.panel.VPanel
import io.kvision.panel.vPanel
import io.kvision.utils.perc
import kotlin.js.Date

class TimeTable:GridPanel(justifyItems = JustifyItems.CENTER,templateColumns = "repeat(7, 1fr);", columnGap = 3) {

    init {
        width = 100.perc
        height = 100.perc
      listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag").forEach {
          this@TimeTable.vPanel { span(it)}
      }
        val month =10
        span(Date(2023,month,1).toString())
        val  d1 = Date(2023,month,1) //1. Sept
        val d2 = Date(2023, month+1,0) //30.Sept
        val d0 = Date(2023,month,0)
        val dSunday = if(d0.getDay() == 0){7}else(d0.getDay())
        for (i in dSunday downTo 1){
            val day = d0.getDate() + 1 - i
            span(day.toString())
        }

        for(i in d1.getDate()..d2.getDate()){
            span(i.toString())
        }

        var lastDate: Int = 1
        for(i in d2.getDay() .. 6){
            span(Date(2023, 10, lastDate).getDate().toString())
            lastDate += 1
        }

    }
}
