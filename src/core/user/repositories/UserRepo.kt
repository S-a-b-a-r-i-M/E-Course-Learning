package core.user.repositories

import core.user.models.NewUserModel
import core.user.models.UserModel

class UserRepo : AbstractUserRepo {

    fun isEmailExists(email: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun createUser(newUserData: NewUserModel): Boolean {
        TODO("Not yet implemented")
    }

    override fun getPasswordByEmail(email: String): String? {
        TODO("Not yet implemented")
    }

    override fun getUserByEmail(email: String): UserModel {
        TODO("Not yet implemented")
    }

    override fun updateUserLastLogin() {
        TODO("Not yet implemented")
    }

}