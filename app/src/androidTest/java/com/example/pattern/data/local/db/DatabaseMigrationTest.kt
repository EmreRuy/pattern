package com.example.pattern.data.local.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Staff Engineer Pillar 3: Migration Testing
 * This test validates that migrations work correctly and that no user data is lost.
 * We cover major schema shifts including renames, column deletions, and new feature toggles.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDataBase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Test a full migration from 1 to 6
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO settings_table (id, quietHoursEnabled, startTime, endTime, totalXP) VALUES (0, 1, '22:00', '08:00', 500)")
            close()
        }
        
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true)
        
        val cursor = db.query("SELECT * FROM settings_table WHERE id = 0")
        assert(cursor.moveToFirst())
        assert(cursor.getInt(cursor.getColumnIndex("is_premium")) == 0) // Default value check
        assert(cursor.getInt(cursor.getColumnIndex("total_xp")) == 500)
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO settings_table (id, quietHoursEnabled, startTime, endTime, totalXP) VALUES (0, 1, '22:00', '08:00', 500)")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true)

        val cursor = db.query("SELECT * FROM settings_table WHERE id = 0")
        assert(cursor.moveToFirst())
        
        assert(cursor.getInt(cursor.getColumnIndex("quiet_hours_enabled")) == 1)
        assert(cursor.getString(cursor.getColumnIndex("start_time")) == "22:00")
        assert(cursor.getString(cursor.getColumnIndex("end_time")) == "08:00")
        assert(cursor.getInt(cursor.getColumnIndex("total_xp")) == 500)
        
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        // Version 4 had 'accumulated_time_ms' in habits table. Version 5 deletes it.
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL("""
                INSERT INTO habits (id, name, type, selected_days, icon_code, is_completed, created_at, accent_color_hex, accumulated_time_ms) 
                VALUES (1, 'Exercise', 'BUILD', '1,1,1,1,1,1,1', 'icon', 0, 123456789, '#FFFFFF', 5000)
            """)
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true)
        
        val cursor = db.query("SELECT * FROM habits WHERE id = 1")
        assert(cursor.moveToFirst())
        assert(cursor.getString(cursor.getColumnIndex("name")) == "Exercise")
        
        // Verify columns are gone
        assert(cursor.getColumnIndex("accumulated_time_ms") == -1)
        assert(cursor.getColumnIndex("active_session_start_ms") == -1)
        
        cursor.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrate5To6() {
        // Version 6 adds 'is_premium' with default 0
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL("INSERT INTO settings_table (id, quiet_hours_enabled, start_time, end_time, total_xp) VALUES (0, 0, '00:00', '00:00', 0)")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true)
        
        val cursor = db.query("SELECT * FROM settings_table WHERE id = 0")
        assert(cursor.moveToFirst())
        assert(cursor.getInt(cursor.getColumnIndex("is_premium")) == 0)
        cursor.close()
    }
}
