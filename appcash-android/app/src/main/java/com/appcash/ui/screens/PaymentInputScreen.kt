package com.appcash.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.appcash.data.model.Member
import com.appcash.data.model.PaymentRecord
import com.appcash.data.repository.AppCashRepository
import com.appcash.ui.theme.OrangeLight
import com.appcash.ui.theme.OrangePrimary
import kotlinx.coroutines.launch

/**
 * VisualTransformation for date input: user types digits only,
 * displayed as YYYY-MM-DD with auto-inserted dashes.
 */
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val out = StringBuilder()
        digits.indices.forEach { i ->
            val c = digits[i]
            out.append(c)
            if ((i == 3 || i == 5) && i < digits.length - 1) out.append('-')
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 6) return offset + 1
                return offset + 2
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 7) return offset - 1
                return offset - 2
            }
        }
        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentInputScreen(repository: AppCashRepository) {
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    var paymentHistory by remember { mutableStateOf<List<PaymentRecord>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Form state
    var selectedMemberId by remember { mutableIntStateOf(0) }
    var expandedMemberDropdown by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("5000") }
    var dateText by remember { mutableStateOf("20260904") }
    var noteText by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // Load data
    LaunchedEffect(Unit) {
        loading = true
        repository.getMembers().fold(
            onSuccess = {
                members = it
                if (selectedMemberId == 0 && it.isNotEmpty()) selectedMemberId = it.first().id
            },
            onFailure = {}
        )
        repository.getPaymentHistory().fold(
            onSuccess = { paymentHistory = it },
            onFailure = {}
        )
        loading = false
    }

    val selectedMember = members.find { it.id == selectedMemberId }
    val amount = amountText.toIntOrNull() ?: 0

    // Confirm submit dialog
    if (showConfirmDialog && selectedMember != null && amount > 0) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            },
            title = { Text("Konfirmasi Pembayaran", fontWeight = FontWeight.Bold, color = OrangePrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Nama Anggota", selectedMember!!.name)
                    DetailRow("Jumlah", "Rp ${formatRupiah(amount)}")
                    DetailRow("Tanggal", dateText.let { d -> if (d.length == 8) "${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}" else d })
                    if (noteText.isNotBlank()) DetailRow("Keterangan", noteText)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        scope.launch {
                            repository.addPaymentRecord(
                                memberId = selectedMemberId,
                                amount = amount,
                                date = dateText.let { d -> if (d.length == 8) "${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}" else d },
                                note = noteText.ifBlank { "Pembayaran Kas" }
                            ).fold(
                                onSuccess = {
                                    noteText = ""
                                    // Reload history
                                    repository.getPaymentHistory().fold(
                                        onSuccess = { paymentHistory = it },
                                        onFailure = {}
                                    )
                                    snackbar.showSnackbar("✓ Pembayaran berhasil dicatat!")
                                },
                                onFailure = {
                                    snackbar.showSnackbar("Gagal mencatat pembayaran")
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Konfirmasi", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal", color = OrangePrimary)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OrangePrimary)
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Page Header
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Input Pembayaran", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Hero Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(OrangePrimary, OrangeLight)
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    Column {
                                        Text("Input Pembayaran Kas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                        Text("Catat pembayaran kas anggota kelas", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                                    Text(
                                        "${paymentHistory.size} Transaksi Tercatat",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Form Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text("Form Pembayaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OrangePrimary)

                                // Member Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = expandedMemberDropdown,
                                    onExpandedChange = { expandedMemberDropdown = !expandedMemberDropdown }
                                ) {
                                    OutlinedTextField(
                                        value = selectedMember?.let { "${it.id} - ${it.name}" } ?: "Pilih Anggota",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Nama Anggota") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMemberDropdown) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedMemberDropdown,
                                        onDismissRequest = { expandedMemberDropdown = false }
                                    ) {
                                        members.forEach { m ->
                                            DropdownMenuItem(
                                                text = { Text("${m.id} - ${m.name}") },
                                                onClick = { selectedMemberId = m.id; expandedMemberDropdown = false }
                                            )
                                        }
                                    }
                                }

                                // Amount Input
                                OutlinedTextField(
                                    value = amountText,
                                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                                    label = { Text("Jumlah Pembayaran (Rp)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary),
                                    singleLine = true,
                                    supportingText = {
                                        if (amount > 0) Text("Rp ${formatRupiah(amount)}", color = OrangePrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                )

                                // Date Input with auto-format
                                OutlinedTextField(
                                    value = dateText,
                                    onValueChange = { newVal ->
                                        val digitsOnly = newVal.filter { c -> c.isDigit() }
                                        if (digitsOnly.length <= 8) dateText = digitsOnly
                                    },
                                    label = { Text("Tanggal (YYYYMMDD)") },
                                    placeholder = { Text("Contoh: 20260904") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary),
                                    singleLine = true,
                                    visualTransformation = DateVisualTransformation(),
                                    supportingText = {
                                        if (dateText.length == 8) {
                                            Text(
                                                "${dateText.substring(0,4)}-${dateText.substring(4,6)}-${dateText.substring(6,8)}",
                                                color = OrangePrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        } else {
                                            Text("Ketik 8 digit angka", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                )

                                // Note Input
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it },
                                    label = { Text("Keterangan (opsional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary),
                                    singleLine = true
                                )

                                // Submit Button
                                Button(
                                    onClick = { showConfirmDialog = true },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    enabled = selectedMember != null && amount > 0 && dateText.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Catat Pembayaran", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Recent Payments Header
                    if (paymentHistory.isNotEmpty()) {
                        item {
                            Text(
                                "Riwayat Pembayaran Terbaru",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        items(paymentHistory.take(20)) { record ->
                            PaymentHistoryCard(record = record)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun PaymentHistoryCard(record: PaymentRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(OrangePrimary, OrangeLight)
                )
            )
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Initial Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = record.memberName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(record.memberName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(record.note, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Rp ${formatRupiah(record.amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(record.date, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
            }
        }
    }
}
