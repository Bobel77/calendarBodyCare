package com.example.calendar

import io.kvision.core.*
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.span
import io.kvision.modal.Modal
import io.kvision.panel.*
import io.kvision.table.*
import io.kvision.table.TableType
import io.kvision.tabulator.*
import io.kvision.toast.Toast
import io.kvision.toast.ToastContainerPosition
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.types.toStringF
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.browser.localStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer
import kotlin.js.Date

class TimeTable: GridPanel(/*justifyItems = JustifyItems.CENTER,*/ templateColumns = "repeat(6, 1fr)", columnGap = 3) {

    init {
        AppScope.launch { Model.getEvents(2023, 9, 13)
            delay(50)
            addDays()
        }

        width = 100.perc
        height = 100.perc



    }
    fun addDays(){
        listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag").forEach {
            this@TimeTable.vPanel { span(it) }
        }

        val month = 8
        val year = 2023
        val dayArr = arrayListOf<Date>()
        val  d1 = Date(year,month,1) //1. Sept
        val d2 = Date(year, month+1,0) //30.Sept
        val d0 = Date(year,month,0)
        val dSunday = if(d0.getDay() == 0){7}else(d0.getDay())
        for (i in dSunday downTo 1){
            val day = d0.getDate() + 1 - i
            dayArr.add(Date(year, month-1, day))
        }

        for(i in d1.getDate()..d2.getDate()){
            dayArr.add(Date(year, month, i))
        }

        var lastDate: Int = 1
        for(i in d2.getDay() .. 6){
            dayArr.add(Date(year, month+1, lastDate))
            lastDate += 1
        }
        console.log(Model.myEvents)
        dayArr.forEach {date->
            if(date.getDay() != 0){
                vPanel {
                    span(date.getDate().toString())

                    val eventsOnDay = Model.myEvents.find{event -> event.first.localDateTime!!.toStringF("DD.MM.YYYY") == date.toStringF("DD.MM.YYYY")}
                    if(eventsOnDay != null){
                        termincard(eventsOnDay)
                        /*   span("${eventsOnDay.first.localDateTime!!.toStringF("HH:mm")} ${eventsOnDay.first.training}" )*/
                    }
                }

            }
        }
    }

    //////////////////
    fun Container.termincard(mevent: Pair<MyEvent, List<Member?>>){
        val event = mevent.first
        add(div(className = "card"){

            border = Border(1.px, BorderStyle.SOLID, Color.name(Col.LIGHTGRAY))
            width = 100.perc
            height = 100.perc
            textAlign = TextAlign.CENTER

            background = Background(color = Color(Col.BLACK.toString()))
            vPanel{
                val members = mevent.second
                width = 100.perc
                height = 100.perc

                val lDate = event.localDateTime

                span(){  fontWeight = FontWeight.BOLDER}
                span("${lDate!!.toStringF("HH:mm")} - ${event.training}"){
                    fontWeight = FontWeight.BOLDER
                }

                AppScope.launch {
                    if(Model.bigM()){
                        if(members[0] == null){
                            span(  "0/ 13")
                        }
                        else{
                            span( "${members.size} /13")
                        }
                        this@vPanel.onClick(){
                            bigMUnit(mevent, event.id!!)
                        }
                        background = Background(color = Color(Col.LIGHTGREEN.toString()))
                        if(members.size == 13){
                            background = Background(color = Color(Col.DIMGRAY.toString()))
                        }
                    }
                    else{

                        if(members.size == 13){
                            background = Background(color = Color(Col.DIMGRAY.toString()))
                        }

                        else if(members.any{it?.username == Model.member.value.username!! }){
                            background = Background(color = Color(Col.GOLD.toString()))
                            onClick {
                                terminAbsagen(event.id)
                            }
                        }
                        else {
                            background = Background(color = Color(Col.LIGHTGREEN.toString()))
                            onClick {
                                if(Model.member.value.abo != true || Date().getDate()+1 == event.localDateTime?.getDate()){
                                    terminBuchen(event.id)
                                }
                            }
                        }
                    }

                    this@vPanel.onEvent{
                        mouseover = {
                            opacity = 0.9
                            cursor = Cursor.POINTER
                        }
                        mouseleave = {
                            opacity = 1.0
                            cursor = Cursor.DEFAULT
                        }
                    }
                }

            }

        })
    }

    private fun bigMUnit(event: Pair<MyEvent, List<Member?>>, id: Int) {
        Modal(event.first.localDateTime?.toStringF("DD.MM.YYYY HH:mm")) {
            width = 100.perc
            height = 100.perc

            tabPanel {
                tab("Teilnehmer"){
                    table(listOf("Vorname", "Nachname", "Abmelden"),
                        setOf(TableType.BORDERED, TableType.SMALL, TableType.STRIPED, TableType.HOVER),
                        responsiveType = ResponsiveType.RESPONSIVE) {
                        event.second.forEach {mem->
                            Row{
                                cell(mem?.vorname)
                                cell(mem?.nachname)
                                cell{
                                    button("X").apply {
                                        this.width = 100.perc
                                        onClick {
                                            if(mem != null){
                                                deleteEvent(mem.id!!, id)
                                            }
                                            this@Modal.hide()
                                        }
                                    }
                                }
                            }.apply {
                                this@table.add(this)

                            }
                        }
                    }

                }
                tab("Alle Mitglieder") {
                    val tabu =  tabulator(Model.allMembers,options = TabulatorOptions(selectable = true,
                        layout = Layout.FITCOLUMNS,
                        pagination = true,
                        paginationSize = 10,
                        columns = listOf(
                            ColumnDefinition("Vorname", "vorname", formatter = Formatter.TEXTAREA),
                            ColumnDefinition("Nachname", "nachname", formatter = Formatter.TEXTAREA),
                            ColumnDefinition("Abo", "abo", formatter = Formatter.TEXTAREA),
                        )), serializer = serializer()
                    ).apply {
                        minWidth = 100.perc
                        minHeight = 100.perc
                    }
                    button("Auswahl speichern").onClick {
                        tabu.getSelectedRows().forEach {
                            bookEvent(id, it.getIndex() as Int)
                        }
                    }
                }
            }
        }.show()
    }

    fun terminAbsagen(id: Int?) {
        val modal = Modal("Termin absagen")
        modal.apply {
            vPanel(spacing = 5) {

                button("Termin absagen").onClick {
                    try {
                        deleteEvent(Model.member.value.id!!,id)
                    } catch (e: Exception){
                        Toast.success("Da ist etwas schief gelaufen")}
                    modal.hide()
                }
                button("Close").onClick {
                    modal.hide()
                }
            }
            show()
        }
    }

    fun terminBuchen(id: Int?) {
        val modal = Modal("Termin buchen")
        modal.apply {
            vPanel(spacing = 5) {
                button("Termin buchen").onClick {
                    bookEvent(id, Model.member.value.id!!)
                    modal.hide()
                }
                button("Close").onClick {
                    modal.hide()
                }
            }
            show()
        }
    }
    private fun deleteEvent(mId: Int, id: Int?) {
        AppScope.launch {
            Model.deleteMemberFromEvent(mId, id!!)
            delay(100)
            updateTable()
            Toast.success("Termin abgesagt", options = ToastOptions(
                position = ToastPosition.TOPLEFT,
                close = true,
                duration = 4000
            )
            )

        }
    }

    suspend fun updateTable() {
        try {
            Model.getEvents(
                year = 2023,
                2,
                day= 2)
        }catch (e:Exception){
            Toast.success("Fehler Laden")}
        delay(100)
        this@TimeTable.removeAll()
        addDays()
    }

    private fun bookEvent(id: Int?, mId: Int) {
        AppScope.launch {
            Model.addMemberToEvent(mId,id!!)
            delay(100)
            Toast.success("Termin gebucht", options = ToastOptions(
                position = ToastPosition.TOPLEFT,
                close = true,
                duration = 4000
            ))
            updateTable()

            /* document.location?.reload()*/
        }
    }
}
