package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.DMR.AN94;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gun561;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MG.Mg42;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SMG.P90;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Thunder;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.AR.G36;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BP.Mos;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BP.SaigaPlate;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.DMR.Kar98;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.DMR.Sass;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.HB.Kriss;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.LR.GSH18;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.LR.Wa;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SA.GUA91;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SA.NagantRevolver;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SG.Usas12;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SR.AWP;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SR.MOSINNAGANT;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SR.Ntw20;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.UG.C96;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;

import java.util.HashMap;

public class WeaponToCard {

    private static final HashMap<Class<? extends MeleeWeapon>, Card> map = new HashMap<>();

    static {
        // ========== 一、HS2000 阵营 ==========
        // （当前地牢中暂无同名枪械类可映射）

        // ========== 二、Vector 阵营（火焰地带） ==========
        // —— 首卡 ——
        map.put(Kriss.class,         FirstCard.Vector);                 // Vector维克托 (Kriss Vector) → Vector
        // —— FinalCard ——
        map.put(G36.class,           FinalCard.Vector.G36);             // H&K G36 → G36

        // ========== 三、VHS 阵营（骇入） ==========
        // —— CommonCard ——
        map.put(Thunder.class,       CommonCard.VHS.Thunder);           // 雷电 → 雷电
        map.put(P90.class,              RareCard.VHS.P90);

        // ========== 四、WA2000 阵营（暴击/爆伤） ==========
        // —— 首卡 ——
        map.put(Wa.class,            FirstCard.WA2000);                 // WA2000 → WA2000
        // —— CommonCard ——
        map.put(AWP.class,           CommonCard.WA2000.SV_98);          // SV-98 → SV-98
        // —— RareCard ——
        map.put(MOSINNAGANT.class,   RareCard.WA2000.MOSIN_NAGANT);     // 莫辛-纳甘 → 莫辛-纳甘
        map.put(Ntw20.class,         RareCard.WA2000.NTW_20);           // NTW-20 → NTW-20

        // ========== 五、刘氏步枪 阵营（增援/傀儡） ==========
        // —— CommonCard ——
        map.put(Mos.class,           CommonCard.General_Liu.MOS);       // 莫斯伯格防弹板 → MOS
        // —— RareCard ——
        map.put(SaigaPlate.class,    RareCard.General_Liu.SAIGA);       // Saiga的防弹板 → SAIGA

        // ========== 六、UNIVERSAL 通用阵营 ==========
        // —— CommonCard ——
        map.put(GSH18.class,            CommonCard.UNIVERSAL.GSh_18);      // GSh-18 → GSh-18
        map.put(Sass.class,             CommonCard.UNIVERSAL.Super_SASS);  // Super SASS → Super SASS
        map.put(GUA91.class,            CommonCard.UNIVERSAL._9A91);       // 9A-91 → 9A-91
        map.put(C96.class,              CommonCard.UNIVERSAL.C96);         // 毛瑟C96 → C96
        map.put(Usas12.class,           CommonCard.UNIVERSAL.USAS_12);     // S&T USAS-12 → USAS-12
        map.put(NagantRevolver.class,   CommonCard.UNIVERSAL.Nagant_M1895); // 纳甘左轮 M1895 → 纳甘左轮
        map.put(Gun561.class,           CommonCard.UNIVERSAL.Type56_1);    // Type 56-1 → 56-1式
//        map.put(AN94.class,             CommonCard.UNIVERSAL.AN94);
        // —— FinalCard ——
        map.put(Kar98.class,            FinalCard.UNIVERSAL.Kar98k);       // Kar98k → Kar98k
        map.put(Mg42.class,             FinalCard.UNIVERSAL.MG5);          // H&K MG4 (Mg42) → MG5
    }
    private static boolean canSelect(Item item) {
        Card card = map.get(item.getClass());
        CardSelector selector = CardSelector.INSTANCE();
        return !selector.contain(card) || selector.curCards.contains(card);
    }

    //该武器是否可以插入丹德莱的抽卡器换取对应卡牌（仅供图鉴等界面查询）
    public static boolean isCardCompatible( MeleeWeapon weapon ) {
        return weapon != null && map.containsKey(weapon.getClass());
    }
    public static WndBag.ItemSelector weaponSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(WeaponToCard.class, "select_title");
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            if (!(item instanceof MeleeWeapon))
                return false;
            if (!map.containsKey(item.getClass()))
                return false;
            return canSelect(item);
        }

        @Override
        public void onSelect( Item item ) {
            if (item != null) {
                Card c = map.get(item.getClass());
                CardSelector selector = CardSelector.INSTANCE();
                GirlsFrontlinePixelDungeon.scene().addToFront( new WndOptions(c.colorTitle(), c.info(),
                                Messages.get(WeaponToCard.class, "Entry"),
                                Messages.get(WeaponToCard.class, "Cancel")){
                            @Override
                            protected void onSelect( int index ) {
                                if (index == 0){
                                    if (item.isEquipped(Dungeon.hero()))
                                        ((EquipableItem) item).detachCursedEquipment(Dungeon.hero());
                                    else
                                        item.detach(Dungeon.hero().belongings.backpack);
                                    if (c instanceof FirstCard)
                                        selector.FirstCards.add((FirstCard) c);
                                    else if (c instanceof CommonCard)
                                        selector.CommonCards.add((CommonCard) c);
                                    else if (c instanceof RareCard)
                                        selector.RareCards.add((RareCard) c);
                                    else if (c instanceof FinalCard)
                                        selector.FinalCards.add((FinalCard) c);
                                    c.onSelect();
                                    if (selector.curCards.remove(c))
                                        Card.addSignalCard(selector);
                                    selector.coolDownLeft += Random.NormalIntRange(300, 600);
                                    hide();
                                    Item.updateQuickslot();
                                }
                                else if (index == 1)
                                    hide();
                            }
                            @Override
                            protected boolean enabled(int index) {
                                if (index == 0) {
                                    if (c instanceof CommonCard)
                                        return !selector.CommonCards.isEmpty() || selector.curCardNum == 1;
                                    else if (c instanceof RareCard)
                                        return !selector.RareCards.isEmpty() || selector.curCardNum == 5;
                                    else if (c instanceof FinalCard)
                                        return !selector.FinalCards.isEmpty() || selector.curCardNum == 7;
                                }

                                return true;
                            }
                        }
                );
            }
        }
    };
}
