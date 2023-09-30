package com.example.calendar

import io.kvision.core.*
import io.kvision.form.check.switch
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.vh
import kotlinx.browser.window

class Flyout(/*val backWidth: Int,val backProp: String,*/ ): Div(/*className = "flyout-overlay"*/){

    init {
        width = 100.perc
        position = Position.FIXED
        zIndex = 100
        display = Display.NONE
        bottom = 0.px
        left = 0.px
        height = 0.px
        background = Background(color = Color.rgba(0, 0, 0, 120))
        overflow = Overflow.HIDDEN
        this.transition = Transition("height",  0.3, "ease-in-out")
    }

    val flyContent = div(/*className = "flyout-content"*/){
        width = 100.perc
        background = Background(color = Color.name(Col.WHITE))
        padding = 20.px
        position = Position.RELATIVE
        height = 100.vh
        bottom = (-100).perc

        this.transition = Transition("bottom",  0.3, "ease-in-out")
    }

    fun showFlyout(){
        this@Flyout.display = Display.BLOCK
        window.setTimeout(handler = {
            this@Flyout.height = 100.perc
            flyContent.bottom = 0.perc
        }, 10)


    }
    fun hideFlyout(){
        flyContent.bottom = (-100).perc
        window.setTimeout(handler = {
            this@Flyout.height = 0.perc
            this@Flyout.display = Display.NONE
        }, 300)
    }
}