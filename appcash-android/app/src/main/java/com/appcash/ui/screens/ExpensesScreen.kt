package com.appcash.ui.screens

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
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        loading = true
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
            error != null -> ErrorView(error = error!!, onRetry = {})
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Page Header
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Pengeluaran Kas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(expenses) { expense ->
                            ExpenseItemCard(
                                expense = expense,
                                isAdmin = isAdmin,
                                onEdit = { editingExpense = expense },
                                onDelete = {
                                    scope.launch {
                                        repository.deleteExpense(expense.id).fold(
                                            onSuccess = {
                                                expenses = expenses.filter { it.id != expense.id }
                                                snackbar.showSnackbar("Pengeluaran dihapus")
                                            },
                                            onFailure = { snackbar.showSnackbar("Gagal menghapus") }
                                        )
                                    }
                                }
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
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(OrangePrimary, OrangeLight)
                )
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(expense.description, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                Text(expense.date, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "-${formatRupiah(expense.amount)}",
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
                    value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() } },
                    label = { Text("Jumlah (Rp)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                OutlinedTextField(
                    value = date, onValueChange = { date = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toIntOrNull() ?: 0
                    if (description.isNotBlank() && amt > 0 && date.isNotBlank()) onConfirm(description, amt, date)
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
    var date by remember { mutableStateOf(expense.date) }

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
                    value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() } },
                    label = { Text("Jumlah (Rp)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                OutlinedTextField(
                    value = date, onValueChange = { date = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toIntOrNull() ?: 0
                    if (description.isNotBlank() && amt > 0 && date.isNotBlank()) onConfirm(description, amt, date)
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
