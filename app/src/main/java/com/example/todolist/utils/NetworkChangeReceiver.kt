package com.example.todolist.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.view.View
import kotlinx.coroutines.*


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

    @OptIn(DelicateCoroutinesApi::class)
    override fun onAvailable(network: Network) {
        GlobalScope.launch {
            withContext(Dispatchers.Main){
                viewForHiding.visibility = View.GONE
            }
        }
        super.onAvailable(network)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onLosing(network: Network, maxMsToLive: Int) {
        GlobalScope.launch {
            withContext(Dispatchers.Main){
                viewForHiding.visibility = View.VISIBLE
            }
        }
        super.onLosing(network, maxMsToLive)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onLost(network: Network) {
        GlobalScope.launch {
            withContext(Dispatchers.Main){
                viewForHiding.visibility = View.VISIBLE
            }
        }
        super.onLost(network)
    }
}