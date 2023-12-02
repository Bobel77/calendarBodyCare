package com.example.calendar


import com.example.calendar.EventsTable.nullable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime


object MemberTbl : IntIdTable() {
    var username = varchar("Username", 8000)
    var password = varchar("Password", 8000)
    var vorname = varchar("Vorname",8000)
    var nachname = varchar("Nachname",8000)
    var abo = bool("Abo")
    var logins = integer("Logins").nullable()
    var letzterLogin = varchar("LetzterLogin",8000).nullable()
    var letzterLoginWeek = varchar("LoginWeeknumer",8000).nullable()
}

object WeekEvents : IntIdTable() {
  val memberID =  reference("mid", MemberTbl)
  val eventID = reference("eid", EventsTable)
}

object EventsTable : IntIdTable() {
    val uhrzeit = datetime("Uhrzeit")
    val training = varchar("Training", 8000).nullable()
    val calendarWeek = integer("CalendarWeek")
    val warteliste = varchar("Warteliste", 8000).nullable()
    val anzahlTeilnehmer = integer("AnzahlTeilnehmer").nullable()
}

object VideoTable : IntIdTable(){
    val video1 = integer("Video1")
    val video2 = integer("Video2")
    val date = datetime("Datum")
}

object LayoutTable : IntIdTable(){
    val name = varchar("Name", 8000)
    val layout = varchar("Layout", 8000)
}





