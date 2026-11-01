package com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;

import java.util.Set;

/**
 * 结构化查询目标：物品类 + 最低等级 + 可选附魔/铭文体。
 * 匹配要求物品类精确一致、强化等级不低于要求；
 * 指定 aug 时，武器走附魔分支、护甲走铭文分支（由 augIsGlyph 在构造时定型，
 * 调用方保证 aug 为附魔则 cls 必为 Weapon、aug 为铭文则 cls 必为 Armor）。
 */
public class WantedTarget {
    public final int minLevel;
    public final Class<?> aug;
    private final boolean augIsGlyph;

    //查种热路径专用：奖励槽可接受的物品类。具体物品=singleton，Holder=fillMap 展开的整类子类；
    //Holder.getClass() 与实际物品类恒不相等，所以奖励槽的类别判定只能查这个集合。
    //直接挂在目标对象上，省掉 SeedFinder 里的 HashMap<WantedTarget, ?> 查表
    public Set<Class<? extends Item>> rewardClasses;
    //该目标在 wantedArr 中的下标；-1 表示不是奖励槽（省掉 rewardIdx 查表）
    public int rewardIndex = -1;

    public WantedTarget(Item item) {
        this.minLevel = item.trueLevel();
        Object aug = null;
        if (item instanceof Armor) {
            aug = ((Armor) item).glyph;
        } else if (item instanceof Weapon) {
            aug = ((Weapon) item).enchantment;
        }
        if (aug != null) {
            this.aug = aug.getClass();
            this.augIsGlyph = aug instanceof Armor.Glyph;
        } else {
            this.aug = null;
            this.augIsGlyph = false;
        }
    }

    public boolean matches(Item item) {
        if (item.level() < minLevel) return false;
        if (aug == null) return true;
        if (augIsGlyph) {
            Armor.Glyph g = ((Armor) item).glyph;
            return g != null && g.getClass() == aug;
        }
        Weapon.Enchantment e = ((Weapon) item).enchantment;
        return e != null && e.getClass() == aug;
    }
}
