package com.example.calendar

import io.kvision.annotations.KVBindingRoute
import io.kvision.annotations.KVService
import io.kvision.types.LocalDate
import io.kvision.types.LocalDateTime



@KVService
interface IDatabaseService{
    suspend fun getEvents(year: Int, month: Int, dayOfMonth: Int) : List<Pair<MyEvent, List<Member?>>>
    suspend fun deleteMemberFromEvent(mId: Int, eId: Int)
    suspend fun addMemberToEvent(mId: Int, eId: Int)
    suspend fun insertEvent(myEvent: MyEvent)
    suspend fun updateEvent(myEvent: MyEvent)
    suspend fun deleteEvent(myEvent: MyEvent)
    suspend fun getMembers(): List<Member>
    suspend fun updateMembers(member: Member)
    suspend fun getVideos(): List<Int>
    suspend fun changeVideos(vid: Array<Int>)

}

@KVService
interface IBiggiService {

}
@KVService
interface IProfileService {
    suspend fun getProfile(): Member
    suspend fun bigMama(): Boolean
}

@KVService
interface IRegisterProfileService {
    suspend fun registerProfile(member: Member, password: String): Boolean
}