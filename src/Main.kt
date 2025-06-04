import core.auth.schemas.SignInData
import core.auth.schemas.SignUpData
import core.auth.services.AuthService
import core.user.repositories.UserRepo
import db.inmemorystore.User

fun authFlow(authService: AuthService): Boolean {
    while (true) {
        println("\nOption to choose ⬇️,")
        println("1 -> Sign In")
        println("2 -> Sign Up")
        println("0 -> Exit")
        val userInput = readln().toInt()

        // When - Auth Flow
        when (userInput) {
            0 -> break
            1 -> {
                // Read Input
                println("Enter email : ")
                val email = readln().trim()
                println("Enter password : ")
                val password = readln().trim()

                val userData: User? = authService.signIn(SignInData(email, password))

                if (userData == null)
                    println("login failed")
                else {
                    println("login success")
                    return true
                }
            }
            2 -> {
                // Read Input
                println("Enter first name : ")
                val firstName = readln().trim()
                println("Enter last name : ")
                val lastName = readln().trim()
                println("Enter email : ")
                val email = readln().trim()
                println("Enter password : ")
                val password1 = readln().trim()
                println("Enter password again : ")
                val password2 = readln().trim()

                if (password1 != password2) {
                    println("Password didn't match.")
                    continue
                }

                val result = authService.signUp(
                    SignUpData(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = password1,
                    )
                )
                if (result) return true
            }
            else -> println("Invalid input. Try again")
        }
    }

    return false
}

fun main() {
    println("Welcome to grate kirigalan show...")
    // Object Creation
    val userRepo = UserRepo()
    val authService = AuthService(userRepo)

    val isUserAuthenticated = authFlow(authService)
    println("isUserAuthenticated $isUserAuthenticated")

    if (isUserAuthenticated){

    }

    println("Welcome..visit again 😊")
}