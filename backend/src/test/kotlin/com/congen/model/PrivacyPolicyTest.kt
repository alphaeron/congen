package com.congen.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrivacyPolicyTest {
    @Test
    fun `should create privacy policy with correct properties`() {
        val dataController =
            DataController(
                name = "Congen Development Team",
                contact = "privacy@congen.dev",
                dpo = "dpo@congen.dev"
            )

        val dataProcessing =
            DataProcessing(
                purposes = listOf("Fitness tracking", "Workout generation"),
                legalBasis = listOf("User consent (GDPR Article 6.1.a)"),
                dataTypes = listOf("Personal profile data", "Exercise preferences"),
                retentionPeriods = mapOf("user_profile" to "7 years", "exercise_data" to "3 years")
            )

        val userRights =
            UserRights(
                access = "You can request access to your personal data",
                rectification = "You can request correction of your data",
                erasure = "You can request deletion of your data",
                portability = "You can request export of your data",
                objection = "You can object to data processing",
                complaint = "You can file a complaint with supervisory authorities"
            )

        val privacyPolicy =
            PrivacyPolicy(
                dataController = dataController,
                dataProcessing = dataProcessing,
                userRights = userRights,
                lastUpdated = "2023-08-08",
                version = "1.0.0"
            )

        assertEquals("Congen Development Team", privacyPolicy.dataController.name)
        assertEquals("privacy@congen.dev", privacyPolicy.dataController.contact)
        assertEquals("dpo@congen.dev", privacyPolicy.dataController.dpo)
        assertEquals(2, privacyPolicy.dataProcessing.purposes.size)
        assertEquals("Fitness tracking", privacyPolicy.dataProcessing.purposes[0])
        assertEquals("Workout generation", privacyPolicy.dataProcessing.purposes[1])
        assertEquals("2023-08-08", privacyPolicy.lastUpdated)
        assertEquals("1.0.0", privacyPolicy.version)
    }

    @Test
    fun `should handle empty lists`() {
        val dataController =
            DataController(
                name = "Test Controller",
                contact = "test@example.com",
                dpo = null
            )

        val dataProcessing =
            DataProcessing(
                purposes = emptyList(),
                legalBasis = emptyList(),
                dataTypes = emptyList(),
                retentionPeriods = emptyMap()
            )

        val userRights =
            UserRights(
                access = "Access info",
                rectification = "Rectification info",
                erasure = "Erasure info",
                portability = "Portability info",
                objection = "Objection info",
                complaint = "Complaint info"
            )

        val privacyPolicy =
            PrivacyPolicy(
                dataController = dataController,
                dataProcessing = dataProcessing,
                userRights = userRights,
                lastUpdated = "2023-01-01",
                version = "1.0.0"
            )

        assertEquals(0, privacyPolicy.dataProcessing.dataTypes.size)
        assertEquals(0, privacyPolicy.dataProcessing.purposes.size)
    }

    @Test
    fun `should handle DataController with null DPO`() {
        val dataController =
            DataController(
                name = "Test Controller",
                contact = "test@example.com",
                dpo = null
            )

        val dataProcessing =
            DataProcessing(
                purposes = listOf("Single purpose"),
                legalBasis = listOf("Single basis"),
                dataTypes = listOf("Single data type"),
                retentionPeriods = mapOf("single_type" to "1 year")
            )

        val userRights =
            UserRights(
                access = "Access info",
                rectification = "Rectification info",
                erasure = "Erasure info",
                portability = "Portability info",
                objection = "Objection info",
                complaint = "Complaint info"
            )

        val privacyPolicy =
            PrivacyPolicy(
                dataController = dataController,
                dataProcessing = dataProcessing,
                userRights = userRights,
                lastUpdated = "2023-01-01",
                version = "1.0.0"
            )

        assertEquals("Test Controller", privacyPolicy.dataController.name)
        assertEquals("test@example.com", privacyPolicy.dataController.contact)
        assertEquals(null, privacyPolicy.dataController.dpo)
        assertEquals(1, privacyPolicy.dataProcessing.dataTypes.size)
        assertEquals("Single data type", privacyPolicy.dataProcessing.dataTypes[0])
    }
}
