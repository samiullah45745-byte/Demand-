package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DemandItem
import com.example.ui.DemandViewModel
import com.example.ui.DemandViewModelFactory
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: DemandViewModel by viewModels {
        DemandViewModelFactory((application as DemandApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.initializeWithDefaultsIfEmpty()

        setContent {
            MyApplicationTheme {
                DemandAppScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemandAppScreen(viewModel: DemandViewModel) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .padding(top = 48.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(
                            onClick = { viewModel.generateSlip(items) },
                            colors = ButtonDefaults.textButtonColors(contentColor = ThemePrimary)
                        ) {
                            Text("Generate Slip", fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(ThemeAvatarBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DT",
                            color = ThemeAvatarText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "دیسی تڑکا ریسٹورنٹ",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "انوینٹری ڈیمانڈ لسٹ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ThemePrimary,
                contentColor = ThemeOnPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar Placeholder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(ThemeSearchBg, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "آئٹم تلاش کریں...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    DemandItemCard(
                        item = item,
                        onQuantityChanged = { newQuantity ->
                            viewModel.updateDemandQuantity(item, newQuantity)
                        },
                        onDelete = {
                            viewModel.deleteItem(item.id)
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddItemDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, unit ->
                    viewModel.addItem(name, unit)
                    showAddDialog = false
                }
            )
        }
        
        val slipState by viewModel.slipUiState.collectAsStateWithLifecycle()
        if (slipState !is com.example.ui.SlipUiState.Idle) {
            SlipDialog(
                slipState = slipState,
                onDismiss = { viewModel.dismissSlip() }
            )
        }
    }
}

@Composable
fun SlipDialog(slipState: com.example.ui.SlipUiState, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Demand Slip") },
        text = {
            when (slipState) {
                is com.example.ui.SlipUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = ThemePrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Generating slip...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                is com.example.ui.SlipUiState.Success -> {
                    Text(
                        text = slipState.slipText,
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                is com.example.ui.SlipUiState.Error -> {
                    Text(
                        text = slipState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = ThemePrimary)) {
                Text("Close")
            }
        },
        containerColor = ThemeCardBg,
        textContentColor = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun DemandItemCard(
    item: DemandItem,
    onQuantityChanged: (String) -> Unit,
    onDelete: () -> Unit
) {
    val iconColor = remember(item.id) { listOf(ThemePrimary, Color(0xFFE7E0FF), Color(0xFFD1E4FF)).random() }
    val emoji = remember(item.id) { listOf("🍅", "🧅", "🧂", "🍶", "🍚", "🥩", "🥔", "🥬").random() }
    val category = remember(item.id) { listOf("سبزی", "مصالحہ", "ڈیری", "گوشت", "اناج").random() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeCardBg),
        border = BorderStroke(1.dp, ThemeCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = item.name.substringBefore("(").trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = item.quantity,
                        onValueChange = onQuantityChanged,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.End
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .background(ThemePillBg, RoundedCornerShape(8.dp))
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .width(60.dp),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.quantity.isEmpty()) {
                                    Text(
                                        text = "0",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                innerTextField()
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.unit.take(2).uppercase(),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val unitOptions = listOf("کلو (Kg)", "لیٹر (Liter)", "گرام (Grams)", "عدد (Pieces)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("نئی چیز شامل کریں") }, // Add new item
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام") }, // Name
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("یونٹ") }, // Unit
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        unitOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    unit = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && unit.isNotBlank()) {
                        onAdd(name, unit)
                    }
                }
            ) {
                Text("محفوظ کریں") // Save
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("منسوخ") // Cancel
            }
        }
    )
}

