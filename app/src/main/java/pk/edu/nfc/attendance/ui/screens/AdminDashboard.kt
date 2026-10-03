package pk.edu.nfc.attendance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.edu.nfc.attendance.domain.ConflictResult
import pk.edu.nfc.attendance.ui.MainViewModel
import pk.edu.nfc.attendance.ui.theme.NfcBluePrimary

@Composable
fun AdminDashboard(vm: MainViewModel) {
    val context = LocalContext.current
    var showStudentDialog by remember { mutableStateOf(false) }
    var showFacultyDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    val students by vm.allStudents.collectAsState()
    val teachers by vm.allTeachers.collectAsState()
    val depts by vm.allDepartments.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Institutional Control Panel") },
                actions = {
                    TextButton(onClick = { vm.logout() }) {
                        Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NfcBluePrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Operational Management", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showStudentDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NfcBluePrimary)
                    ) {
                        Text("+ Student", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { showFacultyDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NfcBluePrimary)
                    ) {
                        Text("+ Faculty", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { showScheduleDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NfcBluePrimary)
                    ) {
                        Text("+ Class", fontSize = 12.sp)
                    }
                }
            }

            item {
                Text("System Statistics", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("• Registered Students: ${students.size}", fontWeight = FontWeight.Medium)
                        Text("• Registered Faculty: ${teachers.size}", fontWeight = FontWeight.Medium)
                        Text("• Academic Departments: ${depts.size}", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // --- Dialog 1: Add Student ---
        if (showStudentDialog) {
            var name by remember { mutableStateOf("") }
            var roll by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showStudentDialog = false },
                title = { Text("Add Student Record") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                        OutlinedTextField(value = roll, onValueChange = { roll = it }, label = { Text("Roll No (e.g. 26-CS-99)") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (name.isNotBlank() && roll.isNotBlank()) {
                            vm.createStudent(name, roll, "dept_cs")
                            showStudentDialog = false
                        }
                    }) { Text("Save") }
                },
                dismissButton = { TextButton(onClick = { showStudentDialog = false }) { Text("Cancel") } }
            )
        }

        // --- Dialog 2: Add Faculty ---
        if (showFacultyDialog) {
            var name by remember { mutableStateOf("") }
            var empId by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showFacultyDialog = false },
                title = { Text("Add Faculty Member") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Teacher Name") })
                        OutlinedTextField(value = empId, onValueChange = { empId = it }, label = { Text("Employee ID") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (name.isNotBlank() && empId.isNotBlank()) {
                            vm.createFaculty(name, empId, "dept_cs")
                            showFacultyDialog = false
                        }
                    }) { Text("Save") }
                },
                dismissButton = { TextButton(onClick = { showFacultyDialog = false }) { Text("Cancel") } }
            )
        }

        // --- Dialog 3: Schedule with Conflict Detection ---
        if (showScheduleDialog) {
            var day by remember { mutableStateOf("MONDAY") }
            var start by remember { mutableStateOf("08:30") }
            var end by remember { mutableStateOf("09:30") }
            var conflictError by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showScheduleDialog = false },
                title = { Text("Schedule Lecture Slot") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Day of Week") })
                        OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start Time (HH:MM)") })
                        OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End Time (HH:MM)") })

                        if (conflictError != null) {
                            Text(conflictError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        vm.scheduleClassWithConflictCheck(
                            day = day,
                            startTime = start,
                            endTime = end,
                            teacherId = teachers.firstOrNull()?.id ?: "usr_tch_usman",
                            subjectId = "sub_cs101",
                            roomId = "room_cr101",
                            section = "A"
                        ) { result ->
                            when (result) {
                                is ConflictResult.Success -> {
                                    Toast.makeText(context, "Class scheduled cleanly!", Toast.LENGTH_SHORT).show()
                                    showScheduleDialog = false
                                }
                                is ConflictResult.Conflict -> {
                                    conflictError = result.reason
                                }
                            }
                        }
                    }) { Text("Validate & Save") }
                },
                dismissButton = { TextButton(onClick = { showScheduleDialog = false }) { Text("Cancel") } }
            )
        }
    }
}
