package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DatabasePrepopulate
import com.example.data.model.LegalCategories
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Закони України", appName)
    }

    @Test
    fun `initial laws contain constitutional civil and criminal categories`() {
        val laws = DatabasePrepopulate.getInitialLaws()
        assertTrue("Initial laws should not be empty", laws.isNotEmpty())

        val categories = laws.map { it.category }.toSet()
        assertTrue(categories.contains("Конституційне право"))
        assertTrue(categories.contains("Цивільне право"))
        assertTrue(categories.contains("Кримінальне право"))
    }

    @Test
    fun `legal categories helper contains civil criminal and constitutional`() {
        val catInfoConstitutional = LegalCategories.getCategoryInfo("Конституційне право")
        assertNotNull(catInfoConstitutional)
        assertEquals("Конституційне", catInfoConstitutional.shortName)

        val catInfoCivil = LegalCategories.getCategoryInfo("Цивільне право")
        assertNotNull(catInfoCivil)
        assertEquals("Цивільне", catInfoCivil.shortName)

        val catInfoCriminal = LegalCategories.getCategoryInfo("Кримінальне право")
        assertNotNull(catInfoCriminal)
        assertEquals("Кримінальне", catInfoCriminal.shortName)
    }
}
