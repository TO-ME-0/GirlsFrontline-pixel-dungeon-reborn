package com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;
import java.util.HashMap;

public interface Holder {

    void fillMap(HashMap<Class<? extends Item>, ArrayList<Integer>> map, int index);

    /** 供各 Holder 复用：向 map 的 key 追加一个目标下标。 */
    static void add(HashMap<Class<? extends Item>, ArrayList<Integer>> map,
                    Class<? extends Item> key, int index) {
        ArrayList<Integer> list = map.get(key);
        if (list == null)
            map.put(key, list = new ArrayList<>());
        list.add(index);
    }

    /** 供各 Holder 复用：按 SeedFindScene 的清单逻辑（probs >= 0f）展开一个 Category。 */
    @SuppressWarnings("unchecked")
    static void addCategory(HashMap<Class<? extends Item>, ArrayList<Integer>> map,
                            Generator.Category category, int index) {
        for (int i = 0; i < category.classes.length; i++)
            if (category.probs[i] >= 0f)
                add(map, (Class<? extends Item>) category.classes[i], index);
    }

    // ================= 护甲 / 法杖 / 戒指 =================

    class ArmorHolder extends Armor implements Holder {
        { image = ItemSpriteSheet.ARMOR_HOLDER; }

        @Override
        public void fillMap(HashMap<Class<? extends Item>, ArrayList<Integer>> map, int index) {
            addCategory(map, Generator.Category.ARMOR, index);
        }

        @Override
        public String name() { return "任意护甲"; }

        @Override
        public String desc() { return ""; }
    }

    class WandHolder extends Item implements Holder {
        { image = ItemSpriteSheet.WAND_HOLDER; }

        @Override
        public void fillMap(HashMap<Class<? extends Item>, ArrayList<Integer>> map, int index) {
            addCategory(map, Generator.Category.WAND, index);
        }

        @Override
        public String name() { return "任意子弹模块"; }

        @Override
        public String desc() { return ""; }
    }

    class RingHolder extends Item implements Holder {
        { image = ItemSpriteSheet.RING_HOLDER; }

        @Override
        public void fillMap(HashMap<Class<? extends Item>, ArrayList<Integer>> map, int index) {
            addCategory(map, Generator.Category.RING, index);
        }

        @Override
        public String name() { return "任意瞄准镜"; }

        @Override
        public String desc() { return ""; }
    }

    class ScrollHolder extends Item implements Holder {
        { image = ItemSpriteSheet.SCROLL_HOLDER; }

        @Override
        public void fillMap(HashMap<Class<? extends Item>, ArrayList<Integer>> map, int index) {
            addCategory(map, Generator.Category.SCROLL, index);
        }

        @Override
        public String name() { return "任意磁盘"; }

        @Override
        public String desc() { return ""; }
    }
    class PotionHolder extends Item implements Holder {
        { image = ItemSpriteSheet.POTION_HOLDER; }

        @Override
        public void fillMap(HashMap<Class<? extends Item>, ArrayList<Integer>> map, int index) {
            addCategory(map, Generator.Category.POTION, index);
        }

        @Override
        public String name() { return "任意药水"; }

        @Override
        public String desc() { return ""; }
    }
    class StoneHolder extends Item implements Holder {
        { image = ItemSpriteSheet.STONE_HOLDER; }

        @Override
        public void fillMap(HashMap<Class<? extends Item>, ArrayList<Integer>> map, int index) {
            addCategory(map, Generator.Category.STONE, index);
        }

        @Override
        public String name() { return "任意模块"; }

        @Override
        public String desc() { return ""; }
    }

    // ================= 武器（总 / 各阶） =================

    class WeaponHolder extends Weapon implements Holder {
        { image = ItemSpriteSheet.WEAPON_HOLDER; }

        @Override
        public void fillMap(HashMap<Class<? extends Item>, ArrayList<Integer>> map, int index) {
            for (int t = 1; t < Generator.wepTiers.length; t++)
                addCategory(map, Generator.wepTiers[t], index);
            for (int t = 1; t < Generator.misTiers.length; t++)
                addCategory(map, Generator.misTiers[t], index);
        }

        @Override public int min(int lvl) { return 0; }

        @Override public int max(int lvl) { return 0; }

        @Override public int STRReq(int lvl) { return 0; }

        @Override
        public String name() { return "任意武器"; }

        @Override
        public String desc() { return ""; }
    }

    class MeleeWeaponHolder extends WeaponHolder {
        public MeleeWeaponHolder() {
            this(0);
        }
        public MeleeWeaponHolder(int tier) {
            this.tier = tier;
        }
        @Override
        public void fillMap(HashMap<Class<? extends Item>, ArrayList<Integer>> map, int index) {
            if (tier == 0)
                for (int t = 1; t < Generator.wepTiers.length; t++)
                    addCategory(map, Generator.wepTiers[t], index);
            else
                addCategory(map, Generator.wepTiers[tier - 1], index);
        }

        @Override
        public String name() { return tier == 0 ? "任意近战武器" : tier + "阶近战武器"; }

    }
    Class<? extends MeleeWeaponHolder>[] meleeHolder = new Class[]{null, Tier2MeleeHolder.class, Tier3MeleeHolder.class, Tier4MeleeHolder.class, Tier5MeleeHolder.class, Tier6MeleeHolder.class};
    class Tier2MeleeHolder extends MeleeWeaponHolder { public Tier2MeleeHolder() {super(2);}}
    class Tier3MeleeHolder extends MeleeWeaponHolder { public Tier3MeleeHolder() {super(3);}}
    class Tier4MeleeHolder extends MeleeWeaponHolder { public Tier4MeleeHolder() {super(4);}}
    class Tier5MeleeHolder extends MeleeWeaponHolder { public Tier5MeleeHolder() {super(5);}}
    class Tier6MeleeHolder extends MeleeWeaponHolder { public Tier6MeleeHolder() {super(6);}}
    class MissileWeaponHolder extends WeaponHolder {
        { image = ItemSpriteSheet.MISSILE_HOLDER; }
        public MissileWeaponHolder() {
            this(0);
        }
        public MissileWeaponHolder(int tier) {
            this.tier = tier;
        }
        @Override
        public void fillMap(HashMap<Class<? extends Item>, ArrayList<Integer>> map, int index) {
            if (tier == 0)
                for (int t = 1; t < Generator.wepTiers.length; t++)
                    addCategory(map, Generator.misTiers[t], index);
            else
                addCategory(map, Generator.misTiers[tier - 1], index);
        }
        @Override
        public String name() { return tier == 0 ? "任意投掷武器" : tier + "阶投掷武器"; }

    }
    Class<? extends MissileWeaponHolder>[] missileHolder = new Class[]{null, Tier2MissileHolder.class, Tier3MissileHolder.class, Tier4MissileHolder.class, Tier5MissileHolder.class};
    class Tier2MissileHolder extends MissileWeaponHolder { public Tier2MissileHolder() {super(2);}}
    class Tier3MissileHolder extends MissileWeaponHolder { public Tier3MissileHolder() {super(3);}}
    class Tier4MissileHolder extends MissileWeaponHolder { public Tier4MissileHolder() {super(4);}}
    class Tier5MissileHolder extends MissileWeaponHolder { public Tier5MissileHolder() {super(5);}}
}