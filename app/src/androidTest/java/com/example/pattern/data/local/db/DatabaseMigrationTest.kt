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
 * Specifically, it checks the version 1 to 2 transition where columns were renamed
 * from camelCase to snake_case in the 'settings_table'.
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
    fun migrate1To2() {
        // 1. Create database in version 1
        helper.createDatabase(TEST_DB, 1).apply {
            // settings_table in version 1 has camelCase columns (based on schema 1.json)
            execSQL("INSERT INTO settings_table (id, quietHoursEnabled, startTime, endTime, totalXP) VALUES (0, 1, '22:00', '08:00', 500)")
            close()
        }

        // 2. Run migration to version 2 and validate schema
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true)

        // 3. Verify data integrity and column renaming
        val cursor = db.query("SELECT * FROM settings_table WHERE id = 0")
        assert(cursor.moveToFirst())
        
        // In version 2, columns are snake_case as per our @ColumnInfo and @RenameColumn
        val quietHoursEnabledIdx = cursor.getColumnIndex("quiet_hours_enabled")
        val startTimeIdx = cursor.getColumnIndex("start_time")
        val endTimeIdx = cursor.getColumnIndex("end_time")
        val totalXpIdx = cursor.getColumnIndex("total_xp")
        
        // Verify values are preserved
        assert(cursor.getInt(quietHoursEnabledIdx) == 1)
        assert(cursor.getString(startTimeIdx) == "22:00")
        assert(cursor.getString(endTimeIdx) == "08:00")
        assert(cursor.getInt(totalXpIdx) == 500)
        
        cursor.close()
    }
}
