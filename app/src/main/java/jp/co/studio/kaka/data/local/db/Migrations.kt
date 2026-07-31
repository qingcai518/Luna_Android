package jp.co.studio.kaka.data.local.db

import androidx.room.migration.Migration

/**
 * No migrations needed yet (schema is at version 1). When fields/tables change later, add a
 * `Migration(oldVersion, newVersion) { db -> db.execSQL(...) }` here - this is the formal Room
 * mechanism replacing iOS's "ALTER TABLE wrapped in try/catch and swallow the error" approach.
 * Never use fallbackToDestructiveMigration() as a substitute for writing a real migration.
 */
val MIGRATIONS: Array<Migration> = arrayOf()
