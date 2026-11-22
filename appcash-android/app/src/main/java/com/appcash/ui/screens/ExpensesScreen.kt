// ==========================================================================
// PETA FILE · AppCash (Kelompok 6) · ExpensesScreen.kt
// Isi file : Halaman Pengeluaran: daftar, total, tambah, edit, hapus.
// Penanda  : CARI-PENGELUARAN, CARI-VALIDASI
// Cara cari: VS Code Ctrl+Shift+F (Android Studio Ctrl+Shift+F) lalu ketik
//            "CARI-" untuk semua penanda, atau salah satu nama di baris Penanda.
// ==========================================================================
//  BACA INI DULU : TAB "PENGELUARAN"
//  Daftar pengeluaran kas (urut terbaru) dengan total di atas. Semua orang bisa MELIHAT.
//  Hanya ADMIN yang melihat tombol Tambah dan tombol Edit/Hapus di tiap kartu.
//  Tiga dialog: AddExpenseDialog (tambah), EditExpenseDialog (ubah), dan dialog konfirmasi hapus.
//  Data lewat repository: getExpenses, addExpense, updateExpense, deleteExpense. Saldo di Dashboard ikut berubah otomatis.
// » Alamat folder (package) file ini di dalam proyek.
package com.appcash.ui.screens

// » orangeCard = dekorasi kartu oranye bergradien (Modifiers.kt).
import com.appcash.ui.theme.orangeCard
// » background = mengisi warna/gradien latar sebuah tampilan.
import androidx.compose.foundation.background
// » Layout dasar: Column (susun atas-bawah), Row (susun kiri-kanan), Box (tumpuk), Spacer (jarak kosong),
// » padding, size, fillMaxWidth, dll.
import androidx.compose.foundation.layout.*
// » LazyColumn = daftar vertikal yang hanya menggambar baris yang terlihat (hemat memori).
import androidx.compose.foundation.lazy.LazyColumn
// » items = mengulang satu tampilan untuk setiap elemen daftar.
import androidx.compose.foundation.lazy.items
// » RoundedCornerShape = bentuk persegi dengan sudut membulat.
import androidx.compose.foundation.shape.RoundedCornerShape
// » Icons = daftar ikon bawaan Material.
import androidx.compose.material.icons.Icons
// » Ikon bawaan Material: Delete (versi terisi).
import androidx.compose.material.icons.filled.Delete
// » Ikon bawaan Material: Edit (versi terisi).
import androidx.compose.material.icons.filled.Edit
// » Komponen Material 3 (gaya resmi Android): Text, Button, Card, TextField, Scaffold, dll.
import androidx.compose.material3.*
// » Alat state Compose: remember (mengingat nilai), mutableStateOf (nilai yang bila berubah menggambar ulang
// » layar), LaunchedEffect, dll.
import androidx.compose.runtime.*
// » Alignment = pengatur posisi isi (tengah, atas, bawah, kiri, kanan).
import androidx.compose.ui.Alignment
// » Modifier = daftar 'dekorasi' sebuah tampilan: ukuran, jarak, warna latar, klik, dll.
import androidx.compose.ui.Modifier
// » clip = memotong tampilan mengikuti sebuah bentuk (mis. sudut bulat atau lingkaran).
import androidx.compose.ui.draw.clip
// » Offset = titik koordinat (x, y) di Canvas.
import androidx.compose.ui.geometry.Offset
// » Brush = 'cat' warna, termasuk gradien (warna berubah halus).
import androidx.compose.ui.graphics.Brush
// » Color = tipe warna Compose; dipakai untuk menulis warna seperti Color.White atau Color(0xFF...).
import androidx.compose.ui.graphics.Color
// » FontWeight = ketebalan huruf (Normal, Medium, Bold, ExtraBold).
import androidx.compose.ui.text.font.FontWeight
// » AnnotatedString = teks dengan gaya berbeda-beda di tiap bagian.
import androidx.compose.ui.text.AnnotatedString
// » OffsetMapping = pemetaan posisi kursor antara teks asli dan teks yang ditampilkan.
import androidx.compose.ui.text.input.OffsetMapping
// » TransformedText = hasil perubahan tampilan teks isian.
import androidx.compose.ui.text.input.TransformedText
// » VisualTransformation = cara menampilkan teks isian (mis. diberi titik ribuan).
import androidx.compose.ui.text.input.VisualTransformation
// » dp = satuan ukuran Android yang sama di semua layar (mis. 16.dp).
import androidx.compose.ui.unit.dp
// » Expense = bentuk data satu pengeluaran.
import com.appcash.data.model.Expense
// » AppCashRepository = pusat data & rumus aplikasi (saldo, denda, pembayaran).
import com.appcash.data.repository.AppCashRepository
// » OrangeDark = oranye gelap.
import com.appcash.ui.theme.OrangeDark
// » OrangeLight = oranye terang.
import com.appcash.ui.theme.OrangeLight
// » OrangePrimary = warna oranye utama.
import com.appcash.ui.theme.OrangePrimary
// » RedNegative = merah untuk denda dan pengeluaran.
import com.appcash.ui.theme.RedNegative
// » launch = menjalankan tugas di jalur belakang (coroutine) supaya layar tidak membeku.
import kotlinx.coroutines.launch

// State: expenses (daftar), showAdd (dialog tambah), editingExpense / deletingExpense (pengeluaran yang sedang diubah / dihapus).
// CARI-PENGELUARAN | Daftar pengeluaran urut terbaru + total di atas; tombol tambah hanya untuk admin.
// » Penanda fungsi penggambar tampilan.
@Composable
// » ExpensesScreen = tab PENGELUARAN (tab ke-2). repository = sumber data; isAdmin = true kalau admin (admin
// » boleh tambah/edit/hapus).
fun ExpensesScreen(repository: AppCashRepository, isAdmin: Boolean) {
    // » expenses = daftar pengeluaran yang ditampilkan. Awalnya kosong.
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    // » error = pesan error bila gagal memuat; null = tidak ada error.
    var error by remember { mutableStateOf<String?>(null) }
    // » loading = true selama data belum selesai dimuat.
    var loading by remember { mutableStateOf(true) }
    // » showAdd = apakah dialog Tambah sedang tampil.
    var showAdd by remember { mutableStateOf(false) }
    // » editingExpense = pengeluaran yang sedang diedit (null = tidak ada).
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    // » deletingExpense = pengeluaran yang akan dihapus dan menunggu konfirmasi (null = tidak ada).
    var deletingExpense by remember { mutableStateOf<Expense?>(null) }
    // » scope = pengelola tugas belakang.
    val scope = rememberCoroutineScope()
    // » snackbar = pengelola pesan kecil di bawah layar (mis. "Pengeluaran ditambahkan").
    val snackbar = remember { SnackbarHostState() }

    // » dataVersion = nomor versi data; kalau berubah, daftar dimuat ulang.
    val dataVersion by repository.version.collectAsState()
    // » Blok dijalankan tiap dataVersion berubah:
    LaunchedEffect(dataVersion) {
        // » Hapus error lama.
        error = null
        // » Ambil daftar pengeluaran dari repository lalu:
        repository.getExpenses().fold(
            // » berhasil: simpan ke expenses,
            onSuccess = { expenses = it },
            // » gagal: simpan pesan error (atau "Gagal memuat pengeluaran").
            onFailure = { error = it.message ?: "Gagal memuat pengeluaran" }
        )
        // » Selesai memuat: loading = false.
        loading = false
    }

    // » Kalau showAdd true, tampilkan dialog Tambah.
    if (showAdd) {
        // » AddExpenseDialog: onDismiss = tutup dialog; onConfirm dijalankan saat Tambah ditekan membawa keterangan,
        // » jumlah, tanggal.
        AddExpenseDialog(onDismiss = { showAdd = false }, onConfirm = { description, amount, date ->
            // » Tutup dialog.
            showAdd = false
            // » Jalankan di jalur belakang:
            scope.launch {
                // » simpan pengeluaran baru lewat repository.addExpense lalu:
                repository.addExpense(com.appcash.data.model.ExpenseRequest(description, amount, date)).fold(
                    // » berhasil (res = id baru):
                    onSuccess = { res ->
                        // » taruh pengeluaran baru di urutan paling atas daftar layar,
                        expenses = listOf(Expense(res.id, description, amount, date)) + expenses
                        // » tampilkan pesan "Pengeluaran ditambahkan".
                        snackbar.showSnackbar("Pengeluaran ditambahkan")
                    },
                    // » gagal: tampilkan "Gagal menambah pengeluaran".
                    onFailure = { snackbar.showSnackbar("Gagal menambah pengeluaran") }
                )
            }
        })
    }

    // » Kalau ada pengeluaran yang akan dihapus (target), tampilkan dialog konfirmasi hapus.
    deletingExpense?.let { target ->
        // » AlertDialog = jendela pop-up.
        AlertDialog(
            // » Bila diketuk di luar, batal (deletingExpense = null).
            onDismissRequest = { deletingExpense = null },
            // » Sudut bulat 20dp.
            shape = RoundedCornerShape(20.dp),
            // » Judul "Hapus Pengeluaran?".
            title = { Text("Hapus Pengeluaran?", fontWeight = FontWeight.Bold, color = OrangePrimary) },
            // » Isi: menyebut keterangan dan jumlah yang akan dihapus.
            text = { Text("\"${target.description}\" sebesar Rp ${formatRupiah(target.amount)} akan dihapus.") },
            // » Tombol konfirmasi.
            confirmButton = {
                // » Button Hapus.
                Button(
                    // » Saat ditekan:
                    onClick = {
                        // » tutup dialog,
                        deletingExpense = null
                        // » jalankan di jalur belakang:
                        scope.launch {
                            // » hapus lewat repository.deleteExpense lalu:
                            repository.deleteExpense(target.id).fold(
                                // » berhasil:
                                onSuccess = {
                                    // » buang pengeluaran itu dari daftar layar,
                                    expenses = expenses.filter { it.id != target.id }
                                    // » tampilkan "Pengeluaran dihapus".
                                    snackbar.showSnackbar("Pengeluaran dihapus")
                                },
                                // » gagal: tampilkan "Gagal menghapus".
                                onFailure = { snackbar.showSnackbar("Gagal menghapus") }
                            )
                        }
                    },
                    // » Warna tombol merah.
                    colors = ButtonDefaults.buttonColors(containerColor = RedNegative),
                    // » Sudut bulat 10dp.
                    shape = RoundedCornerShape(10.dp)
                // » Tulisan "Hapus" putih tebal.
                ) { Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            // » Tombol Batal menutup dialog.
            dismissButton = { TextButton(onClick = { deletingExpense = null }) { Text("Batal", color = OrangePrimary) } }
        )
    }

    // » Kalau ada pengeluaran yang sedang diedit...
    if (editingExpense != null) {
        // » ...tampilkan dialog edit:
        EditExpenseDialog(
            // » bawa data pengeluaran,
            expense = editingExpense!!,
            // » onDismiss = tutup,
            onDismiss = { editingExpense = null },
            // » onConfirm = dijalankan saat Simpan, membawa keterangan, jumlah, tanggal baru.
            onConfirm = { desc, amt, dt ->
                // » Ambil id pengeluaran yang diedit.
                val expId = editingExpense!!.id
                // » Tutup dialog.
                editingExpense = null
                // » Jalankan di jalur belakang:
                scope.launch {
                    // » simpan perubahan lewat repository.updateExpense lalu:
                    repository.updateExpense(expId, desc, amt, dt).fold(
                        // » berhasil:
                        onSuccess = {
                            // » ganti isi pengeluaran itu di daftar layar dengan nilai baru,
                            expenses = expenses.map { if (it.id == expId) it.copy(description = desc, amount = amt, date = dt) else it }
                            // » tampilkan "Pengeluaran diperbarui".
                            snackbar.showSnackbar("Pengeluaran diperbarui")
                        },
                        // » gagal: tampilkan "Gagal memperbarui pengeluaran".
                        onFailure = { snackbar.showSnackbar("Gagal memperbarui pengeluaran") }
                    )
                }
            }
        )
    }

    // » Box penuh layar dengan latar sesuai tema.
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // » when = pilih satu keadaan:
        when {
            // » masih loading: lingkaran berputar di tengah.
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OrangePrimary)
            // » ada error: tampilkan ErrorView dengan tombol coba lagi (repository.refresh).
            error != null -> ErrorView(error = error!!, onRetry = { repository.refresh() })
            // » selain itu: tampilkan daftar.
            else -> {
                // » Kolom penuh layar.
                Column(modifier = Modifier.fillMaxSize()) {
                    // Page Header
                    // » Kolom header halaman.
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        // » Teks kecil "Dashboard".
                        Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        // » Judul "Pengeluaran Kas".
                        Text("Pengeluaran Kas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    }

                    // » LazyColumn = daftar pengeluaran yang bisa digulir.
                    LazyColumn(
                        // » Mengisi sisa tinggi layar (di atas tombol Tambah).
                        modifier = Modifier.weight(1f),
                        // » Jarak isi kiri-kanan 16dp, atas-bawah 6dp.
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        // » Jarak antar kartu 10dp.
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // » item pertama: baris ringkasan.
                        item {
                            // » Baris:
                            Row(
                                // » selebar layar, jarak bawah 4dp,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                // » teks kiri dan kanan.
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // » Teks kiri: jumlah transaksi ("<n> transaksi").
                                Text("${expenses.size} transaksi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                // » Teks kanan: total pengeluaran merah ("Total Rp ...", jumlahkan semua amount).
                                Text("Total Rp ${formatRupiah(expenses.sumOf { it.amount })}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = RedNegative)
                            }
                        }
                        // » Untuk setiap pengeluaran, diurutkan tanggal terbaru dulu (sortedByDescending), kunci = id:
                        items(expenses.sortedByDescending { it.date }, key = { it.id }) { expense ->
                            // » tampilkan kartu pengeluaran:
                            ExpenseItemCard(
                                // » data pengeluaran,
                                expense = expense,
                                // » mode admin,
                                isAdmin = isAdmin,
                                // » onEdit = buka dialog edit untuk pengeluaran ini,
                                onEdit = { editingExpense = expense },
                                // » onDelete = buka konfirmasi hapus.
                                onDelete = { deletingExpense = expense }
                            )
                        }
                        // » Kalau daftar kosong, tampilkan teks:
                        if (expenses.isEmpty()) item {
                            // » "Belum ada pengeluaran",
                            Text(
                                // » selebar layar dengan jarak 32dp,
                                "Belum ada pengeluaran",
                                // » warna abu,
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                // » rata tengah.
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        // » item penutup: jarak kosong 8dp.
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    // Add Expense Button
                    // » Tombol Tambah hanya untuk admin.
                    if (isAdmin) {
                        // » Box pembungkus tombol:
                        Box(
                            // » selebar layar,
                            modifier = Modifier
                                // » jarak 16dp.
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // » Button Tambah Pengeluaran:
                            Button(
                                // » saat ditekan, tampilkan dialog Tambah,
                                onClick = { showAdd = true },
                                // » selebar layar, tinggi 52dp,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                // » sudut bulat 14dp,
                                shape = RoundedCornerShape(14.dp),
                                // » warna oranye.
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                // » Tulisan "Tambah Pengeluaran" putih tebal.
                                Text("Tambah Pengeluaran", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        // » SnackbarHost = tempat pesan kecil muncul di tengah bawah layar.
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// Kartu satu pengeluaran: keterangan, tanggal, kategori, nominal merah. Tombol Edit dan Hapus hanya bila isAdmin.
// Kartu satu pengeluaran. Tombol Edit dan Hapus hanya muncul bila isAdmin.
// » Penanda fungsi penggambar tampilan.
@Composable
// » ExpenseItemCard = kartu satu pengeluaran. Parameter: data, isAdmin, onEdit, onDelete.
fun ExpenseItemCard(expense: Expense, isAdmin: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    // » Box kartu oranye:
    Box(
        // » Modifier:
        modifier = Modifier
            // » selebar layar,
            .fillMaxWidth()
            // » dekorasi kartu oranye 16dp,
            .orangeCard(16.dp)
            // » jarak dalam 16dp.
            .padding(16.dp)
    ) {
        // » Kolom isi, jarak antar baris 10dp.
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // » Baris atas: keterangan di kiri, tanggal di kanan.
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // » Teks keterangan: gaya besar, tebal, putih, mengisi sisa lebar.
                Text(expense.description, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                // » Teks tanggal: huruf kecil, putih agak transparan.
                Text(expense.date, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
            // » Baris bawah: jumlah di kiri, tombol di kanan.
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // » Teks jumlah:
                Text(
                    // » "-Rp <angka>" (minus karena uang keluar),
                    "-Rp ${formatRupiah(expense.amount)}",
                    // » gaya judul besar,
                    style = MaterialTheme.typography.headlineSmall,
                    // » sangat tebal,
                    fontWeight = FontWeight.ExtraBold,
                    // » putih.
                    color = Color.White
                )
                // » Kalau admin, tampilkan tombol Edit dan Hapus.
                if (isAdmin) {
                    // » Baris dua tombol, jarak 6dp.
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // » Surface = tombol Edit:
                        Surface(
                            // » saat ditekan: onEdit,
                            onClick = onEdit,
                            // » sudut bulat 10dp,
                            shape = RoundedCornerShape(10.dp),
                            // » putih transparan 20%.
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            // » Isi tombol: teks + ikon.
                            Row(
                                // » jarak dalam,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                // » rata tengah,
                                verticalAlignment = Alignment.CenterVertically,
                                // » jarak 4dp.
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // » Teks "Edit".
                                Text("Edit", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                // » Ikon pensil.
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        // » Surface = tombol Hapus:
                        Surface(
                            // » saat ditekan: onDelete,
                            onClick = onDelete,
                            // » sudut bulat 10dp,
                            shape = RoundedCornerShape(10.dp),
                            // » putih transparan 20%.
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            // » Isi tombol:
                            Row(
                                // » jarak dalam,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                // » rata tengah,
                                verticalAlignment = Alignment.CenterVertically,
                                // » jarak 4dp.
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // » Teks "Hapus".
                                Text("Hapus", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                // » Ikon tempat sampah.
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Form tambah: keterangan, nominal, tanggal (otomatis hari ini, bentuk yyyyMMdd). Tombol aktif hanya bila isian valid (isValidDate dan nominal > 0).
// CARI-PENGELUARAN CARI-VALIDASI | Form tambah: tanggal otomatis hari ini, tombol aktif hanya bila isian valid.
@Composable
// » AddExpenseDialog = dialog form tambah pengeluaran. onDismiss = tutup; onConfirm = kirim (keterangan,
// » jumlah, tanggal).
fun AddExpenseDialog(onDismiss: () -> Unit, onConfirm: (String, Int, String) -> Unit) {
    // » description = isi kolom deskripsi.
    var description by remember { mutableStateOf("") }
    // » amount = isi kolom jumlah (teks angka).
    var amount by remember { mutableStateOf("") }
    // Tanggal otomatis terisi hari ini supaya bendahara tidak perlu mengetik ulang
    // » date = isi kolom tanggal; awalnya hari ini dalam angka tanpa tanda "-" (mis. 20261123).
    var date by remember { mutableStateOf(java.time.LocalDate.now().toString().replace("-", "")) }

    // » AlertDialog = jendela pop-up.
    AlertDialog(
        // » Tutup bila diketuk di luar.
        onDismissRequest = onDismiss,
        // » Sudut bulat 20dp.
        shape = RoundedCornerShape(20.dp),
        // » Judul dialog:
        title = {
            // » "Tambah Pengeluaran" tebal oranye.
            Text("Tambah Pengeluaran", fontWeight = FontWeight.Bold, color = OrangePrimary)
        },
        // » Isi dialog:
        text = {
            // » kolom isian, jarak 10dp.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // » Isian deskripsi:
                OutlinedTextField(
                    // » nilai dan simpan saat diketik,
                    value = description, onValueChange = { description = it },
                    // » label "Deskripsi", selebar dialog,
                    label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(),
                    // » sudut bulat 12dp,
                    shape = RoundedCornerShape(12.dp),
                    // » warna oranye saat fokus.
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                // » Isian jumlah:
                OutlinedTextField(
                    // » saat diketik: hanya angka, maksimal 9 digit,
                    value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() }.take(9) },
                    // » label "Jumlah (Rp)", selebar dialog, satu baris,
                    label = { Text("Jumlah (Rp)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    // » keyboard angka,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    // » sudut bulat 12dp,
                    shape = RoundedCornerShape(12.dp),
                    // » warna oranye saat fokus.
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                // » Isian tanggal:
                OutlinedTextField(
                    // » nilainya date,
                    value = date,
                    // » saat diketik:
                    onValueChange = { newVal ->
                        // » ambil angka saja,
                        val digitsOnly = newVal.filter { c -> c.isDigit() }
                        // » simpan kalau tidak lebih dari 8 digit.
                        if (digitsOnly.length <= 8) date = digitsOnly
                    },
                    // » Label "Tanggal (YYYYMMDD)".
                    label = { Text("Tanggal (YYYYMMDD)") },
                    // » Contoh isian.
                    placeholder = { Text("Contoh: 20260904") },
                    // » Selebar dialog.
                    modifier = Modifier.fillMaxWidth(),
                    // » Sudut bulat 12dp.
                    shape = RoundedCornerShape(12.dp),
                    // » Warna oranye saat fokus.
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary),
                    // » Satu baris.
                    singleLine = true,
                    // » isError = tepi merah kalau 8 digit tapi tanggal tidak ada di kalender.
                    isError = date.length == 8 && !isValidDate("${date.take(4)}-${date.substring(4, 6)}-${date.takeLast(2)}"),
                    // » Keyboard angka.
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    // » Tampilan tanggal diberi tanda "-" otomatis (DateVisualTransformation).
                    visualTransformation = DateVisualTransformation()
                )
            }
        },
        // » Tombol konfirmasi "Tambah".
        confirmButton = {
            Button(
                // » Saat ditekan:
                onClick = {
                    // » ubah teks jumlah jadi angka (kosong = 0),
                    val amt = amount.toIntOrNull() ?: 0
                    // » ubah tanggal jadi format YYYY-MM-DD,
                    val formattedDate = date.let { d -> if (d.length == 8) "${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}" else d }
                    // » kalau deskripsi terisi, jumlah > 0, dan tanggal valid → panggil onConfirm.
                    if (description.isNotBlank() && amt > 0 && isValidDate(formattedDate)) onConfirm(description, amt, formattedDate)
                },
                // » Tombol aktif hanya bila deskripsi terisi, jumlah > 0, tanggal 8 digit...
                enabled = description.isNotBlank() && (amount.toIntOrNull() ?: 0) > 0 && date.length == 8 &&
                    // » ...dan valid.
                    isValidDate("${date.take(4)}-${date.substring(4, 6)}-${date.takeLast(2)}"),
                // » Warna oranye.
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                // » Sudut bulat 10dp.
                shape = RoundedCornerShape(10.dp)
            // » Tulisan "Tambah" putih tebal.
            ) { Text("Tambah", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            // » Tombol Batal menutup dialog.
            TextButton(onClick = onDismiss) { Text("Batal", color = OrangePrimary) }
        }
    )
}

// CARI-PENGELUARAN | Form edit pengeluaran.
@Composable
// » EditExpenseDialog = dialog form ubah pengeluaran. Sama dengan form tambah, tapi isian awalnya dari data
// » lama.
fun EditExpenseDialog(expense: Expense, onDismiss: () -> Unit, onConfirm: (String, Int, String) -> Unit) {
    // » description = isi awal dari keterangan lama.
    var description by remember { mutableStateOf(expense.description) }
    // » amount = isi awal dari jumlah lama (jadi teks).
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    // Convert existing YYYY-MM-DD to digits only
    // » date = tanggal lama, tanda "-" dibuang jadi angka saja.
    var date by remember { mutableStateOf(expense.date.replace("-", "")) }

    // » AlertDialog.
    AlertDialog(
        // » Tutup bila diketuk di luar.
        onDismissRequest = onDismiss,
        // » Sudut bulat 20dp.
        shape = RoundedCornerShape(20.dp),
        // » Judul dialog:
        title = {
            // » "Edit Pengeluaran" tebal oranye.
            Text("Edit Pengeluaran", fontWeight = FontWeight.Bold, color = OrangePrimary)
        },
        // » Isi dialog:
        text = {
            // » kolom isian, jarak 10dp.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // » Isian deskripsi:
                OutlinedTextField(
                    // » nilai dan simpan saat diketik,
                    value = description, onValueChange = { description = it },
                    // » label "Deskripsi", selebar dialog,
                    label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(),
                    // » sudut bulat 12dp,
                    shape = RoundedCornerShape(12.dp),
                    // » warna oranye saat fokus.
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                // » Isian jumlah:
                OutlinedTextField(
                    // » hanya angka, maksimal 9 digit,
                    value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() }.take(9) },
                    // » label "Jumlah (Rp)", selebar dialog, satu baris,
                    label = { Text("Jumlah (Rp)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    // » keyboard angka,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    // » sudut bulat 12dp,
                    shape = RoundedCornerShape(12.dp),
                    // » warna oranye saat fokus.
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary)
                )
                // » Isian tanggal:
                OutlinedTextField(
                    // » nilainya date,
                    value = date,
                    // » saat diketik:
                    onValueChange = { newVal ->
                        // » ambil angka saja,
                        val digitsOnly = newVal.filter { c -> c.isDigit() }
                        // » simpan kalau tidak lebih dari 8 digit.
                        if (digitsOnly.length <= 8) date = digitsOnly
                    },
                    // » Label "Tanggal (YYYYMMDD)".
                    label = { Text("Tanggal (YYYYMMDD)") },
                    // » Contoh isian.
                    placeholder = { Text("Contoh: 20260904") },
                    // » Selebar dialog.
                    modifier = Modifier.fillMaxWidth(),
                    // » Sudut bulat 12dp.
                    shape = RoundedCornerShape(12.dp),
                    // » Warna oranye saat fokus.
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedLabelColor = OrangePrimary),
                    // » Satu baris.
                    singleLine = true,
                    // » Tepi merah kalau tanggal tidak valid.
                    isError = date.length == 8 && !isValidDate("${date.take(4)}-${date.substring(4, 6)}-${date.takeLast(2)}"),
                    // » Keyboard angka.
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    // » Tampilan tanggal diberi tanda "-" otomatis.
                    visualTransformation = DateVisualTransformation()
                )
            }
        },
        // » Tombol konfirmasi "Simpan".
        confirmButton = {
            Button(
                // » Saat ditekan:
                onClick = {
                    // » ubah jumlah jadi angka,
                    val amt = amount.toIntOrNull() ?: 0
                    // » ubah tanggal jadi YYYY-MM-DD,
                    val formattedDate = date.let { d -> if (d.length == 8) "${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}" else d }
                    // » kalau semua valid → panggil onConfirm.
                    if (description.isNotBlank() && amt > 0 && isValidDate(formattedDate)) onConfirm(description, amt, formattedDate)
                },
                // » Tombol aktif hanya bila deskripsi terisi, jumlah > 0, tanggal 8 digit...
                enabled = description.isNotBlank() && (amount.toIntOrNull() ?: 0) > 0 && date.length == 8 &&
                    // » ...dan valid.
                    isValidDate("${date.take(4)}-${date.substring(4, 6)}-${date.takeLast(2)}"),
                // » Warna oranye.
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                // » Sudut bulat 10dp.
                shape = RoundedCornerShape(10.dp)
            // » Tulisan "Simpan" putih tebal.
            ) { Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            // » Tombol Batal menutup dialog.
            TextButton(onClick = onDismiss) { Text("Batal", color = OrangePrimary) }
        }
    )
}

// Memeriksa tanggal benar-benar ada di kalender (menolak 2026-02-30).
// CARI-VALIDASI | Menolak tanggal yang tidak ada di kalender, mis. 2026-02-30.
/** true jika tanggal YYYY-MM-DD benar-benar ada di kalender (mis. 2026-02-30 ditolak). */
// » isValidDate = cek apakah teks YYYY-MM-DD benar-benar tanggal di kalender. Hasil true/false.
fun isValidDate(value: String): Boolean =
    // » Coba ubah teks jadi tanggal; kalau berhasil = true, kalau error (mis. 2026-02-30) = false (runCatching
    // » menangkap error).
    runCatching { java.time.LocalDate.parse(value) }.isSuccess
