package pages

import config.exceptions.ValidationException
import core.auth.schemas.SignInData
import core.auth.schemas.SignUpData
import core.auth.services.AuthService
import core.user.schemas.BaseUser
import utils.InputValidator
import utils.selectFromOption

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
        val email = InputValidator.validateEmailFormat(readln())
        print("Enter password : ")
        val password = InputValidator.validatePassword(readln())
        return SignInData(email, password)
    }

    /**
     * Handles Auth flow...
     *
     * return [BaseUser] or Null
     */
    fun authFlow(): BaseUser? {
        val options = mapOf(0 to "Exit", 1 to "Sign In", 2 to "Sign Up")
        while (true) {
            println("\n======== Auth Page =========")
            when (selectFromOption(options)) {
                0 -> break // It will break the outer loop
                1 -> {
                    repeat(3) { count ->
                        try {
                            val signInData = getSignInDataFromUser()
                            val userData: BaseUser? = authService.signIn(signInData)
                            if (userData == null)
                                println("login failed. Try again...")
                            else {
                                println("login success")
                                return userData
                            }
                        } catch (exp: ValidationException) {
                            println("Err:{${exp.message}}")
                            if (count < 2) println("Try again....\n")
                        } catch (_: Exception) {
                            return null
                        }
                    }

                    println("Too many attempts, aborting...")
                    return null
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
            }
        }

        return null
    }
}