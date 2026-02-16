package com.kipita

import com.google.common.truth.Truth.assertThat
import com.kipita.data.local.TripMessageDao
import com.kipita.data.local.TripMessageEntity
import com.kipita.data.repository.TripChatRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TripChatRepositoryTest {
    @Test
    fun `participant limit allows up to 10`() {
        val dao = mockk<TripMessageDao>()
        val repo = TripChatRepository(dao)
        repo.enforceParticipantLimit((1..10).map { "$it" })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `participant limit rejects 11`() {
        val dao = mockk<TripMessageDao>()
        val repo = TripChatRepository(dao)
        repo.enforceParticipantLimit((1..11).map { "$it" })
    }

    @Test
    fun `send and load maps domain`() = runTest {
        val dao = mockk<TripMessageDao>()
        val repo = TripChatRepository(dao)
        coEvery { dao.upsert(any()) } returns Unit
        coEvery { dao.getByTrip("trip") } returns listOf(
            TripMessageEntity("1", "trip", "u1", "User", "hi", 1L, false)
        )

        repo.sendMessage("trip", "u1", "User", "hi")
        val messages = repo.getMessages("trip")
        assertThat(messages).hasSize(1)
    }
}
