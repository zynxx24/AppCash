package com.appcash.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appcash.R
import com.appcash.data.model.*
import com.appcash.data.repository.AppCashRepository
import com.appcash.ui.theme.OrangeDark
import com.appcash.ui.theme.OrangeLight
import com.appcash.ui.theme.OrangePrimary
import kotlinx.coroutines.launch

@Composable
fun PaymentsScreen(repository: AppCashRepository, isAdmin: Boolean) {
    var payments by remember { mutableStateOf<PaymentsWrapper?>(null) }
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    var dashboard by remember { mutableStateOf<Dashboard?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showEdit by remember { mutableStateOf(false) }
    var showBniPayment by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        repository.getPayments().fold(
            onSuccess = { payments = it },
            onFailure = { error = it.message ?: "Gagal memuat pembayaran" }
        )
        repository.getMembers().fold(
            onSuccess = { members = it },
            onFailure = {}
        )
        repository.getDashboard().fold(
            onSuccess = { dashboard = it },
            onFailure = {}
        )
        loading = false
    }

    if (showEdit && payments != null) {
        EditPaymentsDialog(
            wrapper = payments!!,
            members = members,
            onDismiss = { showEdit = false },
            onConfirm = { newWrapper ->
                showEdit = false
                scope.launch {
                    repository.savePayments(newWrapper).fold(
                        onSuccess = { payments = newWrapper; snackbar.showSnackbar("Pembayaran disimpan") },
                        onFailure = { snackbar.showSnackbar("Gagal menyimpan") }
                    )
                }
            }
        )
    }

    if (showBniPayment && payments != null) {
        BniPaymentDialog(
            wrapper = payments!!,
            members = members,
            onDismiss = { showBniPayment = false },
            onConfirm = { newWrapper ->
                showBniPayment = false
                scope.launch {
                    repository.savePayments(newWrapper).fold(
                        onSuccess = { payments = newWrapper; snackbar.showSnackbar("Pembayaran Kas Berhasil Dikonfirmasi!") },
                        onFailure = { snackbar.showSnackbar("Gagal memproses konfirmasi") }
                    )
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OrangePrimary)
            error != null -> ErrorView(error = error!!, onRetry = {})
            payments != null -> {
                val p = payments!!
                val d = dashboard
                Column(modifier = Modifier.fillMaxSize()) {
                    // Page Header
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Pembayaran Kas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    }

                    // Hero Card with balance, income/expense, mini chart, and Bayar Kas
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
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Label + Balance
                            Text("Total Kas XII PPLG", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
                            Text(
                                "Rp ${formatRupiah(d?.totalBalance ?: 0)}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            // Income / Expense pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Pemasukan pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Total Pemasukan", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            Text(
                                                "Rp ${formatRupiah(d?.totalKasIncome ?: 0)}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF22C55E)
                                            )
                                        }
                                    }
                                }
                                // Pengeluaran pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Total Pengeluaran", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            Text(
                                                "Rp ${formatRupiah(d?.totalExpenseAmount ?: 0)}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                }
                            }

                            // Bottom row: Mini Chart (left) + Bayar Kas button (right)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Mini bar chart
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (d != null && d.weeklyKasIncome.isNotEmpty()) {
                                        MiniBarChart(
                                            incomes = d.weeklyKasIncome,
                                            expenses = d.weeklyExpenses,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(60.dp)
                                                .padding(8.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(60.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                        }
                                    }
                                }

                                // Bayar Kas button
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showBniPayment = true }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Bayar Kas", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = OrangePrimary)
                                            Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                                        }
                                        Text("Bayar Kas via BNI / QRIS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        // Purple + pill
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color(0xFF6366F1)
                                        ) {
                                            Text(
                                                "  +  ",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment Dates List
                    if (p.dates.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada data pembayaran", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(p.dates) { idx, date ->
                                val paidCount = p.records.count { (_, dates) -> dates[date] == true }
                                PaymentDateCard(
                                    weekIndex = idx + 1,
                                    date = date,
                                    paidCount = paidCount,
                                    totalMembers = members.size,
                                    isAdmin = isAdmin,
                                    onEdit = { showEdit = true }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun PaymentDateCard(
    weekIndex: Int,
    date: String,
    paidCount: Int,
    totalMembers: Int,
    isAdmin: Boolean,
    onEdit: () -> Unit
) {
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
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Minggu ke-$weekIndex", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            "$paidCount/$totalMembers Anggota",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(date, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    if (isAdmin) {
                        TextButton(onClick = onEdit, contentPadding = PaddingValues(0.dp)) {
                            Text("Edit", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BniPaymentDialog(
    wrapper: PaymentsWrapper,
    members: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (PaymentsWrapper) -> Unit
) {
    val context = LocalContext.current
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 1) }
    var expandedMemberDropdown by remember { mutableStateOf(false) }
    var isWaConfirmed by remember { mutableStateOf(false) }
    var showQris by remember { mutableStateOf(false) }

    val records = remember(wrapper) {
        mutableStateMapOf<String, MutableMap<String, Boolean>>().apply {
            wrapper.records.forEach { (k, v) -> put(k, v.toMutableMap()) }
        }
    }

    val selectedMember = members.find { it.id == selectedMemberId } ?: members.firstOrNull()
    val memberKey = selectedMember?.id?.toString() ?: "1"
    val memberRecords = records.getOrPut(memberKey) { mutableMapOf() }
    val unpaidDates = wrapper.dates.filter { date -> memberRecords[date] != true }
    val selectedDates = remember { mutableStateMapOf<String, Boolean>() }
    val selectedCount = selectedDates.count { it.value }
    val totalAmount = selectedCount * 5000

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OrangePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Transfer Bank BNI / QRIS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OrangePrimary)
                        Text("Kas Kelas XII PPLG", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // Tab: BNI / QRIS toggle
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !showQris,
                        onClick = { showQris = false },
                        label = { Text("Transfer BNI", fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangePrimary, selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = showQris,
                        onClick = { showQris = true },
                        label = { Text("Scan QRIS", fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangePrimary, selectedLabelColor = Color.White)
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // BNI or QRIS content
                if (showQris) {
                    // QRIS Image
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Scan QR berikut untuk bayar kas:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                            Image(
                                painter = painterResource(id = R.drawable.qris_payment),
                                contentDescription = "QRIS Pembayaran Kas",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.FillWidth
                            )
                            Text(
                                "a.n. GEDE AGUS WIRA DARMA PUTRA\nNMID: ID1026507245623",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // BNI Account Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = OrangePrimary.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Rekening Tujuan Transfer:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Bank BNI", fontWeight = FontWeight.Bold, color = OrangePrimary)
                                    Text("1892077413", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                                    Text("a.n. Kas XII PPLG", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("No Rekening BNI", "1892077413"))
                                    Toast.makeText(context, "Nomor rekening disalin!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = OrangePrimary)
                                }
                            }
                        }
                    }
                }

                // Member Selection
                ExposedDropdownMenuBox(
                    expanded = expandedMemberDropdown,
                    onExpandedChange = { expandedMemberDropdown = !expandedMemberDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedMember?.let { "${it.id} - ${it.name}" } ?: "Pilih Anggota",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Anggota Kelas") },
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
                                onClick = { selectedMemberId = m.id; selectedDates.clear(); expandedMemberDropdown = false }
                            )
                        }
                    }
                }

                Text("Pilih Tanggal Kas (Rp 5.000 / minggu):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                if (unpaidDates.isEmpty()) {
                    Surface(shape = RoundedCornerShape(10.dp), color = OrangePrimary.copy(alpha = 0.1f)) {
                        Text(
                            "✓ Semua minggu kas sudah LUNAS!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    unpaidDates.forEach { date ->
                        val isChecked = selectedDates[date] ?: false
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { selectedDates[date] = it },
                                colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                            )
                            Text(date, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            Text("Rp 5.000", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Divider()

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Total Transfer:", fontWeight = FontWeight.Bold)
                    Text("Rp ${formatRupiah(totalAmount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = OrangePrimary)
                }

                Button(
                    onClick = {
                        val memberName = selectedMember?.name ?: "Siswa"
                        val message = "Halo Admin Kas XII PPLG, saya $memberName telah transfer iuran kas via BNI 1892077413 sebesar Rp ${formatRupiah(totalAmount)} ($selectedCount minggu). Mohon konfirmasi."
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=6285600487433&text=${Uri.encode(message)}"))
                        try { context.startActivity(intent) } catch (e: Exception) {
                            Toast.makeText(context, "Membuka WhatsApp Admin...", Toast.LENGTH_SHORT).show()
                        }
                        isWaConfirmed = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("1. Kirim Bukti ke WA Admin", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (isWaConfirmed) {
                    Text("✓ Sudah kirim WA Admin. Klik 'Konfirmasi' di bawah.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF25D366))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedDates.forEach { (date, isSelected) ->
                        if (isSelected) memberRecords[date] = true
                    }
                    records[memberKey] = memberRecords
                    onConfirm(PaymentsWrapper(dates = wrapper.dates, records = records))
                },
                enabled = isWaConfirmed && selectedCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("2. Konfirmasi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = OrangePrimary) }
        }
    )
}

@Composable
fun EditPaymentsDialog(wrapper: PaymentsWrapper, members: List<Member>, onDismiss: () -> Unit, onConfirm: (PaymentsWrapper) -> Unit) {
    val dates = remember(wrapper) { wrapper.dates.toMutableStateList() }
    val records = remember(wrapper) { mutableStateMapOf<String, Map<String, Boolean>>().apply { putAll(wrapper.records) } }
    var newDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Edit Pembayaran", fontWeight = FontWeight.Bold, color = OrangePrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newDate, onValueChange = { newDate = it },
                        label = { Text("Tambah Tanggal") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                    )
                    Button(
                        onClick = { if (newDate.isNotBlank() && !dates.contains(newDate)) { dates.add(newDate); newDate = "" } },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("+") }
                }
                dates.forEach { date ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(date, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { dates.remove(date) }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
                    }
                    members.forEach { member ->
                        val memberKey = member.id.toString()
                        val memberRecords = records.getOrPut(memberKey) { emptyMap() }.toMutableMap()
                        val isChecked = memberRecords[date] ?: false
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isChecked, onCheckedChange = { checked -> memberRecords[date] = checked; records[memberKey] = memberRecords }, colors = CheckboxDefaults.colors(checkedColor = OrangePrimary))
                            Text(member.name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val newRecords = dates.associateWith { dateKey ->
                    members.associate { member -> member.id.toString() to (records[member.id.toString()]?.get(dateKey) ?: false) }
                }
                onConfirm(PaymentsWrapper(dates = dates.toList(), records = newRecords))
            }, colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary), shape = RoundedCornerShape(10.dp)) { Text("Simpan", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = OrangePrimary) } }
    )
}

@Composable
fun MiniBarChart(
    incomes: List<Int>,
    expenses: List<Int>,
    modifier: Modifier = Modifier
) {
    val maxVal = (incomes.maxOrNull() ?: 1).coerceAtLeast(expenses.maxOrNull() ?: 1).toFloat()

    Canvas(modifier = modifier) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        val barCount = incomes.size.coerceAtLeast(1)
        val barWidth = (size.width / (barCount * 2.5f)).coerceAtMost(12f)
        val spacing = size.width / barCount

        incomes.forEachIndexed { i, income ->
            val xCenter = spacing * i + (spacing / 2)
            val expenseVal = expenses.getOrElse(i) { 0 }

            val incomeH = ((income / maxVal) * size.height).coerceAtLeast(4f)
            val expenseH = if (expenseVal > 0) ((expenseVal / maxVal) * size.height).coerceAtLeast(4f) else 0f

            drawRoundRect(
                color = androidx.compose.ui.graphics.Color(0xFF22C55E),
                topLeft = androidx.compose.ui.geometry.Offset(xCenter - barWidth - 1f, size.height - incomeH),
                size = androidx.compose.ui.geometry.Size(barWidth, incomeH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
            )
            if (expenseVal > 0) {
                drawRoundRect(
                    color = androidx.compose.ui.graphics.Color(0xFFEF4444),
                    topLeft = androidx.compose.ui.geometry.Offset(xCenter + 1f, size.height - expenseH),
                    size = androidx.compose.ui.geometry.Size(barWidth, expenseH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
                )
            }
        }
    }
}
