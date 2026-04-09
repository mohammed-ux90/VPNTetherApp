package com.example.vpntether

import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import java.net.InetSocketAddress

class ProxyServerManager {
    private var proxyServer: HttpProxyServer? = null

    fun startProxy(port: Int = 8080): Boolean {
        if (proxyServer != null) return true
        
        return try {
            proxyServer = DefaultHttpProxyServer.bootstrap()
                .withPort(port)
                .withAllowLocalOnly(false) // Allow external connections like the tethered PC
                .withName("VPNTetherProxy")
                .start()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun stopProxy() {
        try {
            proxyServer?.stop()
            proxyServer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isRunning(): Boolean {
        return proxyServer != null
    }
}
