import os

files = {
    "settings.gradle.kts": """pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name = "NfcAttendance"
include(":app")""",

    "build.gradle.kts": """plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}""",

    "app/build.gradle.kts": """plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("com.google.devtools.ksp") }
android {
    namespace = "pk.edu.nfc.attendance"
    compileSdk = 34
    defaultConfig { applicationId = "pk.edu.nfc.attendance"; minSdk = 24; targetSdk = 34; versionCode = 1; versionName = "1.0" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
}""",

    "app/src/main/AndroidManifest.xml": """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application android:allowBackup="true" android:label="NFC Attendance" android:theme="@android:style/Theme.DeviceDefault.NoActionBar">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>""",

    "app/src/main/java/pk/edu/nfc/attendance/AppDatabase.kt": """package pk.edu.nfc.attendance
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
}""",

    "app/src/main/java/pk/edu/nfc/attendance/MainViewModel.kt": """package pk.edu.nfc.attendance
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
}""",

    "app/src/main/java/pk/edu/nfc/attendance/MainActivity.kt": """package pk.edu.nfc.attendance
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
    import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(color = MaterialTheme.colorScheme.background) { AppNavigation() } } }
    }
}
@Composable fun AppNavigation(vm: MainViewModel = viewModel()) {
    val user by vm.currentUser.collectAsState()
    if (user == null) LoginScreen(vm) else when (user!!.role) {
        "STUDENT" -> StudentDashboard(vm, user!!); "TEACHER" -> TeacherDashboard(vm, user!!); "HOD" -> HodDashboard(vm, user!!)
    }
}
@Composable fun LoginScreen(vm: MainViewModel) {
    val students by vm.allStudents.collectAsState(); val teachers by vm.allTeachers.collectAsState(); val hods by vm.allHods.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(50.dp)); Text("NFC Attendance App", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F4E78))
        Text("Select test user", color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp))
        teachers.forEach { u -> Button(onClick = { vm.login(u) }, modifier = Modifier.fillMaxWidth().padding(vertical=4.dp)) { Text("Login as ${u.name} (Teacher)") } }
        Spacer(Modifier.height(16.dp))
        students.forEach { u -> Button(onClick = { vm.login(u) }, modifier = Modifier.fillMaxWidth().padding(vertical=4.dp), colors = ButtonDefaults.buttonColors(Color(0xFF0DCAF0))) { Text("Login as ${u.name} (Student)") } }
        Spacer(Modifier.height(16.dp))
        hods.forEach { u -> Button(onClick = { vm.login(u) }, modifier = Modifier.fillMaxWidth().padding(vertical=4.dp), colors = ButtonDefaults.buttonColors(Color(0xFF198754))) { Text("Login as ${u.name} (HOD)") } }
    }
}
@Composable fun StudentDashboard(vm: MainViewModel, user: User) {
    val present by vm.getStudentPresentCount(user.id).collectAsState(0); val total by vm.getStudentTotalCount(user.id).collectAsState(0)
    val pct = if (total > 0) (present.toFloat() / total) * 100 else 100f; val eligible = pct >= 75f
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Header(user.name, "Student Dashboard", vm)
        Card(Modifier.fillMaxWidth().padding(vertical = 16.dp)) { Column(Modifier.padding(16.dp)) {
            Text("Attendance", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Attended: $present / $total", fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
            Text("${pct.toInt()}%", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = if (eligible) Color(0xFF198754) else Color.Red)
            Text(if (eligible) "Eligible" else "Warning!", color = if (eligible) Color(0xFF198754) else Color.Red)
        }}
    }
}
@Composable fun TeacherDashboard(vm: MainViewModel, user: User) {
    val classes by vm.getTeacherClasses(user.id).collectAsState(emptyList())
    var selectedClass by remember { mutableStateOf<ClassSession?>(null) }
    if (selectedClass != null) TakeAttendanceScreen(vm, selectedClass!!) { selectedClass = null }
    else { Column(Modifier.fillMaxSize().padding(16.dp)) {
        Header(user.name, "Teacher Dashboard", vm)
        LazyColumn { items(classes) { cls -> Card(Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { if (!cls.isCompleted) selectedClass = cls }) {
            Column(Modifier.padding(16.dp)) { Text(cls.subjectName, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(if (cls.isCompleted) "Submitted" else "Tap to Mark", color = if (cls.isCompleted) Color(0xFF198754) else Color(0xFF0DCAF0)) }
        }}}}
    }
}
@Composable fun TakeAttendanceScreen(vm: MainViewModel, cls: ClassSession, onBack: () -> Unit) {
    val students by vm.allStudents.collectAsState(); var current by remember { mutableStateOf(0) }; val results = remember { mutableMapOf<Int, String>() }
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(cls.subjectName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        if (current < students.size) { val s = students[current]
            Card(Modifier.fillMaxWidth().padding(16.dp).height(200.dp)) { Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text(s.name, fontSize = 28.sp) } }
            Row(Modifier.fillMaxWidth()) {
                Button(onClick = { results[s.id] = "ABSENT"; current++ }, colors = ButtonDefaults.buttonColors(Color.Red), modifier = Modifier.weight(1f).padding(8.dp)) { Text("ABSENT") }
                Button(onClick = { results[s.id] = "PRESENT"; current++ }, colors = ButtonDefaults.buttonColors(Color(0xFF198754)), modifier = Modifier.weight(1f).padding(8.dp)) { Text("PRESENT") }
            }
        } else { Button(onClick = { vm.submitAttendance(cls.id, results); onBack() }, modifier = Modifier.padding(top = 32.dp).fillMaxWidth()) { Text("Submit") } }
    }
}
@Composable fun HodDashboard(vm: MainViewModel, user: User) {
    val classes by vm.getAllClasses().collectAsState(emptyList()); val done = classes.count { it.isCompleted }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Header(user.name, "HOD Dashboard", vm)
        Card(Modifier.fillMaxWidth().padding(vertical = 16.dp)) { Column(Modifier.padding(16.dp)) { Text("Total Classes: ${classes.size}", fontSize = 16.sp); Text("Submitted: $done", color = Color(0xFF198754)) } }
    }
}
@Composable fun Header(name: String, title: String, vm: MainViewModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text(name, fontWeight = FontWeight.Bold); Text(title, color = Color.Gray) }; Button(onClick = { vm.logout() }, colors = ButtonDefaults.buttonColors(Color.DarkGray)) { Text("Logout") } }
}"""
}

for path, content in files.items():
    if os.path.dirname(path):
        os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content.strip() + "\n")
print("✅ Files generated successfully!")