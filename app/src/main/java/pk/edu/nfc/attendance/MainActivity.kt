package pk.edu.nfc.attendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation(vm: MainViewModel = viewModel()) {
    val user by vm.currentUser.collectAsState()

    if (user == null) {
        LoginScreen(vm)
    } else {
        when (user!!.role) {
            "ADMIN" -> AdminDashboard(vm, user!!)
            "STUDENT" -> StudentDashboard(vm, user!!)
            "TEACHER" -> TeacherDashboard(vm, user!!)
            "HOD" -> HodDashboard(vm, user!!)
        }
    }
}

@Composable
fun LoginScreen(vm: MainViewModel) {
    val admins by vm.allAdmins.collectAsState()
    val teachers by vm.allTeachers.collectAsState()
    val students by vm.allStudents.collectAsState()
    val hods by vm.allHods.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(30.dp))
            Text("NFC-IEFR Attendance", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F4E78))
            Text("Select an identity to test offline role", color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))

            Text("System Administration", fontWeight = FontWeight.Bold, color = Color(0xFFB02A37))
            admins.forEach { u ->
                Button(
                    onClick = { vm.login(u) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB02A37))
                ) {
                    Text("Login as ${u.name} (Admin)")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("HOD Portal", fontWeight = FontWeight.Bold, color = Color(0xFF198754))
            hods.forEach { u ->
                Button(
                    onClick = { vm.login(u) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754))
                ) {
                    Text("Login as ${u.name} (HOD)")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Faculty / Teachers", fontWeight = FontWeight.Bold)
            teachers.forEach { u ->
                Button(
                    onClick = { vm.login(u) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F4E78))
                ) {
                    Text("Login as ${u.name} (Teacher)")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Enrolled Students", fontWeight = FontWeight.Bold)
            students.forEach { u ->
                Button(
                    onClick = { vm.login(u) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0DCAF0))
                ) {
                    Text("Login as ${u.name} [${u.rollNumber}]")
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun AdminDashboard(vm: MainViewModel, user: User) {
    var showStudentDialog by remember { mutableStateOf(false) }
    var showTeacherDialog by remember { mutableStateOf(false) }
    var showClassDialog by remember { mutableStateOf(false) }

    val students by vm.allStudents.collectAsState()
    val teachers by vm.allTeachers.collectAsState()
    val classes by vm.allClasses.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Header(user.name, "Administrator Control Center", vm)

        Spacer(Modifier.height(16.dp))
        Text("Create Institutional Records", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showStudentDialog = true }, modifier = Modifier.weight(1f)) {
                Text("+ Student", fontSize = 12.sp)
            }
            Button(onClick = { showTeacherDialog = true }, modifier = Modifier.weight(1f)) {
                Text("+ Faculty", fontSize = 12.sp)
            }
            Button(onClick = { showClassDialog = true }, modifier = Modifier.weight(1f)) {
                Text("+ Course", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Live System Inventory", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("• Active Students: ${students.size}")
                Text("• Active Faculty Members: ${teachers.size}")
                Text("• Total Scheduled Course Slots: ${classes.size}")
            }
        }

        Text("Course Catalog", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(classes) { cls ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(cls.subjectName, fontWeight = FontWeight.Bold)
                            Text("Slot: ${cls.timeSlot} | Dept: ${cls.department}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showStudentDialog) {
        var name by remember { mutableStateOf("") }
        var roll by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("Computer Science") }

        AlertDialog(
            onDismissRequest = { showStudentDialog = false },
            title = { Text("Add New Student") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                    OutlinedTextField(value = roll, onValueChange = { roll = it }, label = { Text("Roll Number (e.g. 26-CS-05)") })
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && roll.isNotBlank()) {
                        vm.createStudent(name.trim(), roll.trim(), dept.trim())
                        showStudentDialog = false
                    }
                }) { Text("Save Student") }
            },
            dismissButton = { TextButton(onClick = { showStudentDialog = false }) { Text("Cancel") } }
        )
    }

    if (showTeacherDialog) {
        var name by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("Computer Science") }

        AlertDialog(
            onDismissRequest = { showTeacherDialog = false },
            title = { Text("Add Faculty Member") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Faculty Full Name") })
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        vm.createTeacher(name.trim(), dept.trim())
                        showTeacherDialog = false
                    }
                }) { Text("Save Faculty") }
            },
            dismissButton = { TextButton(onClick = { showTeacherDialog = false }) { Text("Cancel") } }
        )
    }

    if (showClassDialog) {
        var subject by remember { mutableStateOf("") }
        var slot by remember { mutableStateOf("08:30 - 09:30") }
        val teachersList by vm.allTeachers.collectAsState()
        var selectedTeacherId by remember { mutableStateOf(teachersList.firstOrNull()?.id ?: 0) }

        AlertDialog(
            onDismissRequest = { showClassDialog = false },
            title = { Text("Schedule New Course") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Course Title") })
                    OutlinedTextField(value = slot, onValueChange = { slot = it }, label = { Text("Time Slot (e.g. 11:30 - 12:30)") })
                    Text("Select Teacher:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    teachersList.forEach { t ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedTeacherId == t.id,
                                onClick = { selectedTeacherId = t.id }
                            )
                            Text(t.name)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (subject.isNotBlank() && selectedTeacherId != 0) {
                        vm.createClass(subject.trim(), selectedTeacherId, slot.trim(), "Computer Science")
                        showClassDialog = false
                    }
                }) { Text("Schedule Class") }
            },
            dismissButton = { TextButton(onClick = { showClassDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun StudentDashboard(vm: MainViewModel, user: User) {
    val presentCount by vm.getStudentPresentCount(user.id).collectAsState(0)
    val totalCount by vm.getStudentTotalCount(user.id).collectAsState(0)
    val percentage = if (totalCount > 0) (presentCount.toFloat() / totalCount) * 100 else 100f
    val isEligible = percentage >= 75f

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Header(user.name, "Student Profile (${user.rollNumber})", vm)

        Card(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Overall Attendance", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Department: ${user.department}", color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text("Total Classes Held: $totalCount", fontSize = 16.sp)
                Text("Total Classes Attended: $presentCount", fontSize = 16.sp)
                Spacer(Modifier.height(16.dp))

                Text(
                    "${percentage.toInt()}%",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEligible) Color(0xFF198754) else Color(0xFFDC3545)
                )
                Text(
                    if (isEligible) "ELIGIBLE FOR EXAMINATIONS" else "SHORT ATTENDANCE DETENTION WARNING",
                    color = if (isEligible) Color(0xFF198754) else Color(0xFFDC3545),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TeacherDashboard(vm: MainViewModel, user: User) {
    val classes by vm.getTeacherClasses(user.id).collectAsState(emptyList())
    var selectedClass by remember { mutableStateOf<ClassSession?>(null) }

    if (selectedClass != null) {
        TakeAttendanceScreen(vm, selectedClass!!) { selectedClass = null }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Header(user.name, "Faculty Workspace", vm)
            Text("Scheduled Classes", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))

            if (classes.isEmpty()) {
                Text("No classes scheduled yet. Ask the Administrator to assign a course.", color = Color.Gray)
            }

            LazyColumn {
                items(classes) { cls ->
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { if (!cls.isCompleted) selectedClass = cls },
                        colors = CardDefaults.cardColors(containerColor = if (cls.isCompleted) Color(0xFFE8F5E9) else Color.White)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(cls.subjectName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(cls.timeSlot, color = Color.Gray)
                            if (cls.isCompleted) {
                                Text("Attendance Submitted", color = Color(0xFF198754), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            } else {
                                Text("Tap to Mark Attendance", color = Color(0xFF0DCAF0), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TakeAttendanceScreen(vm: MainViewModel, cls: ClassSession, onBack: () -> Unit) {
    val students by vm.allStudents.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }
    val results = remember { mutableMapOf<Int, String>() }

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(cls.subjectName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Slot: ${cls.timeSlot}", color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))

        if (students.isEmpty()) {
            Text("No students enrolled yet. Go to Admin to add students.", color = Color.Red)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) { Text("Back") }
        } else if (currentIndex < students.size) {
            val s = students[currentIndex]
            Card(
                Modifier.fillMaxWidth().padding(16.dp).height(200.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.name, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Roll No: ${s.rollNumber}", fontSize = 18.sp, color = Color(0xFF1F4E78))
                }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Button(
                    onClick = { results[s.id] = "ABSENT"; currentIndex++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                    modifier = Modifier.weight(1f).height(55.dp).padding(end = 8.dp)
                ) {
                    Text("ABSENT", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { results[s.id] = "PRESENT"; currentIndex++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754)),
                    modifier = Modifier.weight(1f).height(55.dp).padding(start = 8.dp)
                ) {
                    Text("PRESENT", fontWeight = FontWeight.Bold)
                }
            }
            Text("Student ${currentIndex + 1} of ${students.size}", modifier = Modifier.padding(top = 16.dp), color = Color.Gray)
        } else {
            Text("Attendance Session Complete!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF198754))
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.submitAttendance(cls.id, results); onBack() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Save to Database")
            }
        }
    }
}

@Composable
fun HodDashboard(vm: MainViewModel, user: User) {
    val allClasses by vm.allClasses.collectAsState()
    val completed = allClasses.count { it.isCompleted }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Header(user.name, "HOD Monitoring Dashboard", vm)

        Card(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Department Session Status", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Total Scheduled Classes: ${allClasses.size}", fontSize = 16.sp)
                Text("Conducted / Submitted: $completed", fontSize = 16.sp, color = Color(0xFF198754), fontWeight = FontWeight.Bold)
                Text("Pending Submissions: ${allClasses.size - completed}", fontSize = 16.sp, color = Color(0xFFDC3545))
            }
        }
    }
}

@Composable
fun Header(name: String, title: String, vm: MainViewModel) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Welcome, $name", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1F4E78))
            Text(title, color = Color.Gray, fontSize = 13.sp)
        }
        Button(
            onClick = { vm.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Logout", fontSize = 12.sp)
        }
    }
}
