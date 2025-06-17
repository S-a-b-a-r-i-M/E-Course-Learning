package core.auth.services

import core.auth.schemas.SignInData
import core.auth.schemas.SignUpData
import core.course.schemas.CURRENT_FILE_NAME
import core.user.schemas.NewUserData
import core.user.repositories.AbstractUserRepo
import core.user.schemas.BaseUser
import core.user.schemas.StudentData
import core.user.schemas.UserRole
import core.user.schemas.UserStatus
import utils.PasswordHasher
import utils.InputValidator
import java.time.LocalDateTime
import java.util.UUID

/**
 * Manages user authentication processes, including sign-up, sign-in, and sign-out.
 *
 * @property userRepo The repository for accessing and managing user data.
 */
class AuthService (val userRepo: AbstractUserRepo) {
    /**
     * Interactively prompts the user to enter their details for registration.
     *
     * @return A [SignUpData] object containing the user's input.
     */
    private fun getSignUpDataFromUser(): SignUpData {
        print("Enter first name (min 2 characters): ")
        val firstName = InputValidator.validateName(readln(), "first name", 2)
        print("Enter last name (min 1 character): ")
        val lastName = InputValidator.validateName(readln(), "last name", 1)
        print("Enter email : ")
        val email = InputValidator.validateEmailFormat(readln().trim())
        print("Enter password (min 8 chars, must include: alphabets, digit, special char): ")
        val password1: String = readln().trim()
        InputValidator.validatePassword(password1)
        print("Enter password again : ")
        val password2: String = readln().trim()
        // Validate Passwords Match
        InputValidator.validatePassWordMatch(password1, password2)

        return SignUpData(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password1,
        )
    }

    /**
     * Handles the complete user sign-up process for new students.
     *
     * It gathers user input, validates that the email is not already in use,
     * hashes the password, and creates a new student user in the system.
     *
     * @return The newly created [StudentData] object on success, or `null` if the
     * email address already exists.
     */
    fun signUp(): StudentData? {
        var signUpData: SignUpData? = null
        repeat(3) {
            try {
                signUpData = getSignUpDataFromUser()
                // Check email uniqueness
                if (userRepo.isEmailExists(signUpData.email)){
                    println("AuthService(signUp): Email Already exists!!!")
                    return null
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
            } catch (exp: Exception) {
                println("Err:{${exp.message}}")
                println("Try again....\n")
            }
        }

        println("Too many attempts, aborting...")
        return null
    }

    /**
     * Prompts the user to enter their email and password for logging in.
     *
     * @return A [SignInData] object with the entered credentials.
     */
    private fun getSignInDataFromUser(): SignInData {
        print("Enter email : ")
        val email = readln().trim()
        print("Enter password : ")
        val password = readln().trim()
        return SignInData(email, password)
    }

    /**
     * Authenticates a user based on their email and password.
     *
     * It retrieves user credentials and validates it,
     * On success, it updates the user's last login time.
     *
     * @return The authenticated [BaseUser] object on success. Returns `null` if the user
     * is not found or the password is incorrect, or the account is suspended.
     */
    fun signIn(): BaseUser? {
        val signInData = getSignInDataFromUser()
        // Get User
        val userData: BaseUser? = userRepo.getUserByEmail(signInData.email)
        if(userData == null){
            println("User with email(${signInData.email}) is not found!!!")
            return null
        }

        // Validate Password
        if (!PasswordHasher.checkPasswordMatch(signInData.password,userData.hashPassword)) {
            println("User with email(${signInData.email}) password is not matched!!!")
            return null
        }

        if (userData.status == UserStatus.SUSPENDED) {
            println("User(${userData.id}) account is suspended!!!")
            return null
        }

        return userData
    }

    /**
     * Logs a user out of the system.
     *
     * @param userId The unique identifier of the user to log out.
     * @return Always returns `true` in the current implementation.
     */
    fun logOut(userId: UUID): Boolean {
        return true
    }
}