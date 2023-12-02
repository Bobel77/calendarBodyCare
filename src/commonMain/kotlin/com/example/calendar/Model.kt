@file:UseContextualSerialization(LocalDateTime::class)
package com.example.calendar

import io.kvision.types.LocalDate
import io.kvision.types.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization


@Serializable
data class MyEvent(
    val id: Int? = null,
    val localDateTime: LocalDateTime? = null,
    var training: String? = null,
    var anzahlTeilnehmer: Int? = null
)

@Serializable
data class Member(
    val id: Int?  = null,
    val username: String?  = null,
    var password: String?  = null,
    val vorname: String?  = null,
    val nachname: String?  = null,
    var logins: Int?  = null,
    var letzterlogin: String?  = null,
    var letzterLoginWeek: String? = null,
    var password2: String? = null,
    var abo: Boolean? = null,
)

