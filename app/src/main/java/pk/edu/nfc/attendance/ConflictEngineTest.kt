package pk.edu.nfc.attendance

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.*
import pk.edu.nfc.attendance.data.local.*
import pk.edu.nfc.attendance.domain.ConflictResult
import pk.edu.nfc.attendance.domain.TimetableConflictEngine

class ConflictEngineTest {

    @Test
    fun `test teacher overlap raises conflict`() = runBlocking {
        val mockDao = mock(AppDao::class.java)
        val engine = TimetableConflictEngine(mockDao)

        `when`(mockDao.getOverlappingEntries("MONDAY", "08:30", "09:30")).thenReturn(
            listOf(
                TimetableEntity(
                    "tt_1", "MONDAY", "08:30", "09:30", "tch_1", "sub_1", "room_1", "prog_1", "A", 1, ClassType.THEORY
                )
            )
        )

        val result = engine.validateSlot("MONDAY", "08:30", "09:30", "tch_1", "room_2", "prog_1", "B")
        assertTrue(result is ConflictResult.Conflict)
    }
}
