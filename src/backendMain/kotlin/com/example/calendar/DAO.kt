package com.example.calendar

import com.example.calendar.MemberTbl.autoIncrement
import com.example.calendar.MemberTbl.nullable
import io.ktor.server.http.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.time


object MemberTbl : IntIdTable() {
    var username = varchar("Username", 8000).nullable()
    var password = varchar("Password", 8000).nullable()
    var vorname = varchar("Vorname",8000).nullable()
    var nachname = varchar("Nachname",8000).nullable()
    var abo = bool("Abo").nullable()
    var logins = integer("Logins").nullable()
    var letzterLogin = varchar("LetzterLogin",8000).nullable()
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

}





