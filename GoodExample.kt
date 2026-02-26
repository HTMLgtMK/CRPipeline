// GoodExample.kt - A well-structured Kotlin code example with no errors

import java.time.LocalDate

// Data class for immutable data holding
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val createdAt: LocalDate = LocalDate.now()
)

// Interface defining contract
interface Repository<T> {
    fun findById(id: Int): T?
    fun findAll(): List<T>
    fun save(entity: T): T
    fun delete(id: Int): Boolean
}

// Proper implementation with null safety
class UserRepository : Repository<User> {
    private val users = mutableMapOf<Int, User>()

    override fun findById(id: Int): User? = users[id]

    override fun findAll(): List<User> = users.values.toList()

    override fun save(entity: User): User {
        users[entity.id] = entity
        return entity
    }

    override fun delete(id: Int): Boolean = users.remove(id) != null
}

// Sealed class for proper error handling
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
}

// Service layer with proper dependency injection and error handling
class UserService(private val repository: UserRepository) {

    fun registerUser(name: String, email: String): Result<User> {
        // Input validation
        if (name.isBlank()) {
            return Result.Error("Name cannot be blank")
        }
        if (!isValidEmail(email)) {
            return Result.Error("Invalid email format")
        }

        // Check for duplicate email
        repository.findAll().forEach { user ->
            if (user.email.equals(email, ignoreCase = true)) {
                return Result.Error("Email already exists")
            }
        }

        // Create and save user
        val user = User(
            id = generateId(),
            name = name.trim(),
            email = email.lowercase()
        )

        return Result.Success(repository.save(user))
    }

    fun getUserById(id: Int): Result<User> {
        return repository.findById(id)
            ?.let { Result.Success(it) }
            ?: Result.Error("User not found with id: $id")
    }

    fun deleteUser(id: Int): Result<Unit> {
        return if (repository.delete(id)) {
            Result.Success(Unit)
        } else {
            Result.Error("User not found with id: $id")
        }
    }

    // Extension function for email validation
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailPattern.toRegex())
    }

    private fun generateId(): Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
}

// Main function demonstrating usage
fun main() {
    val repository = UserRepository()
    val userService = UserService(repository)

    // Register users
    when (val result = userService.registerUser("John Doe", "john@example.com")) {
        is Result.Success -> println("User registered: ${result.data}")
        is Result.Error -> println("Registration failed: ${result.message}")
    }

    // Get user
    when (val result = userService.getUserById(1)) {
        is Result.Success -> println("Found user: ${result.data}")
        is Result.Error -> println("Error: ${result.message}")
    }

    // List all users
    println("All users: ${repository.findAll()}")
}
