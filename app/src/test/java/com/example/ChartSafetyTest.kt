package com.example

import com.example.ui.owner.chartBarRatio
import com.example.ui.owner.nonZeroSegments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartSafetyTest {

  @Test
  fun zero_count_segments_are_dropped() {
    val segments = nonZeroSegments(listOf(986 to "active", 0 to "pending", 172 to "expired", 0 to "cancelled"))
    assertEquals(listOf("active", "expired"), segments.map { it.second })
    assertTrue(segments.all { it.first > 0f && !it.first.isNaN() })
  }

  @Test
  fun all_zero_counts_give_an_empty_bar() {
    assertTrue(nonZeroSegments(listOf(0 to "a", 0 to "b")).isEmpty())
  }

  @Test
  fun bar_ratio_is_safe_when_every_value_is_zero() {
    assertEquals(0.1f, chartBarRatio(0.0, 0.0, minRatio = 0.1f))
    assertEquals(0.1f, chartBarRatio(Double.NaN, 100.0, minRatio = 0.1f))
  }

  @Test
  fun bar_ratio_scales_and_clamps_normal_values() {
    assertEquals(0.5f, chartBarRatio(50.0, 100.0, minRatio = 0.1f))
    assertEquals(1f, chartBarRatio(100.0, 100.0, minRatio = 0.1f))
    assertEquals(0.1f, chartBarRatio(1.0, 100.0, minRatio = 0.1f))
  }
}
