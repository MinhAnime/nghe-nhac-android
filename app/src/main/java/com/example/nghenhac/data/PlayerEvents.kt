package com.example.nghenhac.data

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object PlayerEvents {
    sealed class Event {
        object Next : Event()
        object Previous : Event()
    }

    private val _eventChannel = Channel<Event>()
    val eventFlow = _eventChannel.receiveAsFlow()

    suspend fun emitEvent(event: Event) {
        _eventChannel.send(event)
    }
}