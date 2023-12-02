package com.example.calendar.buildWebsite


import com.example.calendar.IDatabaseService
import com.example.calendar.helper.Model
import com.example.calendar.Site
import io.kvision.remote.getService
import io.kvision.utils.syncWithList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object Interactions {
    private val databaseService = getService<IDatabaseService>()

    suspend fun saveFlexPanelToDatabase(name: String, div: String) {
        databaseService.saveLayout(name, div)

    }

    suspend fun loadFlexPanelFromDatabase() {
        var newSite: List<Pair<String, String>>
        withContext(Dispatchers.Default){
            newSite = databaseService.getLayout()
        }

        val liste = mutableListOf<Site>()
        withContext(Dispatchers.Default){
            newSite.forEach { liste.add(Site(it.first, it.second)) }
        }
        Model.offCanvasSpans.syncWithList(liste)

    }

}