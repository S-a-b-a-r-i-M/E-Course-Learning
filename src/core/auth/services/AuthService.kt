package core.auth.services

import core.auth.schemas.SignInData
import core.auth.schemas.SignUpData
import core.user.schemas.NewUserData
import core.user.repositories.AbstractUserRepo
import db.UserRole
import db.UserStatus
import db.inmemorystore.user.User
import utils.PasswordHasher
import java.time.LocalDateTime
import java.util.UUID

class AuthService (val userRepo: AbstractUserRepo) {
    private fun getSignUpDataFromUser(): SignUpData {
        print("Enter first name : ")
        val firstName = readln().trim()
        print("Enter last name : ")
        val lastName = readln().trim()
        print("Enter email : ")
        val email = readln().trim()
        print("Enter password : ")
        val password1 = readln().trim()
        print("Enter password again : ")
        val password2 = readln().trim()

        if (password1 != password2) {
            println("Password didn't match.")
            throw IllegalArgumentException("password didn't match") // TODO: invoke related error
        }

        return SignUpData(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password1,
        )
    }

    fun signUp(): User? {
        val signUpData = getSignUpDataFromUser()
        // Check email uniqueness
        if (userRepo.isEmailExists(signUpData.email)){
            println("AuthService(signUp): Email Already exists!!!")
            return null // TODO: Throw appropriate exception
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
        val user: User? = userRepo.createUser(newUserData)
        println("New User(${newUserData.firstName} ${newUserData.lastName}) creation result is ${user != null}")
        return user
    }

    private fun getSignInDataFromUser(): SignInData {
        print("Enter email : ")
        val email = readln().trim()
        print("Enter password : ")
        val password = readln().trim()
        return SignInData(email, password)
    }

    fun signIn(): User? {
        val signInData = getSignInDataFromUser()
        // Get User
        val user: User? = userRepo.getUserByEmail(signInData.email)
        if(user == null){
            println("User with email(${signInData.email}) is not found!!!")
            return null // TODO: Throw appropriate exception
        }

        // Validate Password
        if (!PasswordHasher.checkPasswordMatch(signInData.password,user.getUserHashedPassword())) {
            println("User with email(${signInData.email}) password is not matched!!!")
            return null // TODO: Throw appropriate exception
        }

        if (user.getUserStatus() == UserStatus.SUSPENDED) {
            println("User(${user.getUserId()}) account is suspended!!!")
            return null // TODO: Throw appropriate exception
        }
        userRepo.updateLastLogin(user.getUserId(), LocalDateTime.now())

        return user
    }

    fun logOut(userId: UUID): Boolean {
        return true
    }
}