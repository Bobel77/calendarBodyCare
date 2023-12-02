package com.example.calendar


import com.example.calendar.helper.Model
import io.kvision.core.*
import io.kvision.html.*
import io.kvision.jquery.invoke
import io.kvision.jquery.jQuery
import io.kvision.offcanvas.offcanvas
import io.kvision.panel.HPanel
import io.kvision.panel.Root
import io.kvision.panel.vPanel
import io.kvision.state.bindEach
import io.kvision.utils.auto
import io.kvision.utils.perc
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Menu {
    var myMenu = Div()
}

class MyMenu(rt: Root) : HPanel() {
    private val btn = Button("Menu", className = "fixedButton") {
        this.onClick {
            getRoot()!!.offcanvas {
                val md = menuDiv.apply {
                    width = 100.perc
                    onClick {
                        AppScope.launch {
                            delay(500)
                            this@offcanvas.hideBootstrap()
                        }
                    }
                }
                this.add(md)
            }.show()
        }
    }
    private val menuDiv = div(className = "ovalzwei-menu") {

        alignSelf = AlignItems.START
        justifySelf = JustifyItems.START
        Menu.myMenu = this@div
        id = "ovalM"
        background = Background(color = Color.hex(13012563))
                this.bindEach(Model.offCanvasSpans){
                site: Site ->
                    link(site.siteName, url = "#!/${site.siteName}", className = "ovalzwei-menu-item") {
                        background = Background(color = Color.hex(13012563))

                        onClick {
                            jQuery(".ovalzwei-menu-item").removeClass("accio")
                            jQuery(getElement()).addClass("accio")
                            site.changePanel(vTapPanel)
                        }
                    }
                }
    }

    private val vTapPanel = vPanel(justify = JustifyContent.CENTER, alignItems = AlignItems.CENTER) {
        overflow = Overflow.HIDDEN
        width = 80.perc
        marginLeft = auto
        marginRight = auto
    }


    init {

        if (window.screen.width < 800.0) {
            rt.add(btn)
            this@MyMenu.remove(menuDiv)
        }
        try {
            Model.offCanvasSpans.single { it.siteName == document.URL.split("#!/")[1] }.changePanel(vTapPanel)

    }catch (_:Exception){}}
}
