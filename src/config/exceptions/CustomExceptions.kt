package config.exceptions

import java.lang.Exception

sealed class ValidationException(message: String) : Exception(message) {
    class InvalidInputException(message: String) : ValidationException(message)
    class PasswordMismatchException(message: String) : ValidationException(message)
    class EmailFormatException(message: String) : ValidationException(message)
    class WeakPasswordException(message: String) : ValidationException(message)
}

