package com.astrum.event

class EventBroadcaster : EventPublisher {
    private val eventPublishers = mutableListOf<EventPublisher>()

    fun use(eventPublisher: EventPublisher) {
        eventPublishers.add(eventPublisher)
    }

    override suspend fun <E : Any> publish(event: E) {
        println("EventBroadcaster.publish: $event")
        eventPublishers.forEach {
            it.publish(event)
        }
    }
}
