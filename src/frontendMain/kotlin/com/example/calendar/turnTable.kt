package com.example.calendar

import io.kvision.core.*
import io.kvision.html.*
import io.kvision.modal.Modal
import io.kvision.panel.*
import io.kvision.table.Row
import io.kvision.table.cell
import io.kvision.tabulator.*
import io.kvision.toast.Toast
import io.kvision.types.toStringF
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer
import kotlin.js.Date
import io.kvision.table.*
import io.kvision.table.TableType
import kotlinx.browser.localStorage

class TurnTable: GridPanel(justifyItems = JustifyItems.CENTER, templateColumns = "repeat(5, 1fr)", templateRows = "repeat(10, 1fr)", className = "ui link cards")  {
    val tCards: ArrayList<Pair<VPanel,Pair<MyEvent, List<Member?>>>> = arrayListOf()
    init {
        width = 100.perc
        height = 100.perc
    this.background = Background(color = Color.hex(15851770))
    /* #f1e0fa 241224250   border = Border(1.px, BorderStyle.SOLID, Color.name(Col.LIGHTGRAY))*/

        val allDays = listOf<String>("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag")
        allDays.forEach {
            val actColumn: Int? = when(it){
                "Montag" -> 1
                "Dienstag" -> 2
                "Mittwoch" -> 3
                "Donnerstag" -> 4
                "Freitag" -> 5
                "Samstag" -> 6
                else -> {null}
            }

            add(Div{
                fontWeight = FontWeight.BOLDER
                width = 100.perc
                height = 100.perc
                textAlign = TextAlign.CENTER
                span(it)
            }, actColumn!!, 1)
        }

        Model.myEvents.forEach {
         termincard(it)
        }
    }



    fun Container.termincard(mevent: Pair<MyEvent, List<Member?>>){
       val event = mevent.first
            val actColumn: Int =  event.localDateTime!!.getUTCDay()

            var actRow: Int = 0
          add(div(className = "card"){

                val hours =event.localDateTime!!.getHours()
                actRow = hours - 6

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

                    span(lDate!!.toStringF("DD.MM.YY")){
                        fontWeight = FontWeight.BOLDER
                    }
                    span(event.training)
                    span("${lDate!!.toStringF("HH:mm")} - ${lDate.toStringF("${lDate.getHours()+1}:mm")} Uhr"){
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

            }, actColumn, actRow)
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
                    } catch (e: Exception){Toast.success("Da ist etwas schief gelaufen")}
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
             Toast.success("Termin abgesagt")

         }
     }

     suspend fun updateTable() {
         try {
             Model.getEvents(
                 year = localStorage.getItem("year")!!.toInt(),
                 localStorage.getItem("month")!!.toInt()+1,
                 day= localStorage.getItem("date")!!.toInt())
         }catch (e:Exception){Toast.success("Fehler Laden")}
         delay(100)
         this@TurnTable.removeAll()
         Model.myEvents.forEach {
             this@TurnTable.termincard(it)
         }
    }

    private fun bookEvent(id: Int?, mId: Int) {
         AppScope.launch {
             Model.addMemberToEvent(mId,id!!)
             delay(100)
             Toast.success("Termin gebucht")
             updateTable()

            /* document.location?.reload()*/
         }
     }

 }

