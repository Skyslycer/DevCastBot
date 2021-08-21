package de.skyslycer.devcast.utils

import de.skyslycer.devcast.database.AlternativeUser
import dev.kord.common.entity.Snowflake
import java.util.regex.Pattern

class UserUtilities {

    companion object {
        private val userMention: Pattern = Pattern.compile("<@!?[0-9]{18,19}?>", Pattern.MULTILINE)

        fun matchesMention(string: String): Long? {
            val matcher = userMention.matcher(string)

            return if (matcher.find()) {
                matcher.group(1).toLong()
            } else {
                null
            }
        }

        fun usersFromString(string: String): MutableList<AlternativeUser> {
            val users = mutableListOf<AlternativeUser>()

            val splits = if (string.contains(", ")) {
                string.split(", ")
            } else if (string.contains(",")) {
                string.split(",")
            } else {
                mutableListOf(string)
            }

            for (split in splits) {
                val newSplit = split.trim()

                val matchesMention = matchesMention(newSplit)

                users.add(
                    AlternativeUser(
                        if (matchesMention != null)
                            Snowflake(matchesMention)
                        else
                            null,
                        newSplit
                    )
                )
            }

            return users
        }
    }

}