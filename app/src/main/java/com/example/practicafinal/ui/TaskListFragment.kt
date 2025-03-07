package com.example.practicafinal.ui

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practicafinal.R
import com.example.practicafinal.TaskAdapter
import com.example.practicafinal.data.AppDatabase
import com.example.practicafinal.data.Task
import com.example.practicafinal.databinding.FragmentTaskListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private lateinit var taskAdapter: TaskAdapter
    private var taskList = mutableListOf<Task>()

    private var mediaPlayer: MediaPlayer? = null
    private var currentPosition = 0
    private var isPlaying = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)

        setupRecyclerView()

        if (username != null) {
            loadTasks(username) // Cargar las tareas del usuario
        }

        binding.fabAddTask.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddTaskFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        // Inicializar el MediaPlayer
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.music) // Asegúrate de tener el archivo `music` en res/raw
        mediaPlayer?.isLooping = true

        // Iniciar la música automáticamente
        playMusic()
    }



    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(taskList,
            onClick = { task ->
                navigateToTaskDetailFragment(task) // Navegar al fragmento en lugar de la actividad
            },
            onDelete = { task -> deleteTask(task) }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun navigateToTaskDetailFragment(task: Task) {
        val taskDetailFragment = TaskDetailFragment.newInstance(task)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, taskDetailFragment) // Reemplazar el fragmento actual
            .addToBackStack(null) // Agregar a la pila de retroceso
            .commit()
    }


    private fun loadTasks(username: String) {
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                database.userDao().getUserByUsername(username)
            }

            if (user != null) {
                database.taskDao().getTasksByUser(user.id).collect { tasks ->
                    taskList.clear()
                    taskList.addAll(tasks)
                    taskAdapter.notifyDataSetChanged()
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteTask(task: Task) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.taskDao().deleteTask(task)
            withContext(Dispatchers.Main) {
                taskList.remove(task)
                taskAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), getString(R.string.task_deleted), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logoutUser() {
        val sharedPreferencesMusic = requireContext().getSharedPreferences("music_prefs", Context.MODE_PRIVATE)
        sharedPreferencesMusic.edit().apply {
            putBoolean("is_app_closed", true)
            apply()
        }

        val sharedPreferencesUser = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferencesUser.edit().apply {
            putBoolean("isLoggedIn", false)
            remove("username")
            apply()
        }

        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
    }


    private fun playMusic() {
        mediaPlayer?.let {
            it.seekTo(currentPosition) // Reanudar desde la posición guardada
            it.start()
            isPlaying = true
        }
    }

    private fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                currentPosition = it.currentPosition // Guardar la posición actual
                it.pause()
                isPlaying = false

                // Guardar el estado en SharedPreferences
                val sharedPreferences = requireContext().getSharedPreferences("music_prefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().apply {
                    putInt("music_position", currentPosition)
                    putBoolean("is_playing", isPlaying)
                    apply()
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        pauseMusic() // Pausa la música y guarda la posición actual
    }

    override fun onResume() {
        super.onResume()

        // Cargar el estado desde SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("music_prefs", Context.MODE_PRIVATE)
        currentPosition = sharedPreferences.getInt("music_position", 0)
        isPlaying = sharedPreferences.getBoolean("is_playing", false)

        // Reanudar la música si estaba reproduciéndose antes de minimizar
        if (isPlaying) {
            playMusic()
        }
    }


}
