package com.example

import com.example.data.services.GymOwnerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AggregatorScopeTest {

  @Test
  fun accepted_passes_are_only_active_passes_with_gym_access() {
    val passes = GymOwnerRepository.getAcceptedPasses()
    assertEquals(listOf("plan_elite_1", "plan_pro_2", "plan_select_3"), passes.map { it.id })
    assertTrue(passes.all { it.isActive && it.grantsGymAccess })
  }
}
