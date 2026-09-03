package com.appcash.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcash.data.model.Dashboard
import com.appcash.data.model.DendaInfo
import com.appcash.data.repository.AppCashRepository
import com.appcash.ui.theme.GreenPositive
import com.appcash.ui.theme.OrangeDark
import com.appcash.ui.theme.OrangeLight
import com.appcash.ui.theme.OrangePrimary
import com.appcash.ui.theme.RedNegative
import com.appcash.ui.theme.TextMedium

@Composable
fun DashboardScreen(repository: AppCashRepository, @Suppress("UNUSED_PARAMETER") isAdmin: Boolean) {
    var dashboard by remember { mutableStateOf<Dashboard?>(null) }
    var dendaList by remember { mutableStateOf<List<DendaInfo>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        repository.getDashboard().fold(
            onSuccess = { dashboard = it },
            onFailure = { error = it.message ?: "Gagal memuat dashboard" }
        )
        repository.getDendaForAllMembers().fold(
            onSuccess = { dendaList = it },
            onFailure = { }
        )
        loading = false
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = OrangePrimary
            )
            error != null -> ErrorView(error = error!!, onRetry = {})
            dashboard != null -> {
                val d = dashboard!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Welcome Back,",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Student XII PPLG",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Hero Balance Card (orange gradient)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(OrangePrimary, OrangeLight)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Total Kas XII PPLG",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Rp ${formatRupiah(d.totalBalance)}",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Income pill
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            "Pemasukan",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "Rp ${formatRupiah(d.totalKasIncome)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                // Expense pill
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            "Pengeluaran",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            "Rp ${formatRupiah(d.totalExpenseAmount)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stat Cards Row – 2 orange gradient cards matching design
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Anggota Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(OrangePrimary, OrangeLight)
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
                                Text("Anggota XII PPLG", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        "${d.totalMembers} Siswa",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        // Periode Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(OrangePrimary, OrangeLight)
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
                                Text("Periode Mingguan", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        "${d.totalPaymentDates} Minggu",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chart Card
                    if (d.weeklyKasIncome.isNotEmpty()) {
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
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Grafik Tren Kas Mingguan",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangePrimary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    LegendDot(color = RedNegative, label = "Pengeluaran")
                                    LegendDot(color = GreenPositive, label = "Kas Masuk")
                                }
                                WeeklyKasBarChart(
                                    labels = d.weeklyLabels,
                                    incomes = d.weeklyKasIncome,
                                    expenses = d.weeklyExpenses,
                                    modifier = Modifier.fillMaxWidth().height(160.dp)
                                )
                            }
                        }
                    }

                    // Denda Summary Card
                    if (dendaList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
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
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "⚠️ Denda Kas Tertunggak",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RedNegative
                                )
                                Text(
                                    "Denda 5% per bulan dari total kas yang belum dibayar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMedium
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            "${dendaList.size} Anggota",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = OrangeDark
                                        )
                                        Text(
                                            "punya tunggakan",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMedium
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "Rp ${formatRupiah(dendaList.sumOf { it.dendaAmount })}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RedNegative
                                        )
                                        Text(
                                            "total denda",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DashStatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrangePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
            }
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = OrangeDark)
        }
    }
}

@Composable
fun WeeklyKasBarChart(
    labels: List<String>,
    incomes: List<Int>,
    expenses: List<Int>,
    modifier: Modifier = Modifier
) {
    val maxVal = (incomes.maxOrNull() ?: 1).coerceAtLeast(expenses.maxOrNull() ?: 1).toFloat()

    Canvas(modifier = modifier) {
        if (size.width <= 0f || size.height <= 24.dp.toPx()) return@Canvas
        val barWidth = 14.dp.toPx()
        val spacing = size.width / (labels.size.coerceAtLeast(1))
        val chartHeight = size.height - 24.dp.toPx()
        if (chartHeight <= 0f) return@Canvas

        labels.forEachIndexed { i, _ ->
            val xCenter = spacing * i + (spacing / 2)
            val incomeVal = incomes.getOrElse(i) { 0 }
            val expenseVal = expenses.getOrElse(i) { 0 }

            val incomeBarHeight = ((incomeVal / maxVal) * chartHeight).coerceAtLeast(4f)
            val expenseBarHeight = ((expenseVal / maxVal) * chartHeight).coerceAtLeast(if (expenseVal > 0) 4f else 0f)

            drawRoundRect(
                color = Color(0xFF22C55E),
                topLeft = Offset(xCenter - barWidth - 2.dp.toPx(), chartHeight - incomeBarHeight),
                size = Size(barWidth, incomeBarHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            if (expenseVal > 0) {
                drawRoundRect(
                    color = Color(0xFFEF4444),
                    topLeft = Offset(xCenter + 2.dp.toPx(), chartHeight - expenseBarHeight),
                    size = Size(barWidth, expenseBarHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = error, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) {
            Text("Coba lagi")
        }
    }
}

fun formatRupiah(value: Int): String {
    return "%,d".format(value).replace(',', '.')
}
