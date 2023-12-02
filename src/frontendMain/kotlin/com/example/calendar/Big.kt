package com.example.calendar

import com.example.calendar.bigUser.Abrechnung
import com.example.calendar.helper.Model
import io.kvision.core.*
import io.kvision.form.check.CheckBox
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.form.text.text
import io.kvision.html.*
import io.kvision.jquery.invoke
import io.kvision.jquery.jQuery
import io.kvision.modal.Confirm
import io.kvision.navbar.Navbar
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.NavbarType
import io.kvision.navbar.navbar
import io.kvision.panel.*
import io.kvision.table.*
import io.kvision.tabulator.*
import io.kvision.toast.Toast
import io.kvision.types.LocalDate
import io.kvision.types.toStringF
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.vh
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer
import org.w3c.dom.events.Event
import kotlin.js.Date

class Big {

    private var nBar = Navbar()

    init {
        AppScope.launch {
            Model.getMembers()
        }
        val flyout = Flyout()
        flyout.flyContent.apply {
            marginTop = 2.perc
            height = 90.vh
            Model.allMembers.forEach {
                it.password2 = it.password
                it.letzterlogin = Date().toString()
            }
            flexPanel(
                FlexDirection.ROW, FlexWrap.WRAP, JustifyContent.SPACEBETWEEN, AlignItems.CENTER,
                spacing = 5
            ) {
                height = 5.perc
                div { (span(" ")) }
                div { span("Mitglieder") }
                div {
                    button("", "bi bi-x", ButtonStyle.LIGHT).apply {
                        onClick {
                            if (document.fullscreen) {
                                document.exitFullscreen()
                            }
                            flyout.hideFlyout()
                        }
                    }
                }
            }

            tabPanel {
                tab("Alle Mitglieder") {
                    Tabulator(
                        Model.allMembers, dataUpdateOnEdit = false, options = TabulatorOptions(
                            layout = Layout.FITCOLUMNS,
                            pagination = true,
                            paginationSize = 10,
                            columns = listOf(
                                ColumnDefinition("Vorname", "vorname", editor = Editor.INPUT),
                                ColumnDefinition("Nachname", "nachname", editor = Editor.INPUT),
                                ColumnDefinition(
                                    "Abo",
                                    "abo",
                                    formatter = Formatter.TICKCROSS,
                                    cellDblClick = { evt: Event, cell ->
                                        evt.preventDefault()
                                        AppScope.launch {
                                            val newMem =
                                                Model.allMembers.find { it.id == cell.getRow().getIndex() as Int }!!
                                                        .apply {
                                                            abo = !abo!!
                                                            Toast.success("Abo $abo ")
                                                            password = null
                                                        }
                                            delay(500)
                                            Model.updateMember(newMem)
                                            delay(500)
                                            Model.getMembers()
                                        }

                                    }),
                                ColumnDefinition("Logins", "logins"),
                                ColumnDefinition("Letzter Login", "letzterlogin"),
                                ColumnDefinition(
                                    "",
                                    formatter = Formatter.BUTTONCROSS,
                                    cellClick = { evt: Event, cell ->
                                        evt.preventDefault()
                                        val mem = cell.getRow().getData().asDynamic()
                                        Confirm.show(
                                            "Teilnehmer löschen?", "${mem.vorname} ${mem.nachname} löschen?",
                                            yesTitle = "Ja",
                                            noTitle = "Nein"
                                        )
                                        {
                                            AppScope.launch {
                                                Model.deleteMember(mem.id as Int)
                                                delay(100)
                                                Model.getMembers()
                                            }
                                        }
                                    })

                            )
                        ), serializer = serializer()
                    ).apply {
                        minWidth = 200.px
                        minHeight = 200.px
                        maxWidth = 100.perc
                        maxHeight = 100.perc
                        this@tab.add(this)

                        setEventListener<Tabulator<Member>> {

                            cellEditedTabulator = { e ->
                                @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
                                (e.detail as io.kvision.tabulator.js.Tabulator.RowComponent).run {
                                    AppScope.launch {
                                        val memD = this@run.getData().asDynamic()

                                        val mem = Member(
                                            id = memD.id as? Int,
                                            vorname = memD.vorname as? String,
                                            nachname = memD.nachname as? String,
                                            abo = memD.abo as? Boolean,
                                            logins = memD.logins as? Int,
                                            letzterlogin = memD.letzterlogin as? String,
                                            letzterLoginWeek = memD.letzterLoginWeek as? String,
                                            password = null
                                        )
                                        Model.updateMember(mem)
                                        delay(200)
                                        Model.getMembers()
                                    }
                                }
                            }
                        }
                    }
                }
                tab("Mitglied hinzufügen") {

                    formPanel<Form> {
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
                                    if (Model.allMembers.any { it.username == "${t1}.${t2}" }) {
                                        Toast.success("Mitglied wurde bereits hinzugefügt")
                                    } else {
                                        Model.registerProfile(
                                            Member(
                                                id = 0,
                                                username = "${t1}.${t2}",
                                                password = t1,
                                                password2 = t1,
                                                nachname = t2,
                                                vorname = t1,
                                                abo = cp
                                            ), t1!!
                                        )
                                        Toast.success("User eingefügt")
                                        Model.getMembers()
                                        this@formPanel.clearData()
                                    }
                                } catch (_: Exception) {
                                }
                            }

                        }
                    }
                }

                tab("Videos") {
                    val t1 = text(type = InputType.NUMBER, label = "Video 1")
                    val t2 = text(type = InputType.NUMBER, label = "Video 2")
                    button("Videos ändern").onClick {
                        AppScope.launch {
                            Model.changeVideos(arrayOf(t1.value!!.toInt(), t2.value!!.toInt()))
                        }
                        flyout.hideFlyout()
                        Toast.success("Videos geändert")
                    }

                }

                tab("Abrechnung") {
                    onClick {
                        AppScope.launch {
                            Abrechnung.updateTable(LocalDate(LocalDate.now()).toStringF("YYYY-MM"))
                            add(Abrechnung)
                      }
                    }
                }
            }
        }
        jQtest()
        Menu.myMenu.getRoot()!!.apply {
            this@apply.add(flyout)
            nBar = navbar(
                expand = NavbarExpand.ALWAYS,
                collapseOnClick = false,
                className = "navbar w-100 navbar-brand navbar-dark bg-dark bg-faded",
                type = NavbarType.FIXEDBOTTOM,
                bgColor = BsBgColor.DARK
            ) {
                nav(className = "navbar-nav w-100 justify-content-center") {

                    button("Termine einfügen", "fas fa-clock", style = ButtonStyle.DARK).onClick {
                        Win(this@apply)
                    }

                    button("Mitglieder", "fas fa-clock", style = ButtonStyle.DARK).onClick {

                        SimplePanel(className = "mmod") {
                            this.display = Display.BLOCK
                            div(className = "mmod-content") {
                                button("close", className = "mmod-close") {}
                            }
                        }
                        flyout.showFlyout()
                    }
                }
            }
        }
    }

     fun addAbr(tab: Tab) {
        if(Abrechnung.abrechnungListe.isEmpty()){
            tab.span(".")
            addAbr(tab)
        }else{}

    }

    private fun jQtest() {
        if (jQuery("#supUser").get().isEmpty()) {
            Link("SuperUser_BigMama", className = "ovalzwei-menu-item").apply {
                id = "supUser"
                background = Background(color = Color.name(Col.MEDIUMSPRINGGREEN))
                this.onClick {
                    jQuery(".ovalzwei-menu-item").removeClass("accio")
                    jQuery(getElement()).addClass("accio")
                    if (nBar.visible) {
                        nBar.hide()
                    } else {
                        nBar.show()
                    }
                }
                Menu.myMenu.add(this)
            }
        }
    }
}


