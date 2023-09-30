package com.example.calendar

import io.kvision.core.*
import io.kvision.html.*
import io.kvision.jquery.invoke
import io.kvision.jquery.jQuery
import io.kvision.offcanvas.offcanvas
import io.kvision.panel.*
import io.kvision.utils.auto
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Menu {
    var myMenu = Div()
}
 class MyMenu(val rt: Root): HPanel(){
     private val btn = Button("Menu", className = "fixedButton"){
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
     private val menuDiv = div(className = "ovalzwei-menu"){
         alignSelf = AlignItems.START
         justifySelf = JustifyItems.START
         Menu.myMenu = this
         id = "ovalM"
         background = Background(color = Color.hex(13012563))

         Model.offCanvasSpans.forEach { site ->

             link(site.siteName, url = "#!/${site.siteName}", className = "ovalzwei-menu-item"){
                 background = Background(color = Color.hex(13012563))

                 onClick {
                     jQuery(".ovalzwei-menu-item").removeClass("accio")
                     jQuery(getElement()).addClass("accio")
                     changePanel(site)
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
        if(window.screen.width < 800.0){
            rt.add(btn)
            this@MyMenu.remove(menuDiv)
        }
        try {
            changePanel(Model.offCanvasSpans.filter { it.siteName == document.URL.split("#!/")[1]}.single())
        }catch (e:Exception){
            changePanel(Model.offCanvasSpans[0])
        }
    }


    private fun changePanel(site: Site) {
            vTapPanel.removeAll()
            site.content.forEach {
               vTapPanel.add(it)
            }

            if(site.function != null){
                when(site.function)
                {
                    "video" -> site.vid(vTapPanel)
                    "calender" -> AppScope.launch { site.calender(vTapPanel) }
                    "Mitgliederbereich" -> site.intern(vTapPanel)
                  /*  "fotos" ->*/

                }
            }
    }
}
