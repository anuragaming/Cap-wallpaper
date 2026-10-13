package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.AntiReturnViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nhật Anh browser", appName)
  }

  @Test
  fun `test google lens state and capture in viewModel`() {
    val viewModel = AntiReturnViewModel()
    assertFalse(viewModel.isGoogleLensOpen.value)

    val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    viewModel.onScreenshotCaptured(testBitmap, openLens = true)

    assertTrue(viewModel.isGoogleLensOpen.value)
    assertNotNull(viewModel.capturedScreenshot.value)
    assertEquals("SOLVE", viewModel.googleLensActiveTab.value)

    viewModel.setGoogleLensTab("OCR")
    assertEquals("OCR", viewModel.googleLensActiveTab.value)

    viewModel.closeGoogleLens()
    assertFalse(viewModel.isGoogleLensOpen.value)
  }
}
