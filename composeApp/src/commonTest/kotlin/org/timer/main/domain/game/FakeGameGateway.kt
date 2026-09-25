package org.timer.main.domain.game

import kotlinx.coroutines.flow.*
import org.timer.main.domain.game.*

class FakeGameGateway() : GameGateway {
    private var fillingProgress = MutableStateFlow(0)
    private var feedDates = MutableStateFlow(emptyList<Long>())
    private var fishNumber = MutableStateFlow(0)


    override fun getBowlFillingProgress(): Flow<Int> {
        return fillingProgress
    }

    override suspend fun setProgress(i: Int) {
        fillingProgress.emit(i)
    }

    override suspend fun resetProgress() {
        fillingProgress.emit(0)
    }

    override fun getKittyFeedDates(): Flow<List<Long>> {
        return feedDates
    }

    override suspend fun saveKittyFeedDate(date: Long) {
        val newList = mutableListOf<Long>()
        val longs = feedDates.value + date
        longs.forEach { newList.add(it) }
      //  logging().d { newList  }
        feedDates.emit(newList)
    }

    override fun getLastResetTime(): Flow<Long> {
        return flow { emit(0L) }
    }

    override suspend fun updateLastResetTime(now: Long) {

    }

    override fun getFishNumber(): Flow<Int> {
        return fishNumber
    }

    override suspend fun setFishNumber(fishNumber: Int) {
        this.fishNumber.emit(fishNumber)
    }

}
