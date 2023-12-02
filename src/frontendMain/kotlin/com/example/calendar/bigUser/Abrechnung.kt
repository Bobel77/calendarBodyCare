package com.example.calendar.bigUser

import com.example.calendar.AppScope
import com.example.calendar.Member
import com.example.calendar.helper.Model
import io.kvision.core.AlignItems
import io.kvision.core.JustifyContent
import io.kvision.core.Overflow
import io.kvision.core.onChange
import io.kvision.form.text.text
import io.kvision.html.InputType
import io.kvision.html.div
import io.kvision.html.span
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.state.bind
import io.kvision.state.observableListOf
import io.kvision.table.*
import io.kvision.types.LocalDate
import io.kvision.types.toStringF
import io.kvision.utils.vh
import kotlinx.coroutines.launch
import kotlin.js.Date

object Abrechnung : VPanel() {
    val abrechnungListe = observableListOf<Pair<Member, List<Date?>>>()

    init {

        div {
            maxHeight = 70.vh
            overflow = Overflow.SCROLL
            table(types = setOf(TableType.STRIPED, TableType.HOVER, TableType.BORDERED)) {
                minHeight = 70.vh
                maxHeight = 70.vh

                addHeaderCell(HeaderCell("Vorname"))
                addHeaderCell(HeaderCell("Nachname"))
                addHeaderCell(HeaderCell("Anwesenheit"))
                addHeaderCell(HeaderCell("Daten"))
                bind(abrechnungListe) { aList ->
                    aList.forEach { data ->
                        row {
                            cell { span(data.first.vorname) }
                            cell { span(data.first.nachname) }
                            cell { span(data.second.size.toString()) }
                            cell {
                                data.second.forEach { date ->
                                    span(" ${date!!.toStringF("DD.MM.YYYY")} || ")
                                }
                            }
                        }
                    }
                }
            }

        }


        hPanel(justify = JustifyContent.CENTER, alignItems = AlignItems.CENTER, spacing = 10) {
            text(InputType.MONTH).apply {
                value = LocalDate(LocalDate.now()).toStringF("YYYY-MM")

                onChange {
                    AppScope.launch {  updateTable(value!!) }

                }
            }
        }
    }

   suspend fun updateTable(value: String) {
        abrechnungListe.clear()
        Model.allMembers.forEach { member ->
            Model.myEvents.filter {
                it.first.localDateTime?.toStringF("YYYY-MM") == value && it.second.any { me -> me == member }
            }.map { it.first.localDateTime }.takeIf { it.isNotEmpty() }?.let { abrechnungListe.add(member to it) }
        }
    }
}