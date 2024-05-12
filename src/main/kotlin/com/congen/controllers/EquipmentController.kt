package com.congen.controllers

import com.congen.dto.EquipmentData
import com.congen.service.EquipmentService
import com.congen.service.ExerciseEquipmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/equipment")
class EquipmentController(
    private val equipmentService: EquipmentService,
    private val exerciseEquipmentService: ExerciseEquipmentService,
) {
    @PostMapping("/")
    fun save(@RequestBody equipmentData: EquipmentData) : ResponseEntity<*>{
        return ResponseEntity.ok(
            equipmentService.saveEquipment(equipmentData)
        )
    }

    @GetMapping("/{name}")
    fun get(@PathVariable("name") name: String): ResponseEntity<*>{
        return ResponseEntity.ok(
            equipmentService.getEquipment(name)
        )
    }

    @GetMapping("/{name}/exercise")
    fun getExercise(@PathVariable("name") name: String): ResponseEntity<*>{
        return ResponseEntity.ok(
            exerciseEquipmentService.getByEquipmentName(name)
        )
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return ResponseEntity.ok(
            equipmentService.getAllEquipments()
        )
    }
}