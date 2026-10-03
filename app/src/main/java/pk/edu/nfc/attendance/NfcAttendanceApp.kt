package pk.edu.nfc.attendance

import android.app.Application
import pk.edu.nfc.attendance.data.local.AppDatabase

class NfcAttendanceApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
