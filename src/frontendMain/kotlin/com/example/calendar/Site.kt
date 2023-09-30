package com.example.calendar

import io.kvision.core.*
import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.html.*
import io.kvision.modal.Modal
import io.kvision.offcanvas.OffPlacement
import io.kvision.offcanvas.offcanvas
import io.kvision.panel.*
import io.kvision.toast.Toast
import io.kvision.types.LocalDate
import io.kvision.types.LocalDateTime
import io.kvision.types.toStringF
import io.kvision.utils.*
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.internal.JSJoda.Clock
import kotlin.js.Date

class Site(
    val siteName: String,
    val content: List<Span>,
    val function: String? = null
){

    fun intern(container: Container){
        container.hPanel(justify = JustifyContent.CENTER){
            AppScope.launch { Model.getEvents(2003,2,2) }
            width = 100.perc

            div(className = "round-menu"){
                div(className = "round-menu-item"){
                div(className = "round-icon-container"){
                    val x = width
                    height = x
                    span("Termine buchen")
                }
                }.onClick {
                    AppScope.launch {  calender(this@hPanel) }
                }

                div(className = "round-menu-item"){
                    height = width
                    div(className = "round-icon-container"){
                        span("Video")
                    }
                }.onClick {vid(this@hPanel) }

                div(className = "round-menu-item"){
                    height = width
                    div(className = "round-icon-container"){
                        span("Passwort ändern")
                    }
                }.onClick {changePW(this@hPanel) }
            }
            }
    }

    fun changePW(container: Container){
        container.offcanvas {
            vPanel {
                console.log(Model.member.value)
               val fp = formPanel<Form> {
                 /*  this.add(Form::text, Password(label = "altes Passwort", floating = true))*/
                    this.add(Form::password, Password(label = "neues Passwort", floating = true),
                        validatorMessage = { "Password too short" }) {
                        (it.getValue()?.length ?: 0) >= 8
                    }
                    add(Form::password2, Password(label = "neues Passwort wiederholen", floating = true),
                        validatorMessage = { "Password too short" }) {
                        (it.getValue()?.length ?: 0) >= 8
                    }
                         validator = {
                             val result1 = it[Form::password] == it[Form::password2]
                             if (!result1) {
                                 it.getControl(Form::password)?.validatorError = "Passwörter stimmen nicht überein"
                                 it.getControl(Form::password2)?.validatorError = "Passwörter stimmen nicht überein"
                             }
                             val result2 = it[Form::text] == Model.member.value.password
                             if (!result2) {
                                 it.getControl(Form::text)?.validatorError = "Das alte Passwort ist nicht korrekt"
                             }
                             val result = if(result1 && result2){true} else {false}
                             result1
                         }
                }
                   button("Passwort ändern").onClick {
                      if(fp.validate()){
                          AppScope.launch {
                              Model.updateMember( Model.member.value.apply { this.password = fp.getData().password })
                              Toast.success("Passwort geändert")
                          }
                          this@offcanvas.hide()
                      }
                   }
            }
        }.show()
    }

    suspend fun calender(container: Container){
        val timetable = TimeTable()

        Flyout().run {
            flyContent.apply {

                width = 100.perc
                height = 100.vh

                val choosedDate = Div{
                    fontWeight = FontWeight.BOLDER
                    fontSize = 32.px
                    marginBottom = 10.px
                    position = Position.RELATIVE

                    span(Date().toLocaleDateString("default", dateLocaleOptions { month = "long"
                        year = "numeric"}))
                }

                    flexPanel(FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.SPACEBETWEEN, AlignItems.CENTER,
                        spacing = 5){
                        height = 5.perc
                        div { (span(" "))}
                        this.add(choosedDate)
                        div {
                            button("", "bi bi-x", ButtonStyle.LIGHT).apply {
                                onClick {
                                    if(document.fullscreen){ document.exitFullscreen()}
                                    this@run.hideFlyout()
                                }
                            }
                        }
                    }

                    this.add(timetable.apply {
                        height  = 80.perc
                        overflow = Overflow.AUTO
                        marginTop = 5.perc
                    })


                div{
                    marginBottom = 5.perc
                    height = 5.perc
                    width = 100.perc
                    button("", style = ButtonStyle.DARK, icon = "bi bi-arrow-left-square" ).apply {
                        position = Position.RELATIVE
                        marginRight = 0.px
                        width =  41.perc
                        left = 5.perc

                        onClick {
                            choosedDate.apply {
                                this.removeAll()
                                this.span(timetable.changeMonth(-1).toLocaleDateString("default", dateLocaleOptions { month = "long";
                                    year = "numeric" }))
                            }
                        }
                    }


                    button("", style = ButtonStyle.DARK, icon = "bi bi-arrow-right-square").apply {
                        position = Position.ABSOLUTE
                        width =  41.perc
                        right = 5.perc

                        onClick {
                            choosedDate.apply {
                                this.removeAll()
                                this.span(timetable.changeMonth(1).toLocaleDateString("default", dateLocaleOptions { month = "long";
                                    year = "numeric" }))
                            }
                        }
                    }
                }
            }

            delay(300)
            container.add(this)
            showFlyout()
            delay(100)
            document.querySelector("body")?.requestFullscreen()
        }
    }

    fun vid(container: Container){
        console.log(Model.member.value)
        val week =  kotlinx.datetime.internal.JSJoda.LocalDate.now(Clock.systemDefaultZone()).isoWeekOfWeekyear().toString()

        val offC = container.offcanvas(caption = "Videos", placement = OffPlacement.BOTTOM){
            height = 100.perc
            vPanel {
                AppScope.launch {
                    Model.videos(this@vPanel)
                }
            }
        }

        if(Model.member.value.letzterLoginWeek?.split(";")?.toTypedArray()?.last() == week ){
            offC.show()
            saveDateToMember()
        } else {
        Modal("Videos aufrufen"){
            vPanel {
                span("Die Videos für diese Woche freischalten?")
                span("(eine Pilatesstunde nutzen)")
                hPanel(spacing = 10) {
                    button("Ja").onClick {

                        AppScope.launch {
                            with(Model.member) {
                            value.letzterLoginWeek = "${value.letzterLoginWeek};$week"
                                .takeIf { value.letzterLoginWeek?.split(";")?.last() != week }
                                ?: value.letzterLoginWeek
                            }
                        }
                        saveDateToMember()
                        offC.show()
                        this@Modal.hide()

                    }
                    button("Nein").onClick { this@Modal.hide() }
                }
            }
        }.show()
        }
    }

    private fun saveDateToMember() {
        with(Model.member) {
            val dateString = LocalDate(LocalDateTime.now()).toStringF("DD.MM.YYYY")
            value.letzterlogin = "${value.letzterlogin};$dateString"
                .takeIf { value.letzterlogin?.split(";")?.last() != dateString }
                ?: value.letzterlogin

            value.logins = (value.logins ?: 1) + 1

            AppScope.launch {
                Model.updateMember(value)
            }
        }
    }
}