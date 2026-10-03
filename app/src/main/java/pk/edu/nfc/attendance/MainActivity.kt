package pk.edu.nfc.attendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pk.edu.nfc.attendance.data.local.UserRole
import pk.edu.nfc.attendance.ui.MainViewModel
import pk.edu.nfc.attendance.ui.screens.*
import pk.edu.nfc.attendance.ui.theme.NfcAttendanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NfcAttendanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavigationHost()
                }
            }
        }
    }
}

@Composable
fun RootNavigationHost(vm: MainViewModel = viewModel()) {
    val currentUser by vm.currentUser.collectAsState()

    if (currentUser == null) {
        LoginScreen(vm)
    } else {
        when (currentUser!!.role) {
            UserRole.STUDENT -> StudentDashboard(vm, currentUser!!)
            UserRole.TEACHER -> TeacherDashboard(vm, currentUser!!)
            UserRole.HOD -> HodDashboard(vm, currentUser!!)
            UserRole.ADMIN -> AdminDashboard(vm)
        }
    }
}
