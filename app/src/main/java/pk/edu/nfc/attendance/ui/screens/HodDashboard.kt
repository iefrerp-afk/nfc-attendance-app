package pk.edu.nfc.attendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.edu.nfc.attendance.data.local.UserEntity
import pk.edu.nfc.attendance.ui.MainViewModel
import pk.edu.nfc.attendance.ui.theme.NfcBluePrimary
import pk.edu.nfc.attendance.ui.theme.StatusPresentGreen

@Composable
fun HodDashboard(vm: MainViewModel, hod: UserEntity) {
    val students by vm.allStudents.collectAsState()
    val teachers by vm.allTeachers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HOD Portal — Computer Science", fontSize = 18.sp) },
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
                Text("Department Academic Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Enrolled Students", fontSize = 12.sp, color = Color.Gray)
                            Text("${students.size}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = NfcBluePrimary)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Active Faculty", fontSize = 12.sp, color = Color.Gray)
                            Text("${teachers.size}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = StatusPresentGreen)
                        }
                    }
                }
            }

            item {
                Text("Department Faculty Roster", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            items(teachers.size) { idx ->
                val teacher = teachers[idx]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(teacher.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("ID: ${teacher.identifierCode} | Email: ${teacher.email}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
