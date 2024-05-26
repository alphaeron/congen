package com.congen.service.impl

import com.congen.dto.EquipmentData
import com.congen.models.Equipment
import com.congen.repo.EquipmentRepo
import com.congen.service.EquipmentService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class EquipmentServiceImpl(
    val equipmentRepo: EquipmentRepo
) : EquipmentService {
    override fun saveEquipment(equipmentData: EquipmentData): Mono<EquipmentData> {
        val equipment = Equipment(
            name=equipmentData.name,
            description=equipmentData.description,
        )
        return equipmentRepo.save(equipment)
            .map {
                EquipmentData(
                    name=it.name,
                    description=it.description,
                )
            }
    }

    override fun getEquipment(name: String): Mono<EquipmentData> {
        return equipmentRepo.findByName(name)
            .map {
                EquipmentData(
                    name=it.name,
                    description=it.description,
                )
            }
    }

    override fun getAllEquipments(): Flux<EquipmentData> {
        return equipmentRepo.findAll()
            .map {
                EquipmentData(
                    name=it.name,
                    description=it.description,
                )
            }
    }
}
