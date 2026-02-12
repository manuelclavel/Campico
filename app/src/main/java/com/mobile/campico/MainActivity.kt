package com.mobile.campico

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.mobile.campico.ui.theme.CampicoTheme




//The Preferences DataStore implementation uses the DataStore and Preferences classes to persist key-value pairs to disk.
//Use the property delegate created by preferencesDataStore to create an instance of DataStore<Preferences>.
//Call it once at the top level of your Kotlin file. Access DataStore through this property
//throughout the rest of your application. This makes it easier to keep your DataStore as a singleton.
val Context.dataStore by preferencesDataStore(
    name = "user_credentials"
)

//Because Preferences DataStore doesn't use a predefined schema,
//you must use the corresponding key type function to define a key for each value that you need to store
//in the DataStore<Preferences> instance.
//For example, to define a key for an int value, use intPreferencesKey()
val TOKEN = stringPreferencesKey("token")
val EMAIL = stringPreferencesKey("email")

class MainActivity: ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val appContext = applicationContext
            //val db = Room.databaseBuilder(
            //    applicationContext,
            //    CampicoDatabase::class.java, "CampicoDatabase"
            //).build()

            // database
            val db = CampicoDatabase.getDatabase(appContext)
            //Room.databaseBuilder(
            //appContext,
            //FlashCardDatabase::class.java, "FlashCardDatabase"
            //).build()

            val campicoDao = db.campicoDao()

            // network
            // Create a single OkHttpClient instance
            val sharedOkHttpClient = OkHttpClient.Builder()
                .addInterceptor(NetworkConnectionInterceptor(appContext))
                .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout
                .readTimeout(30, TimeUnit.SECONDS)    // Set read timeout
                .build()

            // 2. Create the first Retrofit instance, using the shared OkHttpClient
            //.client(sharedOkHttpClient) // Pass the shared client

            // Retrofit requires a valid HttpUrl: The baseUrl() method of Retrofit.Builder expects an okhttp3.HttpUrl object.
            // This object represents a well-formed URL and requires a scheme (like "http" or "https"),
            // a host, and optionally a port and path. It cannot be null or an empty string.
            // You can use a placeholder or dummy URL, such as http://localhost/ or http://example.com/,
            // during the initial setup. This satisfies Retrofit's requirement for a valid base URL.

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("https://placeholder.com")
                .client(sharedOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            // Create an implementation of the API endpoints defined by the service interface.
            val networkService = retrofit.create(NetworkService::class.java)
            CampicoTheme {
                Navigator(
                    navController = navController,
                    dao = campicoDao,
                    networkService = networkService
                )
            }
        }
    }

}


