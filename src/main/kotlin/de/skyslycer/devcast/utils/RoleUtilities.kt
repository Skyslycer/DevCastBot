package de.skyslycer.devcast.utils

import dev.kord.core.entity.Member
import dev.kord.core.entity.Role

class RoleUtilities {

    companion object {
        fun hasRole(member: Member, role: Role): Boolean {
            return member.roleIds.contains(role.id)
        }
    }

}