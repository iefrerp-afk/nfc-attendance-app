package pk.edu.nfc.attendance.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pk.edu.nfc.attendance.data.local.*
import pk.edu.nfc.attendance.ui.MainViewModel
import pk.edu.nfc.attendance.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboard(vm: MainViewModel, teacher: UserEntity) {
    val teacherClasses by vm.repository.getTeacherClasses(teacher.id).collectAsState(initial = emptyList())
    val students by vm.allStudents.collectAsState()
    var activeClassForAttendance by remember { mutableStateOf<TimetableEntity?>(null) }
    val coroutineScope = rememberCoroutineScope()

    if (activeClassForAttendance != null) {
        MarkAttendanceScreen(
            timetable = activeClassForAttendance!!,
            students = students,
            onDismiss = { activeClassForAttendance = null },
            onSubmit = { records ->
                coroutineScope.launch {
                    vm.repository.recordAttendance(activeClassForAttendance!!.id, teacher.id, records)
                    activeClassForAttendance = null
                }
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Faculty Workspace - ${teacher.fullName}", fontSize = 18.sp) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Today's Teaching Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(teacherClasses) { session ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeClassForAttendance = session },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("CS-101 (Theory)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("Section ${session.section} | Slot: ${session.startTime} - ${session.endTime}", fontSize = 12.sp, color = Color.Gray)
                                }
                                Button(
                                    onClick = { activeClassForAttendance = session },
                                    colors = ButtonDefaults.buttonColors(containerColor = NfcBluePrimary)
                                ) {
                                    Text("Mark")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    timetable: TimetableEntity,
    students: List<UserEntity>,
    onDismiss: () -> Unit,
    onSubmit: (Map<String, AttendanceStatus>) -> Unit
) {
    val attendanceMap = remember {
        mutableStateMapOf<String, AttendanceStatus>().apply {
            students.forEach { put(it.id, AttendanceStatus.PRESENT) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Take Attendance - Sec ${timetable.section}") },
                actions = {
                    TextButton(onClick = {
                        students.forEach { attendanceMap[it.id] = AttendanceStatus.PRESENT }
                    }) {
                        Text("All Present", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NfcBluePrimary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { onSubmit(attendanceMap) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPresentGreen)
                ) {
                    Text("Submit Register (${attendanceMap.values.count { it == AttendanceStatus.PRESENT }} Present)", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(students) { std ->
                val currentStatus = attendanceMap[std.id] ?: AttendanceStatus.PRESENT
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(std.fullName, fontWeight = FontWeight.Bold)
                            Text(std.identifierCode, fontSize = 11.sp, color = Color.Gray)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { attendanceMap[std.id] = AttendanceStatus.ABSENT },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (currentStatus == AttendanceStatus.ABSENT) StatusAbsentRed else Color.LightGray.copy(alpha = 0.3f)
                                )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Absent", tint = if (currentStatus == AttendanceStatus.ABSENT) Color.White else Color.Black)
                            }
                            IconButton(
                                onClick = { attendanceMap[std.id] = AttendanceStatus.PRESENT },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (currentStatus == AttendanceStatus.PRESENT) StatusPresentGreen else Color.LightGray.copy(alpha = 0.3f)
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Present", tint = if (currentStatus == AttendanceStatus.PRESENT) Color.White else Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
