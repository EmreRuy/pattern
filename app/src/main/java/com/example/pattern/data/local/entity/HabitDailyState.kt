package com.example.pattern.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Staff Engineer Refactoring:
 * 1. Timer Logic: Migrated to (Accumulated + ActiveStart) pattern. This is the industry standard
 *    for handling multi-session timers, ensuring no precision is lost during pause/resume cycles.
 * 2. Schema: Implemented explicit @ColumnInfo with snake_case naming. This decouples the
 *    Kotlin domain model from the persistence layer and follows SQL best practices.
 * 3. Domain Logic: Integrated 'isTimerRunning' and 'calculateTotalTimeMs' into the entity
 *    to promote a "Fat Domain Model" and keep ViewModels light and focused on state.
 */
@Entity(
    tableName = "habit_daily_state",
    primaryKeys = ["habit_id", "date"],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["habit_id"])]
)
data class HabitDailyState(
    @ColumnInfo(name = "habit_id")
    val habitId: Int,

    /** ISO-8601 date string (e.g., "2025-11-19") */
    @ColumnInfo(name = "date")
    val date: String,

    /** The sum of all completed timer sessions for this day in milliseconds. */
    @ColumnInfo(name = "accumulated_time_ms")
    val accumulatedTimeMs: Long = 0L,

    /** 
     * The timestamp (System.currentTimeMillis()) when the current session started.
     * If null, the timer is currently paused or stopped.
     */
    @ColumnInfo(name = "active_session_start_ms")
    val activeSessionStartMs: Long? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "is_task_completed")
    val isTaskCompleted: Boolean = false
) {
    /** 
     * Reactive state flag for the UI layer. 
     */
    val isTimerRunning: Boolean get() = activeSessionStartMs != null

    /**
     * Calculates the total time spent today. 
     * This logic is encapsulated here to ensure consistency across the entire app.
     * 
     * @param now The current system time. Defaults to System.currentTimeMillis().
     * @return Total time in milliseconds.
     */
    fun calculateTotalTimeMs(now: Long = System.currentTimeMillis()): Long {
        val currentSessionDuration = if (activeSessionStartMs != null) {
            (now - activeSessionStartMs).coerceAtLeast(0L)
        } else 0L
        return accumulatedTimeMs + currentSessionDuration
    }
}
