package com.dicoding.eyesphere_nav.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val DATABASE_NAME = "eyesphere.db"
        private const val DATABASE_VERSION = 3 // Updated for recommendation column
        
        // Table name
        private const val TABLE_CLASSIFICATIONS = "classifications"
        
        // Column names
        private const val COLUMN_ID = "id"
        private const val COLUMN_IMAGE_PATH = "image_path"
        private const val COLUMN_CLASSIFICATION = "classification"
        private const val COLUMN_CONFIDENCE = "confidence"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_PROGRESS = "progress"
        private const val COLUMN_RECOMMENDATION = "recommendation"
        
        private const val TAG = "DatabaseHelper"
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_CLASSIFICATIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_IMAGE_PATH TEXT NOT NULL,
                $COLUMN_CLASSIFICATION TEXT NOT NULL DEFAULT 'Processing...',
                $COLUMN_CONFIDENCE REAL NOT NULL DEFAULT 0.0,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_TIME TEXT NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_STATUS TEXT NOT NULL DEFAULT 'PENDING',
                $COLUMN_PROGRESS INTEGER NOT NULL DEFAULT 0,
                $COLUMN_RECOMMENDATION TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent()
        
        try {
            db.execSQL(createTable)
            Log.d(TAG, "Database table created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating database table", e)
        }
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            when {
                oldVersion < 2 -> {
                    // Add new columns for version 2
                    db.execSQL("ALTER TABLE $TABLE_CLASSIFICATIONS ADD COLUMN $COLUMN_STATUS TEXT NOT NULL DEFAULT 'PENDING'")
                    db.execSQL("ALTER TABLE $TABLE_CLASSIFICATIONS ADD COLUMN $COLUMN_PROGRESS INTEGER NOT NULL DEFAULT 0")
                    Log.d(TAG, "Database upgraded to version 2: added status and progress columns")
                }
                oldVersion < 3 -> {
                    // Add recommendation column for version 3
                    db.execSQL("ALTER TABLE $TABLE_CLASSIFICATIONS ADD COLUMN $COLUMN_RECOMMENDATION TEXT NOT NULL DEFAULT ''")
                    Log.d(TAG, "Database upgraded to version 3: added recommendation column")
                }
            }
            Log.d(TAG, "Database upgraded from version $oldVersion to $newVersion")
        } catch (e: Exception) {
            Log.e(TAG, "Error upgrading database", e)
            // Fallback: recreate table
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CLASSIFICATIONS")
            onCreate(db)
        }
    }
    
    fun insertClassificationResult(result: ClassificationResult): Long {
        val db = writableDatabase
        
        // Optimize image path to store only filename if path is too long to avoid TransactionTooLargeException
        val optimizedImagePath = if (result.imagePath.length > 200) {
            java.io.File(result.imagePath).name
        } else {
            result.imagePath
        }
        
        val contentValues = ContentValues().apply {
            put(COLUMN_IMAGE_PATH, optimizedImagePath)
            put(COLUMN_CLASSIFICATION, result.classification)
            put(COLUMN_CONFIDENCE, result.confidence)
            put(COLUMN_DATE, result.date)
            put(COLUMN_TIME, result.time)
            put(COLUMN_TIMESTAMP, result.timestamp)
            put(COLUMN_STATUS, result.status.name)
            put(COLUMN_PROGRESS, result.progress)
            put(COLUMN_RECOMMENDATION, result.recommendation)
        }
        
        return try {
            val id = db.insert(TABLE_CLASSIFICATIONS, null, contentValues)
            Log.d(TAG, "Classification result inserted with ID: $id, path optimized: ${optimizedImagePath.length} chars")
            id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting classification result", e)
            -1
        } finally {
            db.close()
        }
    }
    
    fun getAllClassificationResults(): List<ClassificationResult> {
        val results = mutableListOf<ClassificationResult>()
        val db = readableDatabase
        
        try {
            val cursor = db.query(
                TABLE_CLASSIFICATIONS,
                null,
                null,
                null,
                null,
                null,
                "$COLUMN_TIMESTAMP DESC"
            )
            
            cursor.use {
                while (it.moveToNext()) {
                    val statusString = it.getString(it.getColumnIndexOrThrow(COLUMN_STATUS))
                    val status = try {
                        ProcessingStatus.valueOf(statusString)
                    } catch (e: Exception) {
                        ProcessingStatus.PENDING
                    }
                    
                    val result = ClassificationResult(
                        id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                        imagePath = it.getString(it.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)),
                        classification = it.getString(it.getColumnIndexOrThrow(COLUMN_CLASSIFICATION)),
                        confidence = it.getFloat(it.getColumnIndexOrThrow(COLUMN_CONFIDENCE)),
                        date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE)),
                        time = it.getString(it.getColumnIndexOrThrow(COLUMN_TIME)),
                        timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                        status = status,
                        progress = it.getInt(it.getColumnIndexOrThrow(COLUMN_PROGRESS)),
                        recommendation = it.getString(it.getColumnIndexOrThrow(COLUMN_RECOMMENDATION))
                    )
                    results.add(result)
                }
            }
            
            Log.d(TAG, "Retrieved ${results.size} classification results")
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving classification results", e)
        } finally {
            db.close()
        }
        
        return results
    }
    
    fun getClassificationResultById(id: Long): ClassificationResult? {
        val db = readableDatabase
        
        return try {
            val cursor = db.query(
                TABLE_CLASSIFICATIONS,
                null,
                "$COLUMN_ID = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )
            
            cursor.use {
                if (it.moveToFirst()) {
                    val statusString = it.getString(it.getColumnIndexOrThrow(COLUMN_STATUS))
                    val status = try {
                        ProcessingStatus.valueOf(statusString)
                    } catch (e: Exception) {
                        ProcessingStatus.PENDING
                    }
                    
                    ClassificationResult(
                        id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                        imagePath = it.getString(it.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)),
                        classification = it.getString(it.getColumnIndexOrThrow(COLUMN_CLASSIFICATION)),
                        confidence = it.getFloat(it.getColumnIndexOrThrow(COLUMN_CONFIDENCE)),
                        date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE)),
                        time = it.getString(it.getColumnIndexOrThrow(COLUMN_TIME)),
                        timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                        status = status,
                        progress = it.getInt(it.getColumnIndexOrThrow(COLUMN_PROGRESS)),
                        recommendation = it.getString(it.getColumnIndexOrThrow(COLUMN_RECOMMENDATION))
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving classification result by ID", e)
            null
        } finally {
            db.close()
        }
    }
    
    fun updateClassificationProgress(id: Long, progress: Int, status: ProcessingStatus = ProcessingStatus.PROCESSING): Boolean {
        val db = writableDatabase
        
        return try {
            val contentValues = ContentValues().apply {
                put(COLUMN_PROGRESS, progress)
                put(COLUMN_STATUS, status.name)
            }
            
            val rowsUpdated = db.update(TABLE_CLASSIFICATIONS, contentValues, "$COLUMN_ID = ?", arrayOf(id.toString()))
            Log.d(TAG, "Updated progress for ID $id: $progress% (${status.name}) - rows affected: $rowsUpdated")
            rowsUpdated > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error updating classification progress for ID $id", e)
            false
        } finally {
            db.close()
        }
    }
    
    fun updateClassificationResult(id: Long, classification: String, confidence: Float, status: ProcessingStatus = ProcessingStatus.COMPLETED): Boolean {
        val db = writableDatabase
        
        return try {
            val contentValues = ContentValues().apply {
                put(COLUMN_CLASSIFICATION, classification)
                put(COLUMN_CONFIDENCE, confidence)
                put(COLUMN_STATUS, status.name)
                put(COLUMN_PROGRESS, if (status == ProcessingStatus.COMPLETED) 100 else 0)
            }
            
            val rowsUpdated = db.update(TABLE_CLASSIFICATIONS, contentValues, "$COLUMN_ID = ?", arrayOf(id.toString()))
            Log.d(TAG, "Updated classification result for ID $id: $classification (${confidence}) - rows affected: $rowsUpdated")
            rowsUpdated > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error updating classification result for ID $id", e)
            false
        } finally {
            db.close()
        }
    }
    
    fun updateRecommendation(id: Long, recommendation: String): Boolean {
        val db = writableDatabase
        
        return try {
            val contentValues = ContentValues().apply {
                put(COLUMN_RECOMMENDATION, recommendation)
            }
            
            val rowsUpdated = db.update(TABLE_CLASSIFICATIONS, contentValues, "$COLUMN_ID = ?", arrayOf(id.toString()))
            Log.d(TAG, "Updated recommendation for ID $id - rows affected: $rowsUpdated")
            rowsUpdated > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error updating recommendation for ID $id", e)
            false
        } finally {
            db.close()
        }
    }
    
    fun getPendingClassifications(): List<ClassificationResult> {
        val results = mutableListOf<ClassificationResult>()
        val db = readableDatabase
        
        try {
            val cursor = db.query(
                TABLE_CLASSIFICATIONS,
                null,
                "$COLUMN_STATUS = ?",
                arrayOf(ProcessingStatus.PENDING.name),
                null,
                null,
                "$COLUMN_TIMESTAMP ASC" // Process in order
            )
            
            cursor.use {
                while (it.moveToNext()) {
                    val result = ClassificationResult(
                        id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                        imagePath = it.getString(it.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)),
                        classification = it.getString(it.getColumnIndexOrThrow(COLUMN_CLASSIFICATION)),
                        confidence = it.getFloat(it.getColumnIndexOrThrow(COLUMN_CONFIDENCE)),
                        date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE)),
                        time = it.getString(it.getColumnIndexOrThrow(COLUMN_TIME)),
                        timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                        status = ProcessingStatus.PENDING,
                        progress = it.getInt(it.getColumnIndexOrThrow(COLUMN_PROGRESS)),
                        recommendation = it.getString(it.getColumnIndexOrThrow(COLUMN_RECOMMENDATION))
                    )
                    results.add(result)
                }
            }
            
            Log.d(TAG, "Retrieved ${results.size} pending classification results")
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving pending classification results", e)
        } finally {
            db.close()
        }
        
        return results
    }
    
    fun deleteClassificationResult(id: Long): Boolean {
        val db = writableDatabase
        
        return try {
            val rowsDeleted = db.delete(TABLE_CLASSIFICATIONS, "$COLUMN_ID = ?", arrayOf(id.toString()))
            Log.d(TAG, "Deleted $rowsDeleted classification result(s)")
            rowsDeleted > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting classification result", e)
            false
        } finally {
            db.close()
        }
    }
}
