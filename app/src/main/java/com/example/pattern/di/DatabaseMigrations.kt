package com.example.pattern.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE habits ADD COLUMN accentColorHex TEXT NOT NULL DEFAULT '#1E88E5'"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE habits ADD COLUMN timerStartTime INTEGER"
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE habits ADD COLUMN timerPauseTime INTEGER"
        )
    }
}
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS habit_daily_state (
                habitId INTEGER NOT NULL,
                date TEXT NOT NULL,
                timerStartTime INTEGER,
                timerPauseTime INTEGER,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(habitId, date)
            )
            """.trimIndent()
        )
    }
}