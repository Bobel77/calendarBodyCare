package com.example.calendar


import io.kvision.core.*
import io.kvision.form.formPanel
import io.kvision.form.number.spinner
import io.kvision.form.text.Text
import io.kvision.form.time.DateTime
import io.kvision.html.InputType
import io.kvision.html.button
import io.kvision.html.span
import io.kvision.modal.Modal
import io.kvision.offcanvas.OffPlacement
import io.kvision.offcanvas.Offcanvas
import io.kvision.offcanvas.offcanvas
import io.kvision.panel.Root
import io.kvision.panel.vPanel
import io.kvision.toast.Toast
import io.kvision.types.*
import io.kvision.types.LocalDateTime
import kotlinx.coroutines.launch
import kotlinx.datetime.*


class Win(container: Root) : Offcanvas() {
    init {
        Menu.myMenu.getRoot()!!.offcanvas("Termin hinzufügen", OffPlacement.START){
       formPanel<Form> {
                add(
                    Form::text,
                    Text(label = "Art des Trainings") {
                        placeholder = "Enter text"
                    })

                add(
                    Form::date,
                    DateTime(format = "DD-MM-YYYY", label = "Date field with a placeholder").apply {
                        placeholder = "Datum"
                    }
                )
                add(
                    Form::time,
                    DateTime(format = "HH:mm", label = "Zeit") {
                        placeholder = "Enter time"
                    }
                )
           add(
               Form::spinner,
               Text(label = "Wiederholen", type = InputType.NUMBER) {
                   placeholder = "Wiederholen"
               }
           )
                 add(Form::zahl,
                    Text(label = "Anzahl Teilnehmer", type = InputType.NUMBER) {
                    placeholder = "Anzahl Teilnehmer"
           }
           )

                button("Termin erstellen").onClick {
                    val fp = this@formPanel.getData()
                    val spin: Int = fp.spinner?.toInt()?: 1
                    Modal("Termin einfügen"){
                        fp.date
                        val myEvent = MyEvent(
                            1,
                            localDateTime = LocalDateTime(year = fp.date!!.getFullYear(), month = fp.date.getMonth(),
                            fp.date.getDate(), fp.time!!.getHours(), fp.time.getMinutes()),
                            training = fp.text,
                            anzahlTeilnehmer = fp.zahl?.toInt()
                        )

                        vPanel {
                            span(myEvent.localDateTime!!.toStringF("DD.MM.YYYY HH:mm"))
                            span(myEvent.training)
                            span(myEvent.anzahlTeilnehmer.toString())
                            span("Wiederholen: $spin")
                            val timeZone = TimeZone.UTC
                            val addTimeZone = kotlinx.datetime.LocalDateTime(fp.date!!.getFullYear(), fp.date.getMonth()+1,
                                fp.date.getDate(), fp.time!!.getHours(), fp.time.getMinutes()).toInstant(timeZone)

                            button("einfügen").onClick {
                                   for(i in 1..spin){
                                    val locDate = addTimeZone.plus(i-1, DateTimeUnit.WEEK, timeZone).toLocalDateTime(timeZone)

                                    AppScope.launch {
                                        Model.insertEvent(
                                            MyEvent(
                                            1,
                                            localDateTime = LocalDateTime(
                                                year = locDate.year,
                                                month = locDate.monthNumber-1,
                                                day = locDate.dayOfMonth,
                                                myEvent.localDateTime!!.getHours(),
                                                myEvent.localDateTime!!.getMinutes()),
                                            training = fp.text,
                                            anzahlTeilnehmer = fp.zahl?.toInt()
                                        )
                                        )
                                    }
                                }
                                Toast.success("Termin eingefügt")
                               this@Modal.hide()
                            }
                        }
                    }.show()
                }
            }
       }.show()
    }
}

