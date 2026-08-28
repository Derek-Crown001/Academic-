package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        SchoolUser::class,
        SchoolClass::class,
        SchoolSubject::class,
        CbtExam::class,
        CbtQuestion::class,
        CbtSubmission::class,
        SchoolAssignment::class,
        AssignmentSubmission::class,
        StudentGrade::class,
        ReportCard::class,
        SchoolAnnouncement::class,
        ChatRoom::class,
        ChatMessage::class,
        SchoolProfile::class,
        TeacherAttendance::class,
        StudentAttendanceRecord::class
    ],
    version = 6,
    exportSchema = false
)
abstract class SchoolDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: SchoolDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SchoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SchoolDatabase::class.java,
                    "secondary_school_portal_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
