package pk.edu.nfc.attendance

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- ENTITIES ---
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // "ADMIN", "STUDENT", "TEACHER", "HOD"
    val department: String,
    val rollNumber: String = "" // For students
)

@Entity(tableName = "classes")
data class ClassSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val teacherId: Int,
    val timeSlot: String,
    val department: String = "CS",
    val isCompleted: Boolean = false
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val classId: Int,
    val studentId: Int,
    val status: String // "PRESENT", "ABSENT"
)

// --- DAO ---
@Dao
interface AppDao {
    @Query("SELECT * FROM users WHERE role = :role ORDER BY id DESC")
    fun getUsersByRole(role: String): Flow<List<User>>

    @Query("SELECT * FROM classes WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getClassesForTeacher(teacherId: Int): Flow<List<ClassSession>>

    @Query("SELECT * FROM classes ORDER BY id DESC")
    fun getAllClasses(): Flow<List<ClassSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classSession: ClassSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<ClassSession>)

    @Insert
    suspend fun insertAttendance(records: List<AttendanceRecord>)

    @Query("UPDATE classes SET isCompleted = 1 WHERE id = :classId")
    suspend fun markClassCompleted(classId: Int)

    @Query("SELECT COUNT(*) FROM attendance_records WHERE studentId = :studentId AND status = 'PRESENT'")
    fun getStudentPresentCount(studentId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE studentId = :studentId")
    fun getStudentTotalClasses(studentId: Int): Flow<Int>
}

// --- DATABASE ---
@Database(entities = [User::class, ClassSession::class, AttendanceRecord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nfc_standalone_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
