package com.appcash.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appcash.data.model.Member
import com.appcash.data.repository.AppCashRepository
import com.appcash.ui.theme.OrangeLight
import com.appcash.ui.theme.OrangePrimary
import kotlinx.coroutines.launch

@Composable
fun MembersScreen(repository: AppCashRepository, isAdmin: Boolean = false) {
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMemberForBio by remember { mutableStateOf<Member?>(null) }
    var editingMember by remember { mutableStateOf<Member?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        repository.getMembers().fold(
            onSuccess = { members = it },
            onFailure = { error = it.message ?: "Gagal memuat anggota" }
        )
        loading = false
    }

    val filteredMembers = remember(members, searchQuery) {
        if (searchQuery.isBlank()) members
        else members.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.nis.contains(searchQuery, ignoreCase = true) ||
            it.role.contains(searchQuery, ignoreCase = true)
        }
    }

    if (selectedMemberForBio != null) {
        MemberBioDialog(
            member = selectedMemberForBio!!,
            isAdmin = isAdmin,
            onDismiss = { selectedMemberForBio = null },
            onEdit = { editingMember = it; selectedMemberForBio = null }
        )
    }

    if (editingMember != null) {
        EditMemberDialog(
            member = editingMember!!,
            onDismiss = { editingMember = null },
            onConfirm = { name, nis, role, bio, phone ->
                val mId = editingMember!!.id
                editingMember = null
                scope.launch {
                    val result = repository.updateMember(mId, name, nis, role, bio, phone)
                    if (result.isSuccess) {
                        members = members.map { if (it.id == mId) it.copy(name = name, nis = nis, role = role, bio = bio, phone = phone) else it }
                        snackbar.showSnackbar("Anggota diperbarui")
                    } else {
                        snackbar.showSnackbar("Gagal memperbarui anggota")
                    }
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OrangePrimary)
            error != null -> ErrorView(error = error!!, onRetry = {})
            else -> Column(modifier = Modifier.fillMaxSize()) {
                // Page Header
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Anggota Kelas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                }

                // Class Info Header Card (orange gradient)
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
                        .padding(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("KELAS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            Text("XII PPLG", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Wali Kelas : Pak Restu", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "${members.size} Siswa",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari Nama/NIS Siswa") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = OrangePrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Members List
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMembers) { member ->
                        MemberCard(member = member, index = members.indexOf(member) + 1, onClick = { selectedMemberForBio = member })
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun MemberCard(member: Member, index: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(OrangePrimary, OrangeLight)
                )
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Profile Photo Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                StudentAvatar(memberId = member.id, size = 50)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(member.phone, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text(member.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.25f)) {
                    Text(
                        member.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                "No. $index",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MemberBioDialog(member: Member, isAdmin: Boolean, onDismiss: () -> Unit, onEdit: (Member) -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StudentAvatar(memberId = member.id, size = 48)
                Column {
                    Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(member.role, style = MaterialTheme.typography.bodySmall, color = OrangePrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(OrangePrimary, OrangeLight)
                            )
                        )
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BioPill("NIS", member.nis)
                        BioPill("Jabatan Kelas", member.role)
                        BioPill("Umur", member.umur)
                        BioPill("Kontak WA", member.phone)
                    }
                }
                Text("Bio & Catatan Siswa :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = OrangePrimary)
                Text(member.bio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isAdmin) {
                    OutlinedButton(
                        onClick = { onEdit(member) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = OrangePrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${member.phone}"))
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hubungi WA", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = OrangePrimary) }
        }
    )
}

@Composable
fun BioPill(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun BioRow(label: String, value: String) {
    BioPill(label, value)
}

@Composable
fun EditMemberDialog(member: Member, onDismiss: () -> Unit, onConfirm: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf(member.name) }
    var nis by remember { mutableStateOf(member.nis) }
    var role by remember { mutableStateOf(member.role) }
    var bio by remember { mutableStateOf(member.bio) }
    var phone by remember { mutableStateOf(member.phone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Edit Anggota", fontWeight = FontWeight.Bold, color = OrangePrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nama") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                OutlinedTextField(
                    value = nis, onValueChange = { nis = it },
                    label = { Text("NIS") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                OutlinedTextField(
                    value = role, onValueChange = { role = it },
                    label = { Text("Jabatan") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("No. Telepon") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                OutlinedTextField(
                    value = bio, onValueChange = { bio = it },
                    label = { Text("Bio") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && nis.isNotBlank()) onConfirm(name, nis, role, bio, phone)
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
