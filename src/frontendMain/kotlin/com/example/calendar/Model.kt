package com.example.calendar

import io.kvision.core.Container
import io.kvision.dropdown.contextMenu
import io.kvision.html.*
import io.kvision.remote.getService
import io.kvision.state.ObservableValue
import io.kvision.state.observableListOf
import io.kvision.utils.syncWithList


object Model {
    private val profileService = getService<IProfileService>()
    private val registerProfileService = getService<IRegisterProfileService>()
    private val databaseService = getService<IDatabaseService>()
    var myEvents = mutableListOf<Pair<MyEvent, List<Member?>>>()
    val member = ObservableValue(Member())
    var allMembers = observableListOf<Member>()


    val offCanvasSpans = arrayListOf<Site>(
        Site("Home", listOf<Span>(Span("Home"))),
        /*   Site("Termine", listOf<Span>(Span("Termine")),""calender""),*/
        Site("Pilates", listOf<Span>(Span("Pilates"))),
        Site("Aktuelles", listOf<Span>(Span("Aktuelles")), ""),
        Site("Preise", listOf<Span>(Span("Preise"))),
        Site("Unser_Team", listOf<Span>(Span("Unser Team"))),
        Site("Kontaktformular", listOf<Span>(Span("Kontaktformular"))),
        Site("Mitgliederbereich", listOf<Span>(Span("Mitgliederbereich")),"Mitgliederbereich"),
        Site("Impressum", listOf<Span>(Span("Impressum"))),
    )
/// functions for members
    suspend fun videos(container: Container){
        Security.withAuth {
           val vid = "https://bodycare-pilates.de/Videos/stunde11low.mp4"
            val myVid: Tag = Tag(
                TAG.VIDEO, attributes = mapOf(
                "src" to vid,
                "type" to "video/mp4",
                "width" to "680",
                "height" to "400",
                "controls" to "false",
                "controlsList" to "nodownload"
            )){
                contextMenu { }
            }
            container.add(myVid)
        }
    }

    suspend fun getMembers() {
        allMembers.syncWithList(databaseService.getMembers())
    }

    suspend fun deleteMemberFromEvent(mId: Int, eId: Int){
        Security.withAuth {
            databaseService.deleteMemberFromEvent(mId, eId)
        }
    }
    suspend fun addMemberToEvent(mId: Int, eId: Int){
        Security.withAuth {
            databaseService.addMemberToEvent(mId, eId)
        }
    }

    suspend fun updateMember(member: Member){
        databaseService.updateMembers(member)
    }

    /// function for SuperUser
    suspend fun insertEvent(myEvent: MyEvent){
        Security.withAuth {
            databaseService.insertEvent(myEvent)
        }
    }
    suspend fun getEvents(year: Int, month: Int, day: Int){
        Security.withAuth {
            myEvents.clear()
        myEvents.syncWithList(databaseService.getEvents(year, month, day))
    }
    }

    suspend fun deleteEvent(event: MyEvent){
        Security.withAuth {
            databaseService.deleteEvent(event)
        }
    }

    suspend fun readProfile() {
        Security.withAuth {
            member.value = profileService.getProfile()
            if(profileService.bigMama()){
                Big()
            }
            UserJoined()
        }

    }

    suspend fun bigM(): Boolean = profileService.bigMama()

    suspend fun registerProfile(member: Member, password: String): Boolean {
        return try {
            registerProfileService.registerProfile(member, password)
        } catch (e: Exception) {
            console.log(e)
            false
        }
    }



}


