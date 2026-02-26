// BadExample.kt - A Kotlin code example with multiple serious problems

import java.io.File
import java.sql.Connection
import java.sql.DriverManager

// Problem 1: Using var for data that should be immutable
// Problem 2: No validation, nullable fields without proper handling
data class User(
    var id: Int?,  // Problem: ID should never be null
    var name: String?,  // Problem: Name should not be nullable
    var email: String?,  // Problem: Email should not be nullable
    var password: String  // Problem: Storing plain text password
)

// Problem 3: Using Java-style singleton instead of Kotlin object
class UserRepository {
    companion object {
        private var instance: UserRepository? = null
        
        fun getInstance(): UserRepository {
            if (instance == null) {
                instance = UserRepository()
            }
            return instance!!  // Problem: Unsafe non-null assertion
        }
    }
    
    // Problem 4: Mutable list exposed directly
    val users = ArrayList<User>()
    
    fun find(id: Int): User? {
        // Problem 5: Linear search instead of using a map
        for (user in users) {
            if (user.id == id) {
                return user
            }
        }
        return null
    }
}

// Problem 6: No interface, tight coupling
class UserService {
    // Problem 7: Creating dependency directly instead of injection
    private val repository = UserRepository.getInstance()
    
    // Problem 8: No input validation
    // Problem 9: Throwing generic Exception
    // Problem 10: SQL Injection vulnerability
    fun register(name: String, email: String, password: String) {
        val user = User(null, name, email, password)
        
        // Problem 11: Direct string concatenation for SQL - SQL INJECTION!
        val query = "INSERT INTO users (name, email, password) VALUES ('${name}', '${email}', '${password}')"
        executeQuery(query)
        
        repository.users.add(user)  // Problem 12: Modifying collection directly
    }
    
    // Problem 13: Resource leak - connection never closed
    // Problem 14: Hardcoded database credentials
    // Problem 15: Catching Exception and ignoring it
    fun executeQuery(sql: String) {
        var conn: Connection? = null
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/mydb",
                "root",
                "password123"  // Problem: Hardcoded credentials
            )
            conn.createStatement().execute(sql)
        } catch (e: Exception) {
            // Problem: Silent failure - error is swallowed
        }
        // Problem: Connection is never closed - resource leak!
    }
    
    // Problem 16: Returning nullable without proper handling
    fun getUser(id: Int): User? {
        return repository.find(id)
    }
    
    // Problem 17: Unsafe cast
    fun getUserEmail(id: Int): String {
        val user = getUser(id)
        return user!!.email!!  // Problem: Multiple unsafe non-null assertions
    }
    
    // Problem 18: Division without zero check
    fun calculateAverageAge(ages: List<Int>): Int {
        val sum = ages.sum()
        return sum / ages.size  // Problem: Division by zero if list is empty
    }
}

// Problem 19: Open class without proper reason
open class Controller {
    // Problem 20: Lateinit without initialization check
    lateinit var userService: UserService
    
    // Problem 21: Unsafe access to lateinit property
    fun handleRequest(id: String): String {
        val userId = id.toInt()  // Problem: No exception handling for NumberFormatException
        val user = userService.getUser(userId)
        return "Hello, ${user!!.name}!"  // Problem: More unsafe assertions
    }
}

// Problem 22: Using Any instead of proper type
fun processData(data: Any): String {
    // Problem 23: Unsafe cast without checking type
    val str = data as String
    return str.toUpperCase()  // Problem: toUpperCase() is deprecated, should use uppercase()
}

// Problem 24: Recursive function without base case
fun infiniteRecursion(n: Int): Int {
    return n + infiniteRecursion(n + 1)  // Problem: StackOverflowError guaranteed
}

// Problem 25: File handle never closed
fun readFile(path: String): String {
    val file = File(path)
    // Problem: No try-finally or use block to close the file
    return file.readText()
}

// Problem 26: Synchronized on mutable collection
class ThreadUnsafe {
    private val list = mutableListOf<String>()
    
    // Problem 27: Incorrect synchronization
    fun addItem(item: String) {
        synchronized(list) {
            list.add(item)
        }
        // Problem: Still unsafe - iteration outside sync block
    }
    
    fun getAll(): List<String> {
        return list  // Problem: Returns reference to mutable collection
    }
    
    // Test: Add a new method for code review testing
    fun removeItem(item: String): Boolean {
        return list.remove(item)
    }
}

// Main function that will crash
fun main() {
    val service = UserService()
    
    // This will cause SQL injection
    service.register("John", "john@test.com", "password123")
    
    // This will crash with NullPointerException
    val controller = Controller()
    // controller.userService is not initialized - lateinit property not set
    
    // This will cause StackOverflowError
    // infiniteRecursion(1)
    
    // This will crash with ArithmeticException
    // service.calculateAverageAge(emptyList())
    
    // This will crash with ClassCastException
    // processData(123)
}
