package com.taher.PolyHome.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.taher.PolyHome.R
import com.taher.PolyHome.models.DeviceCommand
import com.taher.PolyHome.models.Room
import com.taher.PolyHome.network.Api
import com.taher.PolyHome.models.DevicesResponse
import com.google.android.material.button.MaterialButton


class DeviceListActivity : AppCompatActivity() {

    private val api = Api()
    private var token: String? = null
    private var houseId: Int = -1
    private val rooms = mutableListOf<Room>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)


        houseId = intent.getIntExtra("houseId", -1)
        if (houseId == -1) {
            Toast.makeText(this, "Erreur : ID de maison non valide", Toast.LENGTH_SHORT).show()
            Log.e("DeviceListActivity", "ID maison invalide")
            finish()
            return
        }


        val sharedPref = getSharedPreferences("PolyHome", Context.MODE_PRIVATE)
        token = sharedPref.getString("token", null)
        if (token == null) {
            Toast.makeText(this, "Vous devez vous connecter", Toast.LENGTH_SHORT).show()
            Log.e("DeviceListActivity", "Token manquant")
            finish()
            return
        }

        title = "Périphériques - Maison #$houseId"
        loadDevices()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.device_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                loadDevices()
                true
            }
            R.id.action_back -> {
                onBackPressed()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun loadDevices() {
        Toast.makeText(this, "Chargement des périphériques...", Toast.LENGTH_SHORT).show()
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices"

        api.get<DevicesResponse>(
            url,
            onSuccess = { code, response ->
                if (code == 200 && response != null) {
                    val deviceList = response.devices
                    Log.d("DeviceListActivity", "Nombre de périphériques : ${deviceList.size}")


                    rooms.clear()

                    // Création des pièces de base
                    val livingRoom = Room(1, "Salon")
                    val bedroom = Room(2, "Chambre")
                    val kitchen = Room(3, "Cuisine")
                    val outside = Room(4, "Extérieur")


                    for (device in deviceList) {
                        val id = device.id.lowercase()
                        when {
                            id.contains("light") -> livingRoom.devices.add(device)
                            id.contains("shutter") && !id.contains("rolling") -> bedroom.devices.add(device)
                            id.contains("rolling") -> kitchen.devices.add(device)
                            id.contains("garage") -> outside.devices.add(device)
                            else -> livingRoom.devices.add(device)
                        }
                    }


                    listOf(livingRoom, bedroom, kitchen, outside)
                        .filter { it.devices.isNotEmpty() }
                        .forEach { rooms.add(it) }

                    runOnUiThread {
                        updateRoomsUI()
                        Toast.makeText(
                            this,
                            "${deviceList.size} périphériques répartis dans ${rooms.size} pièces",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else if (code == 403) {
                    runOnUiThread {
                        Toast.makeText(this, "Accès non autorisé", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Erreur lors du chargement des périphériques (code $code)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            securityToken = token
        )
    }

    private fun updateRoomsUI() {
        val container = findViewById<LinearLayout>(R.id.roomsContainer)
        container.removeAllViews()

        for (room in rooms) {

            val roomView = layoutInflater.inflate(R.layout.room_containers, container, false)

            val roomNameTextView = roomView.findViewById<TextView>(R.id.textViewRoomName)
            roomNameTextView.text = room.name

            val deviceContainer = roomView.findViewById<LinearLayout>(R.id.deviceContainer)

            for (device in room.devices) {
                val deviceView = layoutInflater.inflate(R.layout.item_device, deviceContainer, false)

                // Informations de base
                deviceView.findViewById<TextView>(R.id.textViewDeviceId).text = device.id
                deviceView.findViewById<TextView>(R.id.textViewDeviceType).text =
                    "Type: ${getReadableDeviceType(device.type)}"
                deviceView.findViewById<TextView>(R.id.textViewDeviceStatus).text = when {
                    device.opening != null -> "Ouverture: ${device.opening}%"
                    device.power != null -> "Puissance: ${device.power}%"
                    else -> "Statut inconnu"
                }


                val commandsLayout = deviceView.findViewById<LinearLayout>(R.id.layoutCommands)
                commandsLayout.removeAllViews()
                device.availableCommands.forEach { command ->
                    val button = layoutInflater.inflate(R.layout.device_commands, commandsLayout, false) as MaterialButton
                    button.text = getCommandLabel(command)
                    button.setOnClickListener {
                        sendCommand(device.id, command)
                    }
                    commandsLayout.addView(button)
                }

                deviceContainer.addView(deviceView)
            }

            container.addView(roomView)
        }
    }


    private fun sendCommand(deviceId: String, command: String) {
        val deviceCommand = DeviceCommand(command)

        api.post<DeviceCommand>(
            "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices/$deviceId/command",
            deviceCommand,
            onSuccess = { code ->
                runOnUiThread {
                    if (code == 200) {
                        Toast.makeText(this, "Commande envoyée avec succès", Toast.LENGTH_SHORT).show()
                        loadDevices() // Rechargement pour mise à jour de l'état
                    } else {
                        Toast.makeText(this, "Erreur lors de l'envoi (code $code)", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            securityToken = token
        )
    }


    private fun logout() {
        getSharedPreferences("PolyHome", Context.MODE_PRIVATE).edit().apply {
            remove("token")
            remove("userLogin")
            apply()
        }

        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }


    private fun getReadableDeviceType(type: String): String = when (type) {
        "sliding-shutter" -> "Volet coulissant"
        "rolling-shutter" -> "Volet roulant"
        "garage-door" -> "Porte de garage"
        "light" -> "Lumière"
        else -> type
    }


    private fun getCommandLabel(command: String): String = when (command) {
        "open" -> "Ouvrir"
        "close" -> "Fermer"
        "on" -> "Allumer"
        "off" -> "Éteindre"
        "stop" -> "Stop"
        else -> command
    }
}
