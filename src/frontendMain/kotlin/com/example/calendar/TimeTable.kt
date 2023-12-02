package com.example.calendar

import com.example.calendar.helper.Model
import io.kvision.core.*
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.form.text.text
import io.kvision.html.*
import io.kvision.modal.Modal
import io.kvision.modal.ModalSize
import io.kvision.panel.*
import io.kvision.table.*
import io.kvision.table.TableType
import io.kvision.tabulator.*
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.types.toStringF
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.browser.document
import kotlinx.coroutines.*
import kotlinx.datetime.*
import kotlinx.serialization.serializer
import kotlin.js.Date

class TimeTable :
    GridPanel(justifyItems = JustifyItems.CENTER, templateColumns = "repeat(6, 1fr)", columnGap = 5, rowGap = 20) {

    var year = Date().getFullYear()
    var month = Date().getMonth()

    init {
        AppScope.launch {
            withContext(Dispatchers.Default) {
                Model.getEvents(2023, 9, 13)
                addDays()
            }
        }
    }

    suspend fun changeMonth(i: Int): Date {
      return  withContext(Dispatchers.Default) {
        month += i
        if (month > 11) {
            month = 0
            year += i
        } else if (month < 0) {
            month = 11
            year += i
        }

        AppScope.launch {
            updateTable()
        }
            return@withContext Date(year, month)
        }
    }

    fun addDays() {
        listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag").forEach {
            this@TimeTable.vPanel {
                span(it)
                fontWeight = FontWeight.BOLD
            }
        }


        val dayArr = mutableListOf<Date>()
        val d1 = Date(year, month, 1) //1. Sept
        val d2 = Date(year, month + 1, 0) //30.Sept
        val d0 = Date(year, month, 0)
        val dSunday = if (d0.getDay() == 0) {
            7
        } else (d0.getDay())
        for (i in dSunday downTo 1) {
            val day = d0.getDate() + 1 - i
            dayArr.add(Date(year, month - 1, day))
        }

        for (i in d1.getDate()..d2.getDate()) {
            dayArr.add(Date(year, month, i))
        }

        var lastDate = 1
        for (i in d2.getDay()..6) {
            dayArr.add(Date(year, month + 1, lastDate))
            lastDate += 1
        }

        dayArr.forEach { date ->
            if (date.getDay() != 0) {
                vPanel(/*justify = JustifyContent.CENTER,*/ alignItems = AlignItems.CENTER) {
                    width = 100.perc
                    span(date.getDate().toString())
                    val eventsOnDay: List<Pair<MyEvent, List<Member?>>> = Model.myEvents.filter { event ->
                        event.first.localDateTime!!.toStringF("DD.MM.YYYY") == date.toStringF("DD.MM.YYYY")
                    }
                    eventsOnDay.sortedBy { it.first.localDateTime!!.toStringF("HHmm") }.forEach {
                        termincard(it)
                    }
                }
            }
        }
    }

    //////////////////
    fun Container.termincard(mevent: Pair<MyEvent, List<Member?>>) {
        val event = mevent.first
        add(div(className = "card") {

            border = Border(1.px, BorderStyle.SOLID, Color.name(Col.LIGHTGRAY))
            width = 100.perc
            textAlign = TextAlign.CENTER
            background = Background(color = Color(Col.BLACK.toString()))

            vPanel {
                val members = mevent.second
                width = 100.perc
                height = 100.perc

                val lDate = event.localDateTime

                span(" ${lDate!!.toStringF("HH:mm")} ${event.training} ") {
                    /*  fontWeight = FontWeight.BOLDER*/
                }

                AppScope.launch {
                    withContext(Dispatchers.Default) {
                    val anzTeilnemer = event.anzahlTeilnehmer ?: 13 //TeilnehmerAnzahl

                    if (Model.bigM()) {
                        if (members[0] == null) {
                            span("0/$anzTeilnemer")
                        } else {
                            span("${members.size} /$anzTeilnemer ")
                        }
                        this@vPanel.onClick() {
                            bigMUnit(mevent, event.id!! /*Pair(this@termincard, this@div)*/)
                        }
                        background = Background(color = Color(Col.LIGHTGREEN.toString()))
                        if (members.size >= anzTeilnemer) {
                            background = Background(color = Color(Col.DIMGRAY.toString()))
                        }
                    } else {
                        if (members.any { it?.username == Model.member.value.username!! }) {
                            background = Background(color = Color(Col.GOLD.toString()))
                            onClick {
                                terminAbsagen(event)
                            }
                        } else if (members.size >= anzTeilnemer) {
                            background = Background(color = Color(Col.DIMGRAY.toString()))
                        } else {
                            background = Background(color = Color(Col.LIGHTGREEN.toString()))
                            onClick {
                                /* if(Model.member.value.abo != true || Date().getDate()+1 == event.localDateTime.getDate()){}*/
                                terminBuchen(event)
                            }
                        }
                    }

                    this@vPanel.onEvent {
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

            }

        })
    }

    private fun bigMUnit(event: Pair<MyEvent, List<Member?>>, id: Int) {
        Modal(event.first.localDateTime?.toStringF("DD.MM.YYYY HH:mm"), size = ModalSize.LARGE) {

            tabPanel {
                tab("Teilnehmer") {
                    table(
                        listOf("Vorname", "Nachname", "Abmelden"),
                        setOf(TableType.BORDERED, TableType.SMALL, TableType.STRIPED, TableType.HOVER),
                        responsiveType = ResponsiveType.RESPONSIVE
                    ) {
                        event.second.forEach { mem ->
                            Row {
                                cell(mem?.vorname)
                                cell(mem?.nachname)
                                cell {
                                    button("X").apply {
                                        this.width = 100.perc
                                        onClick {
                                            if (mem != null) {
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
                    val tabu = tabulator(
                        Model.allMembers, options = TabulatorOptions(
                            selectable = true,
                            layout = Layout.FITCOLUMNS,
                            pagination = true,
                            paginationSize = 10,
                            columns = listOf(
                                ColumnDefinition("Vorname", "vorname", formatter = Formatter.TEXTAREA),
                                ColumnDefinition("Nachname", "nachname", formatter = Formatter.TEXTAREA),
                                ColumnDefinition("Abo", "abo", formatter = Formatter.TEXTAREA),
                            )
                        ), serializer = serializer()
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
                tab("Termin ändern") {
                    formPanel<Form> {
                        add(
                            Form::text,
                            Text(label = "Art des Trainings") {
                                this.value = event.first.training.toString()
                                placeholder = "${event.first.training}"
                            })

                        add(Form::zahl,
                            Text(label = "Anzahl Teilnehmer", type = InputType.NUMBER) {
                                this.value = event.first.anzahlTeilnehmer.toString()
                                placeholder = "${event.first.anzahlTeilnehmer}"
                            }
                        )

                        button("Termin ändern").onClick {
                            val fp = this@formPanel.getData()
                            AppScope.launch {
                                Model.updateEvents(
                                    event.first.apply {
                                        this.training = fp.text ?: this.training
                                        this.anzahlTeilnehmer = fp.zahl?.toInt() ?: this.anzahlTeilnehmer
                                    }
                                )
                                withContext(Dispatchers.Default) {
                                    Model.getEvents(2022, 2, 2)

                                    Toast.success("Termin geändert", ToastOptions(position = ToastPosition.TOPLEFT))
                                    updateTable()
                                    this@Modal.hide()
                                }
                            }
                        }
                    }
                }

                //////////////////////////////
                tab("Termin wiederholen"){
                        span("Termin Wiederholen?")
                            vPanel {
                                span(event.first.localDateTime!!.toStringF("DD.MM.YYYY HH:mm"))
                                span(event.first.training)
                                span(event.first.anzahlTeilnehmer.toString())
                                /*span("Wiederholen: $spin")*/
                                val timeZone = TimeZone.UTC
                                val addTimeZone = kotlinx.datetime.LocalDateTime(
                                    event.first.localDateTime!!.getFullYear(),
                                    event.first.localDateTime!!.getMonth() + 1,
                                    event.first.localDateTime!!.getDate(),
                                    event.first.localDateTime!!.getHours(),
                                    event.first.localDateTime!!.getMinutes()
                                ).toInstant(timeZone)
                                val spinner = text(label = "Wiederholen", type = InputType.NUMBER) {
                                    placeholder = "Wiederholen"
                                    value = "1"
                                }

                                button("einfügen").onClick {
                                    val spin: Int = (spinner.value)!!.toInt()?: 1
                                    for (i in 1..spin) {
                                        val locDate = addTimeZone.plus(i, DateTimeUnit.WEEK, timeZone).toLocalDateTime(timeZone)

                                        AppScope.launch {
                                            withContext(Dispatchers.Default) {
                                                val newId: Int = Model.insertEvent(
                                                    MyEvent(
                                                        1,
                                                        localDateTime = io.kvision.types.LocalDateTime(
                                                            year = locDate.year,
                                                            month = locDate.monthNumber - 1,
                                                            day = locDate.dayOfMonth,
                                                            event.first.localDateTime!!.getHours(),
                                                            event.first.localDateTime!!.getMinutes()
                                                        ),
                                                        training = event.first.training,
                                                        anzahlTeilnehmer = event.first.anzahlTeilnehmer
                                                    )
                                                )

                                                console.log(newId)

                                                event.second.forEach {
                                                    console.log(it!!.id!!)
                                                    Model.addMemberToEvent(it!!.id!!, newId)
                                                }
                                                Toast.success(
                                                    "Termin wiederholt",
                                                    ToastOptions(position = ToastPosition.TOPLEFT)
                                                )
                                                this@Modal.hide()
                                                updateTable()
                                            }
                                        }
                                    }
                                }
                            }
                }

                tab("Termin löschen") {
                    button("Diesen Termin löschen!").onClick {
                        Toast.success("Bitte Warten", ToastOptions(position = ToastPosition.TOPLEFT))
                        AppScope.launch {
                            try {
                                event.second.forEach {
                                    Model.deleteMemberFromEvent(it!!.id!!, event.first.id!!)
                                }
                            } catch (_: Exception) {
                            }

                            try {
                                withContext(Dispatchers.Default) {
                                Model.deleteEvent(event.first)
                                Model.getEvents(2022, 2, 2)
                                Toast.success("Termin gelöscht", ToastOptions(position = ToastPosition.TOPLEFT))
                                updateTable()
                                this@Modal.hide()
                                }
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }
        }.show()
    }

    fun terminAbsagen(event: MyEvent) {
        val modal = Modal("Termin absagen: ${event.localDateTime!!.toStringF("HH:mm")} ${event.training}")
        modal.apply {
            vPanel(spacing = 5) {
                button("Termin absagen").onClick {
                    try {
                        deleteEvent(Model.member.value.id!!, event.id)
                    } catch (e: Exception) {
                        Toast.success("Da ist etwas schief gelaufen")
                    }
                    modal.hide()
                }
                button("Close").onClick {
                    modal.hide()
                }
            }
            show()
        }
    }

    fun terminBuchen(event: MyEvent) {
        val modal = Modal("Termin buchen:  ${event.localDateTime!!.toStringF("HH:mm")} ${event.training}")
        modal.apply {
            vPanel(spacing = 5) {
                button("Termin buchen").onClick {
                    bookEvent(event.id, Model.member.value.id!!)
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
          withContext(Dispatchers.Default) {
              Model.deleteMemberFromEvent(mId, id!!)

            updateTable()
            Toast.success(
                "Termin abgesagt", options = ToastOptions(
                    position = ToastPosition.TOPLEFT,
                    close = true,
                    duration = 4000
                )
            )
        }
        }
    }

    suspend fun updateTable() {
        withContext(Dispatchers.Default){
            try {
                Model.getEvents(
                    year = 2023,
                    2,
                    day = 2
                )
                this@TimeTable.removeAll()
                addDays()
            } catch (e: Exception) {
                document.location!!.reload()
            }
        }
    }

    private fun bookEvent(id: Int?, mId: Int) {
        AppScope.launch {
            withContext(Dispatchers.Default){
            Model.addMemberToEvent(mId, id!!)

            Toast.success(
                "Termin gebucht", options = ToastOptions(
                    position = ToastPosition.TOPLEFT,
                    close = true,
                    duration = 4000
                )
            )
            updateTable()
        }
        }
    }
}
