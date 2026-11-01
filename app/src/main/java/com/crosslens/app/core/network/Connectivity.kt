package com.crosslens.app.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Lifecycle subscribers own registration. No polling, device identifiers or reachability requests. */
fun observeConnectivity(context: Context): Flow<Boolean> = callbackFlow {
    val manager = context.getSystemService(ConnectivityManager::class.java)
    fun connected() = manager.getNetworkCapabilities(manager.activeNetwork)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { trySend(connected()) }
        override fun onLost(network: Network) { trySend(connected()) }
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) { trySend(connected()) }
    }
    manager.registerDefaultNetworkCallback(callback)
    trySend(connected())
    awaitClose { manager.unregisterNetworkCallback(callback) }
}.distinctUntilChanged()
