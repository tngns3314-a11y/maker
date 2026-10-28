#!/usr/bin/env python3
"""Print KoreanHolidays.LUNAR entries; requires korean_lunar_calendar==0.4.0.
Only conversion results are embedded in the app; no runtime dependency/network.
"""
from korean_lunar_calendar import KoreanLunarCalendar
for year in range(2024, 2051):
    values = []
    for month, day in [(1, 1), (4, 8), (8, 15)]:
        calendar = KoreanLunarCalendar()
        assert calendar.setLunarDate(year, month, day, False)
        values.append(calendar.SolarIsoFormat()[5:])
    print('        "' + ','.join(values) + '", // ' + str(year))
