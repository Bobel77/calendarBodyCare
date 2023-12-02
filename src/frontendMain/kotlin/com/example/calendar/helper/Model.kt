package com.example.calendar.helper

import com.example.calendar.*
import io.kvision.core.Container
import io.kvision.dropdown.contextMenu
import io.kvision.html.TAG
import io.kvision.html.Tag
import io.kvision.remote.getService
import io.kvision.state.ObservableValue
import io.kvision.state.observableListOf
import io.kvision.utils.syncWithList
import kotlinx.coroutines.*


object Model {

    private val profileService = getService<IProfileService>()
    private val registerProfileService = getService<IRegisterProfileService>()
    private val databaseService = getService<IDatabaseService>()
    var myEvents = mutableListOf<Pair<MyEvent, List<Member?>>>()
    val member = ObservableValue(Member())
    var allMembers = observableListOf<Member>()


    val offCanvasSpans = observableListOf<Site>(
      /*  Site("Home", "red-rectangle || home"),*/
        /*   Site("Termine", listOf<Span>(Span("Termine")),""calender""),*/
     /*   Site("Pilates", "red-rectangle || Pilates"),
        Site("Aktuelles",  "red-rectangle || aktuelles", ""),
        Site("Preise",  "red-rectangle || preis"),
        Site("Unser_Team", "red-rectangle || Team"),
        Site("Kontaktformular", "red-rectangle || Kontakt"),*/
        Site("Mitgliederbereich", "red-rectangle || ", "Mitgliederbereich"),
/*        Site("Impressum",  "red-rectangle || impr")*/
    )

   /* fun setSearch(search: String?) {
        allMembers.value = addressBook.value.copy(search = search)
    }*/
    /// functions for members
    suspend fun changeVideos(vid: Array<Int>) {
       Security.withAuth {
           databaseService.changeVideos(vid)
       }

    }

    suspend fun videos(container: Container) {
        var vids: List<Int>
        Security.withAuth {
        withContext(Dispatchers.Default){
           vids = databaseService.getVideos()


            try {
                vids.forEach {

                    val vid = "https://bodycare-pilates.de/Videos/stunde${it}low.mp4"
                    val myVid = Tag(
                        TAG.VIDEO, attributes = mapOf(
                            "src" to vid,
                            "type" to "video/mp4",
                            "width" to "680",
                            "height" to "400",
                            "controls" to "false",
                            "controlsList" to "nodownload"
                        )
                    ) {
                        contextMenu { }
                    }
                    container.add(myVid)
                }
            } catch (e: Exception) {
                console.log("Vid init failed")
            }
        }
        }
    }

    suspend fun getMembers() {
        allMembers.apply {
            syncWithList(databaseService.getMembers())
            sortBy { it.nachname }
        }
    }

    suspend fun deleteMember(mId: Int) {
        databaseService.deleteMember(mId)
    }

    suspend fun deleteMemberFromEvent(mId: Int, eId: Int) {
        Security.withAuth {
            databaseService.deleteMemberFromEvent(mId, eId)
        }
    }

    suspend fun addMemberToEvent(mId: Int, eId: Int) {
        Security.withAuth {
            databaseService.addMemberToEvent(mId, eId)
        }
    }

    suspend fun updateMember(member: Member) {
        databaseService.updateMembers(member)
    }

    /// function for SuperUser
    suspend fun insertEvent(myEvent: MyEvent): Int {
        var id: Int = 0
        Security.withAuth {
          id =  databaseService.insertEvent(myEvent)
        }
        return id
    }

    suspend fun updateEvents(myEvent: MyEvent) {
        Security.withAuth {
            databaseService.updateEvent(myEvent)
        }
    }

    suspend fun getEvents(year: Int, month: Int, day: Int) {
        Security.withAuth {
            myEvents.clear()
            myEvents.syncWithList(databaseService.getEvents(year, month, day))
        }
    }

    suspend fun deleteEvent(event: MyEvent) {
        Security.withAuth {
            databaseService.deleteEvent(event)
        }
    }

    suspend fun readProfile() {
        Security.withAuth {
            withContext(Dispatchers.Default) {
                member.value = profileService.getProfile()
                if (profileService.bigMama()) {
                    Big()
                }
                UserJoined()
            }
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


