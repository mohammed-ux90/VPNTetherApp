package com.example.vpntether

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

class MainActivity : AppCompatActivity() {

    private lateinit var btnToggle: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvIpPort: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnToggle = findViewById(R.id.btnToggle)
        tvStatus = findViewById(R.id.tvStatus)
        tvIpPort = findViewById(R.id.tvIpPort)

        updateUI()

        btnToggle.setOnClickListener {
            if (TetheringService.isRunning) {
                sendCommandToService(TetheringService.ACTION_STOP)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                        return@setOnClickListener
                    }
                }
                sendCommandToService(TetheringService.ACTION_START)
            }
            // Add a small delay to let service update state
            btnToggle.postDelayed({ updateUI() }, 500)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        if (TetheringService.isRunning) {
            tvStatus.text = getString(R.string.proxy_status_running)
            tvStatus.setTextColor(Color.GREEN)
            btnToggle.text = getString(R.string.stop_proxy)
            
            val ip = getLocalIpAddress()
            tvIpPort.text = "Server IP: $ip\nPort: 8080"
        } else {
            tvStatus.text = getString(R.string.proxy_status_stopped)
            tvStatus.setTextColor(Color.RED)
            btnToggle.text = getString(R.string.start_proxy)
            tvIpPort.text = "Server IP: -\nPort: 8080"
        }
    }

    private fun sendCommandToService(action: String) {
        val intent = Intent(this, TetheringService::class.java).apply {
            this.action = action
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                // Ignore loopback and VPN tun interfaces, look for rndis (USB tether) or wlan
                if (intf.name.contains("tun") || intf.isLoopback) continue
                
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "0.0.0.0"
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "0.0.0.0"
    }
}
