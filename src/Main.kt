import core.auth.schemas.SignInData
import core.auth.schemas.SignUpData
import core.auth.services.AuthService
import core.user.repositories.UserRepo
import db.UserRole
import db.inmemorystore.Student
import db.inmemorystore.Trainer
import db.inmemorystore.User

fun authFlow(authService: AuthService): User? {
    while (true) {
        println("\nOption to choose ⬇️,")
        println("1 -> Sign In")
        println("2 -> Sign Up")
        println("0 -> Exit")
        val userInput = readln().toInt()

        // When - Auth Flow
        when (userInput) {
            0 -> break // It will break the outer loop
            1 -> {
                // Read Input
                println("Enter email : ")
                val email = readln().trim()
                println("Enter password : ")
                val password = readln().trim()

                val user: User? = authService.signIn(SignInData(email, password))

                if (user == null)
                    println("login failed. Try again...")
                else {
                    println("login success")
                    return user
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

                val user = authService.signUp(
                    SignUpData(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = password1,
                    )
                )
                if (user == null)
                    println("sign up failed. Try again...")
                else {
                    println("sign up success")
                    return user
                }
            }
            else -> println("Invalid input. Try again")
        }
    }

    return null
}

fun main() {
    println("Welcome to grate kirigalan show...")
    // Object Creation
    val userRepo = UserRepo()
    val authService = AuthService(userRepo)

    var user: User? = authFlow(authService)
    println("User from auth flow : $user")

    if (user != null){
        // Next flow

    }

    println("Welcome..visit again 😊")
}