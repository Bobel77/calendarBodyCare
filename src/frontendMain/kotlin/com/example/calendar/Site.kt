package com.example.calendar

import io.kvision.core.Container
import io.kvision.core.onChange
import io.kvision.form.time.DateTime
import io.kvision.html.Span
import io.kvision.html.span
import io.kvision.panel.tab
import io.kvision.panel.tabPanel
import io.kvision.panel.vPanel
import io.kvision.types.KV_DEFAULT_DATE_FORMAT
import io.kvision.utils.perc
import kotlinx.browser.localStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.js.Date

class Site(
    val siteName: String,
    val content: List<Span>,
    val function: String? = null
){

    fun intern(container: Container){
        container.tabPanel(){
            width = 100.perc
                tab("Termine buchen"){
                    calender(this@tab)
                }
                tab("Video"){
                    vid(this@tab)
                }
                tab("Passwort ändern"){
                  /* *//* val timeTable = TimeTable()*//*
                    this@tab.add(timeTable)*/

                   val  d1 = Date(2023,9,1)
                   val d2 = Date(2023,9,0)
                    vPanel {
                        val d1d = if(d1.getDay() == 0){7}else{d1.getDate()}
                        for (i in d1.getDay()downTo 0){
                            val day = Date(2023,8,0).getDate() - i
                            this@vPanel.span(day.toString())
                        }
                        for(i in d1.getDate()..d2.getDate()){
                            this@vPanel.span(i.toString())
                        }

                        var lastDate: Date = d2

                            while(lastDate.getDay() != 0){
                                this@vPanel.span(lastDate.toString())
                                lastDate = Date(lastDate.getMilliseconds() + 86400000)
                            }


                    }

                }
            }
    }

    fun calender(container: Container){
        var table = TurnTable()

        container.add(
            DateTime(format = "YYYY-MM-DD", label = "Trainingswoche wählen").apply {
                placeholder = "Datum auswählen"
                onChange {
                    AppScope.launch {
                        localStorage.setItem("year",value!!.getFullYear().toString() )
                        localStorage.setItem("month",value!!.getUTCMonth().toString() )
                        localStorage.setItem("date",value!!.getDate().toString() )
                        Model.getEvents(value!!.getFullYear(), value!!.getUTCMonth()+1,
                            value!!.getDate())
                        delay(100)
                        table.updateTable()
                    }
                }
            }
        )
        container.add(table)
        AppScope.launch {
            table.updateTable()
        }
    }

    fun vid(container: Container){
        AppScope.launch {
            Model.videos(container)
        }
    }
}