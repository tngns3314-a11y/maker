package kr.co.coinnumber
import org.junit.Test
import org.junit.Assert.*
class CalculatorTest {
    @Test fun baseline() {
        val r = Calculator.calculate(Input(1.0, 3000))
        assertEquals(459L, r.withoutCeiling99); assertEquals(300000.0, r.comparisonCost!!, 0.001)
        assertEquals(1377000.0, r.withoutCeiling99!! * r.input.unitCost, 0.001)
        assertEquals(0.6339676587, Calculator.cumulative(r.input,100), 0.00000001)
    }
    @Test fun ceiling() {
        val r = Calculator.calculate(Input(1.0,3000,ceiling=200))
        assertEquals(200L,r.maximum); assertEquals(600000.0,r.comparisonCost!!,0.001)
        assertEquals(259806.0975,r.average!!*3000,0.001); assertEquals(200L,r.actual99)
    }
    @Test fun bundles() {
        val r=Calculator.calculate(Input(1.0,33000,10,200))
        assertEquals(660000.0,r.comparisonCost!!,0.001)
        assertEquals(1514700.0,r.withoutCeiling99!!*r.input.unitCost,0.001)
        assertEquals(100.0/3,Input(1.0,100,3).unitCost,0.000001)
    }
    @Test fun boundaries() {
        assertNull(Calculator.calculate(Input(0.0,0)).average)
        assertEquals(200.0,Calculator.calculate(Input(0.0,100,ceiling=200)).average!!,0.0)
        assertEquals(1L,Calculator.calculate(Input(100.0,3000,ceiling=200)).maximum)
        assertEquals(1.0,Calculator.calculate(Input(1.0,3000,ceiling=1)).average!!,0.000001)
        assertEquals(0.0,Calculator.calculate(Input(1.0,0)).comparisonCost!!,0.0)
    }
    @Test fun thresholdMinimality() {
        for(p in listOf(0.000001,0.001,0.1,1.0,50.0,99.9,100.0)) {
            val input=Input(p,1_000_000_000); val r=Calculator.calculate(input); val n=r.withoutCeiling99!!
            assertTrue(Calculator.cumulative(input,n) >= 0.99-1e-14)
            assertTrue(Calculator.cumulative(input,n-1) < 0.99)
            assertTrue(r.comparisonCost!!.isFinite())
        }
    }
    @Test fun foodRounding() {
        assertEquals(26L,Calculator.foodCount(600000.0,23000)); assertEquals(0L,Calculator.foodCount(9999.0,10000))
        assertEquals(0L,Calculator.foodCount(0.0,10000)); assertNull(Calculator.foodCount(100.0,0)); assertNull(Calculator.foodCount(null,1000))
    }
    @Test fun invalidInputs() {
        for(block in listOf<()->Unit>({ Input(-1.0,1) }, { Input(Double.NaN,1) }, {Input(0.0000001,1)}, {Input(1.0,-1)}, {Input(1.0,1,0)}, {Input(1.0,1,ceiling=0)})) {
            try { block(); fail("invalid input accepted") } catch(_: IllegalArgumentException) {}
        }
    }
}
