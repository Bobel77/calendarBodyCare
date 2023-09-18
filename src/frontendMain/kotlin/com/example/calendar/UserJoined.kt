package com.example.calendar

import io.kvision.html.*
import io.kvision.jquery.invoke
import io.kvision.jquery.jQuery
import io.kvision.modal.Confirm
import io.kvision.panel.Root
import io.kvision.remote.LoginService
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserJoined() {

   init {
          Menu.myMenu.add(Link("Ausloggen", className = "ovalzwei-menu-item", url = "/logout").apply {
                this.onClick {
                    jQuery(".ovalzwei-menu-item").removeClass("accio")
                    jQuery(getElement()).addClass("accio")
                }
            })
       AppScope.launch {

           delay(20)
          /* if(Model.myEvents.isEmpty()){

               document.location?.reload()
           }*/
       }



   }
}