package com.appcash.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.appcash.data.model.Member
import com.appcash.data.repository.AppCashRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(repository: AppCashRepository, onLoginSuccess: (role: String) -> Unit) {
    var selectedRole by remember { mutableStateOf("admin") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var memberId by remember { mutableStateOf("") }
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (repository.hasToken()) {
            val role = repository.getRole() ?: "user"
            onLoginSuccess(role)
        }
        repository.getMembers().fold(
            onSuccess = { members = it },
            onFailure = { }
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.padding(24.dp).fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("AppCash", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("XII PPLG Cash Management", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedRole == "admin",
                        onClick = { selectedRole = "admin" },
                        label = { Text("Admin") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedRole == "user",
                        onClick = { selectedRole = "user" },
                        label = { Text("Siswa") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedRole == "admin") {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = members.find { it.id == memberId.toIntOrNull() }?.name ?: if (memberId.isBlank()) "Pilih Anggota" else "ID: $memberId",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Anggota") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            members.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text("${member.id} - ${member.name}") },
                                    onClick = {
                                        memberId = member.id.toString()
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (error != null) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        error = null
                        loading = true
                        scope.launch {
                            val memberIdInt = memberId.toIntOrNull()
                            val result = repository.login(
                                role = selectedRole,
                                username = if (selectedRole == "admin") username else null,
                                password = if (selectedRole == "admin") password else null,
                                memberId = if (selectedRole == "user") memberIdInt else null,
                                name = if (selectedRole == "user") members.find { it.id == memberIdInt }?.name else null
                            )
                            when {
                                result.isSuccess -> {
                                    if (selectedRole == "user" && memberIdInt != null) repository.setMemberId(memberIdInt)
                                    onLoginSuccess(selectedRole)
                                }
                                else -> error = result.exceptionOrNull()?.message ?: "Login gagal"
                            }
                            loading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Masuk")
                }
            }
        }
    }
}
