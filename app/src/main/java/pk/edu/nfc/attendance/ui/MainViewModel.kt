package pk.edu.nfc.attendance.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pk.edu.nfc.attendance.NfcAttendanceApp
import pk.edu.nfc.attendance.data.local.*
import pk.edu.nfc.attendance.data.repository.AttendanceRepository
import pk.edu.nfc.attendance.domain.ConflictResult
import pk.edu.nfc.attendance.domain.TimetableConflictEngine
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as NfcAttendanceApp).database.appDao()
    val repository = AttendanceRepository(dao)
    private val conflictEngine = TimetableConflictEngine(dao)

    // Current Session
    val currentUser = MutableStateFlow<UserEntity?>(null)
    val loginError = MutableStateFlow<String?>(null)

    // Global Collections
    val allStudents = dao.getUsersByRole(UserRole.STUDENT).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allTeachers = dao.getUsersByRole(UserRole.TEACHER).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allDepartments = dao.getAllDepartments().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allSubjects = dao.getAllSubjects().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allRooms = dao.getAllRooms().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun login(credential: String) {
        viewModelScope.launch {
            val user = dao.findUserByCredentials(credential.trim())
            if (user != null) {
                currentUser.value = user
                loginError.value = null
            } else {
                loginError.value = "Invalid Registration No, Employee ID or Username."
            }
        }
    }

    fun logout() {
        currentUser.value = null
    }

    // Admin Operations
    fun createStudent(name: String, roll: String, deptId: String) {
        viewModelScope.launch {
            dao.insertUser(
                UserEntity(
                    id = "std_${UUID.randomUUID()}",
                    username = roll.lowercase().replace("-", "_"),
                    fullName = name,
                    role = UserRole.STUDENT,
                    departmentId = deptId,
                    identifierCode = roll,
                    email = "$roll@nfc-iefr.edu.pk"
                )
            )
        }
    }

    fun createFaculty(name: String, empId: String, deptId: String) {
        viewModelScope.launch {
            dao.insertUser(
                UserEntity(
                    id = "tch_${UUID.randomUUID()}",
                    username = empId.lowercase().replace("-", "_"),
                    fullName = name,
                    role = UserRole.TEACHER,
                    departmentId = deptId,
                    identifierCode = empId,
                    email = "$empId@nfc-iefr.edu.pk"
                )
            )
        }
    }

    fun scheduleClassWithConflictCheck(
        day: String,
        startTime: String,
        endTime: String,
        teacherId: String,
        subjectId: String,
        roomId: String,
        section: String,
        onResult: (ConflictResult) -> Unit
    ) {
        viewModelScope.launch {
            val check = conflictEngine.validateSlot(
                day = day,
                startTime = startTime,
                endTime = endTime,
                teacherId = teacherId,
                roomId = roomId,
                programId = "prog_bscs",
                section = section
            )
            if (check is ConflictResult.Success) {
                dao.insertTimetableEntry(
                    TimetableEntity(
                        id = "tt_${UUID.randomUUID()}",
                        dayOfWeek = day,
                        startTime = startTime,
                        endTime = endTime,
                        teacherId = teacherId,
                        subjectId = subjectId,
                        roomId = roomId,
                        programId = "prog_bscs",
                        section = section,
                        semesterNumber = 1,
                        classType = ClassType.THEORY
                    )
                )
            }
            onResult(check)
        }
    }
}
