package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class LegalCategoryInfo(
    val name: String,
    val shortName: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val surfaceColor: Color
)

object LegalCategories {
    val ALL = "Всі галузі"

    val CATEGORIES = listOf(
        LegalCategoryInfo(
            name = "Конституційне право",
            shortName = "Конституційне",
            description = "Основи державного ладу, права і свободи громадян",
            icon = Icons.Default.AccountBalance,
            primaryColor = Color(0xFF1E3A8A),      // Deep Blue
            surfaceColor = Color(0xFFDBEAFE)
        ),
        LegalCategoryInfo(
            name = "Цивільне право",
            shortName = "Цивільне",
            description = "Майнові та особисті немайнові відносини, договори",
            icon = Icons.Default.Description,
            primaryColor = Color(0xFF0D9488),      // Teal
            surfaceColor = Color(0xFFCCFBF1)
        ),
        LegalCategoryInfo(
            name = "Кримінальне право",
            shortName = "Кримінальне",
            description = "Кримінальні правопорушення, відповідальність і покарання",
            icon = Icons.Default.Gavel,
            primaryColor = Color(0xFF991B1B),      // Crimson / Burgundy
            surfaceColor = Color(0xFFFEE2E2)
        ),
        LegalCategoryInfo(
            name = "Адміністративне право",
            shortName = "Адміністративне",
            description = "Державне управління, адміністративні правопорушення",
            icon = Icons.Default.Security,
            primaryColor = Color(0xFF7C3AED),      // Violet
            surfaceColor = Color(0xFFEDE9FE)
        ),
        LegalCategoryInfo(
            name = "Господарське право",
            shortName = "Господарське",
            description = "Підприємництво, корпоративні відносини та комерція",
            icon = Icons.Default.BusinessCenter,
            primaryColor = Color(0xFFB45309),      // Amber / Bronze
            surfaceColor = Color(0xFFFEF3C7)
        ),
        LegalCategoryInfo(
            name = "Трудове право",
            shortName = "Трудове",
            description = "Трудові договори, оплата праці, відпочинок і безпека",
            icon = Icons.Default.Work,
            primaryColor = Color(0xFF047857),      // Emerald
            surfaceColor = Color(0xFFD1FAE5)
        ),
        LegalCategoryInfo(
            name = "Податкове та фінансове право",
            shortName = "Податкове",
            description = "Податки, збори, бюджет та банківська діяльність",
            icon = Icons.Default.Payments,
            primaryColor = Color(0xFF0369A1),      // Sky / Ocean
            surfaceColor = Color(0xFFE0F2FE)
        ),
        LegalCategoryInfo(
            name = "Процесуальне право",
            shortName = "Процесуальне",
            description = "Судочинство, процесуальні кодекси та правила розгляду",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            primaryColor = Color(0xFF4338CA),      // Indigo
            surfaceColor = Color(0xFFE0E7FF)
        ),
        LegalCategoryInfo(
            name = "Військове право",
            shortName = "Військове",
            description = "Оборона, військова служба, мобілізація та захист держави",
            icon = Icons.Default.Shield,
            primaryColor = Color(0xFF3F6212),      // Military Olive
            surfaceColor = Color(0xFFECFCCB)
        ),
        LegalCategoryInfo(
            name = "Інше",
            shortName = "Інше",
            description = "Інші спеціальні закони та нормативні акти",
            icon = Icons.Default.Description,
            primaryColor = Color(0xFF4B5563),      // Slate
            surfaceColor = Color(0xFFF3F4F6)
        )
    )

    fun getCategoryInfo(name: String): LegalCategoryInfo {
        return CATEGORIES.firstOrNull { it.name.equals(name, ignoreCase = true) || it.shortName.equals(name, ignoreCase = true) }
            ?: CATEGORIES.last()
    }

    fun getAllCategoryNames(): List<String> = CATEGORIES.map { it.name }
}
