package com.congen.controllers

import com.congen.model.Equipment
import com.congen.model.ExerciseEquipment
import com.congen.dal.EquipmentDAL
import com.congen.dal.ExerciseEquipmentDAL
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
    private val equipmentDAL: EquipmentDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
) {
    @PostMapping("/")
    fun save(@RequestBody equipment: Equipment) : ResponseEntity<*> {
        return ResponseEntity.ok(
            equipmentDAL.insertEquipment(equipment)
        )
    }

    @GetMapping("/{name}")
    fun get(@PathVariable("name") name: String): Mono<ResponseEntity<Equipment>> {
        return equipmentDAL.selectEquipmentByName(name)
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/{name}/exercise")
    fun getExercise(@PathVariable("name") name: String): Mono<ResponseEntity<List<ExerciseEquipment>>> {
        return exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(name)
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return ResponseEntity.ok(
            equipmentDAL.selectEquipment()
        )
    }
}
