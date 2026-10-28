package kr.co.coinnumber
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
class FlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun separateResultsSelectionAndBack() {
        ui.onNodeWithText("1회 비용 계산").performClick()
        ui.onNodeWithText("처음부터 시작하는 기준").assertDoesNotExist()
        ui.onNodeWithText("결과 보기").performScrollTo().performClick()
        ui.onNodeWithText("600,000원").assertExists()
        ui.onNodeWithText("평균 예상 비용").assertDoesNotExist()
        ui.onNodeWithText("1,377,000원").assertExists()
        ui.onNodeWithText("이 돈이면 얼마나 먹을까?").assertDoesNotExist()
        ui.onNodeWithText("상세 계산 보기").assertDoesNotExist()
        ui.onNodeWithText("평균 횟수:", substring = true).assertDoesNotExist()
        ui.onNodeWithText("현재 조건 99% 이상:", substring = true).assertDoesNotExist()
        ui.onNodeWithText("천장 미적용 가정", substring = true).assertDoesNotExist()
        ui.onNodeWithText("천장 없이 99% 확률로 획득시 비용").assertExists()
        ui.onNodeWithText("천장 미적용 99%: 459회").assertExists()
        ui.onNodeWithText("매회 확률 일정", substring = true).assertDoesNotExist()
        ui.onNodeWithText("이 돈이면 얼마나 먹을까?").assertDoesNotExist()
        ui.onNodeWithTag("costResult").performClick()
        ui.onNodeWithText("이 돈이면 얼마나 먹을까?").assertExists()
        ui.onNodeWithText("평균 예상 비용").assertDoesNotExist()
        ui.onNodeWithText("가챠 비용으로 할 수 있는 일").assertExists()
        ui.onNodeWithText("치킨 24마리").assertExists()
        ui.onNodeWithText("음식 가격 설정").assertDoesNotExist()
        ui.onNodeWithText("버튼을 눌러 음식을 선택하세요").assertDoesNotExist()
        val counts = mapOf("햄버거" to "75개", "냉면" to "35그릇", "족발" to "15접시", "순대국밥" to "54그릇", "치킨" to "24마리")
        var current = "치킨"
        for(name in listOf("햄버거", "냉면", "족발", "순대국밥", "치킨")) {
            ui.onNodeWithText("$current  ▾").performClick()
            for(option in listOf("치킨", "족발", "냉면", "순대국밥", "햄버거")) ui.onNodeWithText(option).assertIsDisplayed()
            ui.onNodeWithText(name).performClick()
            ui.onNodeWithText("$name  ▾").assertExists()
            ui.onNodeWithText("$name ${counts[name]}").assertExists()
            current = name
        }
        ui.activityRule.scenario.recreate()
        ui.onNodeWithText("이 돈이면 얼마나 먹을까?").assertExists()
        ui.onNodeWithContentDescription("뒤로가기").performClick()
        ui.onNodeWithText("600,000원").assertExists()
        ui.onNodeWithContentDescription("뒤로가기").performClick()
        ui.onNodeWithText("3000").assertExists()
    }
    @Test fun bundleAndRotation() {
        ui.onNodeWithText("다회차 비용 계산").performClick()
        ui.onNodeWithText("처음부터 시작하는 기준").assertDoesNotExist()
        ui.onNodeWithText("1회 환산 비용  3,300원").assertExists()
        ui.onNodeWithText("결과 보기").performScrollTo().performClick()
        ui.onNodeWithText("660,000원").assertExists()
        ui.onNodeWithText("평균 예상 환산 비용").assertDoesNotExist()
        ui.onNodeWithText("천장 미적용 99%: 459회").assertExists()
        ui.onNodeWithText("묶음 전체 결제액이 아닌", substring = true).assertExists()
        ui.activityRule.scenario.recreate()
        ui.onNodeWithText("660,000원").assertExists()
        ui.onNodeWithText("1,514,700원").assertExists()
        ui.onNodeWithTag("costResult").performClick()
        ui.onNodeWithText("치킨 26마리").assertExists()
        ui.onNodeWithText("최대 환산 비용 660,000원 기준").assertDoesNotExist()
        ui.onNodeWithText("다시 계산").performScrollTo().performClick()
        ui.onNodeWithText("33000").assertExists()
    }
    @Test fun historyLabelWithoutHelp() {
        ui.onNodeWithText("계산한 내역").performClick()
        ui.onNodeWithText("계산 기준과 도움말 보기").assertDoesNotExist()
        ui.onNodeWithText("기록 · 도움말").assertDoesNotExist()
        ui.onNodeWithText("닫기").performClick()
        ui.onNodeWithText("1회 비용 계산").assertExists()
    }
}
