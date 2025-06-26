package utils

import config.LogLevel
import config.exceptions.ValidationException
import config.logInfo
import core.user.schemas.UserRole

object InputValidator {
    fun validateName(value: String, fieldName: String, minChars: Int = -1, maxChars: Int = -1): String {
        val trimmedVal = value.trim()

        if (trimmedVal.isEmpty())
            throw ValidationException.InvalidInputException("$fieldName cannot be empty")
        else if (minChars != -1 && trimmedVal.length < minChars)
            throw ValidationException.InvalidInputException(
                "$fieldName must be at least $minChars characters long"
            )
        else if (maxChars != -1 && trimmedVal.length > maxChars)
            throw ValidationException.InvalidInputException(
                "$fieldName cannot exceed $maxChars characters"
            )
        else
            return trimmedVal
    }

    fun validatePassWordMatch(password1: String, password2: String) {
        if (password1 != password2)
            throw ValidationException.PasswordMismatchException("Passwords do not match")
    }

    fun validateEmailFormat(email: String): String {
        val trimmedEmail = email.trim()

        when {
            trimmedEmail.isEmpty() ->
                throw ValidationException.EmailFormatException("Email cannot be empty")
            !isValidEmailFormat(trimmedEmail) ->
                throw ValidationException.EmailFormatException("Invalid email format")
            else -> return trimmedEmail
        }
    }

    fun validatePassword(password: String): String {
        when {
            password.isEmpty() ->
                throw ValidationException.WeakPasswordException("Password cannot be empty")
            password.length < 8 ->
                throw ValidationException.WeakPasswordException(
                    "Password must be at least 8 characters long"
                )
            password.length > 128 ->
                throw ValidationException.WeakPasswordException(
                    "Password cannot exceed 128 characters"
                )
            !hasAlphabet(password) ->
                throw ValidationException.WeakPasswordException(
                    "Password must contain at least one alphabet letter"
                )
            !hasDigit(password) ->
                throw ValidationException.WeakPasswordException(
                    "Password must contain at least one digit"
                )
            !hasSpecialChar(password) ->
                throw ValidationException.WeakPasswordException(
                    "Password must contain at least one special character (!@#$%^&*)"
                )
            else -> return password
        }
    }

    fun validatePositiveDouble(): Double {
        val input = readln().toDoubleOrNull() ?:
            throw ValidationException.InvalidInputException("Input is not a number")

        if (input <= 0)
            throw ValidationException.InvalidInputException("Input cannot be negative or 0")

        return input
    }

    fun validatePositiveInt(): Int {
        val input = readln().toIntOrNull() ?:
            throw ValidationException.InvalidInputException("Input is not a number")

        if (input <= 0)
            throw ValidationException.InvalidInputException("Input cannot be negative or 0")

        return input
    }

    fun isValidEmailFormat(email: String): Boolean {
        val emailRegex = Regex("^[a-zA-Z0-9.+%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        return emailRegex.matches(email)
    }

    fun hasUppercase(value: String) = value.any { it.isUpperCase() }

    fun hasLowercase(value: String) = value.any { it.isLowerCase() }

    fun hasAlphabet(value: String) = value.any { it.isLetter() }

    fun hasDigit(value: String) = value.any { it.isDigit() }

    fun hasSpecialChar(value: String) = value.any { it in "!@#$%^&*()_+-=[]{}|;:,.<>?" }
}

fun hasPermission(
    currentUserRole: UserRole,
    allowedUserRoles: List<UserRole> = listOf(UserRole.ADMIN),
    errMessage: String = "User doesn't have permission to perform this action"
): Boolean {
    if (!allowedUserRoles.contains(currentUserRole)) {
        logInfo(errMessage, LogLevel.EXCEPTION)
        return false
    }
    
    return true
}

fun hasPermissionV2(
    currentUserRole: UserRole,
    allowedUserRoles: List<UserRole> = listOf(UserRole.ADMIN),
    errMessage: String = "User doesn't have permission to perform this action"
): Result<Unit> {
    if (!allowedUserRoles.contains(currentUserRole)) {
        logInfo(errMessage, LogLevel.EXCEPTION)
        return Result.Error(errMessage, ErrorCode.PERMISSION_DENIED)
    }

    return Result.Success(Unit)
}