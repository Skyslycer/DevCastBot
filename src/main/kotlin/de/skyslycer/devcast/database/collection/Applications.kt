package de.skyslycer.devcast.database.collection

import de.skyslycer.devcast.database.Application
import java.util.concurrent.ConcurrentHashMap

class Applications : ConcurrentHashMap<String, Application>()