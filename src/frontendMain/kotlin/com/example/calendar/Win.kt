package com.example.calendar

import io.kvision.core.*
import io.kvision.form.formPanel
import io.kvision.form.number.Spinner
import io.kvision.form.number.spinner
import io.kvision.form.text.Text
import io.kvision.form.time.DateTime
import io.kvision.form.text.MaskOptions
import io.kvision.form.text.Password
import io.kvision.html.button
import io.kvision.html.nav
import io.kvision.html.span
import io.kvision.modal.Dialog
import io.kvision.modal.Modal
import io.kvision.navbar.Navbar
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.NavbarType
import io.kvision.offcanvas.OffPlacement
import io.kvision.offcanvas.Offcanvas
import io.kvision.offcanvas.offcanvas
import io.kvision.panel.Root
import io.kvision.panel.simplePanel
import io.kvision.panel.vPanel
import io.kvision.remote.Credentials
import io.kvision.remote.LoginService
import io.kvision.toast.Toast
import io.kvision.types.LocalDateTime
import io.kvision.types.toStringF
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.vh
import io.kvision.utils.vw
import io.kvision.window.Window
import kotlinx.coroutines.launch
import kotlin.js.Date
import kotlin.random.Random

class Win(container: Root) : Offcanvas() {
    init {
        Menu.myMenu.getRoot()!!.offcanvas("Termin hinzufügen", OffPlacement.START) {
       formPanel<Form> {

                add(
                    Form::text,
                    Text(label = "Art des Trainings") {
                        placeholder = "Enter text"
                    })

                add(
                    Form::date,
                    DateTime(format = "YYYY-MM-DD", label = "Date field with a placeholder").apply {
                        placeholder = "Enter date"
                        daysOfWeekDisabled = arrayOf(0, 6) // Saturday, Sunday
                    }
                )
                add(
                    Form::time,
                    DateTime(format = "HH:mm", label = "Time field") {
                        placeholder = "Enter time"
                    }
                )
                var spin: Int = 1
              spinner(label = "Widerholen",
               min = 1,
               max = 20,
               step = 1).onChange { spin = value as Int }

                button("Termin erstellen").onClick {
                    val fp = this@formPanel.getData()

                    Modal("Termin einfügen"){
                        val myEvent =   MyEvent(
                            1,
                            localDateTime = LocalDateTime(fp.date!!.getFullYear(), fp.date.getUTCMonth(),
                                fp.date.getDate(), fp.time!!.getHours(), fp.time.getMinutes()),
                            training = fp.text
                        )
                        vPanel {
                            span(myEvent.localDateTime!!.toStringF("YYYY.MM.DD HH:mm"))
                            span(myEvent.training)
                            span("Wiederholen: $spin")

                            button("einfügen").onClick {
                                for(i in 0..spin){
                                    AppScope.launch {
                                        Model.insertEvent(MyEvent(
                                            1,
                                            localDateTime =  LocalDateTime(myEvent.localDateTime!!.getTime() + (604800000 * i)),
                                            training = fp.text
                                        ))
                                    }
                                }
                                Toast.success("Termin eingefügt")
                            }
                        }
                    }.show()

                }
            }
       }.show()
    }
}

