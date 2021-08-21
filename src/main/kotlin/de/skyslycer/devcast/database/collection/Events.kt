package de.skyslycer.devcast.database.collection

import de.skyslycer.devcast.database.Event
import java.util.concurrent.ConcurrentHashMap

class Events : ConcurrentHashMap<String, Event>()