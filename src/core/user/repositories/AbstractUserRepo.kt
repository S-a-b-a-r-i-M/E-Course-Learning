package core.user.repositories

import core.user.models.BaseUser
import core.user.models.NewUserModel
import core.user.models.UserModel

interface AbstractUserRepo {
    fun createUser(newUserData: NewUserModel): Boolean
    fun getPasswordByEmail(email: String): String?
    fun getUserByEmail(email: String): UserModel
    fun updateUserLastLogin()
}