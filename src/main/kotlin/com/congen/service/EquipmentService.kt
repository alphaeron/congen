package com.congen.service

import com.congen.dto.EquipmentData
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface EquipmentService {
    fun saveEquipment(equipmentData: EquipmentData): Mono<EquipmentData>

    fun getEquipment(name: String): Mono<EquipmentData>

    fun getAllEquipments(): Flux<EquipmentData>
}