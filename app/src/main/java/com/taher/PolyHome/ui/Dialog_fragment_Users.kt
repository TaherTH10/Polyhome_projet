package com.taher.PolyHome.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment


class AddUserDialogFragment(
    private val onUserAdded: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val input = EditText(requireContext())
        input.hint = "Login de l'utilisateur"

        return AlertDialog.Builder(requireContext())
            .setTitle("Ajouter un utilisateur")
            .setView(input)
            .setPositiveButton("Ajouter") { _, _ ->
                val login = input.text.toString()
                if (login.isNotBlank()) {
                    onUserAdded(login)
                }
            }
            .setNegativeButton("Annuler", null)
            .create()
    }
}
