package com.fakhry.pomodojo.focus.data.repository

import com.fakhry.pomodojo.focus.data.db.FocusSessionDao
import com.fakhry.pomodojo.focus.data.db.PomoDojoRoomDatabase
import com.fakhry.pomodojo.focus.data.mapper.toDomain
import com.fakhry.pomodojo.focus.data.mapper.toEntity
import com.fakhry.pomodojo.focus.data.mapper.toHourSplitEntities
import com.fakhry.pomodojo.focus.data.mapper.toSegmentEntities
import com.fakhry.pomodojo.focus.domain.model.PomodoroSessionDomain
import com.fakhry.pomodojo.focus.domain.repository.ActiveSessionRepository

class ActiveSessionRepositoryImpl(
    private val focusDao: FocusSessionDao,
) : ActiveSessionRepository {
    constructor(database: PomoDojoRoomDatabase) : this(focusDao = database.focusSessionDao())

    override suspend fun getActiveSession(): PomodoroSessionDomain {
        val snapshot = focusDao.getActiveSessionWithRelations()
            ?: throw IllegalStateException("No active session stored in database.")
        return snapshot.toDomain()
    }

    override suspend fun saveActiveSession(snapshot: PomodoroSessionDomain) {
        focusDao.clearActiveSession()
        persistSnapshot(snapshot, sessionIdOverride = null)
    }

    override suspend fun updateActiveSession(snapshot: PomodoroSessionDomain) {
        val existingId = focusDao.getActiveSessionId()
        persistSnapshot(snapshot, sessionIdOverride = existingId)
    }

    override suspend fun completeSession(snapshot: PomodoroSessionDomain) {
        focusDao.clearActiveSession()
    }

    override suspend fun clearActiveSession() {
        focusDao.clearActiveSession()
    }

    override suspend fun hasActiveSession(): Boolean = focusDao.hasActiveSession()

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
