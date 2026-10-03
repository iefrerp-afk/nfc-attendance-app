package pk.edu.nfc.attendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.edu.nfc.attendance.data.local.UserEntity
import pk.edu.nfc.attendance.ui.MainViewModel
import pk.edu.nfc.attendance.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(vm: MainViewModel, user: UserEntity) {
    val presentCount by vm.repository.getStudentPresentCount(user.id).collectAsState(initial = 0)
    val totalCount by vm.repository.getStudentTotalCount(user.id).collectAsState(initial = 0)
    val notices by vm.repository.getNotices().collectAsState(initial = emptyList())

    val percentage = if (totalCount > 0) (presentCount.toFloat() / totalCount.toFloat()) * 100f else 100f
    val isShortage = percentage < 75f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Welcome, ${user.fullName}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(user.identifierCode, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Attendance Ratio", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = (percentage / 100f).coerceIn(0f, 1f),
                                modifier = Modifier.size(110.dp),
                                strokeWidth = 10.dp,
                                color = if (isShortage) StatusAbsentRed else StatusPresentGreen,
                                trackColor = Color(0xFFE0E0E0)
                            )
                            Text(
                                text = "${percentage.toInt()}%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isShortage) StatusAbsentRed else StatusPresentGreen
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Attended: $presentCount / Total: $totalCount Classes", fontSize = 13.sp, color = Color.Gray)

                        if (isShortage) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .background(StatusAbsentRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusAbsentRed, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Shortage Alert: Below 75% Examination Threshold", color = StatusAbsentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Text("Campus Circulars & Notices", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            items(notices.size) { idx ->
                val notice = notices[idx]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NfcBluePrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(notice.body, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(6.dp))
                        Text("Date: ${notice.publishedDate}", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
