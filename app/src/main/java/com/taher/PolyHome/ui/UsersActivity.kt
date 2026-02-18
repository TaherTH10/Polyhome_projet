package com.taher.PolyHome.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.taher.PolyHome.R
import com.taher.PolyHome.adapters.UserAdapter
import com.taher.PolyHome.network.Api
import com.taher.PolyHome.ui.dialogs.AddUserDialogFragment


class UsersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private val users = mutableListOf<Map<String, Any>>()

    private var houseId: String? = null
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        houseId = intent.getStringExtra("houseId")
        token = getSharedPreferences("PolyHome", Context.MODE_PRIVATE).getString("token", null)

        if (houseId == null || token == null) {
            Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerViewUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(users) { userLogin -> removeUser(userLogin) }
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnAddUser).setOnClickListener {
            showAddUserDialog()
        }

        loadUsers()
    }


    private fun loadUsers() {
        Api().get<List<Map<String, Any>>>(
            path = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users",
            onSuccess = { code, result ->
                runOnUiThread {
                    if (code == 200 && result != null) {
                        users.clear()
                        users.addAll(result)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, "Erreur chargement (code $code)", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            securityToken = token
        )
    }


    private fun removeUser(userLogin: String) {
        val currentUser = users.find { it["userLogin"] == userLogin }
        val isOwner = currentUser?.get("owner") as? Boolean ?: false
        val currentLogin = getSharedPreferences("PolyHome", Context.MODE_PRIVATE)
            .getString("userLogin", null)

        when {
            userLogin == currentLogin -> {
                Toast.makeText(this, "Vous ne pouvez pas vous retirer vous-même.", Toast.LENGTH_SHORT).show()
                return
            }
            isOwner -> {
                Toast.makeText(this, "Impossible de supprimer le propriétaire", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val data = mapOf("userLogin" to userLogin)
        Api().delete(
            path = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users",
            data = data,
            onSuccess = { code ->
                runOnUiThread {
                    if (code == 200) {
                        Toast.makeText(this, "Utilisateur supprimé", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        Toast.makeText(this, "Erreur suppression (code $code)", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            securityToken = token
        )
    }


    private fun showAddUserDialog() {
        AddUserDialogFragment { userLogin ->
            addUser(userLogin)
        }.show(supportFragmentManager, "AddUserDialog")
    }


    private fun addUser(userLogin: String) {
        val data = mapOf("userLogin" to userLogin)
        Api().post(
            path = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users",
            data = data,
            onSuccess = { code ->
                runOnUiThread {
                    if (code == 200) {
                        Toast.makeText(this, "Utilisateur ajouté", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        Toast.makeText(this, "Erreur ajout (code $code)", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            securityToken = token
        )
    }
}
