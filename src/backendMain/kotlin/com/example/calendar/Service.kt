package com.example.calendar

import com.example.calendar.DB.dbQuery

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.sessions.*
import io.kvision.types.LocalDateTime
import org.apache.commons.codec.digest.DigestUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.month
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.time.temporal.WeekFields
import java.util.*


suspend fun <RESP> ApplicationCall.withProfile(block: suspend (Member) -> RESP): RESP {
    val member = this.sessions.get<Member>()
    return member?.let {
        block(member)
    } ?: throw IllegalStateException("Profile not set!")
}

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class DatabaseService: IDatabaseService {

    override suspend fun getMembers(): List<Member> {
        return dbQuery {
            MemberTbl.selectAll().map {
                resultRowToMemberTable(it) }
        }.toMutableList()
    }

    override suspend fun updateMembers(member: Member): Unit{

        return dbQuery {   MemberTbl.update({ MemberTbl.id eq member.id }) {

            it[this.vorname] = member.vorname!!
            it[this.nachname] = member.nachname!!
            it[this.username] = member.vorname + "." + member.nachname
            it[this.logins] = 0

        }  }
    /*    if(member.id == 0){
            dbQuery {
                MemberTbl.insert {
                    it[this.vorname] = member.vorname!!
                    it[this.nachname] = member.nachname!!
                    it[this.username] = member.vorname + "." + member.nachname
                    it[this.logins] = 0
                    it[this.password] = DigestUtils.sha256Hex(member.password)
                }
            }
        }*/

    }
    private fun resultRowToMemberTable(row: ResultRow) = Member(
        id = row[MemberTbl.id].value,
        username = row[MemberTbl.username],
        password = row[MemberTbl.password],
        vorname = row[MemberTbl.vorname],
        nachname = row[MemberTbl.nachname],
        logins = row[MemberTbl.logins],
        letzterlogin = row[MemberTbl.letzterLogin],
        abo = row[MemberTbl.abo]
    )

    //// Neue Stunde hinzufügen
    override suspend fun insertEvent(myEvent: MyEvent): Unit =  dbQuery {
          EventsTable.insert {
                it[training] = myEvent.training
                it[uhrzeit] = myEvent.localDateTime!!
                it[calendarWeek] = myEvent.localDateTime!!.get(WeekFields.of(Locale.GERMANY).weekOfYear())
            }
        }


    override suspend fun addMemberToEvent(mId: Int, eId: Int): Unit = dbQuery {
        WeekEvents.insert {
            it[memberID] = mId
            it[eventID] = eId
        }
    }

    override suspend fun deleteMemberFromEvent(mId: Int, eId: Int): Unit = dbQuery {
        WeekEvents.deleteWhere {
            (WeekEvents.eventID eq eId) and (WeekEvents.memberID eq mId)
        }
    }



    //// alle Stunden der Woche bekommen
   override suspend fun getEvents(year: Int, month: Int, dayOfMonth: Int): List<Pair<MyEvent, List<Member?>>> {
     /*val week = LocalDate.of(year, month, dayOfMonth).get(WeekFields.of(Locale.GERMANY).weekOfYear())*/

    return dbQuery {
        EventsTable
            .leftJoin(WeekEvents, { EventsTable.id }, { WeekEvents.eventID })
            .leftJoin(MemberTbl, { WeekEvents.memberID }, { MemberTbl.id })
            .slice(EventsTable.columns + MemberTbl.columns)
            .selectAll() /*{
                *//*EventsTable.uhrzeit.month() eq month*//*
                        EventsTable.calendarWeek eq week
            }*/
            .map {

                val event = MyEvent(
                    id = it[EventsTable.id].value,
                    localDateTime = it[EventsTable.uhrzeit],
                    training = it[EventsTable.training]
                )
                /*val member = resultRowToMemberTable(it)*/
            try {
                val member = Member(
                    it[MemberTbl.id].value,
                    it[MemberTbl.username],
                    it[MemberTbl.password],
                    it[MemberTbl.vorname],
                    it[MemberTbl.nachname],
                    it[MemberTbl.logins],
                    it[MemberTbl.letzterLogin],
                    password2 = it[MemberTbl.password]
                )
                event to member
            }catch (e:Exception){
                event to null
            }

            }
            .groupBy({ it.first }, { it.second })
            .map { (event, members) -> event to members }
    }
   }
}

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class BiggiService : IBiggiService{

}
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class ProfileService(private val call: ApplicationCall) : IProfileService {
    override suspend fun getProfile(): Member {

     return call.withProfile { it }

    }

    override suspend fun bigMama(): Boolean {
        return call.withProfile { it.username } == "Brigitte.Wolter"
    }


}

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class RegisterProfileService : IRegisterProfileService {

    override suspend fun registerProfile(member: Member, password: String): Boolean {
        try {
            val user = member.vorname + "." + member.nachname
            dbQuery {
                MemberTbl.insert {
                    it[this.vorname] = member.vorname!!
                    it[this.nachname] = member.nachname!!
                    it[this.username] = user
                    it[this.logins] = 0
                    it[this.password] = DigestUtils.sha256Hex(password)
                    it[this.abo] = member.abo
                    it[this.letzterLogin] = member.letzterlogin
                }
            }
        } catch (e: Exception) {
            throw Exception("Register operation failed!")
        }
        return true
    }

}

