package com.example.calendar

import com.example.calendar.DB.dbQuery
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.kvision.remote.applyRoutes
import io.kvision.remote.getAllServiceManagers
import io.kvision.remote.getServiceManager
import io.kvision.remote.kvisionInit
import org.jetbrains.exposed.sql.select
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.jetbrains.exposed.sql.and
import org.apache.commons.codec.digest.DigestUtils
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.defaultheaders.*
import java.io.File
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders



fun Application.main() {
    install(Compression)
    install(DefaultHeaders)
    install(CallLogging)
    install(XForwardedHeaders)
    install(Sessions){
        cookie<Member>("KTSESSION", storage = directorySessionStorage(File("build/.sessions"))) {
            cookie.path = "/"
          /*  cookie.extensions["SameSite"] = "strict"*/
            cookie.secure = true
            cookie.maxAge = null
        }
    }

    DB.init()

    install(Authentication){
        form {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                dbQuery {
                    MemberTbl.select {
                        (MemberTbl.username eq credentials.name) and (MemberTbl.password eq DigestUtils.sha256Hex(
                            credentials.password
                        ))
                    }.firstOrNull()?.let {
                        UserIdPrincipal(credentials.name)
                    }
                }
            }
            skipWhen { call -> call.sessions.get<Member>() != null }
        }
    }


    routing {
        applyRoutes(getServiceManager<IRegisterProfileService>())
        authenticate {
            post("login") {
                val principal = call.principal<UserIdPrincipal>()
                val result = if (principal != null) {
                    dbQuery {
                        MemberTbl.select { MemberTbl.username eq principal.name }.firstOrNull()?.let {
                            val profile =
                                Member(
                                    it[MemberTbl.id].value,
                                    username = it[MemberTbl.username],
                                    vorname = it[MemberTbl.vorname],
                                    nachname = it[MemberTbl.nachname],
                                    logins = it[MemberTbl.logins],
                                    letzterlogin = it[MemberTbl.letzterLogin],
                                    letzterLoginWeek = it[MemberTbl.letzterLoginWeek],
                                    abo = it[MemberTbl.abo]
                                )
                            call.sessions.set(profile)
                            HttpStatusCode.OK
                        } ?: HttpStatusCode.Unauthorized
                    }
                } else {
                    HttpStatusCode.Unauthorized
                }
                call.respond(result)
            }
            get("/logout") {
                call.sessions.clear<Member>()
                call.respondRedirect("/")
            }
            getAllServiceManagers().forEach { applyRoutes(it) }
        }
    }

    val module = module {
        factoryOf(::DatabaseService)
        factoryOf(::RegisterProfileService)
        factoryOf(::ProfileService)
    }
    kvisionInit(module)
}
