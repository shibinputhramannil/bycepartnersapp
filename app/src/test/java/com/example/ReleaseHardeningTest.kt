package com.example

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards the merged manifest settings from release hardening (step 7).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReleaseHardeningTest {

  private val context = ApplicationProvider.getApplicationContext<Context>()

  @Test
  fun app_requests_no_network_permissions_until_backend_exists() {
    val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
    val permissions = info.requestedPermissions?.toList().orEmpty()
    assertTrue(permissions.contains("android.permission.CAMERA"))
    assertFalse(permissions.contains("android.permission.INTERNET"))
    assertFalse(permissions.contains("android.permission.ACCESS_NETWORK_STATE"))
  }

  @Test
  fun backup_and_cleartext_traffic_are_disabled() {
    val flags = context.applicationInfo.flags
    assertEquals(0, flags and ApplicationInfo.FLAG_ALLOW_BACKUP)
    assertEquals(0, flags and ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC)
  }

  @Test
  fun display_name_is_byce_partner() {
    assertEquals("BYCE Partner", context.getString(R.string.app_name))
  }
}
