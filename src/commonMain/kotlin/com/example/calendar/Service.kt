package com.example.calendar

import io.kvision.annotations.KVService


@KVService
interface IDatabaseService {
    suspend fun getEvents(year: Int, month: Int, dayOfMonth: Int): List<Pair<MyEvent, List<Member?>>>
    suspend fun deleteMemberFromEvent(mId: Int, eId: Int)
    suspend fun addMemberToEvent(mId: Int, eId: Int)
    suspend fun insertEvent(myEvent: MyEvent): Int
    suspend fun updateEvent(myEvent: MyEvent)
    suspend fun deleteEvent(myEvent: MyEvent)
    suspend fun getMembers(): List<Member>
    suspend fun updateMembers(member: Member)
    suspend fun deleteMember(mId: Int)
    suspend fun getVideos(): List<Int>
    suspend fun changeVideos(vid: Array<Int>)
    suspend fun saveLayout(name: String, json: String)
    suspend fun getLayout(): List<Pair<String, String>>
}

/*@KVService
interface ILayoutService {

}*/

@KVService
interface IProfileService {
    suspend fun getProfile(): Member
    suspend fun bigMama(): Boolean
}

@KVService
interface IRegisterProfileService {
    suspend fun registerProfile(member: Member, password: String): Boolean
}