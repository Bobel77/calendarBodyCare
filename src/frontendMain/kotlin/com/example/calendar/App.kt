package com.example.calendar


import io.kvision.*
import io.kvision.core.*
import io.kvision.form.number.Spinner
import io.kvision.form.text.Password
import io.kvision.form.time.DateTime
import io.kvision.html.*
import io.kvision.navbar.NavbarType
import io.kvision.navbar.navbar
import io.kvision.offcanvas.offcanvas
import io.kvision.panel.*
import io.kvision.remote.LoginService
import io.kvision.utils.*
import kotlinx.browser.document
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.js.Date


val AppScope = CoroutineScope(window.asCoroutineDispatcher())

class App : Application() {

    init {
        require("css/ovalzwei.css")
        require("css/flyout.css")
        require("css/cs.css")


     try {
     LoginService("/login")
    }catch (e:Exception){console.log("no User")}
    }


    override fun start(state: Map<String, Any>) {
         root("kvapp") {

vPanel() {
    background = Background(color = Color.hex(13012563))
    minHeight = 100.vh

    hPanel() {
        border = Border(2.px, BorderStyle.SOLID, Color.hex(8806196))
        width = 80.perc
        background = Background(color = Color.hex(16110785))
        marginTop = 20.px
        marginLeft = auto
        marginRight = auto
        padding = 20.px

        image(require("img/test.JPG") as? String, shape = ImageShape.CIRCLE){width = 10.perc; height =10.perc}
        span("  BodyCare Pilates"){
            fontWeight=FontWeight.BOLDER
            this.justifySelf = JustifyItems.CENTER

        }
    }

    add(MyMenu(this@root).apply {
        border = Border(2.px, BorderStyle.SOLID, Color.hex(8806196))
        width = 80.perc
        background = Background(color = Color.hex(16110785))
        marginTop = 0.px
        marginLeft = auto
        marginRight = auto
        padding = 20.px
    })
        }

        }
    }


}
@Serializable
data class Form(
    val text: String? = null,
    @Contextual val date: Date? = null,
    @Contextual val time: Date? = null,
    @Contextual val daTime: DateTime? = null,
    @Contextual val spinner: String? = null,
    val vorname: String? = null,
    val nachname: String? = null,
    val password: String? = null,
    val password2: String? = null,
    val checkBox: Boolean?,
    val zahl: String? = null,
)

fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        DatetimeModule,
        ToastifyModule,
        FontAwesomeModule,
        BootstrapIconsModule,
        PrintModule,
        CoreModule,
        TabulatorCssBulmaModule,
        TabulatorModule,
    )
}
