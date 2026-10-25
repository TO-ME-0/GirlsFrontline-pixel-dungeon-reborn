/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;


import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemStatusHandler;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.UnstableSpellbook;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfAntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlink;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfClairvoyance;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfDeepSleep;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfDisarming;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfFear;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfFlock;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfShock;
import com.shatteredpixel.shatteredpixeldungeon.items.ColorItem;
import com.shatteredpixel.shatteredpixeldungeon.items.AutoIdentify;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.IdentifyKnowledge;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AlchemyScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndStartGame;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public abstract class Scroll extends Item implements ColorItem {

	public static final String AC_READ	= "READ";
    protected float mulOnTalentUsed = 1F;

    public boolean isCost(){
        return false;
    }
	protected static final float TIME_TO_READ	= 1f;

	private static final HashMap<String, Integer> runes = new HashMap<String, Integer>() {
		{
			put("KAUNAN",ItemSpriteSheet.SCROLL_KAUNAN);
			put("SOWILO",ItemSpriteSheet.SCROLL_SOWILO);
			put("LAGUZ",ItemSpriteSheet.SCROLL_LAGUZ);
			put("YNGVI",ItemSpriteSheet.SCROLL_YNGVI);
			put("GYFU",ItemSpriteSheet.SCROLL_GYFU);
			put("RAIDO",ItemSpriteSheet.SCROLL_RAIDO);
			put("ISAZ",ItemSpriteSheet.SCROLL_ISAZ);
			put("MANNAZ",ItemSpriteSheet.SCROLL_MANNAZ);
			put("NAUDIZ",ItemSpriteSheet.SCROLL_NAUDIZ);
			put("BERKANAN",ItemSpriteSheet.SCROLL_BERKANAN);
			put("ODAL",ItemSpriteSheet.SCROLL_ODAL);
			put("TIWAZ",ItemSpriteSheet.SCROLL_TIWAZ);
		}
	};

	protected static ItemStatusHandler<Scroll> handler;

	protected String rune;

	{
		stackable = true;
		defaultAction = AC_READ;
	}

	@SuppressWarnings("unchecked")
	public static void initLabels() {
		handler = new ItemStatusHandler<>( (Class<? extends Scroll>[])Generator.Category.SCROLL.classes, runes );
		//符文重掷后，旧符文标签上的卷轴知识全部失效
		AutoIdentify.onLabelsRegenerated(IdentifyKnowledge.Category.SCROLL);
	}

	public static void save( Bundle bundle ) {
		handler.save( bundle );
	}

	public static void saveSelectively( Bundle bundle, ArrayList<Item> items ) {
		ArrayList<Class<?extends Item>> classes = new ArrayList<>();
		for (Item i : items){
			if (i instanceof ExoticScroll){
				if (!classes.contains(ExoticScroll.exoToReg.get(i.getClass()))){
					classes.add(ExoticScroll.exoToReg.get(i.getClass()));
				}
			} else if (i instanceof Scroll){
				if (!classes.contains(i.getClass())){
					classes.add(i.getClass());
				}
			}
		}
		handler.saveClassesSelectively( bundle, classes );
	}

	@SuppressWarnings("unchecked")
	public static void restore( Bundle bundle ) {
		handler = new ItemStatusHandler<>( (Class<? extends Scroll>[])Generator.Category.SCROLL.classes, runes, bundle );
	}

	public Scroll() {
		super();
		reset();
	}

	//anonymous scrolls are always IDed, do not affect ID status,
	//and their sprite is replaced by a placeholder if they are not known,
	//useful for items that appear in UIs, or which are only spawned for their effects
	protected boolean anonymous = false;
	public void anonymize(){
		if (!isKnown()) image = ItemSpriteSheet.SCROLL_HOLDER;
		anonymous = true;
	}


	@Override
	public void reset(){
		super.reset();
		if (handler != null && handler.contains(this)) {
			image = handler.image(this);
			rune = handler.label(this);
		}
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_READ );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_READ )) {

			if (hero.buff(MagicImmune.class) != null){
				GLog.w( Messages.get(this, "no_magic") );
			} else if (hero.buff( Blindness.class ) != null) {
				GLog.w( Messages.get(this, "blinded") );
			} else if (hero.buff(UnstableSpellbook.bookRecharge.class) != null
					&& hero.buff(UnstableSpellbook.bookRecharge.class).isCursed()
					&& !(this instanceof ScrollOfRemoveCurse || this instanceof ScrollOfAntiMagic)){
				GLog.n( Messages.get(this, "cursed") );
			} else {
				curUser = hero;
				doRead();
                if(!isCost()){
                    curItem = detach( hero.belongings.backpack );
                }
			}

		}
	}

	public abstract void doRead();
    public void BookRead(){
        float mul = mulOnTalentUsed;
        mulOnTalentUsed /= 2;
        doRead();
        mulOnTalentUsed = mul;
    }

	protected void readAnimation() {
		if (!anonymous) {
			Catalog.countUse(getClass());
		}
        Invisibility.dispel(true);
        Talent.onScrollUsed(Dungeon.hero(), mulOnTalentUsed);
		curUser.spend( TIME_TO_READ );
		curUser.busy();
		((HeroSprite)curUser.sprite).read();

	}

	public boolean isKnown() {
		return anonymous || (handler != null && handler.isKnown( this ));
	}

	public void setKnown() {
		if (!anonymous) {
			if (!isKnown()) {
				handler.know(this);
				updateQuickslot();
			}

			if (Dungeon.hero().isAlive()) {
				Catalog.setSeen(getClass());
			}
		}
	}

    public void setIgnore(){
        if (isKnown()) {
            //已真鉴定：原逻辑，让 handler 忽略该类
            if (this instanceof ExoticScroll)
                handler.ignore((Scroll) ScrollOfTransmutation.changeItem(this));
            else
                handler.ignore(this);
			Dungeon.guessType.remove(getClass());
        } else if (isGuess()) {
            //已猜未鉴定：撤销正面猜测并加入 vetoed，符文与知识保留
            Dungeon.guessType.remove(guessKey());
            if (Notes.knowledge != null) Notes.knowledge.revokeGuess(AutoIdentify.keyOf(this));
        } else {
            //真未鉴定：重掷符文（initLabels 内部会清空该类别知识）
            initLabels();
        }
        updateQuickslot();
    }

	@Override
	public Item identify( boolean byHero ) {
		super.identify(byHero);

		if (!isKnown()) {
			setKnown();
		}
		return this;
	}

	@Override
	public String name() {
		if (isKnown() || Dungeon.isGameMode(WndStartGame.GameMode.IDENTIFY))
			return super.name();
		//自动推断命中时显示“? 真名”，与瞄准镜的猜中显示一致
		if (showGuess())
			return "? " + super.name();
		return Messages.get(this, rune);
	}

	@Override
	public String info() {
        String note = NoteGet();
		//已推断：显示真实描述并附推断依据（来源与楼层）
		if (!isKnown() && showGuess()){
			String ev = AutoIdentify.guessEvidence(this);
			return note + super.info() + (ev.isEmpty() ? "" : "\n\n" + ev);
		}
		return isKnown() ?
			super.info() :
			note+Messages.get(this, "unknown_desc");
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return isKnown();
	}

	public static HashSet<Class<? extends Scroll>> getKnown() {
		return handler.known();
	}

	public static HashSet<Class<? extends Scroll>> getUnknown() {
		return handler.unknown();
	}

	//—— 自动鉴定知识系统的静态访问器（handler 为 protected，其他包不可直接读）——

	@Override
	public String guessLabel() {
		return rune;
	}

	public static HashSet<Class<? extends Scroll>> unknownClasses() {
		return handler.unknown();
	}

	//未注册的类（如占位符）返回 null
	public static String labelOf(Class<? extends Scroll> cls) {
		return handler == null ? null : handler.label(cls);
	}

	public static boolean isKnownClass(Class<? extends Scroll> cls) {
		return handler != null && handler.isKnown(cls);
	}

	public static boolean contains(Scroll s) {
		return handler != null && handler.contains(s);
	}

	public static boolean allKnown() {
		return handler.known().size() == Generator.Category.SCROLL.classes.length;
	}

	@Override
	public int value() {
		return 30 * quantity;
	}

	@Override
	public int energyVal() {
		return 6 * quantity;
	}

	public static class PlaceHolder extends Scroll {

		{
			image = ItemSpriteSheet.SCROLL_HOLDER;
		}

		@Override
		public boolean isSimilar(Item item) {
			return ExoticScroll.regToExo.containsKey(item.getClass())
					|| ExoticScroll.regToExo.containsValue(item.getClass());
		}

		@Override
		public void doRead() {}

		@Override
		public String info() {
			return "";
		}
	}

	public static class ScrollToStone extends Recipe {

		private static HashMap<Class<?extends Scroll>, Class<?extends Runestone>> stones = new HashMap<>();
		static {
			stones.put(ScrollOfIdentify.class,      StoneOfIntuition.class);
			stones.put(ScrollOfLullaby.class,       StoneOfDeepSleep.class);
			stones.put(ScrollOfMagicMapping.class,  StoneOfClairvoyance.class);
			stones.put(ScrollOfMirrorImage.class,   StoneOfFlock.class);
			stones.put(ScrollOfRetribution.class,   StoneOfBlast.class);
			stones.put(ScrollOfRage.class,          StoneOfAggression.class);
			stones.put(ScrollOfRecharging.class,    StoneOfShock.class);
			stones.put(ScrollOfRemoveCurse.class,   StoneOfDisarming.class);
			stones.put(ScrollOfTeleportation.class, StoneOfBlink.class);
			stones.put(ScrollOfTerror.class,        StoneOfFear.class);
			stones.put(ScrollOfTransmutation.class, StoneOfAugmentation.class);
			stones.put(ScrollOfUpgrade.class,       StoneOfEnchantment.class);
		}

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (ingredients.size() != 1
					|| !(ingredients.get(0) instanceof Scroll)
					|| !stones.containsKey(ingredients.get(0).getClass())){
				return false;
			}

			return true;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 0;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;

			Scroll s = (Scroll) ingredients.get(0);

			s.quantity(s.quantity() - 1);
			if (GirlsFrontlinePixelDungeon.scene() instanceof AlchemyScene && !s.isIdentified())
				((AlchemyScene) GirlsFrontlinePixelDungeon.scene()).showIdentify(s);
			else
				s.identify();

			return Reflection.newInstance(stones.get(s.getClass())).quantity(2);
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;

			Scroll s = (Scroll) ingredients.get(0);

			if (!s.isKnown()){
				return new Runestone.PlaceHolder().quantity(2);
			} else {
				return Reflection.newInstance(stones.get(s.getClass())).quantity(2);
			}
		}
	}
}
