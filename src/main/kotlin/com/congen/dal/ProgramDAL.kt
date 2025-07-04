package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.Program
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ProgramDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ProgramDAL::class.java)
    }

    fun selectProgramById(id: Long): Mono<Program> {
        logger.debug("Selecting program by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM program WHERE id=$1",
            id,
        )
    }

    fun selectPrograms(): Mono<List<Program>> {
        logger.debug("Selecting all programs")
        return postgresClient.select("SELECT * FROM program ORDER BY name")
    }

    fun insertProgram(program: Program): Mono<Program> {
        logger.debug("Inserting program: {}", program.name)
        return postgresClient.update(
            """
            INSERT INTO program
                (name, description)
            VALUES
                ($1, $2)
            """.trimIndent(),
            program.name,
            program.description,
        )
    }

    fun updateProgram(program: Program): Mono<Program> {
        logger.debug("Updating program: {}", program.id)
        return postgresClient.update(
            """
            UPDATE program
            SET name=$2, description=$3
            WHERE id=$1
            """.trimIndent(),
            program.id,
            program.name,
            program.description,
        )
    }

    fun deleteProgram(id: Long): Mono<Program> {
        logger.debug("Deleting program: {}", id)
        return postgresClient.update(
            "DELETE FROM program WHERE id=$1",
            id,
        )
    }
}
