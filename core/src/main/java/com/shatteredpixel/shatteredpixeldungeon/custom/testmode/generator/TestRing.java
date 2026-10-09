package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfKing;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ChaoticCenser;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.CrackedSpyglass;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.DimensionalSundial;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ExoticCrystals;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.EyeOfNewt;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.FerretTuft;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.MimicTooth;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.MossyClump;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ParchmentScrap;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.PetrifiedSeed;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.SaltCube;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ThirteenLeafClover;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrapMechanism;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Objects;

//Also contains wand
public class TestRing extends TestGenerator {
    {
        image = ItemSpriteSheet.RING_HOLDER;
    }

    private int levelToGen;
    private int category;
    private boolean cursed;
    private int selected;
    private static final int RING_CAT = 0;
    private static final int WAND_CAT = 1;
    private static final int TRINKET_CAT = 2;
    private static final String AC_SETTING = "setting";

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_GIVE)) {
            GameScene.show(new SettingsWindow());
        }
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        return super.actions(hero);
    }

    private void createItem(){
        boolean collect = false;
        if (category == RING_CAT) {
            Ring r = Reflection.newInstance(idToRing(selected));
            if(r != null) {
                if(Challenges.isItemBlocked(r)) return;
                modifyRing(r);
                collect = r.identify().collect();
                if(collect){
                    GLog.i(Messages.get(this, "collect_success", r.name()));
                }else{
                    r.doDrop(curUser);
                }
            }
        } else if (category == WAND_CAT) {
            Wand w = Reflection.newInstance(idToWand(selected));
            if( w != null) {
                if(Challenges.isItemBlocked(w)) return;
                modifyWand(w);
                collect = w.identify().collect();
                if(collect){
                    GLog.i(Messages.get(this, "collect_success", w.name()));
                }else{
                    w.doDrop(curUser);
                }
            }
        } else if (category == TRINKET_CAT) {
            Trinket t = Reflection.newInstance(idToTrinket(selected));
            if (t != null) {
                t.level(Math.min(3, levelToGen));
                collect = t.identify().collect();
                if(collect){
                    GLog.i(Messages.get(this, "collect_success", t.name()));
                }else{
                    t.doDrop(curUser);
                }
            }
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("selected", selected);
        bundle.put("is_cursed", cursed);
        bundle.put("level_to_gen", levelToGen);
        bundle.put("category", category);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        selected = bundle.getInt("selected");
        cursed = bundle.getBoolean("is_cursed");
        levelToGen = bundle.getInt("level_to_gen");
        category = bundle.getInt("category");
    }

    private void modifyRing(Ring r) {
        r.level(levelToGen);
        r.cursed = cursed;
    }

    private void modifyWand(Wand w) {
        w.level(levelToGen);
        w.curCharges = w.maxCharges;
        w.cursed = cursed;
    }

    private Class<? extends Trinket> idToTrinket(int id) {
        switch (id) {
            case 0:  return RatSkull.class;
            case 1:  return ParchmentScrap.class;
            case 2:  return PetrifiedSeed.class;
            case 3:  return ExoticCrystals.class;
            case 4:  return MossyClump.class;
            case 5:  return DimensionalSundial.class;
            case 6:  return ThirteenLeafClover.class;
            case 7:  return TrapMechanism.class;
            case 8:  return MimicTooth.class;
            case 9:  return WondrousResin.class;
            case 10: return EyeOfNewt.class;
            case 11: return SaltCube.class;
            case 12: return VialOfBlood.class;
            case 13: return ShardOfOblivion.class;
            case 14: return ChaoticCenser.class;
            case 15: return FerretTuft.class;
            case 16:
            default: return CrackedSpyglass.class;
        }
    }

    private static ArrayList<Class<? extends Trinket>> trinketList = new ArrayList<>();

    private void buildTrinketList() {
        if (!trinketList.isEmpty()) return;
        for (int i = 0; i < 17; ++i) {
            trinketList.add(idToTrinket(i));
        }
    }

    private Class<? extends Ring> idToRing(int id) {
        switch (id) {
            case 0:
                return RingOfAccuracy.class;
            case 1:
                return RingOfKing.class;
            case 2:
                return RingOfEvasion.class;
            case 3:
                return RingOfEnergy.class;
            case 4:
                return RingOfForce.class;
            case 5:
                return RingOfFuror.class;
            case 6:
                return RingOfHaste.class;
            case 7:
                return RingOfMight.class;
            case 8:
                return RingOfSharpshooting.class;
            case 9:
                return RingOfTenacity.class;
            case 10:
            default:
                return RingOfWealth.class;
        }
    }

    private Class<? extends Wand> idToWand(int id) {
        switch (id) {
            case 0:
                return WandOfBlastWave.class;
            case 1:
                return WandOfCorrosion.class;
            case 2:
                return WandOfCorruption.class;
            case 3:
                return WandOfDisintegration.class;
            case 4:
                return WandOfFireblast.class;
            case 5:
                return WandOfFrost.class;
            case 6:
                return WandOfLightning.class;
            case 7:
                return WandOfLivingEarth.class;
            case 8:
                return WandOfMagicMissile.class;
            case 9:
                return WandOfPrismaticLight.class;
            case 10:
                return WandOfRegrowth.class;
            case 11:
                return WandOfTransfusion.class;
            case 12:
            default:
                return WandOfWarding.class;
        }

    }

    private static ArrayList<Class<? extends Ring>> ringList = new ArrayList<>();
    private static ArrayList<Class<? extends Wand>> wandList = new ArrayList<>();

    private void buildRingList() {
        if (!ringList.isEmpty()) return;
        for (int i = 0; i < 11; ++i) {
            ringList.add(idToRing(i));
        }
    }

    private void buildWandList() {
        if (!wandList.isEmpty()) return;
        for (int i = 0; i < 13; ++i) {
            wandList.add(idToWand(i));
        }
    }

    private int total(int category){
        if (category == RING_CAT) return 11;
        if (category == WAND_CAT) return 13;
        if (category == TRINKET_CAT) return 17;
        return 0;
    }

    //每行图标数，超出则换行（饰品有17个，需要多行排布）
    private static final int PER_ROW = 7;

    private int iconRows(){
        return (total(category) + PER_ROW - 1) / PER_ROW;
    }

    private class SettingsWindow extends Window {
        private static final int WIDTH = 120;
        private static final int BTN_SIZE = 17;
        private static final int GAP = 2;
        private OptionSlider o_level;
        private OptionSlider o_category;
        private CheckBox c_curse;
        private RenderedTextBlock t_selected;
        private RedButton b_create;
        private ArrayList<IconButton> iconButtonsList = new ArrayList<>();

        public SettingsWindow() {
            buildRingList();
            buildWandList();
            buildTrinketList();

            o_category = new OptionSlider(Messages.get(this, "category"), Messages.get(this, "ring"), Messages.get(this, "trinket"), 0, 2) {
                @Override
                protected void onChange() {
                    category = getSelectedValue();
                    selected = 0;
                    for (IconButton ib : iconButtonsList.toArray(new IconButton[0])) {
                        ib.destroy();
                    }
                    createImage();
                    layout();
                }
            };
            o_category.setSelectedValue(category);
            o_category.setRect(0, GAP, WIDTH, 24);
            add(o_category);

            createImage();

            t_selected = PixelScene.renderTextBlock("", 6);
            t_selected.text();
            add(t_selected);

            o_level = new OptionSlider(Messages.get(this, "level"), "0", "12", 0, 12) {
                @Override
                protected void onChange() {
                    levelToGen = getSelectedValue();
                }
            };
            o_level.setSelectedValue(levelToGen);
            add(o_level);

            c_curse = new CheckBox(Messages.get(this, "curse")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    cursed = checked();
                }
            };
            c_curse.checked(cursed);
            add(c_curse);

            b_create = new RedButton(Messages.get(this, "create_button")) {
                @Override
                protected void onClick() {
                    createItem();
                }
            };
            add(b_create);

            updateText();
        }

        private void layout() {
            o_category.setRect(0, GAP, WIDTH, 24);
            t_selected.setPos(0, o_category.bottom() + GAP + iconRows() * (BTN_SIZE + GAP));
            o_level.setRect(0, t_selected.bottom() + GAP, WIDTH, 24);
            c_curse.setRect(0, o_level.bottom() + GAP, WIDTH, 18);
            b_create.setRect(0, c_curse.bottom()+GAP, WIDTH, 16);
            resize(WIDTH, (int) b_create.bottom() + GAP);
        }

        private void updateText() {
            Class<? extends Item> item = ringList.get(0);
            if(category == RING_CAT){
                item = ringList.get(selected);
            }
            else if (category == WAND_CAT) {
                item = wandList.get(selected);
            }
            else if (category == TRINKET_CAT) {
                item = trinketList.get(selected);
            }
            t_selected.text(Messages.get(TestRing.class, "selected", Messages.get(item, "name")));
            layout();
        }

        private void createImage() {
            float top = GAP + o_category.bottom();
            int length = (category == RING_CAT ? ringList.size() : (category == WAND_CAT ? wandList.size() : (category == TRINKET_CAT ? trinketList.size() : 0)));
            for (int i = 0; i < length; ++i) {
                final int j = i;
                IconButton btn = new IconButton() {
                    @Override
                    protected void onClick() {
                        selected = Math.min(j,total(category)-1);
                        updateText();
                        super.onClick();
                    }
                };
                if (category == RING_CAT) {
                    Image im = new Image(Assets.Sprites.ITEM_ICONS);
                    im.frame(ItemSpriteSheet.Icons.film.get(Objects.requireNonNull(Reflection.newInstance(ringList.get(i))).icon));
                    im.scale.set(1.6f);
                    btn.icon(im);
                } else if (category == WAND_CAT || category == TRINKET_CAT) {
                    Class<? extends Item> cls = category == WAND_CAT ? wandList.get(i) : trinketList.get(i);
                    Image im = new Image(Assets.Sprites.ITEMS);
                    im.frame(ItemSpriteSheet.film.get(Objects.requireNonNull(Reflection.newInstance(cls)).image));
                    im.scale.set(0.9f);
                    btn.icon(im);
                }

                int row = i / PER_ROW;
                int col = i % PER_ROW;
                int inThisRow = Math.min(PER_ROW, length - row * PER_ROW);
                float left = (WIDTH - BTN_SIZE * inThisRow) / 2f;
                btn.setRect(left + col * BTN_SIZE, top + row * (BTN_SIZE + GAP), BTN_SIZE, BTN_SIZE);
                add(btn);
                iconButtonsList.add(btn);
            }
        }

    }
}
