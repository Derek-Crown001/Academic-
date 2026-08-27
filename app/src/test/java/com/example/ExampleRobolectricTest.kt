package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SchoolRole
import com.example.ui.viewmodel.PortalTab
import com.example.ui.viewmodel.SchoolViewModel
import org.junit.Assert.assertEquals
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
    assertEquals("AcademiaTrack", appName)
  }

  @Test
  fun `verify role based portal switching and default tabs`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = SchoolViewModel(app)

    // Select Student Portal
    val studentResult = viewModel.selectPortal(SchoolRole.STUDENT)
    assertTrue(studentResult)
    assertEquals(SchoolRole.STUDENT, viewModel.currentRole.value)
    assertEquals(PortalTab.STUDENT_CBT, viewModel.currentTab.value)

    // Select Parent Portal
    val parentResult = viewModel.selectPortal(SchoolRole.PARENT)
    assertTrue(parentResult)
    assertEquals(SchoolRole.PARENT, viewModel.currentRole.value)
    assertEquals(PortalTab.PARENT_CHILD_OVERVIEW, viewModel.currentTab.value)

    // Select Admin Portal with PIN
    val adminResult = viewModel.selectPortal(SchoolRole.ADMIN, pin = "admin123")
    assertTrue(adminResult)
    assertEquals(SchoolRole.ADMIN, viewModel.currentRole.value)
    assertEquals(PortalTab.DASHBOARD, viewModel.currentTab.value)

    // Select Teacher Portal with PIN
    val teacherResult = viewModel.selectPortal(SchoolRole.TEACHER, pin = "teach123")
    assertTrue(teacherResult)
    assertEquals(SchoolRole.TEACHER, viewModel.currentRole.value)
    assertEquals(PortalTab.TEACHER_DASHBOARD, viewModel.currentTab.value)
  }
}
