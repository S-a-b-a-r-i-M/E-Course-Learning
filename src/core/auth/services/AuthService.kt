package core.auth.services

import core.auth.models.LogOutModel
import core.auth.models.SignInModel
import core.auth.models.SignUpModel
import core.user.models.NewUserModel
import core.user.models.UserModel
import core.user.repositories.UserRepo
import db.tables.UserRole
import db.tables.UserStatus
import utils.PasswordHasher

class AuthService (val userRepo: UserRepo) {
    fun signUp(signUpData: SignUpModel): Boolean {
        // Check email is already exists or not
        if (userRepo.isEmailExists(signUpData.email)){
            println("AuthService(signUp): Email Already exists!!!")
            return false // TODO: Throw appropriate exception
        }

        // Create User
        val hashedPassword = PasswordHasher.getHashPassword(signUpData.password)
        val newUserData = NewUserModel(
            firstName = signUpData.firstName,
            lastName = signUpData.lastName,
            email = signUpData.email,
            hashedPassword = hashedPassword,
            role = UserRole.STUDENT // Student only can sign up directly
        )
        val result = userRepo.createUser(newUserData)
        println("New User(${newUserData.email} creation result is $result")
        return result
    }

    fun signIn(signInData: SignInModel): UserModel? {
        val hashedPassword = userRepo.getPasswordByEmail(signInData.email)
        if(hashedPassword == null){
            println("User with email(${signInData.email} is not found!!!")
            return null // TODO: Throw appropriate exception
        }

        // Check Password match
        if (!PasswordHasher.checkPasswordMatch(signInData.password,hashedPassword)) {
            println("User with email(${signInData.email} password is not matched!!!")
            return null // TODO: Throw appropriate exception
        }

        // Get User from db
        val user = userRepo.getUserByEmail(signInData.email)
        if (user.status == UserStatus.SUSPENDED) {
            println("User(${user.userId} account is suspended!!!")
            return null // TODO: Throw appropriate exception
        }
        userRepo.updateUserLastLogin()

        return user
    }

    fun logOut(logOutData: LogOutModel): Boolean {
        return true
    }

    fun forgetPassword(){
        TODO("Need to confirm")
    }

    fun resetPassword(){
        TODO("Need to confirm")
    }
}