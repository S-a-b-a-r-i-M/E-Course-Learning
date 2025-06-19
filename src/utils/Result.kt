package utils

//sealed class Result<out T> {
//    class Success<T>(val data: T) : Result<T>()
//    class Error<T>(val message: String, val code: ErrorCode? = null) : Result<T>()
//}

sealed class Result<out T> {
    class Success<T>(val data: T, val message: String = "") : Result<T>()
    class Error(val message: String, val code: ErrorCode? = null) : Result<Nothing>()
}

enum class ErrorCode {
    RESOURCE_NOT_FOUND,
    UNAUTHORIZED_ACCESS,
    VALIDATION,
    DUPLICATION,
    PERMISSION_DENIED,
}