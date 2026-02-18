package com.taher.PolyHome.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.taher.PolyHome.R
import com.taher.PolyHome.models.House


class HouseAdapter(
    private var houses: List<House>,
    private val onHouseClick: (House) -> Unit,
    private val onManageUsersClick: (House) -> Unit
) : RecyclerView.Adapter<HouseAdapter.HouseViewHolder>() {


    inner class HouseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val houseIdTextView: TextView = view.findViewById(R.id.textViewHouseId)
        val ownerStatusTextView: TextView = view.findViewById(R.id.textViewOwnerStatus)
        val manageUsersButton: Button = view.findViewById(R.id.buttonManageUsers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_houses_list, parent, false)
        return HouseViewHolder(view)
    }

    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        val house = houses[position]

        holder.houseIdTextView.text = "Maison #${house.houseId}"
        holder.ownerStatusTextView.text = if (house.owner) "Propriétaire" else "Invité"

        holder.itemView.setOnClickListener {
            onHouseClick(house)
        }

        holder.manageUsersButton.setOnClickListener {
            onManageUsersClick(house)
        }
    }

    override fun getItemCount(): Int = houses.size


    fun updateHouses(newHouses: List<House>) {
        houses = newHouses
        notifyDataSetChanged()
    }
}
