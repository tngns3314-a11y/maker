# 대한민국 공휴일 데이터

위젯과 앱 달력은 같은 `KoreanHolidays` 데이터를 사용합니다. 지원 범위는 2024~2050년입니다. 일요일은 날짜를 빨갛게 표시하며, 별도 공휴일에만 빨간 네모를 표시합니다.

- 양력 공휴일, 음력 설·부처님오신날·추석, 대체공휴일을 오프라인으로 계산합니다.
- 2026년부터 노동절과 제헌절 및 해당 대체공휴일을 포함합니다.
- 확정된 별도 날짜: 2024-04-10 국회의원 선거, 2024-10-01 임시공휴일, 2025-01-27 임시공휴일, 2025-06-03 대통령 선거, 2026-06-03 지방선거.
- 향후 임시공휴일·선거일과 법령 변경은 앱 데이터 업데이트가 필요합니다. 지원 범위 밖의 날짜에는 공휴일을 추측해서 표시하지 않습니다.
- 음력 날짜표는 `korean_lunar_calendar==0.4.0`의 한국 음력 변환 결과이며 `scripts/generate-holiday-table.py`로 재생성합니다. 패키지 코드 자체는 앱에 포함하지 않습니다.

검증 기준 및 참고:

- [관공서의 공휴일에 관한 규정](https://law.go.kr/LSW/lsInfoP.do?ancYnChk=0&lsId=002404)
- [인사혁신처: 올해부터 노동절, 제헌절 공휴일로 (2026-04-29)](https://www.mpm.go.kr/mpm/comm/newsPress/newsPressRelease/?mode=list&searchCondition=title&searchKeyword=%EA%B3%B5%ED%9C%B4%EC%9D%BC)
- [우주항공청: 2027년 월력요항](https://www.kasa.go.kr/prog/plcyBrf/brief/kor/sub01_01_04/view.do?plcyBrfNo=431)

2027년의 일요일 외 공휴일 24일, 2026년 설·추석과 대체공휴일, 2025년 어린이날·부처님오신날 중복, 2028년 개천절·추석 중복 등을 테스트합니다.

음력 변환 도구: [korean_lunar_calendar_py](https://github.com/usingsky/korean_lunar_calendar_py) (MIT).
