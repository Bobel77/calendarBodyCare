package com.example.calendar

import com.example.calendar.DB.dbQuery

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.sessions.*
import io.kvision.types.LocalDateTime
import io.kvision.types.toStringF
import org.apache.commons.codec.digest.DigestUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDate
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
                resultRowToMemberTable(it)
            }
        }.toMutableList()
    }

    override suspend fun updateMembers(member: Member): Unit = dbQuery {
        MemberTbl.update({ MemberTbl.id eq member.id }) {
            it[this.vorname] = member.vorname!!
            it[this.nachname] = member.nachname!!
            it[this.username] = member.vorname + "." + member.nachname
            it[this.logins] = member.logins
            it[this.letzterLogin] = member.letzterlogin
            it[this.letzterLoginWeek] = member.letzterLoginWeek
            if (member.password != null) {
                it[this.password] = DigestUtils.sha256Hex(member.password)
            }
        }
    }

    private fun resultRowToMemberTable(row: ResultRow) = Member(
        id = row[MemberTbl.id].value,
        username = row[MemberTbl.username],
        password = row[MemberTbl.password],
        vorname = row[MemberTbl.vorname],
        nachname = row[MemberTbl.nachname],
        logins = row[MemberTbl.logins],
        letzterlogin = row[MemberTbl.letzterLogin],
        letzterLoginWeek = row[MemberTbl.letzterLoginWeek],
        abo = row[MemberTbl.abo]
    )

    override suspend fun getEvents(year: Int, month: Int, dayOfMonth: Int): List<Pair<MyEvent, List<Member?>>> {

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
                        training = it[EventsTable.training],
                        anzahlTeilnehmer = it[EventsTable.anzahlTeilnehmer]
                    )
                    try {
                        val member = resultRowToMemberTable(it)
                        event to member
                    } catch (e: Exception) {
                        event to null
                    }

                }
                .groupBy({ it.first }, { it.second })
                .map { (event, members) -> event to members }
        }
    }



    //// Neue Stunde hinzufügen
    override suspend fun insertEvent(myEvent: MyEvent): Unit = dbQuery {
        EventsTable.insert {
            it[training] = myEvent.training
            it[uhrzeit] = myEvent.localDateTime!!
            it[calendarWeek] = myEvent.localDateTime!!.get(WeekFields.of(Locale.GERMANY).weekOfYear())
            it[anzahlTeilnehmer] = myEvent.anzahlTeilnehmer
        }
    }

    override suspend fun updateEvent(myEvent: MyEvent): Unit = dbQuery {
        EventsTable.update({ EventsTable.id eq myEvent.id }) {
            it[training] = myEvent.training
            it[uhrzeit] = myEvent.localDateTime!!
            it[calendarWeek] = myEvent.localDateTime!!.get(WeekFields.of(Locale.GERMANY).weekOfYear())
            it[anzahlTeilnehmer] = myEvent.anzahlTeilnehmer
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

    override suspend fun deleteEvent(event: MyEvent): Unit = dbQuery {
        EventsTable.deleteWhere {
            (EventsTable.id eq event.id)
        }
    }

    override suspend fun getVideos(): List<Int> {
        return dbQuery {
            VideoTable.select(VideoTable.id eq 1)
                .map {
                    listOf(it[VideoTable.video1].toInt(), it[VideoTable.video2].toInt())
                }
                .flatten()
        }
    }

    override suspend fun changeVideos(vid: Array<Int>) {
        val dateNow = LocalDateTime.now()
        dbQuery {
            VideoTable.insert {
                it[video1] = vid[0]
                it[video2] = vid[1]
                it[date] = dateNow
            }
        }

        dbQuery {
            VideoTable.update({ VideoTable.id eq 1 }) {
                it[video1] = vid[0]
                it[video2] = vid[1]
            }
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
                    it[this.abo] = member.abo!!
                    it[this.letzterLogin] = member.letzterlogin
                }
            }
        } catch (e: Exception) {
            throw Exception("Register operation failed!")
        }
        return true
    }
}

