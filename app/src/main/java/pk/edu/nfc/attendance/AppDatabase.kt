package pk.edu.nfc.attendance
import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Entity(tableName = "users") data class User(@PrimaryKey(autoGenerate = true) val id: Int = 0, val name: String, val role: String, val department: String)
@Entity(tableName = "classes") data class ClassSession(@PrimaryKey(autoGenerate = true) val id: Int = 0, val subjectName: String, val teacherId: Int, val timeSlot: String, val isCompleted: Boolean = false)
@Entity(tableName = "attendance_records") data class AttendanceRecord(@PrimaryKey(autoGenerate = true) val id: Int = 0, val classId: Int, val studentId: Int, val status: String)
@Dao interface AppDao {
    @Query("SELECT * FROM users WHERE role = :role") fun getUsersByRole(role: String): Flow<List<User>>
    @Query("SELECT * FROM classes WHERE teacherId = :teacherId") fun getClassesForTeacher(teacherId: Int): Flow<List<ClassSession>>
    @Query("SELECT * FROM classes") fun getAllClasses(): Flow<List<ClassSession>>
    @Insert suspend fun insertUsers(users: List<User>)
    @Insert suspend fun insertClasses(classes: List<ClassSession>)
    @Insert suspend fun insertAttendance(records: List<AttendanceRecord>)
    @Query("UPDATE classes SET isCompleted = 1 WHERE id = :classId") suspend fun markClassCompleted(classId: Int)
    @Query("SELECT COUNT(*) FROM attendance_records WHERE studentId = :studentId AND status = 'PRESENT'") fun getStudentPresentCount(studentId: Int): Flow<Int>
    @Query("SELECT COUNT(*) FROM attendance_records WHERE studentId = :studentId") fun getStudentTotalClasses(studentId: Int): Flow<Int>
}
@Database(entities = [User::class, ClassSession::class, AttendanceRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) { Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "nfc_db").build().also { INSTANCE = it } }
    }
}
