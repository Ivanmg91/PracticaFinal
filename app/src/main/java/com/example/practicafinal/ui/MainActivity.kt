package com.example.practicafinal.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.practicafinal.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Asegúrate de que el layout contenga el `FragmentContainerView`

        // Cargar el TaskListFragment al iniciar la actividad
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TaskListFragment()) // Reemplaza "fragmentContainer" con el ID del contenedor del fragmento
                .commit()
        }
    }
}
