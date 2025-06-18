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
import java.util.UUID

/**
 * Manages user authentication processes, including sign-up, sign-in, and sign-out.
 *
 * @property userRepo The repository for accessing and managing user data.
 */
class AuthService (val userRepo: AbstractUserRepo) {
    /**
     * Handles the complete user sign-up process for new students.
     *
     * It gathers user input, validates that the email is not already in use,
     * hashes the password, and creates a new student user in the system.
     *
     * @return The newly created [StudentData] object on success, or `null` if the
     * email address already exists.
     */
    fun signUp(signUpData: SignUpData): StudentData? {
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
    fun signIn(signInData: SignInData): BaseUser? {
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