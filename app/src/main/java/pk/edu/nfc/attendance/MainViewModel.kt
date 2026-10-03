package pk.edu.nfc.attendance
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val currentUser = MutableStateFlow<User?>(null)
    val allStudents = dao.getUsersByRole("STUDENT").stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allTeachers = dao.getUsersByRole("TEACHER").stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allHods = dao.getUsersByRole("HOD").stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    init { viewModelScope.launch { if (dao.getUsersByRole("TEACHER").first().isEmpty()) {
        dao.insertUsers(listOf(User(1, "Ali Raza", "STUDENT", "CS"), User(2, "Sara Khan", "STUDENT", "CS"), User(3, "Dr. Usman", "TEACHER", "CS"), User(4, "Prof. Ahmed", "HOD", "CS")))
        dao.insertClasses(listOf(ClassSession(1, "Programming Fundamentals", 3, "08:30 - 09:30"), ClassSession(2, "Data Structures", 3, "10:30 - 11:30")))
    }}}
    fun login(user: User) { currentUser.value = user }
    fun logout() { currentUser.value = null }
    fun getTeacherClasses(teacherId: Int) = dao.getClassesForTeacher(teacherId)
    fun getAllClasses() = dao.getAllClasses()
    fun getStudentPresentCount(studentId: Int) = dao.getStudentPresentCount(studentId)
    fun getStudentTotalCount(studentId: Int) = dao.getStudentTotalClasses(studentId)
    fun submitAttendance(classId: Int, records: Map<Int, String>) = viewModelScope.launch {
        dao.insertAttendance(records.map { AttendanceRecord(classId = classId, studentId = it.key, status = it.value) })
        dao.markClassCompleted(classId)
    }
}
