package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.X1Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.X2Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.X3Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.X4Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.*;
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
import com.shatteredpixel.shatteredpixeldungeon.ui.WndTextNumberInput;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Objects;

public class TestArmor extends TestGenerator {
    {
        image = ItemSpriteSheet.ARMOR_HOLDER;
    }

    private int armor_id;
    private boolean cursed;
    private int armor_level;
    private int enchant_id;
    private int enchant_rarity;

    public TestArmor() {
        this.armor_id = 0;
        this.cursed = false;
        this.armor_level = 0;
        this.enchant_id = 0;
        this.enchant_rarity = 0;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        return super.actions(hero);
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_GIVE)) {
            GameScene.show(new SettingsWindow());
        }
    }

    private Armor getArmor(int armor_id) {
        switch (armor_id) {
            case 1:
                return new LeatherArmor();
            case 2:
                return new MailArmor();
            case 3:
                return new ScaleArmor();
            case 4:
                return new PlateArmor();
            //X系列试验外骨骼
            case 5:
                return new X1Armor();
            case 6:
                return new X2Armor();
            case 7:
                return new X3Armor();
            case 8:
                return new X4Armor();
            case 0:
            default:
                return new ClothArmor();
        }
    }

    private void createArmor() {
        Armor armor = getArmor(armor_id);
        if (Challenges.isItemBlocked(armor)) {
            return;
        }
        armor = modifyArmor(armor);
        armor.identify();
        if (armor.collect()) {
            GameScene.pickUp(armor, curUser.pos);
            Sample.INSTANCE.play(Assets.Sounds.ITEM);
            GLog.i(Messages.get(this, "collect_success", armor.name()));
        } else {
            armor.doDrop(curUser);
        }
    }

    private Armor modifyArmor(Armor armor) {
        armor.level(armor_level);
        armor.cursed = cursed;
        Class<? extends Armor.Glyph> glyphCls = generateEnchant(enchant_rarity, enchant_id);
        armor.inscribe(glyphCls == null ? null : Reflection.newInstance(glyphCls));
        return armor;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("armor_id", armor_id);
        bundle.put("cursed", cursed);
        bundle.put("armor_level", armor_level);
        bundle.put("enchant_rarity", enchant_rarity);
        bundle.put("enchant_id", enchant_id);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        armor_id = bundle.getInt("armor_id");
        cursed = bundle.getBoolean("cursed");
        armor_level = bundle.getInt("armor_level");
        enchant_rarity = bundle.getInt("enchant_rarity");
        enchant_id = bundle.getInt("enchant_id");
    }

    private String currentGlyphName(Class<? extends Armor.Glyph> glyph) {
        if (enchant_rarity < 4)
            return currentGlyphName(glyph, Messages.get(Armor.Glyph.class, "glyph"));
        else
            return currentGlyphName(glyph, Messages.get(Item.class, "curse"));
    }

    private String currentGlyphName(Class<? extends Armor.Glyph> glyph, String armorName) {
        return Messages.get(glyph, "name", armorName);
    }

    private Class<? extends Armor.Glyph> generateEnchant(int enc_type, int enc_id) {
        if (enc_type == 1) switch (enc_id) {
            case 0:
                return Obfuscation.class;
            case 1:
                return Swiftness.class;
            case 2:
                return Viscosity.class;
            case 3:
                return Potential.class;
            default:
                return null;
        } else if (enc_type == 2) switch (enc_id) {
            case 0:
                return Stone.class;
            case 1:
                return Brimstone.class;
            case 2:
                return Entanglement.class;
            case 3:
                return Repulsion.class;
            case 4:
                return Camouflage.class;
            case 5:
                return Flow.class;
            default:
                return null;
        } else if (enc_type == 3) switch (enc_id) {
            case 0:
                return AntiMagic.class;
            case 1:
                return Thorns.class;
            case 2:
                return Affection.class;
            default:
                return null;
        } else if (enc_type == 4) switch (enc_id) {
            case 0:
                return AntiEntropy.class;
            case 1:
                return Bulk.class;
            case 2:
                return Corrosion.class;
            case 3:
                return Displacement.class;
            case 4:
                return Metabolism.class;
            case 5:
                return Multiplicity.class;
            case 6:
                return Overgrowth.class;
            case 7:
                return Stench.class;
            default:
                return null;
        }
        return null;
    }

    private class SettingsWindow extends Window {
        private static final int WIDTH = 150;
        private static final int HEIGHT = 220;
        private static final int GAP = 2;
        private static final int BTN_SIZE = 18;
        private static final int MAX_ICONS_PER_LINE = 4;

        private Class[] AllArmor;
        private CheckBox CheckBox_curse;
        private RenderedTextBlock RenderedTextBlock_enchantInfo;
        private OptionSlider OptionSlider_enchantId;
        private OptionSlider OptionSlider_enchantRarity;
        private RedButton RedButton_create;
        private final ArrayList<IconButton> IconButtons = new ArrayList<>();
        private RedButton Button_Level;

        public SettingsWindow() {
            super();
            resize(WIDTH, HEIGHT);

            createArmorList();
            createArmorImage(AllArmor);

            Button_Level = new RedButton("") {
                @Override
                protected void onClick() {
                    Game.runOnRenderThread(() -> GameScene.show(new WndTextNumberInput(
                            Messages.get(TestArmor.SettingsWindow.class, "armor_level"),
                            Messages.get(TestArmor.SettingsWindow.class, "armor_level_desc"),
                            Integer.toString(armor_level),
                            4, false,
                            Messages.get(TestArmor.SettingsWindow.class, "confirm"),
                            Messages.get(TestArmor.SettingsWindow.class, "cancel"), false) {
                        @Override
                        public void onSelect(boolean check, String text) {
                            if (check && text.matches("-?\\d+")) {
                                int level = Integer.parseInt(text);
                                armor_level = level;
                            }
                        }
                    }));
                }
            };
            updateSelectedArmorText();
            add(Button_Level);

            RenderedTextBlock_enchantInfo = PixelScene.renderTextBlock("", 6);
            RenderedTextBlock_enchantInfo.visible = true;
            RenderedTextBlock_enchantInfo.maxWidth(WIDTH);
            updateEnchantText();
            add(RenderedTextBlock_enchantInfo);

            OptionSlider_enchantRarity = new OptionSlider(Messages.get(this, "enchant_rarity"), "0", "4", 0, 4) {
                @Override
                protected void onChange() {
                    enchant_rarity = getSelectedValue();
                    updateEnchantText();
                    layout();
                }
            };
            OptionSlider_enchantRarity.setSelectedValue(enchant_rarity);
            add(OptionSlider_enchantRarity);

            OptionSlider_enchantId = new OptionSlider(Messages.get(this, "enchant_id"), "0", "8", 0, 8) {
                @Override
                protected void onChange() {
                    enchant_id = getSelectedValue();
                    updateEnchantText();
                    layout();
                }
            };
            OptionSlider_enchantId.setSelectedValue(enchant_id);
            add(OptionSlider_enchantId);

            CheckBox_curse = new CheckBox(Messages.get(this, "cursed")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    cursed = checked();
                }
            };
            CheckBox_curse.checked(cursed);
            add(CheckBox_curse);

            RedButton_create = new RedButton(Messages.get(this, "create")) {
                @Override
                protected void onClick() {
                    createArmor();
                    hide();
                }
            };
            add(RedButton_create);

            layout();
        }

        private void layout() {
            int numLines = (int) Math.ceil(AllArmor.length / (float) MAX_ICONS_PER_LINE);
            float totalHeight = 2;
            if (numLines > 0) {
                totalHeight += numLines * (BTN_SIZE + GAP);
            }

            Button_Level.setRect(0, totalHeight, WIDTH, 24);
            RenderedTextBlock_enchantInfo.setPos(0, GAP + Button_Level.top() + Button_Level.height());
            OptionSlider_enchantRarity.setRect(0, GAP + RenderedTextBlock_enchantInfo.bottom(), WIDTH, 24);
            OptionSlider_enchantId.setRect(0, GAP + OptionSlider_enchantRarity.bottom(), WIDTH, 24);
            CheckBox_curse.setRect(0, GAP + OptionSlider_enchantId.bottom(), WIDTH / 2f - GAP / 2f, 16);
            RedButton_create.setRect(WIDTH / 2f + GAP / 2f, OptionSlider_enchantId.bottom() + GAP, WIDTH / 2f - GAP / 2f, 16);
            resize(WIDTH, (int) RedButton_create.bottom());
        }

        private void createArmorList() {
            AllArmor = new Class<?>[]{
                    ClothArmor.class,
                    LeatherArmor.class,
                    MailArmor.class,
                    ScaleArmor.class,
                    PlateArmor.class,
                    //X系列试验外骨骼
                    X1Armor.class,
                    X2Armor.class,
                    X3Armor.class,
                    X4Armor.class
            };
        }

        private void createArmorImage(Class<? extends Armor>[] all) {
            float left = BTN_SIZE / 2f;
            float top = 0;
            int placed = 0;
            int length = all.length;
            for (int i = 0; i < length; ++i) {
                final int j = i;
                IconButton btn = new IconButton() {
                    @Override
                    protected void onClick() {
                        armor_id = j;
                        updateSelectedArmorText();
                        layout();
                        super.onClick();
                    }
                };
                Image im = new Image(Assets.Sprites.ITEMS);
                im.frame(ItemSpriteSheet.film.get(Objects.requireNonNull(Reflection.newInstance(all[i])).image));
                im.scale.set(1f);
                btn.icon(im);
                int row = placed / MAX_ICONS_PER_LINE;
                int col = placed % MAX_ICONS_PER_LINE;
                float x = left + col * (BTN_SIZE + GAP) * 2;
                float y = top + row * (BTN_SIZE + GAP);
                btn.setRect(x, y, BTN_SIZE, BTN_SIZE);
                add(btn);
                placed++;
                IconButtons.add(btn);
            }
        }

        private void updateSelectedArmorText() {
            Armor armor = Reflection.newInstance(getArmor(armor_id).getClass());
            Button_Level.text(armor.name());
        }

        private String getEnchantInfo(Class enchant) {
            return enchant == null ? Messages.get(this, "no_enchant") : Messages.get(enchant, "name", Messages.get(this, "enchant"));
        }

        private int getEnchantCount(int rarity) {
            switch (rarity) {
                case 1:
                    return 4;
                case 2:
                    return 6;
                case 3:
                    return 3;
                case 4:
                    return 8;
            }
            return 0;
        }

        private void updateEnchantText() {
            StringBuilder info = new StringBuilder();
            if (enchant_rarity == 0) {
                info = new StringBuilder(Messages.get(this, "no_enchant"));
            } else {
                for (int i = 0; i < getEnchantCount(enchant_rarity); i++) {
                    info.append(i + 1).append(":").append(getEnchantInfo(generateEnchant(enchant_rarity, i))).append(" ");
                    if ((i + 1) % 4 == 0 || i == (getEnchantCount(enchant_rarity) - 1)) {
                        info.append("\n");
                    }
                }
                info.append(Messages.get(this, "current_enchant", getEnchantInfo(generateEnchant(enchant_rarity, enchant_id))));
            }
            RenderedTextBlock_enchantInfo.text(info.toString());
        }
    }
}
