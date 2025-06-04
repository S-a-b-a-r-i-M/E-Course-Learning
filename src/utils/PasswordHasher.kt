package utils

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val ALGORITHM = "PBKDF2WithHmacSHA512"
private const val ITERATIONS = 120_000
private const val KEY_LENGTH = 256
private const val SALT_LENGTH = 16 // 128 bits

class PasswordHasher {
    companion object{
        private fun generateRandomSalt(): ByteArray {
            val salt = ByteArray(SALT_LENGTH)
            SecureRandom().nextBytes(salt)
            return salt
        }

        private fun getHash(password: String, salt: ByteArray): ByteArray {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            return factory.generateSecret(spec).encoded
        }

        fun getHashPassword(password: String): String {
            // Get Salt & Password's Hash
            val salt: ByteArray = generateRandomSalt()
            val hash: ByteArray = getHash(password, salt)

            // Encode to String
            val encoder = Base64.getEncoder()
            return "${encoder.encodeToString(salt)}:${encoder.encodeToString(hash)}"
        }

        fun checkPasswordMatch(plainPassword: String, hashPassword: String): Boolean {
            val parts = hashPassword.split(":")
            if (parts.size != 2) {
                println("Invalid hash password format!!!")
                return false
            }

            val decoder = Base64.getDecoder()
            val salt = decoder.decode(parts[0])
            val existingPasswordHash = decoder.decode(parts[1])

            // Hash Input & Compare
            val inputPasswordHash: ByteArray = getHash(plainPassword, salt)
            return existingPasswordHash.contentEquals(inputPasswordHash)
        }
    }
}