package com.shatteredpixel.shatteredpixeldungeon.utils;

import java.util.Calendar;

public class Holidays {

    public enum Holiday {
        NONE,
        EASTER,             // T BD
        BREAD_INDEPENDENT,  // 6월 4일 빵복절
        HWEEN,              // 公历10月29日~11月1日 万圣节
        midAutumnFestival,  // 八月十五中秋节
        XMAS,               // 3rd week of december through first week of january
        SPRING_FESTIVAL,    // 除夕及正月初一至初七 春节
        LANTERN_FESTIVAL,   // 正月十五 元宵节
        QINGMING,           // 公历4月4日~6日 清明节
        DRAGON_BOAT,        // 五月初五 端午节
        QIXI,               // 七月初七 七夕
        DOUBLE_NINTH,       // 九月初九 重阳节
        NATIONAL_DAY        // 公历10月1日~7日 国庆节

    }

    public static Holiday holiday;

    static {
        checkSolarHolidays();
    }

    //按公历重置节日状态；农历节日由 Gregorian.LunarCheckDate() 在此基础上覆盖
    public static void checkSolarHolidays() {
        holiday = Holiday.NONE;

        final Calendar calendar = Calendar.getInstance();

        switch (calendar.get(Calendar.MONTH)) {
            case Calendar.JANUARY:
                if (calendar.get(Calendar.WEEK_OF_MONTH) == 1)
                    holiday = Holiday.XMAS;
                break;

            case Calendar.MAY:
                if (calendar.get(Calendar.WEEK_OF_MONTH) == 4)
                    holiday = Holiday.BREAD_INDEPENDENT;
                break;
            case Calendar.JUNE:
                if (calendar.get(Calendar.WEEK_OF_MONTH) <= 2)
                    holiday = Holiday.BREAD_INDEPENDENT;
                break;

            case Calendar.OCTOBER:
                if (calendar.get(Calendar.DAY_OF_MONTH) >= 29)
                    holiday = Holiday.HWEEN;
                break;
            case Calendar.NOVEMBER:
                if (calendar.get(Calendar.DAY_OF_MONTH) == 1)
                    holiday = Holiday.HWEEN;
                break;

            case Calendar.DECEMBER:
                if (calendar.get(Calendar.WEEK_OF_MONTH) >= 3)
                    holiday = Holiday.XMAS;
                break;
        }
    }

    public static Holiday getHolidays() { return holiday; }
}