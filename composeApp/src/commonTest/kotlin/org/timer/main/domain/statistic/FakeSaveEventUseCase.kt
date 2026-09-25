package org.timer.main.domain.statistic

class FakeSaveEventUseCase : SaveEventUseCase {
    var savedEvent: Pair<EventType, Map<String, Any>>? = null
    override suspend fun invoke(type: EventType, meta: Map<String, Any>) {
        savedEvent = Pair(type, meta)
    }
}