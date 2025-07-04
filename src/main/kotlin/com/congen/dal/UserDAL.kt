package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.User
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserDAL::class.java)
    }

    fun selectUserById(userId: Int): Mono<User> {
        logger.debug("Selecting user by id: {}", userId)
        return postgresClient.selectIndividual(
            "SELECT * FROM \"user\" WHERE id=$1",
            userId,
        )
    }

    fun selectUsers(): Mono<List<User>> {
        logger.debug("Selecting all users")
        return postgresClient.select("SELECT * FROM \"user\"")
    }

    fun insertUser(user: User): Mono<User> {
        logger.debug("Inserting user: {}", user.name)

        // Validate all CHECK constraints
        ValidationUtil.validateUserAge(user.age)
        ValidationUtil.validateUserHeight(user.height)
        ValidationUtil.validateUserWeight(user.weight)

        return postgresClient.update(
            """
            INSERT INTO "user"
                (name, age, height, weight)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent(),
            user.name,
            user.age,
            user.height,
            user.weight,
        )
    }

    fun updateUser(user: User): Mono<User> {
        logger.debug("Updating user: {}", user.id)

        // Validate all CHECK constraints
        ValidationUtil.validateUserAge(user.age)
        ValidationUtil.validateUserHeight(user.height)
        ValidationUtil.validateUserWeight(user.weight)

        return postgresClient.update(
            """
            UPDATE "user"
            SET name=$2, age=$3, height=$4, weight=$5
            WHERE id=$1
            """.trimIndent(),
            user.id,
            user.name,
            user.age,
            user.height,
            user.weight,
        )
    }

    fun deleteUser(userId: Int): Mono<User> {
        logger.debug("Deleting user: {}", userId)
        return postgresClient.update(
            "DELETE FROM \"user\" WHERE id=$1",
            userId,
        )
    }
}
