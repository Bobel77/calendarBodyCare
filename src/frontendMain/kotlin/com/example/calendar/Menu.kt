package com.example.calendar

import io.kvision.core.*
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.link
import io.kvision.jquery.invoke
import io.kvision.jquery.jQuery
import io.kvision.panel.HPanel
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import io.kvision.utils.auto
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.browser.document

object Menu {
    var myMenu = Div()
}
 class MyMenu: HPanel(){
     private val menuDiv = div(className = "ovalzwei-menu"){
         alignSelf = AlignItems.START
         justifySelf = JustifyItems.START
         Menu.myMenu = this
         id = "ovalM"
         background = Background(color = Color.hex(13012563))

         Model.offCanvasSpans.forEach {site->

             link(site.siteName,url = "#!/${site.siteName}", className = "ovalzwei-menu-item"){
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
         marginRight = auto}


    init {
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
                    "calender" -> site.calender(vTapPanel)
                    "Mitgliederbereich" -> site.intern(vTapPanel)
                  /*  "fotos" ->*/

                }
            }
    }
}
