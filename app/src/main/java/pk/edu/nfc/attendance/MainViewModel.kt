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
    val allAdmins = dao.getUsersByRole("ADMIN").stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allClasses = dao.getAllClasses().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            val admins = dao.getUsersByRole("ADMIN").first()
            if (admins.isEmpty()) {
                // Pre-seed System Administrator
                dao.insertUser(User(name = "System Admin", role = "ADMIN", department = "All"))
                
                // Pre-seed Core Staff and Students
                val teacherId = dao.insertUser(User(name = "Dr. Usman", role = "TEACHER", department = "Computer Science")).toInt()
                dao.insertUser(User(name = "Prof. Tariq", role = "HOD", department = "Computer Science"))
                dao.insertUser(User(name = "Ali Raza", role = "STUDENT", department = "Computer Science", rollNumber = "26-CS-01"))
                dao.insertUser(User(name = "Sara Khan", role = "STUDENT", department = "Computer Science", rollNumber = "26-CS-02"))

                // Pre-seed Starter Classes
                dao.insertClass(ClassSession(subjectName = "Programming Fundamentals", teacherId = teacherId, timeSlot = "08:30 - 09:30"))
                dao.insertClass(ClassSession(subjectName = "Data Structures & Algorithms", teacherId = teacherId, timeSlot = "10:30 - 11:30"))
            }
        }
    }

    fun login(user: User) { currentUser.value = user }
    fun logout() { currentUser.value = null }

    // Admin Creation Handlers
    fun createStudent(name: String, roll: String, dept: String) {
        viewModelScope.launch {
            dao.insertUser(User(name = name, role = "STUDENT", department = dept, rollNumber = roll))
        }
    }

    fun createTeacher(name: String, dept: String) {
        viewModelScope.launch {
            dao.insertUser(User(name = name, role = "TEACHER", department = dept))
        }
    }

    fun createClass(subject: String, teacherId: Int, slot: String, dept: String) {
        viewModelScope.launch {
            dao.insertClass(ClassSession(subjectName = subject, teacherId = teacherId, timeSlot = slot, department = dept))
        }
    }

    // Role-specific Data
    fun getTeacherClasses(teacherId: Int) = dao.getClassesForTeacher(teacherId)
    fun getStudentPresentCount(studentId: Int) = dao.getStudentPresentCount(studentId)
    fun getStudentTotalCount(studentId: Int) = dao.getStudentTotalClasses(studentId)

    fun submitAttendance(classId: Int, records: Map<Int, String>) {
        viewModelScope.launch {
            val entities = records.map { 
                AttendanceRecord(classId = classId, studentId = it.key, status = it.value) 
            }
            dao.insertAttendance(entities)
            dao.markClassCompleted(classId)
        }
    }
}
