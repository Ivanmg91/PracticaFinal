package com.example.practicafinal.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.practicafinal.data.AppDatabase
import com.example.practicafinal.data.Task
import com.example.practicafinal.databinding.FragmentAddTaskBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        binding.btnSave.setOnClickListener {
            saveTask()
        }
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val details = binding.etDetails.text.toString().trim()
        val imageUrl = binding.etImageUrl.text.toString().trim()

        if (title.isNotEmpty() && description.isNotEmpty() && details.isNotEmpty() && imageUrl.isNotEmpty()) {
            val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val username = sharedPreferences.getString("username", null)

            if (username != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val user = database.userDao().getUserByUsername(username)
                    if (user != null) {
                        val task = Task(
                            title = title,
                            description = description,
                            details = details,
                            imageUrl = imageUrl,
                            userId = user.id
                        )

                        database.taskDao().insertTask(task)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Tarea añadida con éxito", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack() // Volver al fragmento anterior
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
