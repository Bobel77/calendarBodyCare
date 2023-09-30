package com.example.calendar

import io.kvision.core.onEvent
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.form.text.Text
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.i18n.I18n.tr
import io.kvision.modal.Dialog
import io.kvision.remote.Credentials
import io.kvision.remote.LoginService
import io.kvision.remote.SecurityMgr
import io.kvision.utils.ENTER_KEY
import kotlinx.browser.document

class LoginWindow : Dialog<Credentials>(closeButton = false, escape = false, animation = false) {

    private val loginPanel: FormPanel<Credentials>
    private val loginButton: Button
    private val closeBtn: Button

    init {
        loginPanel = formPanel {

            add(Credentials::username, Text(label = "${tr("Login")}:"), required = true)
            add(Credentials::password, Password(label = "${tr("Password")}:"), required = true)
            onEvent {
                keydown = {
                    if (it.keyCode == ENTER_KEY) {
                        this@LoginWindow.processCredentials()
                    }
                }
            }
        }

        loginButton = Button("Login", "fas fa-check", ButtonStyle.PRIMARY) {
            onClick {
                this@LoginWindow.processCredentials()
            }
        }

        closeBtn =  Button("schließen","fas fa-times", ButtonStyle.PRIMARY){
             onClick {
                document.location!!.href = document.URL.split("#!/")[0] +"#!/Home"
                document.location!!.reload()
             }
        }
        addButton(closeBtn)
        addButton(loginButton)

    }


    private fun processCredentials() {
        if (loginPanel.validate()) {
            setResult(loginPanel.getData())
            loginPanel.clearData()
        }
    }
}

object Security : SecurityMgr() {

    private val loginService = LoginService("/login")
    private val loginWindow = LoginWindow()
    override suspend fun login(): Boolean {
        return loginService.login(loginWindow.getResult())
    }

    override suspend fun afterLogin() {
        Model.readProfile()
    }
}