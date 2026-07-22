package com.example.pattern.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.pattern.domain.repository.HabitRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupData = repository.getBackupData()
            val json = gson.toJson(backupData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            } ?: return@withContext Result.failure(Exception("Failed to open output stream"))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
