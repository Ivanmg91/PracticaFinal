package com.example.practicafinal.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.practicafinal.R
import com.example.practicafinal.data.Task
import com.example.practicafinal.databinding.FragmentTaskDetailBinding

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private var task: Task? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            task = it.getSerializable(ARG_TASK) as Task
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTaskDetails()

        // Escuchar el resultado de la tarea actualizada
        parentFragmentManager.setFragmentResultListener("task_updated", viewLifecycleOwner) { _, bundle ->
            val updatedTask = bundle.getSerializable("updated_task") as Task
            refreshTask(updatedTask) // Recargar los detalles con los datos actualizados
        }

        binding.btnUpdateTask.setOnClickListener {
            task?.let {
                val updateFragment = UpdateTaskFragment.newInstance(it)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, updateFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    fun refreshTask(updatedTask: Task) {
        task = updatedTask // Actualiza la referencia de la tarea
        loadTaskDetails()  // Recarga los detalles en la interfaz
    }


    private fun loadTaskDetails() {
        task?.let {
            binding.tvTitle.text = it.title
            binding.tvDescription.text = it.description
            binding.tvDetails.text = it.details

            Glide.with(requireContext())
                .load(it.imageUrl)
                .into(binding.ivTaskImage)
        }
    }

    private fun startMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.music) // Asegúrate de tener un archivo `music` en res/raw
            mediaPlayer?.isLooping = true
        }
        mediaPlayer?.seekTo(currentPosition)
        mediaPlayer?.start()
        Toast.makeText(requireContext(), "Reproduciendo música", Toast.LENGTH_SHORT).show()
    }

    private fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                currentPosition = it.currentPosition
                it.pause()
                Toast.makeText(requireContext(), "Música pausada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pauseMusic()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val ARG_TASK = "task"

        fun newInstance(task: Task): TaskDetailFragment {
            return TaskDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TASK, task)
                }
            }
        }
    }
}
