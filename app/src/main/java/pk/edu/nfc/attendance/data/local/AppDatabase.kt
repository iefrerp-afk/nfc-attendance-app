package pk.edu.nfc.attendance.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        DepartmentEntity::class,
        ProgramEntity::class,
        AcademicSessionEntity::class,
        SubjectEntity::class,
        RoomEntity::class,
        TimetableEntity::class,
        AttendanceDraftEntity::class,
        AttendanceRecordEntity::class,
        NoticeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nfc_iefr_master.db"
                )
                .addCallback(DatabaseSeederCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseSeederCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateBaselineInstitutionalData(database.appDao())
                }
            }
        }

        suspend fun populateBaselineInstitutionalData(dao: AppDao) {
            // Seed Root Admin
            dao.insertUser(
                UserEntity("usr_admin", "admin", "System Administrator", UserRole.ADMIN, "dept_all", "ADMIN-001", "admin@nfc-iefr.edu.pk")
            )
            // Seed HOD
            dao.insertUser(
                UserEntity("usr_hod_cs", "hod_cs", "Prof. Dr. Tariq Bashir", UserRole.HOD, "dept_cs", "EMP-HOD-01", "tariq@nfc-iefr.edu.pk")
            )
            // Seed Teacher
            dao.insertUser(
                UserEntity("usr_tch_usman", "dr_usman", "Dr. Muhammad Usman", UserRole.TEACHER, "dept_cs", "EMP-CS-102", "usman@nfc-iefr.edu.pk")
            )
            // Seed Students
            dao.insertUser(
                UserEntity("usr_std_ali", "ali_raza", "Ali Raza", UserRole.STUDENT, "dept_cs", "2026-NFC-CS-001", "ali@nfc-iefr.edu.pk")
            )
            dao.insertUser(
                UserEntity("usr_std_sara", "sara_khan", "Sara Khan", UserRole.STUDENT, "dept_cs", "2026-NFC-CS-002", "sara@nfc-iefr.edu.pk")
            )
            dao.insertUser(
                UserEntity("usr_std_bilal", "bilal_ahmed", "Bilal Ahmed", UserRole.STUDENT, "dept_cs", "2026-NFC-CS-003", "bilal@nfc-iefr.edu.pk")
            )

            // Seed Department
            dao.insertDepartment(DepartmentEntity("dept_cs", "Computer Science", "CS", "usr_hod_cs"))
            dao.insertDepartment(DepartmentEntity("dept_ee", "Electrical Engineering", "EE", null))

            // Seed Subjects
            dao.insertSubject(SubjectEntity("sub_cs101", "CS-101", "Programming Fundamentals", 3, 0, 1))
            dao.insertSubject(SubjectEntity("sub_cs101l", "CS-101L", "Programming Fundamentals Lab", 0, 1, 1))

            // Seed Rooms
            dao.insertRoom(RoomEntity("room_cr101", "CR-101", "CS Block", 50, false))
            dao.insertRoom(RoomEntity("room_lab2", "Lab-2", "IT Complex", 40, true))

            // Seed Timetable Entry (Monday 08:30)
            dao.insertTimetableEntry(
                TimetableEntity(
                    "tt_001", "MONDAY", "08:30", "09:30", "usr_tch_usman", "sub_cs101", "room_cr101", "prog_bscs", "A", 1, ClassType.THEORY
                )
            )

            // Seed Notice
            dao.insertNotice(
                NoticeEntity(
                    "not_001", "Spring 2026 Semester Guidelines", "All students must maintain 75% minimum attendance to sit in midterms.", "2026-10-03", "ALL"
                )
            )
        }
    }
}
