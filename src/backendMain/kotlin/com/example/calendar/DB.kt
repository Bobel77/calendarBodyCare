package com.example.calendar


import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction


object DB {
    fun init() {
        val driverClassName = "org.postgresql.Driver"
       /* val jdbcURL = "jdbc:postgresql://localhost:5432/member?user=laz&password=lazy"*/
        val jdbcURL = "jdbc:postgresql://localhost:6060/member?user=laz&password=lazy"
        val database = Database.connect(jdbcURL, driverClassName)


        transaction(database) {
            SchemaUtils.create(MemberTbl)
            SchemaUtils.create(WeekEvents)
            SchemaUtils.create(EventsTable)
            SchemaUtils.create(VideoTable)
        }
    }
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}