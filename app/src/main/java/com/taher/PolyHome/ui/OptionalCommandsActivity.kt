package com.taher.PolyHome.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.taher.PolyHome.R
import com.taher.PolyHome.models.Device
import com.taher.PolyHome.models.DeviceCommand
import com.taher.PolyHome.models.DevicesResponse
import com.taher.PolyHome.network.Api
import kotlinx.coroutines.*


class OptionalCommandsActivity : AppCompatActivity() {

    private val api = Api()
    private var token: String? = null
    private var houseId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_optional_commands)

        houseId = intent.getIntExtra("houseId", -1)
        if (houseId == -1) {
            Toast.makeText(this, "Erreur: ID maison invalide", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        token = getSharedPreferences("PolyHome", Context.MODE_PRIVATE)
            .getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Vous devez vous connecter", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Actions lumières
        findViewById<Button>(R.id.buttonTurnOffAllLights).setOnClickListener {
            lifecycleScope.launch {
                performDeviceAction("light", "TURN OFF", "Lumières éteintes")
            }
        }

        findViewById<Button>(R.id.buttonTurnOnAllLights).setOnClickListener {
            lifecycleScope.launch {
                performDeviceAction("light", "TURN ON", "Lumières allumées")
            }
        }

        // Actions volets
        findViewById<Button>(R.id.buttonOpenAllShutters).setOnClickListener {
            lifecycleScope.launch {
                performDeviceAction("rolling shutter", "OPEN", "Volets ouverts")
            }
        }

        findViewById<Button>(R.id.buttonCloseAllShutters).setOnClickListener {
            lifecycleScope.launch {
                performDeviceAction("rolling shutter", "CLOSE", "Volets fermés")
            }
        }

        // Scénario : tout fermer et éteindre
        findViewById<Button>(R.id.buttonCloseAllAndTurnOffLights).setOnClickListener {
            lifecycleScope.launch {
                performDeviceAction("rolling shutter", "CLOSE", "Volets fermés")
                performDeviceAction("garage door", "CLOSE", "Portes fermées")
                performDeviceAction("light", "TURN OFF", "Lumières éteintes")
            }
        }

        // Scénario : tout ouvrir et allumer
        findViewById<Button>(R.id.btnOpenAllAndTurnOnLights).setOnClickListener {
            lifecycleScope.launch {
                performDeviceAction("rolling shutter", "OPEN", "Volets ouverts")
                performDeviceAction("garage door", "OPEN", "Portes ouvertes")
                performDeviceAction("light", "TURN ON", "Lumières allumées")
            }
        }
    }


    private suspend fun performDeviceAction(typeFilter: String, command: String, actionDescription: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices"

        withContext(Dispatchers.IO) {
            api.get<DevicesResponse>(
                url,
                securityToken = token,
                onSuccess = { code, response ->
                    if (code == 200 && response != null) {
                        val devices = response.devices.filter {
                            it.type.contains(typeFilter, ignoreCase = true)
                        }

                        lifecycleScope.launch {
                            val successCount = executeCommands(devices, command, url)
                            val msg = "$actionDescription : $successCount/${devices.size} succès"
                            Toast.makeText(this@OptionalCommandsActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(this@OptionalCommandsActivity, "Erreur chargement périphériques", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }


    private suspend fun executeCommands(
        devices: List<Device>,
        command: String,
        baseUrl: String
    ): Int {
        return withContext(Dispatchers.IO) {
            devices.map { device ->
                async {
                    var resultCode = -1
                    api.post<DeviceCommand>(
                        path = "$baseUrl/${device.id}/command",
                        data = DeviceCommand(command),
                        onSuccess = { code -> resultCode = code },
                        securityToken = token
                    )
                    resultCode
                }
            }.awaitAll().count { it == 200 }
        }
    }
}
