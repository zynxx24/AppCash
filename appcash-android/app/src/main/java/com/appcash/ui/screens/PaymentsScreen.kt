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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.appcash.R
import com.appcash.data.model.*
import com.appcash.data.repository.AppCashRepository
import com.appcash.ui.theme.GreenPositive
import com.appcash.ui.theme.OrangeDark
import com.appcash.ui.theme.OrangeLight
import com.appcash.ui.theme.OrangePrimary
import com.appcash.ui.theme.RedNegative
import com.appcash.ui.theme.TextMedium
import kotlinx.coroutines.launch

// ============================================================
// PAYMENTS SCREEN – Branched by role
// ============================================================

@Composable
fun PaymentsScreen(repository: AppCashRepository, isAdmin: Boolean) {
    if (isAdmin) {
        AdminPaymentsScreen(repository)
    } else {
        UserPaymentsScreen(repository)
    }
}

// ============================================================
// ADMIN PAYMENTS SCREEN
// ============================================================

@Composable
fun AdminPaymentsScreen(repository: AppCashRepository) {
    var payments by remember { mutableStateOf<PaymentsWrapper?>(null) }
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    var dashboard by remember { mutableStateOf<Dashboard?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showAdminPayment by remember { mutableStateOf(false) }
    var selectedWeekDate by remember { mutableStateOf<String?>(null) }
    var selectedWeekIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    fun reload() {
        scope.launch {
            repository.getPayments().fold(
                onSuccess = { payments = it },
                onFailure = {}
            )
            repository.getDashboard().fold(
                onSuccess = { dashboard = it },
                onFailure = {}
            )
        }
    }

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

    // Admin Payment Dialog
    if (showAdminPayment && payments != null) {
        AdminPaymentDialog(
            wrapper = payments!!,
            members = members,
            repository = repository,
            onDismiss = { showAdminPayment = false },
            onConfirm = { newWrapper ->
                showAdminPayment = false
                scope.launch {
                    repository.savePayments(newWrapper).fold(
                        onSuccess = {
                            payments = newWrapper
                            snackbar.showSnackbar("✓ Pembayaran berhasil dicatat!")
                            reload()
                        },
                        onFailure = { snackbar.showSnackbar("Gagal menyimpan") }
                    )
                }
            }
        )
    }

    // Week Detail Dialog (admin can edit)
    if (selectedWeekDate != null && payments != null) {
        AdminWeekDetailDialog(
            weekIndex = selectedWeekIndex,
            date = selectedWeekDate!!,
            members = members,
            wrapper = payments!!,
            onDismiss = { selectedWeekDate = null },
            onSave = { newWrapper ->
                selectedWeekDate = null
                scope.launch {
                    repository.savePayments(newWrapper).fold(
                        onSuccess = {
                            payments = newWrapper
                            snackbar.showSnackbar("✓ Data pembayaran diperbarui!")
                            reload()
                        },
                        onFailure = { snackbar.showSnackbar("Gagal menyimpan") }
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
                        Text("Admin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Kelola Pembayaran Kas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    }

                    // Hero Card with balance
                    AdminHeroCard(d = d)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Add Payment Button
                    Button(
                        onClick = { showAdminPayment = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Pembayaran", fontWeight = FontWeight.Bold, color = Color.White)
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
                                    isAdmin = true,
                                    onClick = {
                                        selectedWeekIndex = idx + 1
                                        selectedWeekDate = date
                                    },
                                    onEdit = {
                                        selectedWeekIndex = idx + 1
                                        selectedWeekDate = date
                                    }
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
fun AdminHeroCard(d: Dashboard?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(colors = listOf(OrangePrimary, OrangeLight)))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Total Kas XII PPLG", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
            Text(
                "Rp ${formatRupiah(d?.totalBalance ?: 0)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Text("Total Pemasukan", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            "Rp ${formatRupiah(d?.totalKasIncome ?: 0)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
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
    }
}

// ============================================================
// ADMIN PAYMENT DIALOG – Select student → show unpaid → confirm
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPaymentDialog(
    wrapper: PaymentsWrapper,
    members: List<Member>,
    repository: AppCashRepository,
    onDismiss: () -> Unit,
    onConfirm: (PaymentsWrapper) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 1) }
    var expandedMemberDropdown by remember { mutableStateOf(false) }

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
    val kasAmount = selectedCount * 5000

    // Reset selection when member changes
    LaunchedEffect(selectedMemberId) {
        selectedDates.clear()
    }

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
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Tambah Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OrangePrimary)
                        Text("Pilih siswa & centang minggu yang dibayar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Member Selection with photo
                ExposedDropdownMenuBox(
                    expanded = expandedMemberDropdown,
                    onExpandedChange = { expandedMemberDropdown = !expandedMemberDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedMember?.let { "${it.id} - ${it.name}" } ?: "Pilih Anggota",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Siswa") },
                        leadingIcon = {
                            if (selectedMember != null) {
                                StudentAvatar(memberId = selectedMember.id, size = 28)
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary)
                            }
                        },
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
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        StudentAvatar(memberId = m.id, size = 28)
                                        Text("${m.id} - ${m.name}", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = { selectedMemberId = m.id; expandedMemberDropdown = false }
                            )
                        }
                    }
                }

                // Unpaid weeks header
                Text("Minggu yang belum dibayar (Rp 5.000/minggu):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = OrangeDark)

                if (unpaidDates.isEmpty()) {
                    Surface(shape = RoundedCornerShape(10.dp), color = GreenPositive.copy(alpha = 0.1f)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = GreenPositive, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Semua minggu kas sudah LUNAS!", style = MaterialTheme.typography.bodyMedium, color = GreenPositive, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Select all button
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            unpaidDates.forEach { selectedDates[it] = true }
                        }) {
                            Text("Pilih Semua (${unpaidDates.size})", color = OrangePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    unpaidDates.forEach { date ->
                        val isChecked = selectedDates[date] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isChecked) OrangePrimary.copy(alpha = 0.08f) else Color.Transparent)
                                .clickable { selectedDates[date] = !isChecked }
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { selectedDates[date] = it },
                                colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                            )
                            Text(date, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text("Rp 5.000", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (selectedCount > 0) {
                    Divider()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = OrangePrimary.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Kas ($selectedCount minggu):", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                                Text("Rp ${formatRupiah(kasAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Total:", fontWeight = FontWeight.Bold)
                                Text("Rp ${formatRupiah(kasAmount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = OrangePrimary)
                            }
                        }
                    }
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
                enabled = selectedCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Konfirmasi ($selectedCount minggu)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = OrangePrimary) }
        }
    )
}

// ============================================================
// ADMIN WEEK DETAIL DIALOG – With editable checkboxes
// ============================================================

@Composable
fun AdminWeekDetailDialog(
    weekIndex: Int,
    date: String,
    members: List<Member>,
    wrapper: PaymentsWrapper,
    onDismiss: () -> Unit,
    onSave: (PaymentsWrapper) -> Unit
) {
    val records = remember(wrapper) {
        mutableStateMapOf<String, MutableMap<String, Boolean>>().apply {
            wrapper.records.forEach { (k, v) -> put(k, v.toMutableMap()) }
        }
    }

    val paidCount = members.count { member ->
        records[member.id.toString()]?.get(date) == true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    "Minggu ke-$weekIndex",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = OrangePrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        "$paidCount / ${members.size} Sudah Bayar",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                members.forEach { member ->
                    val memberKey = member.id.toString()
                    val memberMap = records.getOrPut(memberKey) { mutableMapOf() }
                    val isPaid = memberMap[date] == true

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPaid) GreenPositive.copy(alpha = 0.08f) else RedNegative.copy(alpha = 0.04f))
                            .clickable {
                                memberMap[date] = !isPaid
                                records[memberKey] = memberMap
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPaid,
                            onCheckedChange = { checked ->
                                memberMap[date] = checked
                                records[memberKey] = memberMap
                            },
                            colors = CheckboxDefaults.colors(checkedColor = GreenPositive),
                            modifier = Modifier.size(36.dp)
                        )
                        StudentAvatar(memberId = member.id, size = 30)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            member.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isPaid) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isPaid) GreenPositive else RedNegative.copy(alpha = 0.2f)
                        ) {
                            Text(
                                if (isPaid) "✓" else "✗",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPaid) Color.White else RedNegative,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(PaymentsWrapper(dates = wrapper.dates, records = records))
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = OrangePrimary) }
        }
    )
}

// ============================================================
// USER PAYMENTS SCREEN – Auto-detect logged-in user
// ============================================================

@Composable
fun UserPaymentsScreen(repository: AppCashRepository) {
    var payments by remember { mutableStateOf<PaymentsWrapper?>(null) }
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    var dashboard by remember { mutableStateOf<Dashboard?>(null) }
    var currentMemberId by remember { mutableStateOf<Int?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showBniPayment by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        currentMemberId = repository.getCurrentMemberId()
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

    val currentMember = members.find { it.id == currentMemberId }
    val memberKey = currentMemberId?.toString() ?: "0"
    val memberRecords = payments?.records?.get(memberKey) ?: emptyMap()
    val allDates = payments?.dates ?: emptyList()
    val paidCount = allDates.count { memberRecords[it] == true }
    val unpaidCount = allDates.size - paidCount
    val unpaidDates = allDates.filter { memberRecords[it] != true }

    // Bayar Kas Dialog for user
    if (showBniPayment && payments != null && currentMemberId != null) {
        UserBniPaymentDialog(
            wrapper = payments!!,
            currentMember = currentMember ?: Member(0, "Unknown"),
            currentMemberId = currentMemberId!!,
            repository = repository,
            onDismiss = { showBniPayment = false },
            onConfirm = { newWrapper ->
                showBniPayment = false
                scope.launch {
                    repository.savePayments(newWrapper).fold(
                        onSuccess = {
                            payments = newWrapper
                            snackbar.showSnackbar("✓ Pembayaran Kas Berhasil Dikonfirmasi!")
                        },
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Page Header
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            Text("Pembayaran Kas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Status Kas Saya", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // User Info Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.linearGradient(colors = listOf(OrangePrimary, OrangeLight)))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (currentMemberId != null) {
                                    StudentAvatar(memberId = currentMemberId!!, size = 56)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(currentMember?.name ?: "Siswa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(currentMember?.nis ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(shape = RoundedCornerShape(20.dp), color = if (unpaidCount == 0) GreenPositive else Color.White.copy(alpha = 0.25f)) {
                                            Text(
                                                if (unpaidCount == 0) "✓ LUNAS" else "$paidCount/${allDates.size} Lunas",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        if (unpaidCount > 0) {
                                            Surface(shape = RoundedCornerShape(20.dp), color = RedNegative.copy(alpha = 0.9f)) {
                                                Text(
                                                    "$unpaidCount belum bayar",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bayar Kas Button
                    if (unpaidCount > 0) {
                        item {
                            Button(
                                onClick = { showBniPayment = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bayar Kas ($unpaidCount minggu)", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Personal Payment List Header
                    item {
                        Text(
                            "Riwayat Pembayaran Per Minggu",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    // Week-by-week status
                    itemsIndexed(allDates) { idx, date ->
                        val isPaid = memberRecords[date] == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isPaid) GreenPositive.copy(alpha = 0.08f) else RedNegative.copy(alpha = 0.05f))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Minggu ke-${idx + 1}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isPaid) GreenPositive else RedNegative.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    if (isPaid) "✓ Lunas" else "Belum Bayar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPaid) Color.White else RedNegative,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ============================================================
// USER BNI PAYMENT DIALOG – Pre-filled with logged-in user
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBniPaymentDialog(
    wrapper: PaymentsWrapper,
    currentMember: Member,
    currentMemberId: Int,
    repository: AppCashRepository,
    onDismiss: () -> Unit,
    onConfirm: (PaymentsWrapper) -> Unit
) {
    val context = LocalContext.current
    var isWaConfirmed by remember { mutableStateOf(false) }
    var showQris by remember { mutableStateOf(false) }

    val records = remember(wrapper) {
        mutableStateMapOf<String, MutableMap<String, Boolean>>().apply {
            wrapper.records.forEach { (k, v) -> put(k, v.toMutableMap()) }
        }
    }

    val memberKey = currentMemberId.toString()
    val memberRecords = records.getOrPut(memberKey) { mutableMapOf() }
    val unpaidDates = wrapper.dates.filter { date -> memberRecords[date] != true }
    val selectedDates = remember { mutableStateMapOf<String, Boolean>() }
    val selectedCount = selectedDates.count { it.value }
    val baseKasAmount = selectedCount * 5000

    var memberDendaInfo by remember { mutableStateOf<DendaInfo?>(null) }
    LaunchedEffect(currentMemberId) {
        repository.getDendaForMember(currentMemberId).onSuccess { info ->
            memberDendaInfo = info
        }
    }

    val dendaAmount = if (selectedCount > 0) (memberDendaInfo?.dendaAmount ?: 0) else 0
    val totalAmount = baseKasAmount + dendaAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StudentAvatar(memberId = currentMemberId, size = 40)
                    Column {
                        Text("Bayar Kas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OrangePrimary)
                        Text(currentMember.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
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
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
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

                // Unpaid week checkboxes
                Text("Pilih Minggu Kas (Rp 5.000 / minggu):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                if (unpaidDates.isEmpty()) {
                    Surface(shape = RoundedCornerShape(10.dp), color = GreenPositive.copy(alpha = 0.1f)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = GreenPositive, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Semua minggu kas sudah LUNAS!", style = MaterialTheme.typography.bodyMedium, color = GreenPositive, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    unpaidDates.forEach { date ->
                        val isChecked = selectedDates[date] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChecked) OrangePrimary.copy(alpha = 0.06f) else Color.Transparent)
                                .clickable { selectedDates[date] = !isChecked }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

                // Payment Breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = OrangePrimary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Kas ($selectedCount minggu):", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                            Text("Rp ${formatRupiah(baseKasAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        if (dendaAmount > 0) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Denda Keterlambatan:", style = MaterialTheme.typography.bodySmall, color = RedNegative, fontWeight = FontWeight.SemiBold)
                                Text("Rp ${formatRupiah(dendaAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = RedNegative)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Total Transfer:", fontWeight = FontWeight.Bold)
                            Text("Rp ${formatRupiah(totalAmount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = OrangePrimary)
                        }
                    }
                }

                Button(
                    onClick = {
                        val memberName = currentMember.name
                        val dendaText = if (dendaAmount > 0) " (termasuk denda Rp ${formatRupiah(dendaAmount)})" else ""
                        val message = "Halo Admin Kas XII PPLG, saya $memberName telah transfer iuran kas via BNI 1892077413 sebesar Rp ${formatRupiah(totalAmount)} ($selectedCount minggu$dendaText). Mohon konfirmasi."
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

// ============================================================
// SHARED COMPONENTS
// ============================================================

@Composable
fun PaymentDateCard(
    weekIndex: Int,
    date: String,
    paidCount: Int,
    totalMembers: Int,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colors = listOf(OrangePrimary, OrangeLight)))
            .clickable { onClick() }
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
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f)) {
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

/**
 * Global LRU bitmap cache for student avatars.
 * Stores pre-decoded, downsampled ImageBitmap instances keyed by
 * "resId_targetPx" to avoid redundant BitmapFactory decoding cycles.
 * Limited to 20 entries (~200KB max at 200x200) to balance memory vs cache hits.
 */
private val avatarBitmapCache = object : LinkedHashMap<String, androidx.compose.ui.graphics.ImageBitmap>(20, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, androidx.compose.ui.graphics.ImageBitmap>?): Boolean {
        return size > 20
    }
}

/**
 * Calculates the optimal BitmapFactory inSampleSize power-of-2 factor.
 * Subsamples the image so the decoded bitmap's dimensions roughly match
 * the target display size, reducing memory allocation by up to 16x.
 *
 * @param outWidth  Original image width from BitmapFactory.Options.outWidth
 * @param outHeight Original image height from BitmapFactory.Options.outHeight
 * @param reqSize   Target display size in pixels
 * @return Power-of-2 sample size (1, 2, 4, 8, etc.)
 */
private fun calculateInSampleSize(outWidth: Int, outHeight: Int, reqSize: Int): Int {
    var inSampleSize = 1
    if (outHeight > reqSize || outWidth > reqSize) {
        val halfHeight = outHeight / 2
        val halfWidth = outWidth / 2
        while ((halfHeight / inSampleSize) >= reqSize && (halfWidth / inSampleSize) >= reqSize) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

/**
 * Performance-optimized composable for rendering student profile photos.
 *
 * Optimization pipeline:
 * 1. Resolves drawable resource ID via cached `remember(memberId)`
 * 2. Uses BitmapFactory.Options.inJustDecodeBounds to read image dimensions
 *    WITHOUT allocating any pixel memory (zero-copy metadata read)
 * 3. Calculates power-of-2 inSampleSize to subsample the JPEG at decode time,
 *    reducing memory allocation proportional to (1/inSampleSize²)
 * 4. Decoded ImageBitmap is stored in a global LRU LinkedHashMap cache
 *    keyed by "resId_targetPx", eliminating redundant decode operations
 *    when the same avatar appears on multiple screens or recomposes
 * 5. Falls back to a styled initial-letter circle if no photo resource exists
 *
 * @param memberId Student member ID, mapped to R.drawable.student_{memberId}
 * @param size     Display size in dp (converted to px for sampling calculation)
 */
@Composable
fun StudentAvatar(memberId: Int, size: Int = 44) {
    val context = LocalContext.current
    val density = LocalContext.current.resources.displayMetrics.density
    val targetPx = (size * density).toInt()

    // Resolve drawable resource ID once per memberId (cached across recompositions)
    val resId = remember(memberId) {
        val resName = "student_$memberId"
        context.resources.getIdentifier(resName, "drawable", context.packageName)
    }

    if (resId != 0) {
        // Decode and cache the bitmap with subsampling for the target display size
        val imageBitmap = remember(resId, targetPx) {
            val cacheKey = "${resId}_$targetPx"
            avatarBitmapCache.getOrPut(cacheKey) {
                val options = android.graphics.BitmapFactory.Options().apply {
                    // Phase 1: Decode bounds only (no pixel allocation)
                    inJustDecodeBounds = true
                }
                android.graphics.BitmapFactory.decodeResource(context.resources, resId, options)

                // Phase 2: Calculate optimal subsample ratio and decode pixels
                options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, targetPx)
                options.inJustDecodeBounds = false
                val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, resId, options)
                bitmap.asImageBitmap()
            }
        }

        Image(
            bitmap = imageBitmap,
            contentDescription = "Foto Profil",
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        // Fallback: styled circle with member ID initial
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(OrangePrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = memberId.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = OrangePrimary
            )
        }
    }
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
