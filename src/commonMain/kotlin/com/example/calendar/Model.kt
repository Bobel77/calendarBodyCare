@file:UseContextualSerialization(LocalDateTime::class)
package com.example.calendar

import io.kvision.types.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class MyEvent(
    val id: Int? = null,
    val localDateTime: LocalDateTime? = null,
    val training: String? = null
)
@Serializable
data class Member(
    val id: Int?  = null,
    val username: String?  = null,
    val password: String?  = null,
    val vorname: String?  = null,
    val nachname: String?  = null,
    val logins: Int?  = null,
    var letzterlogin: String?  = null,
    var password2: String? = null,
    val abo: Boolean? = null,
)
