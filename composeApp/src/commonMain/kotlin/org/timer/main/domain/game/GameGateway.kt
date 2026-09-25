package org.timer.main.domain.game

import kotlinx.coroutines.flow.*

interface GameGateway {
    fun getBowlFillingProgress(): Flow<Int>

    suspend fun setProgress(i: Int)

    suspend fun resetProgress()
    fun getKittyFeedDates(): Flow<List<Long>>
    suspend fun saveKittyFeedDate(date: Long)
    fun getLastResetTime(): Flow<Long>
    suspend fun updateLastResetTime(now: Long)
    fun getFishNumber(): Flow<Int>
    suspend fun setFishNumber(fishNumber: Int)

    companion object {
        const val TOTAL_PROGRESS = 4
    }
}
