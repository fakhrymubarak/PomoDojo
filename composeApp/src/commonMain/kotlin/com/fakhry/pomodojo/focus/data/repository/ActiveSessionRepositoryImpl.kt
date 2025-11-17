package com.fakhry.pomodojo.focus.data.repository

import com.fakhry.pomodojo.core.database.PomoDojoRoomDatabase
import com.fakhry.pomodojo.core.utils.kotlin.DispatcherProvider
import com.fakhry.pomodojo.focus.data.db.FocusSessionDao
import com.fakhry.pomodojo.focus.data.mapper.toDomain
import com.fakhry.pomodojo.focus.data.mapper.toEntity
import com.fakhry.pomodojo.focus.data.mapper.toHourSplitEntities
import com.fakhry.pomodojo.focus.data.mapper.toSegmentEntities
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.repository.ActiveSessionRepository
import kotlinx.coroutines.withContext

class ActiveSessionRepositoryImpl(
    private val focusDao: FocusSessionDao,
    private val dispatcher: DispatcherProvider,
) : ActiveSessionRepository {
    constructor(
        database: PomoDojoRoomDatabase,
        dispatcher: DispatcherProvider,
    ) : this(focusDao = database.focusSessionDao(), dispatcher = dispatcher)

    override suspend fun getActiveSession(): PomodoroSessionDomain = withContext(dispatcher.io) {
        val snapshot = focusDao.getActiveSessionWithRelations()
            ?: throw IllegalStateException("No active session stored in database.")
        return@withContext snapshot.toDomain()
    }

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) =
        withContext(dispatcher.io) {
            focusDao.clearActiveSession()
            persistSnapshot(snapshot, sessionIdOverride = null)
        }

    override suspend fun updateActiveSession(snapshot: PomodoroSessionDomain) =
        withContext(dispatcher.io) {
            val existingId = focusDao.getActiveSessionId()
            persistSnapshot(snapshot, sessionIdOverride = existingId)
        }

    override suspend fun completeSession(snapshot: PomodoroSessionDomain) =
        withContext(dispatcher.io) {
            focusDao.clearActiveSession()
        }

    override suspend fun clearActiveSession() = withContext(dispatcher.io) {
        focusDao.clearActiveSession()
    }

    override suspend fun hasActiveSession(): Boolean = withContext(dispatcher.io) {
        focusDao.hasActiveSession()
    }

    private suspend fun persistSnapshot(
        snapshot: PomodoroSessionDomain,
        sessionIdOverride: Long?,
    ) {
        val entity = snapshot.toEntity(sessionIdOverride)
        val upsertId = focusDao.upsertActiveSession(entity)
        val resolvedId = if (upsertId == -1L) entity.sessionId else upsertId
        focusDao.replaceSegments(resolvedId, snapshot.toSegmentEntities(resolvedId))
        focusDao.replaceHourSplits(resolvedId, snapshot.toHourSplitEntities(resolvedId))
    }
}
