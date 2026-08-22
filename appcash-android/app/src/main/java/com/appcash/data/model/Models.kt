package com.appcash.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val role: String,
    val username: String? = null,
    val password: String? = null,
    @SerializedName("member_id") val memberId: Int? = null,
    val name: String? = null
)

data class LoginResponse(
    val token: String,
    val role: String
)

data class IdResponse(
    val id: Int
)

data class Dashboard(
    @SerializedName("total_balance") val totalBalance: Int,
    @SerializedName("previous_balance") val previousBalance: Int,
    @SerializedName("kas_amount") val kasAmount: Int,
    @SerializedName("total_kas_income") val totalKasIncome: Int,
    @SerializedName("total_payments") val totalPayments: Int,
    @SerializedName("total_payment_dates") val totalPaymentDates: Int,
    @SerializedName("total_members") val totalMembers: Int,
    @SerializedName("total_expense_amount") val totalExpenseAmount: Int,
    @SerializedName("total_expense_count") val totalExpenseCount: Int,
    @SerializedName("latest_paid") val latestPaid: Int,
    val weeklyLabels: List<String> = emptyList(),
    val weeklyKasIncome: List<Int> = emptyList(),
    val weeklyExpenses: List<Int> = emptyList()
)

data class Member(
    val id: Int,
    val name: String,
    val nis: String = "",
    val role: String = "Anggota",
    val bio: String = "",
    val umur: String = "17 Tahun",
    val phone: String = "",
    val avatarUrl: String = ""
)

data class Expense(
    val id: Int,
    val description: String,
    val amount: Int,
    val date: String,
    val category: String = "Perlengkapan"
)

data class Levy(
    val id: Int,
    val title: String,
    val amount: Int,
    @SerializedName("start_date") val startDate: String,
    val deadline: String,
    @SerializedName("paid_members") val paidMembers: List<Int> = emptyList(),
    val transferred: Boolean = false
)

data class Config(
    @SerializedName("kas_amount") val kasAmount: Int = 5000,
    @SerializedName("previous_balance") val previousBalance: Int = 500000,
    @SerializedName("real_balance") val realBalance: Int = 3365000,
    @SerializedName("class_name") val className: String = "XII PPLG",
    val year: String = "2026/2027"
)

data class PaymentsWrapper(
    val dates: List<String> = emptyList(),
    val records: Map<String, Map<String, Boolean>> = emptyMap()
)

data class ExpenseRequest(
    val description: String,
    val amount: Int,
    val date: String,
    val category: String = "Perlengkapan"
)

data class LevyRequest(
    val title: String,
    val amount: Int,
    @SerializedName("start_date") val startDate: String,
    val deadline: String,
    @SerializedName("paid_members") val paidMembers: List<Int> = emptyList(),
    val transferred: Boolean = false
)

data class LevyPaymentRequest(
    @SerializedName("member_id") val memberId: Int
)

data class ConfigRequest(
    @SerializedName("kas_amount") val kasAmount: Int? = null,
    @SerializedName("previous_balance") val previousBalance: Int? = null,
    @SerializedName("real_balance") val realBalance: Int? = null,
    @SerializedName("class_name") val className: String? = null,
    val year: String? = null
)

data class PaymentRecord(
    val id: Int,
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("member_name") val memberName: String,
    val amount: Int,
    val date: String,
    val note: String = "",
    val type: String = "Kas"
)
