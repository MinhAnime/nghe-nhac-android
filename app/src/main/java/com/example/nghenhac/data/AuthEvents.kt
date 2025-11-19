package com.example.nghenhac.data

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object AuthEvents {
    private val _logoutChannel = Channel<Unit>()
    val logoutFlow = _logoutChannel.receiveAsFlow()

    suspend fun emitLogout() {
        _logoutChannel.send(Unit)
    }
}