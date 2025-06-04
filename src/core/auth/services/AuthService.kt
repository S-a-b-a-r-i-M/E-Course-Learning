package core.auth.services

import core.auth.schemas.LogOutModel
import core.auth.schemas.SignInData
import core.auth.schemas.SignUpData
import core.user.schemas.NewUserData
import core.user.schemas.UserUpdateData
import core.user.repositories.AbstractUserRepo
import db.UserRole
import db.UserStatus
import db.inmemorystore.User
import utils.PasswordHasher
import java.time.LocalDateTime

class AuthService (val userRepo: AbstractUserRepo) {
    fun signUp(signUpData: SignUpData): Boolean {
        // Check email is already exists
        if (userRepo.isEmailExists(signUpData.email)){
            println("AuthService(signUp): Email Already exists!!!")
            return false // TODO: Throw appropriate exception
        }

        // Create User
        val hashedPassword = PasswordHasher.getHashPassword(signUpData.password)
        val newUserData = NewUserData(
            firstName = signUpData.firstName,
            lastName = signUpData.lastName,
            email = signUpData.email,
            hashedPassword = hashedPassword,
            role = UserRole.STUDENT // Student only can sign up directly
        )
        val result = userRepo.createUser(newUserData)
        println("New User(${newUserData.email}) creation result is $result")
        return result
    }

    fun signIn(signInData: SignInData): User? {
        val user: User? = userRepo.getUserByEmail(signInData.email)
        if(user == null){
            println("User with email(${signInData.email}) is not found!!!")
            return null // TODO: Throw appropriate exception
        }

        // Validate Password
        if (!PasswordHasher.checkPasswordMatch(signInData.password,user.hashPassword)) {
            println("User with email(${signInData.email}) password is not matched!!!")
            return null // TODO: Throw appropriate exception
        }

        if (user.status == UserStatus.SUSPENDED) {
            println("User(${user.id}) account is suspended!!!")
            return null // TODO: Throw appropriate exception
        }
        userRepo.updateUser(user.id, UserUpdateData(lastLoginAt = LocalDateTime.now()))

        return user
    }

    fun logOut(logOutData: LogOutModel): Boolean {
        return true
    }
}