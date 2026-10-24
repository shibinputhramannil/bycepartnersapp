package com.example

import com.example.ui.owner.substituteCandidates
import com.example.ui.owner.substituteCoachName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AssignCoachTest {

  private val options = listOf(
    "Coach Rahul (Freelance Boxing & HIIT)",
    "Coach Ramesh (Combat Trainer)"
  )

  @Test
  fun assigned_name_keeps_the_full_coach_name() {
    assertEquals("Coach Divya", substituteCoachName("Coach Divya (Certified Yoga & Mobility)"))
    assertEquals("Coach Sanjay", substituteCoachName("Coach Sanjay"))
  }

  @Test
  fun current_coach_is_not_offered_as_their_own_substitute() {
    val candidates = substituteCandidates(options, currentCoach = "Coach Ramesh")
    assertEquals(listOf("Coach Rahul (Freelance Boxing & HIIT)"), candidates)
    assertFalse(candidates.any { it.startsWith("Coach Ramesh") })
  }
}
