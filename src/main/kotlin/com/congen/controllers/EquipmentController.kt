package com.congen.controllers

import com.congen.dto.EquipmentData
import com.congen.dto.ExerciseEquipmentData
import com.congen.service.EquipmentService
import com.congen.service.ExerciseEquipmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/equipment")
class EquipmentController(
    private val equipmentService: EquipmentService,
    private val exerciseEquipmentService: ExerciseEquipmentService,
) {
    @PostMapping("/")
    fun save(@RequestBody equipmentData: EquipmentData) : ResponseEntity<*> {
        return ResponseEntity.ok(
            equipmentService.saveEquipment(equipmentData)
        )
    }

    @GetMapping("/{name}")
    fun get(@PathVariable("name") name: String): Mono<ResponseEntity<EquipmentData>> {
        return equipmentService
            .getEquipment(name)
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/{name}/exercise")
    fun getExercise(@PathVariable("name") name: String): Mono<ResponseEntity<List<ExerciseEquipmentData>>> {
        return exerciseEquipmentService
            .getByEquipmentName(name)
            .collectList()
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return ResponseEntity.ok(
            equipmentService.getAllEquipments()
        )
    }
}
