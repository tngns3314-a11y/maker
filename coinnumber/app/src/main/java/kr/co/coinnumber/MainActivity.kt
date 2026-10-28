package kr.co.coinnumber

import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val Ink = Color(0xFF25282B)
private val Muted = Color(0xFF66717F)
private fun number(n: Number): String = NumberFormat.getNumberInstance(Locale.KOREA).apply { maximumFractionDigits = 0 }.format(n)
private fun won(n: Double?): String = n?.let { "${number(it)}원" } ?: "계산 불가"
private fun decimal(n: Double): String = NumberFormat.getNumberInstance(Locale.KOREA).apply { maximumFractionDigits = 2 }.format(n)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Ink, onPrimary = Color.White, secondary = Ink, background = Color.White, surface = Color.White, onSurface = Ink, onBackground = Ink)) {
                Surface(Modifier.fillMaxSize(), color = Color.White) { CalculatorApp() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorApp() {
    val context = LocalContext.current
    val storage = remember { Storage(context) }
    var page by rememberSaveable { mutableStateOf("home") }
    var multiple by rememberSaveable { mutableStateOf(false) }
    var percent by rememberSaveable { mutableStateOf("1") }
    var singlePrice by rememberSaveable { mutableStateOf("3000") }
    var bundlePrice by rememberSaveable { mutableStateOf("33000") }
    var bundle by rememberSaveable { mutableStateOf("10") }
    var ceiling by rememberSaveable { mutableStateOf("200") }
    var guaranteed by rememberSaveable { mutableStateOf(true) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedFood by rememberSaveable { mutableIntStateOf(0) }
    var sheet by rememberSaveable { mutableStateOf<String?>(null) }
    var history by remember { mutableStateOf(storage.history()) }
    fun currentInput(): Input {
        val p = percent.toDoubleOrNull() ?: throw IllegalArgumentException("확률을 숫자로 입력하세요.")
        val cost = (if(multiple) bundlePrice else singlePrice).toLongOrNull() ?: throw IllegalArgumentException("가격을 정수로 입력하세요.")
        val k = if(multiple) bundle.toLongOrNull() ?: throw IllegalArgumentException("묶음 횟수를 정수로 입력하세요.") else 1L
        val t = if(guaranteed) ceiling.toLongOrNull() ?: throw IllegalArgumentException("천장 횟수를 정수로 입력하세요.") else null
        return Input(p, cost, k, t)
    }
    fun back() { page = when(page) { "food" -> "result"; "result" -> "input"; else -> "home" }; error = null }
    BackHandler(page != "home" && sheet == null) { back() }
    val scrollState = key(page) { rememberScrollState() }
    val resultTap = if(page == "result") Modifier.testTag("costResult").clickable(
        interactionSource = remember { MutableInteractionSource() }, indication = null,
        onClickLabel = "가챠 비용으로 할 수 있는 일 화면으로 이동"
    ) { page = "food" } else Modifier
    Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().then(resultTap).padding(horizontal = 24.dp).verticalScroll(scrollState)) {
        Spacer(Modifier.height(18.dp))
        if(page == "home") {
            Text("과금계산기", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(72.dp))
            Text("과금 전에,\n얼마나 들까?", Modifier.fillMaxWidth(), fontSize = 38.sp, lineHeight = 49.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            Hint("확률과 비용을 간단하게 확인하세요", Modifier.fillMaxWidth(), centered = true)
            Spacer(Modifier.height(40.dp))
            Image(painterResource(R.drawable.ic_calculator), contentDescription = null, modifier = Modifier.size(90.dp, 108.dp).align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(54.dp))
            Action("1회 비용 계산") { multiple = false; page = "input" }
            Spacer(Modifier.height(12.dp))
            Action("다회차 비용 계산", outline = true) { multiple = true; page = "input" }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { sheet = "history" }, shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("계산한 내역", color = Muted) }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { back() }, contentPadding = PaddingValues(0.dp), modifier = Modifier.width(48.dp).semantics { contentDescription = "뒤로가기" }) { Text("←", fontSize = 28.sp) }
                Text(if(page == "food") "가챠 비용으로 할 수 있는 일" else if(page == "result") "계산 결과" else if(multiple) "다회차 비용 계산" else "1회 비용 계산", fontSize = if(page == "food") 21.sp else 25.sp, fontWeight = FontWeight.Bold)
            }
            if(page == "input") {
                Spacer(Modifier.height(12.dp))
                Hint(if(multiple) "묶음 횟수와 가격을 입력하세요" else "1회에 드는 비용을 입력하세요", Modifier.fillMaxWidth(), true)
                Spacer(Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Numeric("확률", percent, "%", Modifier.weight(1f), decimal = true) { percent = it; error = null }
                    if(!multiple) Numeric("1회 비용", singlePrice, "원", Modifier.weight(1.2f)) { singlePrice = it; error = null }
                    Numeric("천장", ceiling, "회", Modifier.weight(1f), enabled = guaranteed) { ceiling = it; error = null }
                }
                if(multiple) {
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Numeric("묶음 횟수", bundle, "회", Modifier.weight(1f)) { bundle = it; error = null }
                        Numeric("묶음 가격", bundlePrice, "원", Modifier.weight(1f)) { bundlePrice = it; error = null }
                    }
                    Spacer(Modifier.height(22.dp)); HorizontalDivider(); Spacer(Modifier.height(16.dp))
                    val k = bundle.toLongOrNull(); val b = bundlePrice.toLongOrNull()
                    Text("1회 환산 비용  ${if(k != null && k > 0 && b != null && b >= 0) decimal(b.toDouble()/k) + "원" else "—"}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Hint("횟수당 환산 기준으로 계산해요")
                }
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = guaranteed, onCheckedChange = { guaranteed = it; error = null })
                    Text("확정 천장", fontWeight = FontWeight.Medium)
                }
                Hint("천장이 없으면 체크를 해제하세요")
                error?.let { Spacer(Modifier.height(16.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.height(if(multiple) 70.dp else 180.dp))
                Spacer(Modifier.height(12.dp))
                Action("결과 보기") {
                    try {
                        val input = currentInput()
                        history = (listOf(Entry(System.currentTimeMillis(), multiple, input)) + history).take(50)
                        storage.save(history); selectedFood = 0; page = "result"; error = null
                    } catch(e: IllegalArgumentException) { error = e.message }
                }
            } else {
                val result = Calculator.calculate(currentInput())
                if(page == "result") {
                    Hint(if(multiple) "다회차 비용 계산 기준 · 1회 환산" else "1회 비용 계산 기준")
                    Spacer(Modifier.height(14.dp))
                    Text(if(result.maximum != null) if(multiple) "확정 최대 환산 비용" else "확정 최대 비용" else "확정 최대 비용 없음", fontWeight = FontWeight.Medium)
                    Text(if(result.maximum != null) won(result.maximum * result.input.unitCost) else if(result.average == null) "획득 불가능" else "보장할 수 없어요", fontSize = if(result.maximum != null) 38.sp else 28.sp, fontWeight = FontWeight.ExtraBold)
                    Hint(result.maximum?.let { "최대 ${number(it)}회 · 목표 캐릭터 확정" } ?: "천장 없는 독립 시행 기준")
                    Spacer(Modifier.height(14.dp)); HorizontalDivider()
                    ResultRow("천장 없이 99% 확률로 획득시 비용", result.withoutCeiling99?.let { won(it * result.input.unitCost) } ?: "도달 불가")
                    Hint("천장 미적용 99%: ${result.withoutCeiling99?.let { number(it) + "회" } ?: "도달 불가"}")
                    if(multiple) Hint("묶음 전체 결제액이 아닌 횟수당 환산액입니다. 미사용 횟수 비용은 포함하지 않아요.")
                    Spacer(Modifier.height(32.dp))
                    Hint("화면을 누르면 가챠 비용으로 할 수 있는 일로 넘어가요", Modifier.fillMaxWidth(), true)
                } else {
                    Spacer(Modifier.height(18.dp))
                    Text("이 돈이면 얼마나 먹을까?", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    val food = foods[selectedFood]
                    Spacer(Modifier.height(10.dp))
                    var foodMenuExpanded by remember { mutableStateOf(false) }
                    Box(Modifier.align(Alignment.CenterHorizontally)) {
                        OutlinedButton(onClick = { foodMenuExpanded = true }, shape = RoundedCornerShape(8.dp), modifier = Modifier.widthIn(min = 140.dp).semantics { contentDescription = "현재 ${food.name}, 음식 선택" }) {
                            Text("${food.name}  ▾", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(expanded = foodMenuExpanded, onDismissRequest = { foodMenuExpanded = false }) {
                            foods.forEachIndexed { index, option ->
                                DropdownMenuItem(text = { Text(option.name) }, onClick = { selectedFood = index; foodMenuExpanded = false })
                            }
                        }
                    }
                    FoodImage(selectedFood)
                    val price = food.priceWon
                    val count = Calculator.foodCount(result.comparisonCost, price)
                    Text(if(count != null) "${food.name} ${number(count)}${food.unit}" else "환산 불가", Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, fontSize = 26.sp, textAlign = TextAlign.Center)
                    Hint("1${food.unit} ${number(price)}원 (2025년 기준)", Modifier.fillMaxWidth(), true)
                    Spacer(Modifier.height(32.dp)); Action("다시 계산", outline = true) { page = "input" }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
    if(sheet != null) ModalBottomSheet(onDismissRequest = { sheet = null }, containerColor = Color.White) {
        Column(Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState())) {
            when(sheet) {
                "history" -> {
                    Text("계산한 내역", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    if(history.isEmpty()) Hint("아직 계산 기록이 없어요.")
                    history.forEach { entry ->
                        HorizontalDivider()
                        Text(SimpleDateFormat("MM.dd HH:mm", Locale.KOREA).format(entry.id), color = Muted, fontSize = 12.sp)
                        Text("${if(entry.multiple) "다회차" else "1회"} · ${entry.input.percent}% · ${number(entry.input.price)}원", fontWeight = FontWeight.Medium)
                        Hint("${entry.input.bundle}회 단위 · ${entry.input.ceiling?.let { "천장 ${it}회" } ?: "천장 없음"}")
                        Row {
                            TextButton(onClick = { multiple = entry.multiple; percent = entry.input.percent.toString(); if(multiple) bundlePrice = entry.input.price.toString() else singlePrice = entry.input.price.toString(); bundle = entry.input.bundle.toString(); guaranteed = entry.input.ceiling != null; ceiling = (entry.input.ceiling ?: 200).toString(); page = "input"; error = null; sheet = null }) { Text("다시 사용") }
                            TextButton(onClick = { history = history.filterNot { it.id == entry.id }; storage.save(history) }) { Text("삭제") }
                        }
                    }
                    if(history.isNotEmpty()) TextButton(onClick = { history = emptyList(); storage.save(history) }) { Text("기록 전체 삭제") }
                }

            }
            TextButton(onClick = { sheet = null }, modifier = Modifier.align(Alignment.End)) { Text("닫기") }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable private fun Numeric(label: String, value: String, unit: String, modifier: Modifier, decimal: Boolean = false, enabled: Boolean = true, change: (String) -> Unit) {
    Column(modifier) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(value = value, onValueChange = { if(it.length <= 20) change(it) }, enabled = enabled, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = if(decimal) KeyboardType.Decimal else KeyboardType.Number), suffix = { Text(unit, fontSize = 13.sp) }, textStyle = LocalTextStyle.current.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().semantics { contentDescription = "$label, $unit" })
    }
}
@Composable private fun Action(label: String, outline: Boolean = false, action: () -> Unit) {
    val modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
    if(outline) OutlinedButton(onClick = action, modifier = modifier, shape = RoundedCornerShape(8.dp)) { Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
    else Button(onClick = action, modifier = modifier, shape = RoundedCornerShape(8.dp)) { Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
}
@Composable private fun Hint(text: String, modifier: Modifier = Modifier, centered: Boolean = false) { Text(text, modifier, color = Muted, fontSize = 14.sp, lineHeight = 21.sp, textAlign = if(centered) TextAlign.Center else TextAlign.Start) }
@Composable private fun ResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text(label, Modifier.weight(1f), fontSize = 15.sp); Text(value, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.End) }
}
@Composable private fun FoodImage(index: Int) {
    val resources = LocalResources.current
    val bitmap = remember(index, resources) {
        BitmapFactory.decodeResource(resources, R.drawable.food_atlas)?.let { source ->
            val width = source.width / 5
            Bitmap.createBitmap(source, index * width, (source.height - width) / 2, width, width).asImageBitmap()
        }
    }
    if(bitmap != null) Image(bitmap, "${foods[index].name} 예시 사진", Modifier.fillMaxWidth().height(140.dp))
}
