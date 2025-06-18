package pages

import core.auth.schemas.SignInData
import core.auth.schemas.SignUpData
import core.auth.services.AuthService
import core.user.schemas.BaseUser
import core.user.schemas.NewUserData
import core.user.schemas.StudentData
import core.user.schemas.UserRole
import utils.InputValidator
import utils.PasswordHasher

class AuthPage (val authService: AuthService) {
    /**
     * Interactively prompts the user to enter their details for registration.
     *
     * @return A [SignUpData] object containing the user's input.
     */
    fun getSignUpDataFromUser(): SignUpData {
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
     * Prompts the user to enter their email and password for logging in.
     *
     * @return A [SignInData] object with the entered credentials.
     */
    fun getSignInDataFromUser(): SignInData {
        print("Enter email : ")
        val email = readln().trim()
        print("Enter password : ")
        val password = readln().trim()
        return SignInData(email, password)
    }

    /**
     * Handles Auth flow...
     *
     * return [BaseUser] or Null
     */
    fun authFlow(): BaseUser? {
        while (true) {
            println("\n======== Auth Page =========")
            println("\nOption to choose ⬇️")
            println("0 -> Exit")
            println("1 -> Sign In")
            println("2 -> Sign Up")
            val userInput = readln().toInt()

            // When - Auth Flow
            when (userInput) {
                0 -> break // It will break the outer loop
                1 -> {
                    val signInData = getSignInDataFromUser()
                    val userData: BaseUser? = authService.signIn(signInData)
                    if (userData == null)
                        println("login failed. Try again...")
                    else {
                        println("login success")
                        return userData
                    }
                }

                2 -> {
                    repeat(3) { count ->
                        try {
                            val signUpData = getSignUpDataFromUser()
                            val userData = authService.signUp(signUpData)
                            if (userData == null)
                                println("sign up failed. Try again...")
                            else {
                                println("sign up success")
                                return userData as BaseUser
                            }
                        } catch (exp: Exception) {
                            println("Err:{${exp.message}}")
                            if (count < 2) println("Try again....\n")
                        }
                    }

                    println("Too many attempts, aborting...")
                    return null
                }

                else -> println("Invalid input. Try again")
            }
        }

        return null
    }
}