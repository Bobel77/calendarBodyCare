package com.example.calendar

import io.kvision.core.*
import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.form.text.password
import io.kvision.form.time.DateTime
import io.kvision.html.*
import io.kvision.offcanvas.OffPlacement
import io.kvision.offcanvas.offcanvas
import io.kvision.panel.*
import io.kvision.toast.Toast
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
                }.onClick {calender(this@hPanel) }

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

    fun calender(container: Container){
        val timetable = TimeTable()
           container.offcanvas {
               caption = "Kalender"
               width = 100.perc
              val choosedDate = hPanel(justify = JustifyContent.CENTER, spacing = 10){
                width = 100.perc
                fontWeight = FontWeight.BOLDER
                span(Date().toLocaleDateString("default", dateLocaleOptions { month = "long" }))
              }
               flexPanel(
                   FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.SPACEBETWEEN, AlignItems.CENTER,
                   spacing = 5
               ) {
                   width = 100.perc
                   height = 90.perc
                   button("<", style = ButtonStyle.DARK).apply {
                       height = 80.perc
                       onClick {
                           choosedDate.apply {
                               this.removeAll()
                               this.span(timetable.changeMonth(-1).toLocaleDateString("default", dateLocaleOptions { month = "long" }))
                           }
                       }
                   }

                   this.add(timetable)

                   button(">", style = ButtonStyle.DARK).apply {
                       height = 80.perc
                       onClick {
                           choosedDate.apply {
                               this.removeAll()
                               this.span(timetable.changeMonth(1).toLocaleDateString("default", dateLocaleOptions { month = "long" }))
                           }
                       }
                   }
               }
           }.show()



        AppScope.launch {
          /*  table.updateTable()*/
        }
    }

    fun vid(container: Container){
        container.offcanvas(caption = "Videos", placement = OffPlacement.BOTTOM){
           height = 100.perc
        AppScope.launch {
                Model.videos(this@offcanvas)
            }

        }.show()
    }
}