package com.appcash.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcash.AppCashApplication
import com.appcash.data.repository.AppCashRepository
import com.appcash.ui.screens.*
import com.appcash.ui.theme.AppCashTheme
import com.appcash.ui.theme.OrangeContainer
import com.appcash.ui.theme.OrangePrimary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as AppCashApplication
        val repository = AppCashRepository(app)

        setContent {
            AppCashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(repository = repository)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent(repository: AppCashRepository) {
    val isAdmin = remember { true }
    val pagerState = rememberPagerState(initialPage = 2) { bottomNavItems.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(72.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    bottomNavItems.forEachIndexed { index, screen ->
                        val selected = pagerState.currentPage == index
                        val isCenter = index == 2 // Dashboard = center

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = if (isCenter) 0.dp else 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isCenter) {
                                // Elevated center Home button
                                Box(
                                    modifier = Modifier
                                        .offset(y = (-8).dp)
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) OrangePrimary else OrangeContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                                    ) {
                                        Icon(
                                            imageVector = if (selected) screen.iconFilled else screen.icon,
                                            contentDescription = screen.label,
                                            tint = if (selected) Color.White else OrangePrimary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            } else {
                                // Regular tab with icon + label
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) OrangeContainer else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                                    ) {
                                        Icon(
                                            imageVector = if (selected) screen.iconFilled else screen.icon,
                                            contentDescription = screen.label,
                                            tint = if (selected) OrangePrimary else Color(0xFF9E9E9E),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = screen.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) OrangePrimary else Color(0xFF9E9E9E),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> PaymentsScreen(repository = repository, isAdmin = isAdmin)
                1 -> ExpensesScreen(repository = repository, isAdmin = isAdmin)
                2 -> DashboardScreen(repository = repository, isAdmin = isAdmin)
                3 -> ScheduleScreen()
                4 -> MembersScreen(repository = repository)
            }
        }
    }
}

data class ScreenItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconFilled: ImageVector = icon
)

val bottomNavItems = listOf(
    ScreenItem("payments", "Kas",        Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
    ScreenItem("expenses", "Pengeluaran",Icons.Outlined.Info,         Icons.Filled.Info),
    ScreenItem("dashboard","Dashboard",  Icons.Outlined.Home,         Icons.Filled.Home),
    ScreenItem("schedule", "Jadwal",     Icons.Outlined.DateRange,    Icons.Filled.DateRange),
    ScreenItem("members",  "Anggota",    Icons.Outlined.Person,       Icons.Filled.Person)
)
