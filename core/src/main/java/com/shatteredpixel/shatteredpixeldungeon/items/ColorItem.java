package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;

public interface ColorItem extends Bundlable {
    void setIgnore();
    void anonymize();

    //猜测归属的“知识键”：异变药水/卷轴映射回普通类，普通类即自身
    @SuppressWarnings("unchecked")
    default Class<? extends ColorItem> guessKey() {
        return (Class<? extends ColorItem>) getClass();
    }

    //猜测用的标签（药水颜色/卷轴符文），无标签的类返回 null
    default String guessLabel() {
        return null;
    }

    default void guessType(String cause) {
        if (Dungeon.guessType.add(guessKey()))
            if (cause != null && !cause.isEmpty() && SPDSettings.isAutoIdentify() && SPDSettings.showAutoGuessingText()) {
                GLog.newLine();
                GLog.i(cause);
            }
    }

    //创建道具时顺手标记为“可推断”，便于在投放点链式书写
    static <T extends Item & ColorItem> T guess(T item, String cause) {
        item.guessType(cause);
        return item;
    }
    default boolean isGuess(){
        return Dungeon.guessType.contains(guessKey());
    }
    default boolean showGuess(){
        return isGuess() && SPDSettings.isAutoIdentify();
    }
}
