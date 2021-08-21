package de.skyslycer.devcast.database

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class AlternativeUser(
    val user: Snowflake?,
    val name: String
) {

    fun getAlternativeName(): String {
        return if (user == null) {
            name
        } else {
            "<@${user.asString}>"
        }
    }

}