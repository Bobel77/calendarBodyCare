package com.example.calendar

import io.kvision.core.Container
import io.kvision.core.onChange
import io.kvision.form.time.DateTime
import io.kvision.html.*
import io.kvision.offcanvas.offcanvas
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

           /*<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="cs.css">
  <title>Hover Menu</title>
</head>
<body>
  <div class="menu">
    <a href="#" class="menu-item">
      <div class="icon-container">
        <span class="icon">Icon 1</span>
      </div>
    </a>
    <a href="#" class="menu-item">
      <div class="icon-container">
        <span class="icon">Icon 2</span>
      </div>
    </a>
    <a href="#" class="menu-item">
      <div class="icon-container">
        <span class="icon">Icon 3</span>
      </div>
    </a>
  </div>
</body>
</html>*/

                }
            }
    }

    fun calender(container: Container){
        container.button("Kalender").onClick {
           container.offcanvas {
               this.add(TimeTable())
           width = 100.perc
           }.show()


        }
        AppScope.launch {
          /*  table.updateTable()*/
        }
    }

    fun vid(container: Container){
        AppScope.launch {
            Model.videos(container)
        }
    }
}