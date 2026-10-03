package pk.edu.nfc.attendance.domain

import pk.edu.nfc.attendance.data.local.AppDao
import pk.edu.nfc.attendance.data.local.TimetableEntity

sealed class ConflictResult {
    data object Success : ConflictResult()
    data class Conflict(val reason: String) : ConflictResult()
}

class TimetableConflictEngine(private val dao: AppDao) {
    suspend fun validateSlot(
        day: String,
        startTime: String,
        endTime: String,
        teacherId: String,
        roomId: String,
        programId: String,
        section: String
    ): ConflictResult {
        val overlaps = dao.getOverlappingEntries(day, startTime, endTime)

        // Check 1: Teacher Conflict
        val teacherClash = overlaps.find { it.teacherId == teacherId }
        if (teacherClash != null) {
            return ConflictResult.Conflict("Teacher already has a lecture scheduled from ${teacherClash.startTime} to ${teacherClash.endTime}.")
        }

        // Check 2: Room Conflict
        val roomClash = overlaps.find { it.roomId == roomId }
        if (roomClash != null) {
            return ConflictResult.Conflict("Room is already occupied by another class from ${roomClash.startTime} to ${roomClash.endTime}.")
        }

        // Check 3: Student Cohort / Section Conflict
        val groupClash = overlaps.find { it.programId == programId && it.section == section }
        if (groupClash != null) {
            return ConflictResult.Conflict("Section $section already has an active lecture slot in this timeframe.")
        }

        return ConflictResult.Success
    }
}
