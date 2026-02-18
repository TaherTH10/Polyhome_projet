package com.taher.PolyHome.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.taher.PolyHome.R
import com.taher.PolyHome.models.Device


class DeviceAdapter(
    private var devices: List<Device>,
    private val onCommandClick: (Device, String) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {


    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceIdTextView: TextView = view.findViewById(R.id.textViewDeviceId)
        val deviceTypeTextView: TextView = view.findViewById(R.id.textViewDeviceType)
        val deviceStatusTextView: TextView = view.findViewById(R.id.textViewDeviceStatus)
        val commandsLayout: LinearLayout = view.findViewById(R.id.layoutCommands)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]


        holder.deviceIdTextView.text = device.id
        holder.deviceTypeTextView.text = "Type : ${getReadableDeviceType(device.type)}"

        // Affiche le statut (ouverture ou puissance selon le type)
        holder.deviceStatusTextView.text = when {
            device.opening != null -> "Ouverture : ${device.opening}%"
            device.power != null -> "Puissance : ${device.power}%"
            else -> "Statut inconnu"
        }


        holder.commandsLayout.removeAllViews()


        device.availableCommands.forEach { command ->
            val button = Button(holder.itemView.context).apply {
                text = getCommandLabel(command)
                setOnClickListener { onCommandClick(device, command) }
            }
            holder.commandsLayout.addView(button)
        }
    }

    override fun getItemCount(): Int = devices.size


    fun updateDevices(newDevices: List<Device>) {
        devices = newDevices
        notifyDataSetChanged()
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
