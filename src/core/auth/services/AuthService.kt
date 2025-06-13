package core.auth.services

import core.auth.schemas.SignInData
import core.auth.schemas.SignUpData
import core.user.schemas.NewUserData
import core.user.repositories.AbstractUserRepo
import core.user.schemas.BaseUser
import core.user.schemas.StudentData
import core.user.schemas.UserRole
import core.user.schemas.UserStatus
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

    fun signUp(): StudentData? {
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
        val student: StudentData = userRepo.createStudentUser(newUserData)
        println("New User(${newUserData.firstName} ${newUserData.lastName}) created.")
        return student
    }

    private fun getSignInDataFromUser(): SignInData {
        print("Enter email : ")
        val email = readln().trim()
        print("Enter password : ")
        val password = readln().trim()
        return SignInData(email, password)
    }

    fun signIn(): BaseUser? {
        val signInData = getSignInDataFromUser()
        // Get User
        val userData: BaseUser? = userRepo.getUserByEmail(signInData.email)
        if(userData == null){
            println("User with email(${signInData.email}) is not found!!!")
            return null // TODO: Throw appropriate exception
        }

        // Validate Password
        if (!PasswordHasher.checkPasswordMatch(signInData.password,userData.hashPassword)) {
            println("User with email(${signInData.email}) password is not matched!!!")
            return null // TODO: Throw appropriate exception
        }

        if (userData.status == UserStatus.SUSPENDED) {
            println("User(${userData.id}) account is suspended!!!")
            return null // TODO: Throw appropriate exception
        }
        userRepo.updateLastLogin(userData.id, LocalDateTime.now())

        return userData
    }

    fun logOut(userId: UUID): Boolean {
        return true
    }
}