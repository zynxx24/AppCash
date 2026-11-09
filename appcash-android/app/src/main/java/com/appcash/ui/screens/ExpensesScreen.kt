package com.appcash.ui.screens

import com.appcash.ui.theme.orangeCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.appcash.data.model.Expense
import com.appcash.data.repository.AppCashRepository
import com.appcash.ui.theme.OrangeDark
import com.appcash.ui.theme.OrangeLight
import com.appcash.ui.theme.OrangePrimary
import com.appcash.ui.theme.RedNegative
import kotlinx.coroutines.launch

@Composable
fun ExpensesScreen(repository: AppCashRepository, isAdmin: Boolean) {
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showAdd by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var deletingExpense by remember { mutableStateOf<Expense?>(null) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    val dataVersion by repository.version.collectAsState()
    LaunchedEffect(dataVersion) {
        error = null
        repository.getExpenses().fold(
            onSuccess = { expenses = it },
            onFailure = { error = it.message ?: "Gagal memuat pengeluaran" }
        )
        loading = false
    }

    if (showAdd) {
        AddExpenseDialog(onDismiss = { showAdd = false }, onConfirm = { description, amount, date ->
            showAdd = false
            scope.launch {
                repository.addExpense(com.appcash.data.model.ExpenseRequest(description, amount, date)).fold(
                    onSuccess = { res ->
                        expenses = listOf(Expense(res.id, description, amount, date)) + expenses
                        snackbar.showSnackbar("Pengeluaran ditambahkan")
                    },
                    onFailure = { snackbar.showSnackbar("Gagal menambah pengeluaran") }
                )
            }
        })
    }

    deletingExpense?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingExpense = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Hapus Pengeluaran?", fontWeight = FontWeight.Bold, color = OrangePrimary) },
            text = { Text("\"${target.description}\" sebesar Rp ${formatRupiah(target.amount)} akan dihapus.") },
            confirmButton = {
                Button(
                    onClick = {
                        deletingExpense = null
                        scope.launch {
                            repository.deleteExpense(target.id).fold(
                                onSuccess = {
                                    expenses = expenses.filter { it.id != target.id }
                                    snackbar.showSnackbar("Pengeluaran dihapus")
                                },
                                onFailure = { snackbar.showSnackbar("Gagal menghapus") }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedNegative),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { deletingExpense = null }) { Text("Batal", color = OrangePrimary) } }
        )
    }

    if (editingExpense != null) {
        EditExpenseDialog(
            expense = editingExpense!!,
            onDismiss = { editingExpense = null },
            onConfirm = { desc, amt, dt ->
                val expId = editingExpense!!.id
                editingExpense = null
                scope.launch {
                    repository.updateExpense(expId, desc, amt, dt).fold(
                        onSuccess = {
                            expenses = expenses.map { if (it.id == expId) it.copy(description = desc, amount = amt, date = dt) else it }
                            snackbar.showSnackbar("Pengeluaran diperbarui")
                        },
                        onFailure = { snackbar.showSnackbar("Gagal memperbarui pengeluaran") }
                    )
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OrangePrimary)
            error != null -> ErrorView(error = error!!, onRetry = { repository.refresh() })
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Page Header
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Pengeluaran Kas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(expenses) { expense ->
                            ExpenseItemCard(
                                expense = expense,
                                isAdmin = isAdmin,
                                onEdit = { editingExpense = expense },
                                onDelete = { deletingExpense = expense }
                            )
                        }
                        if (expenses.isEmpty()) item {
                            Text(
                                "Belum ada pengeluaran",
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    // Add Expense Button
                    if (isAdmin) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Button(
                                onClick = { showAdd = true },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Text("Tambah Pengeluaran", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun ExpenseItemCard(expense: Expense, isAdmin: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .orangeCard(16.dp)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(expense.description, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                Text(expense.date, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "-Rp ${formatRupiah(expense.amount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                if (isAdmin) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            onClick = onEdit,
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Edit", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Surface(
                            onClick = onDelete,
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Hapus", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onConfirm: (String, Int, String) -> Unit) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Tambah Pengeluaran", fontWeight = FontWeight.Bold, color = OrangePrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() }.take(9) },
                    label = { Text("Jumlah (Rp)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { newVal ->
                        val digitsOnly = newVal.filter { c -> c.isDigit() }
                        if (digitsOnly.length <= 8) date = digitsOnly
                    },
                    label = { Text("Tanggal (YYYYMMDD)") },
                    placeholder = { Text("Contoh: 20260904") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary),
                    singleLine = true,
                    isError = date.length == 8 && !isValidDate("${date.take(4)}-${date.substring(4, 6)}-${date.takeLast(2)}"),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    visualTransformation = DateVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toIntOrNull() ?: 0
                    val formattedDate = date.let { d -> if (d.length == 8) "${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}" else d }
                    if (description.isNotBlank() && amt > 0 && isValidDate(formattedDate)) onConfirm(description, amt, formattedDate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Tambah", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = OrangePrimary) }
        }
    )
}

@Composable
fun EditExpenseDialog(expense: Expense, onDismiss: () -> Unit, onConfirm: (String, Int, String) -> Unit) {
    var description by remember { mutableStateOf(expense.description) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    // Convert existing YYYY-MM-DD to digits only
    var date by remember { mutableStateOf(expense.date.replace("-", "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Edit Pengeluaran", fontWeight = FontWeight.Bold, color = OrangePrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() }.take(9) },
                    label = { Text("Jumlah (Rp)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { newVal ->
                        val digitsOnly = newVal.filter { c -> c.isDigit() }
                        if (digitsOnly.length <= 8) date = digitsOnly
                    },
                    label = { Text("Tanggal (YYYYMMDD)") },
                    placeholder = { Text("Contoh: 20260904") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary),
                    singleLine = true,
                    isError = date.length == 8 && !isValidDate("${date.take(4)}-${date.substring(4, 6)}-${date.takeLast(2)}"),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    visualTransformation = DateVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toIntOrNull() ?: 0
                    val formattedDate = date.let { d -> if (d.length == 8) "${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}" else d }
                    if (description.isNotBlank() && amt > 0 && isValidDate(formattedDate)) onConfirm(description, amt, formattedDate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = OrangePrimary) }
        }
    )
}

/** true jika tanggal YYYY-MM-DD benar-benar ada di kalender (mis. 2026-02-30 ditolak). */
fun isValidDate(value: String): Boolean =
    runCatching { java.time.LocalDate.parse(value) }.isSuccess
