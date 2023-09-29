package com.example.calendar

import io.kvision.core.*
import io.kvision.form.check.CheckBox
import io.kvision.form.check.checkBox
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.html.*
import io.kvision.jquery.invoke
import io.kvision.jquery.jQuery
import io.kvision.navbar.Navbar
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.NavbarType
import io.kvision.navbar.navbar
import io.kvision.panel.tab
import io.kvision.panel.tabPanel
import io.kvision.tabulator.*
import io.kvision.toast.Toast
import io.kvision.utils.Serialization.toObj
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer
import kotlin.js.Date

class Big{
        private var nBar = Navbar()

        init {
            AppScope.launch{
                Model.getMembers()
            }
            val flyout = Flyout()
            jQtest()
            Menu.myMenu.getRoot()!!.apply{
                this@apply.add(flyout)
                nBar =  navbar (expand = NavbarExpand.ALWAYS,
                    collapseOnClick = false,
                    className = "navbar w-100 navbar-brand navbar-dark bg-dark bg-faded",
                    type = NavbarType.FIXEDBOTTOM,
                    bgColor = BsBgColor.DARK){
                    nav(className = "navbar-nav w-100 justify-content-center") {

                        button("Termine einfügen", "fas fa-clock", style = ButtonStyle.DARK).onClick {
                            Win(this@apply)
                        }

                        button("Mitglieder", "fas fa-clock", style = ButtonStyle.DARK).onClick {
                          flyout.showFlyout()
                        }
                    }
                }
            }
        }

    class Flyout: Div(className = "flyout-overlay"){

                private val flyContent = div(className = "flyout-content"){
                    Model.allMembers.forEach{
                        it.password2 = it.password
                        it.letzterlogin = Date().toString()
                    }
                    button("x",className = "close-flyout btn").onClick {
                        /*   closeFlyout()*/this@Flyout.hideFlyout()
                    }
                    tabPanel {
                        tab("Alle Mitglieder") {

                            Tabulator(Model.allMembers,options = TabulatorOptions(
                                layout = Layout.FITCOLUMNS,
                                pagination = true,
                                paginationSize = 10,
                                columns = listOf(
                                ColumnDefinition("Vorname", "vorname", editor = Editor.INPUT),
                                ColumnDefinition("Nachname", "nachname", editor = Editor.INPUT),
                                ColumnDefinition("Abo", "abo", formatter = Formatter.BUTTONTICK),
                                ColumnDefinition("Logins", "logins", editor = Editor.INPUT),
                                ColumnDefinition("PW2", "password2", editor = Editor.INPUT),
                                ColumnDefinition("Letzter Login", "letzterlogin", editor = Editor.INPUT),

                            )), serializer = serializer()
                            ).apply {
                                minWidth = 200.px
                                minHeight = 200.px
                                maxWidth = 100.perc
                                maxHeight = 100.perc
                                this@tab.add(this)


                                setEventListener<Tabulator<Member>> {
                                    cellEditedTabulator = { e ->
                                        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
                                            (e.detail as io.kvision.tabulator.js.Tabulator.CellComponent).run{
                                            val id =  this.getRow().getData()

                                                this.getData
                                                val searched = Model.allMembers.filter { id ==  id.asDynamic().id}
                                                console.log(searched)
                                                val data = searched[0]
                                                val  memb = Member(id = data.id as? Int,
                                                       username= data.username as? String,
                                                       nachname = data.nachname as? String,
                                                       password = null,
                                                       logins = data.logins as? Int,
                                                       letzterlogin = data.letzterlogin as? String,
                                                       abo = data.abo as? Boolean
                                                   )
                                             /* val test: Member? = Model.allMembers.find { it.id == index }*/
                                                        console.log(memb)
                                             /*   if (memb != null){
                                                    AppScope.launch {   Model.updateMember(memb) }

                                                }*/
                                            }

                                    }

                                }
                            }
                        }
                        tab("Mitglied hinzufügen"){

                          val fp =  formPanel<Form> {
                              add(Form::vorname, Text(label = "Vorname", floating = true))
                              add(Form::nachname, Text(label = "Nachname", floating = true))
                              add(Form::checkBox, CheckBox(true, label = "Abo") { circled = false })
                              button("Mitglied einfügen").onClick {
                                  val data = this@formPanel.getData()
                                  AppScope.launch {
                                      val t1 = data.vorname
                                      val t2 = data.nachname
                                      val cp = data.checkBox
                                      try {
                                          if(Model.allMembers.any { it.username == "${t1}.${t2}"}){
                                              Toast.success("Mitglied wurde bereits hinzugefügt")
                                          }
                                          else{
                                              Model.registerProfile(
                                                  Member(
                                                      id = 0,
                                                      username = "${t1}.${t2}",
                                                      password = t1,
                                                      password2 = t1,
                                                      nachname = t2,
                                                      vorname = t1,
                                                      logins = 0,
                                                      letzterlogin = "0",
                                                      abo =  cp
                                                  ), t1!!)
                                              Toast.success("User eingefügt")
                                              Model.getMembers()
                                              this@formPanel.clearData()
                                          }
                                      }catch (e:Exception){
                                          console.log(e)
                                          Toast.success("Fehler")
                                      }
                                  }

                              }
                            }
                        /*  val t1 =  text(label = "Vorname")
                          val t2 = text(label = "Nachname")
                          val cB = checkBox(true, label = "Abo") { circled = false }*/
                        }
                        tab("Termine"){}
                    }
                }

        fun showFlyout(){
                this@Flyout.display = Display.BLOCK
                    this@Flyout.height = 100.perc
                    flyContent.bottom = 0.perc

             /*   withTimeout(10) {
                    this@Flyout.height = 100.perc
                    flyContent.bottom = 0.px
                }*/

        }
        fun hideFlyout(){

                var fly = 100

                while(this@Flyout.height != 0.perc){
                    fly-=2
                    this@Flyout.height = fly.perc
                    flyContent.bottom = (fly-100).perc
                }
                this@Flyout.display = Display.NONE
        }
    }

    private fun jQtest(){
        if(jQuery("#supUser").get().isEmpty()) {
            Link("SuperUser_BigMama", className = "ovalzwei-menu-item").apply {
                id = "supUser"
                background = Background(color = Color.name(Col.MEDIUMSPRINGGREEN))
                this.onClick {
                    jQuery(".ovalzwei-menu-item").removeClass("accio")
                    jQuery(getElement()).addClass("accio")
                    if(nBar.visible){
                        nBar.hide()
                    }
                        else{
                            nBar.show()
                        }
                    }
                Menu.myMenu.add(this)
            }
        }
    }

}


