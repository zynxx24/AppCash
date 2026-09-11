package com.appcash.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcash.data.model.Dashboard
import com.appcash.data.model.DendaInfo
import com.appcash.data.model.UserProfile
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
    var dendaExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var userPaidWeeks by remember { mutableIntStateOf(0) }
    var userTotalWeeks by remember { mutableIntStateOf(0) }
    var userDenda by remember { mutableStateOf<DendaInfo?>(null) }
    var currentMemberId by remember { mutableStateOf<Int?>(null) }

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
        // Load user-specific data
        repository.getCurrentUser().fold(
            onSuccess = { userProfile = it },
            onFailure = {}
        )
        val memberId = repository.getCurrentMemberId()
        currentMemberId = memberId
        if (memberId != null && !isAdmin) {
            repository.getPaymentStatusForMember(memberId).fold(
                onSuccess = { (paid, total) ->
                    userPaidWeeks = paid
                    userTotalWeeks = total
                },
                onFailure = {}
            )
            repository.getDendaForMember(memberId).fold(
                onSuccess = { userDenda = it },
                onFailure = {}
            )
        }
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (!isAdmin && currentMemberId != null) {
                                StudentAvatar(memberId = currentMemberId!!, size = 44)
                            }
                            Column {
                                Text(
                                    "Welcome Back,",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    if (isAdmin) "Administrator" else userProfile?.name ?: "Student XII PPLG",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
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

                    // USER: Status Kas Anda Card
                    if (!isAdmin && currentMemberId != null) {
                        val unpaidWeeks = userTotalWeeks - userPaidWeeks
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = if (unpaidWeeks == 0) GreenPositive.copy(alpha = 0.08f) else Color(0xFFFFF8E1)),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Status Kas Anda",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unpaidWeeks == 0) GreenPositive else OrangeDark
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        if (unpaidWeeks == 0) {
                                            Text(
                                                "✓ Semua kas sudah LUNAS!",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = GreenPositive
                                            )
                                        } else {
                                            Text(
                                                "⚠ $unpaidWeeks minggu belum bayar",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = OrangeDark
                                            )
                                        }
                                        Text(
                                            "$userPaidWeeks / $userTotalWeeks minggu sudah dibayar",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMedium
                                        )
                                    }
                                    if (userDenda != null) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "Denda",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = RedNegative
                                            )
                                            Text(
                                                "Rp ${formatRupiah(userDenda!!.dendaAmount)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = RedNegative
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

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
                                    LegendDot(color = GreenPositive, label = "Kas Masuk")
                                    LegendDot(color = RedNegative, label = "Pengeluaran")
                                }
                                WeeklyKasLineChart(
                                    labels = d.weeklyLabels,
                                    incomes = d.weeklyKasIncome,
                                    expenses = d.weeklyExpenses,
                                    modifier = Modifier.fillMaxWidth().height(180.dp)
                                )
                            }
                        }
                    }

                    // Denda Summary Card — Expandable
                    if (dendaList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { dendaExpanded = !dendaExpanded },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Header row with toggle icon
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "⚠️ Denda Kas Tertunggak",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RedNegative
                                    )
                                    Icon(
                                        imageVector = if (dendaExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (dendaExpanded) "Tutup" else "Buka",
                                        tint = RedNegative
                                    )
                                }
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

                                // Expandable detail list
                                AnimatedVisibility(
                                    visible = dendaExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Divider(color = RedNegative.copy(alpha = 0.2f))
                                        Text(
                                            "Detail Per Anggota:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = OrangeDark
                                        )
                                        dendaList.forEach { denda ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(RedNegative.copy(alpha = 0.06f))
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        denda.memberName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        "${denda.unpaidMonths} bulan tunggakan • ${denda.dendaPercentage.toInt()}%",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TextMedium
                                                    )
                                                }
                                                Text(
                                                    "Rp ${formatRupiah(denda.dendaAmount)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = RedNegative
                                                )
                                            }
                                        }
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
fun WeeklyKasLineChart(
    labels: List<String>,
    incomes: List<Int>,
    expenses: List<Int>,
    modifier: Modifier = Modifier
) {
    val maxVal = (incomes.maxOrNull() ?: 1).coerceAtLeast(expenses.maxOrNull() ?: 1).toFloat()
    val greenColor = Color(0xFF22C55E)
    val redColor = Color(0xFFEF4444)

    Canvas(modifier = modifier) {
        if (size.width <= 0f || size.height <= 24.dp.toPx()) return@Canvas
        val count = labels.size.coerceAtLeast(1)
        val chartHeight = size.height - 24.dp.toPx()
        val chartTop = 8.dp.toPx()
        if (chartHeight <= 0f) return@Canvas
        val stepX = if (count > 1) size.width / (count - 1).toFloat() else size.width / 2f

        // Helper to get Y coordinate
        fun getY(value: Int): Float {
            return chartTop + chartHeight - (value / maxVal) * chartHeight
        }

        // Draw grid lines
        for (i in 0..3) {
            val y = chartTop + chartHeight * i / 3f
            drawLine(
                color = Color.Gray.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }

        // Draw income line + fill
        if (incomes.size >= 2) {
            val incomePath = Path()
            val fillPath = Path()
            incomes.forEachIndexed { i, v ->
                val x = if (count > 1) stepX * i else size.width / 2f
                val y = getY(v)
                if (i == 0) { incomePath.moveTo(x, y); fillPath.moveTo(x, y) }
                else { incomePath.lineTo(x, y); fillPath.lineTo(x, y) }
            }
            // Fill area
            fillPath.lineTo(stepX * (incomes.size - 1), chartTop + chartHeight)
            fillPath.lineTo(0f, chartTop + chartHeight)
            fillPath.close()
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(greenColor.copy(alpha = 0.25f), greenColor.copy(alpha = 0.02f)),
                    startY = chartTop,
                    endY = chartTop + chartHeight
                )
            )
            // Line
            drawPath(
                path = incomePath,
                color = greenColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Dots
            incomes.forEachIndexed { i, v ->
                val x = if (count > 1) stepX * i else size.width / 2f
                drawCircle(color = greenColor, radius = 4.dp.toPx(), center = Offset(x, getY(v)))
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, getY(v)))
            }
        }

        // Draw expense line + fill
        if (expenses.size >= 2) {
            val expensePath = Path()
            val fillPath = Path()
            expenses.forEachIndexed { i, v ->
                val x = if (count > 1) stepX * i else size.width / 2f
                val y = getY(v)
                if (i == 0) { expensePath.moveTo(x, y); fillPath.moveTo(x, y) }
                else { expensePath.lineTo(x, y); fillPath.lineTo(x, y) }
            }
            fillPath.lineTo(stepX * (expenses.size - 1), chartTop + chartHeight)
            fillPath.lineTo(0f, chartTop + chartHeight)
            fillPath.close()
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(redColor.copy(alpha = 0.20f), redColor.copy(alpha = 0.02f)),
                    startY = chartTop,
                    endY = chartTop + chartHeight
                )
            )
            drawPath(
                path = expensePath,
                color = redColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            expenses.forEachIndexed { i, v ->
                if (v > 0) {
                    val x = if (count > 1) stepX * i else size.width / 2f
                    drawCircle(color = redColor, radius = 4.dp.toPx(), center = Offset(x, getY(v)))
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, getY(v)))
                }
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
