package kr.co.coinnumber

import kotlin.math.*

data class Input(val percent: Double, val price: Long, val bundle: Long = 1, val ceiling: Long? = null) {
    val unitCost: Double get() = price.toDouble() / bundle
    init {
        require(percent.isFinite() && percent in 0.0..100.0 && (percent == 0.0 || percent >= 0.000001)) { "확률은 0% 또는 0.000001~100%로 입력하세요." }
        require(price in 0..1_000_000_000L) { "가격은 0~1,000,000,000원 정수로 입력하세요." }
        require(bundle in 1..1_000_000L) { "묶음 횟수는 1~1,000,000회로 입력하세요." }
        require(ceiling == null || ceiling in 1..1_000_000L) { "천장은 1~1,000,000회로 입력하세요." }
    }
}
data class Calculation(val maximum: Long?, val average: Double?, val withoutCeiling99: Long?, val actual99: Long?, val input: Input) {
    val comparisonCost: Double? get() = (maximum?.toDouble() ?: average)?.times(input.unitCost)
}
object Calculator {
    fun calculate(input: Input): Calculation {
        val p = input.percent / 100
        val t = input.ceiling
        val n99 = when(p) {
            0.0 -> null
            1.0 -> 1L
            else -> {
                val logFailure = ln1p(-p)
                val threshold = ln(0.01)
                var n = ceil(threshold / logFailure).toLong()
                while (n * logFailure > threshold) n++
                while (n > 1 && (n - 1) * logFailure <= threshold) n--
                n
            }
        }
        val maximum = if (p == 1.0) 1L else t
        val average = when {
            p == 1.0 -> 1.0
            p == 0.0 -> t?.toDouble()
            t != null -> -expm1(t * ln1p(-p)) / p
            else -> 1 / p
        }
        return Calculation(maximum, average, n99, if(t != null) minOf(n99 ?: t, t) else n99, input)
    }
    fun cumulative(input: Input, n: Long): Double = when {
        n <= 0 -> 0.0
        input.ceiling != null && n >= input.ceiling -> 1.0
        input.percent == 100.0 -> 1.0
        else -> -expm1(n * ln1p(-input.percent / 100))
    }
    fun foodCount(amount: Double?, price: Long?): Long? = if(amount == null || price == null || price <= 0) null else floor(amount / price).toLong()
}
