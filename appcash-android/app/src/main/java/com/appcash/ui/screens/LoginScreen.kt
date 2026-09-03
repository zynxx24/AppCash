package com.appcash.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.appcash.data.model.Member
import com.appcash.data.repository.AppCashRepository
import com.appcash.ui.theme.OrangePrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(repository: AppCashRepository, onLoginSuccess: (role: String) -> Unit) {
    var isEmailMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var memberId by remember { mutableStateOf("") }
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.getMembers().fold(
            onSuccess = { members = it },
            onFailure = { }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo / Title
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = OrangePrimary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "💸",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "AppCash XII PPLG",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Sistem Pengelolaan Kas Kelas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Unified Login Inputs
                if (!isEmailMode) {
                    // Member Selection Dropdown
                    var expanded by remember { mutableStateOf(false) }
                    val selectedMember = members.find { it.id == memberId.toIntOrNull() }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMember?.let { "${it.id} - ${it.name}" } ?: "Pilih Anggota",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Nama Siswa") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                focusedLabelColor = OrangePrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            members.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text("${member.id} - ${member.name}") },
                                    onClick = {
                                        memberId = member.id.toString()
                                        expanded = false
                                        error = null
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Email & Password Fields
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; error = null },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            focusedLabelColor = OrangePrimary
                        )
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            focusedLabelColor = OrangePrimary
                        )
                    )
                }

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Submit Button
                Button(
                    onClick = {
                        error = null
                        loading = true
                        scope.launch {
                            if (isEmailMode) {
                                val result = repository.login(
                                    role = "admin",
                                    email = email,
                                    password = password
                                )
                                if (result.isSuccess) {
                                    onLoginSuccess("admin")
                                } else {
                                    error = result.exceptionOrNull()?.message ?: "Email atau password salah"
                                }
                            } else {
                                val memberIdInt = memberId.toIntOrNull()
                                if (memberIdInt != null) {
                                    val memberName = members.find { it.id == memberIdInt }?.name
                                    val result = repository.login(
                                        role = "user",
                                        memberId = memberIdInt,
                                        name = memberName
                                    )
                                    if (result.isSuccess) {
                                        repository.setMemberId(memberIdInt)
                                        onLoginSuccess("user")
                                    } else {
                                        error = result.exceptionOrNull()?.message ?: "Login gagal"
                                    }
                                } else {
                                    error = "Silakan pilih nama siswa terlebih dahulu"
                                }
                            }
                            loading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            "Masuk",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Toggle Email / Siswa Login Mode (without "Admin" label)
                TextButton(
                    onClick = {
                        isEmailMode = !isEmailMode
                        error = null
                    }
                ) {
                    Text(
                        text = if (isEmailMode) "← Masuk sebagai Siswa (Pilih Nama)" else "Masuk dengan Email & Password",
                        color = OrangePrimary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
