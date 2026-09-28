package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LegalCategories

@Composable
fun AddLawDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onDownload: (input: String, category: String) -> Unit,
    isLoading: Boolean = false
) {
    if (!isOpen) return

    var input by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LegalCategories.CATEGORIES.first().name) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Завантажити закон з ВРУ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Введіть посилання на закон із сайту zakon.rada.gov.ua або офіційний номер документа:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Номер закону або URL") },
                    placeholder = { Text("наприклад: 580-19 або zakon.rada.gov.ua/laws/show/...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_input_field"),
                    singleLine = false,
                    maxLines = 3,
                    enabled = !isLoading
                )

                // Category selector
                Text(
                    text = "Галузь права (категорія):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { categoryDropdownExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("select_category_button"),
                        enabled = !isLoading
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selectedCategory, fontSize = 14.sp)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        LegalCategories.CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat.name
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Quick preset examples
                Text(
                    text = "Популярні приклади:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickExampleChip("580-19\n(Поліція)") {
                        input = "580-19"
                        selectedCategory = "Адміністративне право"
                    }
                    QuickExampleChip("1700-18\n(Антикорупція)") {
                        input = "1700-18"
                        selectedCategory = "Конституційне право"
                    }
                    QuickExampleChip("1934-19\n(Нацбезпека)") {
                        input = "1934-19"
                        selectedCategory = "Військове право"
                    }
                }

                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = "Завантаження та збереження в пам'ять телефону...",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (input.isNotBlank()) {
                        onDownload(input.trim(), selectedCategory)
                    }
                },
                enabled = input.isNotBlank() && !isLoading,
                modifier = Modifier.testTag("confirm_download_button")
            ) {
                Text("Завантажити")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
                modifier = Modifier.testTag("cancel_download_button")
            ) {
                Text("Скасувати")
            }
        }
    )
}

@Composable
private fun QuickExampleChip(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(text = text, fontSize = 10.sp, lineHeight = 12.sp)
    }
}
