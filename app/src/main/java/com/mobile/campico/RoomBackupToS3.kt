package com.mobile.campico

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.encoding.Base64

// to recover the backup
// base64 -d ~/Downloads/campico_2026-02-12-13-19.db > campico.db
fun checkpointDatabase(db: CampicoDatabase) {
    val query = SimpleSQLiteQuery("pragma wal_checkpoint(full)")
    db.campicoDao().checkpoint(query)
// Assuming you have a DAO with the checkpoint method
}

fun exportRoomDatabase(
    appContext: Context,
    scope: CoroutineScope,
    databaseName: String,
    networkService: NetworkService,
    changeMessage: (String) -> Unit
) {

    scope.launch {
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        val token = preferences[TOKEN] ?: ""
        val email = preferences[EMAIL] ?: ""
        if (email == "") {
            changeMessage("Please, log-in first")
        } else {
            val db = CampicoDatabase.getDatabase(appContext)
            changeMessage("Backing up flashcard database in S3...")
            scope.launch {
                withContext(Dispatchers.IO) {
                    checkpointDatabase(db)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    val dbFile = appContext.getDatabasePath(databaseName)
                    val walPath = File(dbFile.parent, "$databaseName-wal")
                    val shmPath = File(dbFile.parent, "$databaseName-shm")
                    try {
                        // Copy the main database file
                        FileInputStream(dbFile).use { inputStream ->
                            inputStream.copyTo(byteArrayOutputStream)
                        }

                        // Optionally copy WAL and SHM files if they exist and are not empty
                        if (walPath.exists() && walPath.length() > 0) {
                            FileInputStream(walPath).use { inputStream ->
                                inputStream.copyTo(byteArrayOutputStream)
                            }
                        }
                        if (shmPath.exists() && shmPath.length() > 0) {
                            FileInputStream(shmPath).use { inputStream ->
                                inputStream.copyTo(byteArrayOutputStream)
                            }
                        }
                        val byteArray = byteArrayOutputStream.toByteArray()
                        val encodedString = Base64.encode(byteArray)


                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
                        val current = LocalDateTime.now().format(formatter)
                        val key = "campico_$current.db"

                        val response = networkService.uploadBackupDB(
                            backup = UploadBackupRequest(
                                key = key,
                                content = encodedString,
                                token = token,
                                email = email
                            )
                        )

                        changeMessage(response.message)
                       // Log.d("FLASHCARD", response.message)


                    } catch (e: Exception) {
                        changeMessage("Unexpected exception: Database cannot back up in S3")
                        Log.d("FLASHCARD", "EXCEPTION: $e")

                    }
                }
            }
        }
    }

}