package com.appcash.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcash.data.model.DendaInfo
import com.appcash.data.model.UserProfile
import com.appcash.data.repository.AppCashRepository
import com.appcash.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    repository: AppCashRepository,
    isAdmin: Boolean,
    onLogout: () -> Unit
) {
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var dendaInfo by remember { mutableStateOf<DendaInfo?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.getCurrentUser().fold(
            onSuccess = { profile = it; loading = false },
            onFailure = { loading = false }
        )
        if (!isAdmin) {
            repository.getDendaForMember(0).fold(
                onSuccess = { /* will be fetched per member */ },
                onFailure = { }
            )
        }
        // If user, get their denda
        val role = repository.getRole()
        if (role == "user") {
            repository.getDendaForAllMembers().fold(
                onSuccess = { list ->
                    // Find current user's denda
                    val currentProfile = profile
                    if (currentProfile != null) {
                        dendaInfo = list.find { it.memberName == currentProfile.name }
                    }
                },
                onFailure = { }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GreyBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrangePrimary)
                .padding(top = 48.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(48.dp),
                        tint = OrangePrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = profile?.name ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isAdmin) "👑 Admin" else profile?.role ?: "",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Informasi Profil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isAdmin) {
                    ProfileInfoRow("Email", profile?.email ?: "-")
                } else {
                    ProfileInfoRow("NIS", profile?.nis ?: "-")
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GreyBorder)
                ProfileInfoRow("Jabatan", profile?.role ?: "-")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GreyBorder)
                ProfileInfoRow("Umur", profile?.umur ?: "-")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GreyBorder)
                ProfileInfoRow("Telepon", profile?.phone.takeIf { !it.isNullOrBlank() } ?: "-")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GreyBorder)
                ProfileInfoRow("Bio", profile?.bio.takeIf { !it.isNullOrBlank() } ?: "-")
            }
        }

        // Denda Card (for users with outstanding fines)
        if (!isAdmin && dendaInfo != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Denda",
                        tint = OrangeDark,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Denda Kas Tertunggak",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangeDark
                        )
                        Text(
                            "${dendaInfo!!.unpaidMonths} bulan nunggak (${dendaInfo!!.dendaPercentage.toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMedium
                        )
                    }
                    Text(
                        "Rp ${String.format("%,d", dendaInfo!!.dendaAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RedNegative
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Edit Profile
                TextButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = OrangePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Edit Profil",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = GreyBorder)

                // Logout
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.logout()
                            onLogout()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = RedNegative,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Keluar",
                            style = MaterialTheme.typography.bodyLarge,
                            color = RedNegative,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // App version
        Text(
            "AppCash v2.0 • XII PPLG",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Edit Profile Dialog
    if (showEditDialog && profile != null) {
        EditProfileDialog(
            profile = profile!!,
            isAdmin = isAdmin,
            onDismiss = { showEditDialog = false },
            onSave = { name, phone, bio ->
                scope.launch {
                    repository.updateProfile(name, phone, bio).fold(
                        onSuccess = {
                            // Refresh profile
                            repository.getCurrentUser().fold(
                                onSuccess = { profile = it },
                                onFailure = { }
                            )
                            showEditDialog = false
                        },
                        onFailure = { }
                    )
                }
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextDark,
            modifier = Modifier.weight(0.65f)
        )
    }
}

@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, bio: String) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var phone by remember { mutableStateOf(profile.phone) }
    var bio by remember { mutableStateOf(profile.bio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profil", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isAdmin) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. Telepon") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, phone, bio) }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
