package com.congen.service

import com.congen.dal.ProgramDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Program
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service for program-related business logic and authorization checks.
 *
 * This service provides methods for program operations and authorization
 * validation, particularly for checking program ownership.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class ProgramService(
    private val programDAL: ProgramDAL,
) {
    /**
     * Gets the owner of the program.
     *
     * @param programId The ID of the program
     * @return Mono<String> The Keycloak user ID of the owner
     */
    fun getOwner(programId: Long): Mono<String> {
        return programDAL.selectProgramById(programId)
            .map { program -> program.userId }
    }

    /**
     * Checks if a user is the owner of a specific program.
     *
     * This method is used by Spring Security's @PreAuthorize annotation
     * to determine if a user has permission to access a program.
     *
     * @param programId The ID of the program to check ownership for
     * @param userId The Keycloak user ID to check ownership against
     * @return Mono<Boolean> true if the user owns the program, false otherwise
     */
    fun isOwner(
        programId: Long,
        userId: String
    ): Mono<Boolean> {
        return getOwner(programId)
            .map { ownerId -> ownerId == userId }
            .onErrorReturn(false)
    }

    /**
     * Retrieves a program by its unique identifier.
     *
     * @param id The unique identifier of the program to retrieve
     * @return Mono containing the program if found
     * @throws NoResultsFoundException if no program exists with the given ID
     */
    fun getProgramById(id: Long): Mono<Program> {
        return programDAL.selectProgramById(id)
    }

    /**
     * Retrieves all programs from the database.
     *
     * @return Mono containing a list of all programs
     */
    fun getAllPrograms(): Mono<List<Program>> {
        return programDAL.selectPrograms()
    }

    /**
     * Retrieves programs for a specific user, optionally filtered by active status.
     *
     * @param userId The Keycloak user ID to filter programs by
     * @param isActive Optional filter for active status. If null, returns all programs for the user
     * @return Mono containing a list of programs for the user
     */
    fun getProgramsByUserId(
        userId: String,
        isActive: Boolean? = null
    ): Mono<List<Program>> {
        return programDAL.selectProgramsByUserId(userId, isActive)
    }

    /**
     * Inserts a new program for a user.
     *
     * @param userId The Keycloak user ID to associate with the new program
     * @param name The name of the new program
     * @param currentWeekNumber The current week number for the new program
     * @param isActive Whether the new program should be active (default: true)
     * @return Mono containing the inserted program
     * @throws NoResultsFoundException if the insert operation fails
     */
    fun createProgram(
        userId: String,
        name: String,
        currentWeekNumber: Int,
        isActive: Boolean = true
    ): Mono<Program> {
        return programDAL.insertProgram(userId, name, currentWeekNumber, isActive)
    }

    /**
     * Updates an existing program in the database.
     *
     * @param id The unique identifier of the program to update
     * @param name The updated name of the program
     * @param currentWeekNumber The updated current week number
     * @param isActive Whether the program should be active
     * @return Mono containing the updated program
     * @throws NoResultsFoundException if no program exists with the given ID
     */
    fun updateProgram(
        id: Long,
        name: String,
        currentWeekNumber: Int,
        isActive: Boolean
    ): Mono<Program> {
        return programDAL.updateProgram(id, name, currentWeekNumber, isActive)
    }

    /**
     * Deletes a program from the database.
     *
     * @param id The unique identifier of the program to delete
     * @return Mono containing the deleted program
     * @throws NoResultsFoundException if no program exists with the given ID
     */
    fun deleteProgram(id: Long): Mono<Program> {
        return programDAL.deleteProgram(id)
    }
}
