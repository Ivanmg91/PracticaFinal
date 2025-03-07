package com.example.practicafinal

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.practicafinal.data.Task
import com.example.practicafinal.databinding.ItemTaskBinding

class TaskAdapter(
    private val taskList: MutableList<Task>,
    private val onClick: (Task) -> Unit,
    private val onDelete: (Task) -> Unit // Callback para eliminar tarea
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            binding.tvDescription.text = task.description

            // Cargar imagen con Glide
            Glide.with(binding.root.context)
                .load(task.imageUrl)
                .into(binding.ivTaskImage)

            binding.root.setOnClickListener {
                onClick(task)  // Llamar a la función onClick cuando se presiona un elemento
            }

            // Agregar evento de mantener presionado para eliminar la tarea
            binding.root.setOnLongClickListener {
                showDeleteConfirmationDialog(task)
                true // Indica que el evento ha sido manejado
            }
        }

        // Mostrar diálogo de confirmación para eliminar tarea
        private fun showDeleteConfirmationDialog(task: Task) {
            val context = binding.root.context
            AlertDialog.Builder(context)
                .setTitle("Eliminar tarea")
                .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
                .setPositiveButton("Sí") { _, _ ->
                    onDelete(task) // Llamar a la función para eliminar
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(taskList[position])
    }

    override fun getItemCount(): Int = taskList.size
}
