package com.example

import com.example.data.model.Category
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import android.os.Bundle
import android.app.Activity
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.data.supabase.SupabaseRoleAuthenticator
import com.example.data.supabase.SupabaseSession
import com.example.data.supabase.SupabaseSessionProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import com.example.ui.StorePortalApp
import com.example.ui.StoreNavScreen
import com.example.ui.StoreViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: StoreViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SupabaseMerchantAuthGate(viewModel = viewModel) {
                    StorePortalApp(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize(),
                        showSimulationControls = false,
                    )
                }
            }
        }
    }
}


@Composable
private fun SupabaseMerchantAuthGate(
    viewModel: StoreViewModel,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authenticator = remember(context) { SupabaseRoleAuthenticator(context, "merchant") }
    val scope = rememberCoroutineScope()
    val currentScreen by viewModel.currentScreen.collectAsState()
    var session by remember { mutableStateOf<SupabaseSession?>(null) }
    var restoring by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var ready by remember { mutableStateOf(false) }
    var onboardingRequired by remember { mutableStateOf(false) }
    var storeNameAr by remember { mutableStateOf("") }
    var storeNameEn by remember { mutableStateOf("") }
    var storePhone by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    suspend fun activate(candidate: SupabaseSession): Boolean {
        SupabaseSessionProvider.session = candidate
        val result = viewModel.loginWithSupabase(candidate.userId, candidate.email.orEmpty())
        if (result.isSuccess) {
            session = candidate
            ready = true
            onboardingRequired = false
            errorMessage = null
            return true
        }
        val message = result.exceptionOrNull()?.message.orEmpty()
        if (message.contains("لا يوجد متجر مرتبط بالحساب بعد")) {
            session = candidate
            ready = false
            onboardingRequired = true
            val categoryResult = viewModel.loadStoreCategories()
            categories = categoryResult.getOrDefault(emptyList())
            if (categories.isNotEmpty()) selectedCategoryId = categories.first().id
            errorMessage = if (categories.isEmpty()) {
                "لا توجد فئات متاحة حالياً؛ يلزم تهيئتها من لوحة الإدارة."
            } else null
            return false
        }
        authenticator.signOut(candidate)
        SupabaseSessionProvider.session = null
        session = null
        ready = false
        onboardingRequired = false
        errorMessage = message.ifBlank { "تعذر فتح بوابة المتجر." }
        return false
    }

    LaunchedEffect(authenticator) {
        val restored = authenticator.restore()
        if (restored.isSuccess) restored.getOrNull()?.let { activate(it) }
        else errorMessage = restored.exceptionOrNull()?.message
        restoring = false
    }

    LaunchedEffect(currentScreen, session) {
        if (session != null && !onboardingRequired && currentScreen == StoreNavScreen.LOGIN) {
            authenticator.signOut(session)
            SupabaseSessionProvider.session = null
            session = null
            ready = false
        }
    }

    LaunchedEffect(currentScreen, session) {
        if (session != null && ready && currentScreen != StoreNavScreen.LOGIN) {
            while (true) {
                viewModel.refreshRemoteData()
                delay(15_000)
            }
        }
    }

    if (session != null && ready) {
        content()
    } else if (onboardingRequired) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("إنشاء متجر جديد", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("سيُنشأ المتجر بحالة غير مفعّلة حتى تراجعه الإدارة.")
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = storeNameAr, onValueChange = { storeNameAr = it },
                label = { Text("اسم المتجر بالعربية") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = storeNameEn, onValueChange = { storeNameEn = it },
                label = { Text("Store name in English") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            Box {
                OutlinedTextField(
                    value = categories.firstOrNull { it.id == selectedCategoryId }?.nameAr.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("تصنيف المتجر") },
                    modifier = Modifier.fillMaxWidth().clickable { categoryExpanded = true },
                )
                DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.nameAr) },
                            onClick = { selectedCategoryId = category.id; categoryExpanded = false },
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = storePhone, onValueChange = { storePhone = it },
                label = { Text("هاتف المتجر") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = storeAddress, onValueChange = { storeAddress = it },
                label = { Text("عنوان المتجر") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(Modifier.height(16.dp))
            Button(
                enabled = !loading && categories.isNotEmpty() && storeNameAr.isNotBlank() &&
                    storeNameEn.isNotBlank() && selectedCategoryId.isNotBlank() &&
                    storePhone.isNotBlank() && storeAddress.isNotBlank(),
                onClick = {
                    scope.launch {
                        loading = true
                        errorMessage = null
                        val created = viewModel.createStoreForMerchant(
                            storeNameAr, storeNameEn, selectedCategoryId, storePhone, storeAddress
                        )
                        if (created.isSuccess) {
                            ready = true
                            onboardingRequired = false
                            errorMessage = null
                        } else {
                            errorMessage = created.exceptionOrNull()?.message ?: "تعذر إنشاء المتجر."
                        }
                        loading = false
                    }
                },
            ) {
                Text(if (loading) "جارٍ الحفظ..." else "إرسال المتجر للمراجعة")
            }
            androidx.compose.material3.TextButton(onClick = {
                scope.launch {
                    authenticator.signOut(session)
                    SupabaseSessionProvider.session = null
                    session = null
                    onboardingRequired = false
                }
            }) { Text("تسجيل الخروج") }
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("بوابة المتجر — وصّل", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("الدخول متاح لحسابات التجار المفعّلة والمرتبطة بمتجر.")
            Spacer(Modifier.height(24.dp))
            if (restoring || loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    enabled = authenticator.isConfigured() && activity != null,
                    onClick = {
                        val hostActivity = activity ?: return@Button
                        scope.launch {
                            loading = true
                            errorMessage = null
                            val result = authenticator.signIn(hostActivity)
                            if (result.isSuccess) activate(result.getOrThrow())
                            else errorMessage = result.exceptionOrNull()?.message ?: "تعذر تسجيل الدخول."
                            loading = false
                        }
                    },
                ) { Text("المتابعة باستخدام Google") }
            }
            if (!authenticator.isConfigured()) {
                Spacer(Modifier.height(12.dp))
                Text("أكمل إعداد Supabase وGoogle OAuth في local.properties أولاً.", color = MaterialTheme.colorScheme.error)
            }
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
