package com.mobile.campico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.mobile.campico.ui.theme.CampicoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            //val db = Room.databaseBuilder(
            //    applicationContext,
            //    CampicoDatabase::class.java, "CampicoDatabase"
            //).build()

            val db =
                Room.databaseBuilder(
                    applicationContext,
                    CampicoDatabase::class.java, "CampicoDatabase"
                )
                    .fallbackToDestructiveMigration(true)
                    //.addMigrations(MIGRATION_1_2) // Add the migration here
                .build()

            val campicoDao = db.campicoDao()
            CampicoTheme {
                Navigator(
                        navController = navController,
                        dao = campicoDao
                )
            }
        }
    }
}


