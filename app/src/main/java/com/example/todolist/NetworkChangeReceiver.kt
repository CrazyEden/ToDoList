package com.example.todolist

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.view.View


class NetworkChangeReceiver(context: Context, private val viewForHiding:View)
    : ConnectivityManager.NetworkCallback(){
    init {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    override fun onAvailable(network: Network) {
        viewForHiding.visibility = View.GONE
        super.onAvailable(network)
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        viewForHiding.visibility = View.VISIBLE
        super.onLosing(network, maxMsToLive)
    }

    override fun onLost(network: Network) {
        viewForHiding.visibility = View.VISIBLE
        super.onLost(network)
    }
}