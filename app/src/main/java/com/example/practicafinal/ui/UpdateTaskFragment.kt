package com.example.practicafinal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.practicafinal.data.AppDatabase
import com.example.practicafinal.data.Task
import com.example.practicafinal.databinding.FragmentUpdateTaskBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateTaskFragment : Fragment() {

    private var _binding: FragmentUpdateTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private var task: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            task = it.getSerializable(ARG_TASK) as Task?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        // Cargar datos de la tarea
        task?.let {
            binding.etTitle.setText(it.title)
            binding.etDescription.setText(it.description)
            binding.etDetails.setText(it.details)
            binding.etImageUrl.setText(it.imageUrl)
        }

        // Guardar cambios
        binding.btnSaveChanges.setOnClickListener {
            updateTask()
        }
    }

    private fun updateTask() {
        val updatedTitle = binding.etTitle.text.toString().trim()
        val updatedDescription = binding.etDescription.text.toString().trim()
        val updatedDetails = binding.etDetails.text.toString().trim()
        val updatedImageUrl = binding.etImageUrl.text.toString().trim()

        if (updatedTitle.isNotEmpty() && updatedDescription.isNotEmpty() && updatedDetails.isNotEmpty() && updatedImageUrl.isNotEmpty()) {
            task?.let {
                val updatedTask = it.copy(
                    title = updatedTitle,
                    description = updatedDescription,
                    details = updatedDetails,
                    imageUrl = updatedImageUrl
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    database.taskDao().updateTask(updatedTask)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Tarea actualizada con éxito", Toast.LENGTH_SHORT).show()

                        // Enviar la tarea actualizada al fragmento anterior
                        parentFragmentManager.setFragmentResult("task_updated", Bundle().apply {
                            putSerializable("updated_task", updatedTask)
                        })

                        // Volver al fragmento anterior
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TASK = "task"

        fun newInstance(task: Task): UpdateTaskFragment {
            return UpdateTaskFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TASK, task)
                }
            }
        }
    }
}