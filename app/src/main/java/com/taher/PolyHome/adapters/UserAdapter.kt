package com.taher.PolyHome.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.taher.PolyHome.R

class UserAdapter(
    private val users: List<Map<String, Any>>,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userLoginTextView: TextView = view.findViewById(R.id.textViewUserLogin)
        val roleTextView: TextView = view.findViewById(R.id.textViewRole)
        val btnRemove: Button = view.findViewById(R.id.btnRemoveUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_users, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val login = user["userLogin"]?.toString().orEmpty()
        val isOwner = user["owner"] as? Boolean ?: false

        holder.userLoginTextView.text = login
        holder.roleTextView.text = if (isOwner) "Propriétaire" else "Invité"

        val sharedPref = holder.itemView.context.getSharedPreferences("PolyHome", Context.MODE_PRIVATE)
        val currentLogin = sharedPref.getString("userLogin", null)

        if (isOwner || login == currentLogin) {
            holder.btnRemove.visibility = View.GONE
        } else {
            holder.btnRemove.visibility = View.VISIBLE
            holder.btnRemove.setOnClickListener {
                onRemoveClick(login)
            }
        }
    }

    override fun getItemCount(): Int = users.size
}
