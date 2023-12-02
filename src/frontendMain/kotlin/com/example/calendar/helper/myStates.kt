package com.example.calendar.helper

import io.kvision.redux.RAction
import io.kvision.redux.createReduxStore
import io.kvision.remote.getService
import io.kvision.state.observableListOf
import kotlinx.serialization.Serializable

object MyStates {
    val store = createReduxStore(::myReducer, MyState("", 0))
    @Serializable
    data class MyState(val content: String, val counter: Int)
    @kotlinx.serialization.Serializable
    sealed class MyAction : RAction {
        object Increment : MyAction()
        object Decrement : MyAction()
        data class SetContent(val content: String) : MyAction()
        data class FetchDataSuccess(val data: Boolean)
    }

    fun myReducer(state: MyState, action: MyAction): MyState = when (action) {
        is MyAction.Increment -> {
            state.copy(counter = state.counter + 1)
        }
        is MyAction.Decrement -> {
            state.copy(counter = state.counter - 1)
        }
        is MyAction.SetContent -> {
            state.copy(content = action.content)
        }
    }
}