package pk.edu.nfc.attendance
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
}
