package com.example.pattern.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.pattern.data.model.BackupDto
import com.example.pattern.domain.repository.HabitRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject

class ImportBackupUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val backupData = gson.fromJson(reader, BackupDto::class.java)
                    if (backupData == null) {
                        return@withContext Result.failure(Exception("Invalid backup file"))
                    }
                    repository.restoreBackupData(backupData)
                }
            } ?: return@withContext Result.failure(Exception("Failed to open input stream"))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
