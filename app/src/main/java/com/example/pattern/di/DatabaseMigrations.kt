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