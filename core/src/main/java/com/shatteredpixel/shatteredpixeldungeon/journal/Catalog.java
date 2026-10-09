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

package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder.SeedFinder;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.ArcaneResin;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Grass;
import com.shatteredpixel.shatteredpixeldungeon.items.Gun562Accessories;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.Stylus;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.Torch;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.XMasGift;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MedicalKit;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.RedBook;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.FoodPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ItemHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.GolyatBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.fairyitems.Commander;
import com.shatteredpixel.shatteredpixeldungeon.items.fairyitems.Gemini;
import com.shatteredpixel.shatteredpixeldungeon.items.fairyitems.Letter;
import com.shatteredpixel.shatteredpixeldungeon.items.fairyitems.Peach;
import com.shatteredpixel.shatteredpixeldungeon.items.fairyitems.Succor;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.AlchemicalCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DarkGold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GooBlob;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.ArcaneCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.TippedDart;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickRecipe;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public enum Catalog {
	MELEE_WEAPONS_T1,
	MELEE_WEAPONS_T2,
	MELEE_WEAPONS_T3,
	MELEE_WEAPONS_T4,
	MELEE_WEAPONS_T5,
	MELEE_WEAPONS_T6,
	ARMOR,
	ENCHANTMENTS,
	GLYPHS,
	MISSILE_WEAPONS,
	WANDS,
	RINGS,
	ARTIFACTS,
	MISC_EQUIPMENT,
	//CONSUMABLES
	POTIONS,
	SEEDS,
	SCROLLS,
	STONES,
	FOOD,
	EXOTIC_POTIONS,
	EXOTIC_SCROLLS,
	BOMBS,
	TIPPED_DARTS,
	BREWS_ELIXIRS,
	SPELLS,
	FAIRY,
	MISC_CONSUMABLES,
		WEAPONS,
		TRINKETS;
	
	private LinkedHashMap<Class<?>, Boolean> seen = new LinkedHashMap<>();
	//tracks upgrades spent for equipment, uses for consumables
	private LinkedHashMap<Class<?>, Integer> useCount = new LinkedHashMap<>();
	
	public Collection<Class<?>> items(){
		return seen.keySet();
	}

	//should only be used when initializing
	private void addItems( Catalog items){
		for (Class<?> item : items.seen.keySet()){
			seen.put(item, false);
			useCount.put(item, 0);
		}
	}
	private void addItems( Class<?>... items){
		for (Class<?> item : items){
			seen.put(item, false);
			useCount.put(item, 0);
		}
	}
	private void addItems( ArrayList<QuickRecipe> recipes){
		for (QuickRecipe recipe : recipes){
			if (recipe == null)
				continue;
			Item item = recipe.output.item();
			if (item == null)
				continue;
			Class<?> cl = recipe.output.item().getClass();
			seen.put(cl, false);
			useCount.put(cl, 0);
		}
	}
	public String title(){
		return Messages.get(this, name() + ".title");
	}
	public int totalItems(){
		return seen.size();
	}

	public int totalSeen(){
		int seenTotal = 0;
		for (boolean itemSeen : seen.values()){
			if (itemSeen) seenTotal++;
		}
		return seenTotal;
	}

	public boolean allSeen(){
		for (Class<?> item : items()){
			if (!seen.get(item)){
				return false;
			}
		}
		return true;
	}

	static {

		MELEE_WEAPONS_T1.addItems(Generator.Category.WEP_T1.classes);
		MELEE_WEAPONS_T2.addItems(Generator.Category.WEP_T2.classes);
		MELEE_WEAPONS_T3.addItems(Generator.Category.WEP_T3.classes);
		MELEE_WEAPONS_T4.addItems(Generator.Category.WEP_T4.classes);
		MELEE_WEAPONS_T5.addItems(Generator.Category.WEP_T5.classes);
		MELEE_WEAPONS_T6.addItems(Generator.Category.WEP_T6.classes);
		WEAPONS.addItems(MELEE_WEAPONS_T1);
		WEAPONS.addItems(MELEE_WEAPONS_T2);
		WEAPONS.addItems(MELEE_WEAPONS_T3);
		WEAPONS.addItems(MELEE_WEAPONS_T4);
		WEAPONS.addItems(MELEE_WEAPONS_T5);
		WEAPONS.addItems(MELEE_WEAPONS_T6);

		ARMOR.addItems(Generator.Category.ARMOR.classes);

		MISSILE_WEAPONS.addItems(Generator.Category.MIS_T1.classes);
		MISSILE_WEAPONS.addItems(Generator.Category.MIS_T2.classes);
		MISSILE_WEAPONS.addItems(Generator.Category.MIS_T3.classes);
		MISSILE_WEAPONS.addItems(Generator.Category.MIS_T4.classes);
		MISSILE_WEAPONS.addItems(Generator.Category.MIS_T5.classes);
		WEAPONS.addItems(MISSILE_WEAPONS);

		ENCHANTMENTS.addItems(Weapon.Enchantment.common);
		ENCHANTMENTS.addItems(Weapon.Enchantment.uncommon);
		ENCHANTMENTS.addItems(Weapon.Enchantment.rare);
		ENCHANTMENTS.addItems(Weapon.Enchantment.curses);

		GLYPHS.addItems(Armor.Glyph.common);
		GLYPHS.addItems(Armor.Glyph.uncommon);
		GLYPHS.addItems(Armor.Glyph.rare);
		GLYPHS.addItems(Armor.Glyph.curses);

		WANDS.addItems(Generator.Category.WAND.classes);

		RINGS.addItems(Generator.Category.RING.classes);

		ARTIFACTS.addItems(Generator.Category.ARTIFACT.classes);
		//角色专属神器不在生成池中，额外注册进图鉴
		ARTIFACTS.addItems(RedBook.class, MedicalKit.class);

		TRINKETS.addItems(Generator.Category.TRINKET.classes);

		MISC_EQUIPMENT.addItems(BrokenSeal.class, SpiritBow.class, Waterskin.class, VelvetPouch.class,
				PotionBandolier.class, ScrollHolder.class, MagicalHolster.class, FoodPouch.class,
				ItemHolder.class, Amulet.class);

		POTIONS.addItems(Generator.Category.POTION.classes);

		SCROLLS.addItems(Generator.Category.SCROLL.classes);

		SEEDS.addItems(Generator.Category.SEED.classes);

		STONES.addItems(Generator.Category.STONE.classes);

		FOOD.addItems(Generator.Category.FOOD.classes);

		EXOTIC_POTIONS.addItems(ExoticPotion.exoToReg.keySet().toArray(new Class[0]));

		EXOTIC_SCROLLS.addItems(ExoticScroll.exoToReg.keySet().toArray(new Class[0]));

		BOMBS.addItems(Bomb.class);
		BOMBS.addItems(Bomb.EnhanceBomb.validIngredients.values().toArray(new Class[0]));
		BOMBS.addItems(GolyatBomb.class);

		TIPPED_DARTS.addItems(TippedDart.types.values().toArray(new Class[0]));

		BREWS_ELIXIRS.addItems(AlchemicalCatalyst.class);
		BREWS_ELIXIRS.addItems(QuickRecipe.getRecipes(QuickRecipe.BREW));

		SPELLS.addItems(ArcaneCatalyst.class);
		SPELLS.addItems(QuickRecipe.getRecipes(QuickRecipe.SPELL));

		FAIRY.addItems(Commander.class, Gemini.class, Letter.class, Peach.class, Succor.class);

		MISC_CONSUMABLES.addItems( Gold.class, EnergyCrystal.class, Dewdrop.class,
				IronKey.class, GoldenKey.class, CrystalKey.class, SkeletonKey.class,
				TrinketCatalyst.class, Stylus.class, Torch.class, Honeypot.class, Ankh.class,
				CorpseDust.class, Embers.class, CeremonialCandle.class, DarkGold.class, DwarfToken.class,
				GooBlob.class, TengusMask.class, MetalShard.class, KingsCrown.class,
				DriedRose.Petal.class, TimekeepersHourglass.sandBag.class,
				LiquidMetal.class, ArcaneResin.class, XMasGift.class, Gun562Accessories.class, Grass.class);

	}
	public static final ArrayList<Catalog> equipmentCatalogs = new ArrayList<>();
	static {
		equipmentCatalogs.add(MELEE_WEAPONS_T1);
		equipmentCatalogs.add(MELEE_WEAPONS_T2);
		equipmentCatalogs.add(MELEE_WEAPONS_T3);
		equipmentCatalogs.add(MELEE_WEAPONS_T4);
		equipmentCatalogs.add(MELEE_WEAPONS_T5);
		equipmentCatalogs.add(MELEE_WEAPONS_T6);
		equipmentCatalogs.add(ARMOR);
		equipmentCatalogs.add(ENCHANTMENTS);
		equipmentCatalogs.add(GLYPHS);
		equipmentCatalogs.add(MISSILE_WEAPONS);
		equipmentCatalogs.add(WANDS);
		equipmentCatalogs.add(RINGS);
		equipmentCatalogs.add(ARTIFACTS);
		equipmentCatalogs.add(MISC_EQUIPMENT);
		equipmentCatalogs.add(TRINKETS);
	}

	public static final ArrayList<Catalog> consumableCatalogs = new ArrayList<>();
	static {
		consumableCatalogs.add(POTIONS);
		consumableCatalogs.add(SCROLLS);
		consumableCatalogs.add(SEEDS);
		consumableCatalogs.add(STONES);
		consumableCatalogs.add(FOOD);
		consumableCatalogs.add(EXOTIC_POTIONS);
		consumableCatalogs.add(EXOTIC_SCROLLS);
		consumableCatalogs.add(BOMBS);
		consumableCatalogs.add(TIPPED_DARTS);
		consumableCatalogs.add(BREWS_ELIXIRS);
		consumableCatalogs.add(SPELLS);
		consumableCatalogs.add(FAIRY);
		consumableCatalogs.add(MISC_CONSUMABLES);
	}
	
	public static LinkedHashMap<Catalog, Badges.Badge> catalogBadges = new LinkedHashMap<>();
	static {
		catalogBadges.put(WEAPONS, Badges.Badge.ALL_WEAPONS_IDENTIFIED);
		catalogBadges.put(ARMOR, Badges.Badge.ALL_ARMOR_IDENTIFIED);
		catalogBadges.put(WANDS, Badges.Badge.ALL_WANDS_IDENTIFIED);
		catalogBadges.put(RINGS, Badges.Badge.ALL_RINGS_IDENTIFIED);
		catalogBadges.put(ARTIFACTS, Badges.Badge.ALL_ARTIFACTS_IDENTIFIED);
		catalogBadges.put(POTIONS, Badges.Badge.ALL_POTIONS_IDENTIFIED);
		catalogBadges.put(SCROLLS, Badges.Badge.ALL_SCROLLS_IDENTIFIED);
	}

	public static boolean isSeen(Class<?> cls){
		if (DeviceCompat.isDebug() && Dungeon.isChallenged(Challenges.TEST_MODE))
			return true;
		for (Catalog cat : values()) {
			if (cat.seen.containsKey(cls)) {
				return cat.seen.get(cls);
			}
		}
		return false;
	}

	public static void setSeen(Class<?> cls){
		if(Dungeon.isChallenged(Challenges.TEST_MODE) || SeedFinder.SeedFinding)
			return;
		for (Catalog cat : values()) {
			if (cat.seen.containsKey(cls) && !cat.seen.get(cls)) {
				cat.seen.put(cls, true);
				Journal.saveNeeded = true;
			}
		}
		Badges.validateItemsIdentified();
	}

	public static int useCount(Class<?> cls){
		for (Catalog cat : values()) {
			if (cat.useCount.containsKey(cls)) {
				return cat.useCount.get(cls);
			}
		}
		return 0;
	}

	public static void countUse(Class<?> cls){
		if(Dungeon.isChallenged(Challenges.TEST_MODE))
			return;
		countUses(cls, 1);
	}

	public static void countUses(Class<?> cls, int uses){
		for (Catalog cat : values()) {
			if (cat.useCount.containsKey(cls) && cat.useCount.get(cls) != Integer.MAX_VALUE) {
				cat.useCount.put(cls, cat.useCount.get(cls)+uses);
				if (cat.useCount.get(cls) < -1_000_000_000){ //to catch cases of overflow
					cat.useCount.put(cls, Integer.MAX_VALUE);
				}
				Journal.saveNeeded = true;
			}
		}
	}

	private static final String CATALOG_CLASSES = "catalog_classes";
	private static final String CATALOG_SEEN    = "catalog_seen";
	private static final String CATALOG_USES    = "catalog_uses";

	public static void store( Bundle bundle ){

		ArrayList<Class<?>> classes = new ArrayList<>();
		ArrayList<Boolean> seen = new ArrayList<>();
		ArrayList<Integer> uses = new ArrayList<>();

		for (Catalog cat : values()) {
			for (Class<?> item : cat.items()) {
				if (cat.seen.get(item) == null || cat.useCount.get(item) == null)
					continue;
				if (cat.seen.get(item) || cat.useCount.get(item) > 0){
					classes.add(item);
					seen.add(cat.seen.get(item));
					uses.add(cat.useCount.get(item));
				}
			}
		}

		Class<?>[] storeCls = new Class[classes.size()];
		boolean[] storeSeen = new boolean[seen.size()];
		int[] storeUses = new int[uses.size()];

		for (int i = 0; i < storeCls.length; i++){
			storeCls[i] = classes.get(i);
			storeSeen[i] = seen.get(i);
			storeUses[i] = uses.get(i);
		}

		bundle.put( CATALOG_CLASSES, storeCls );
		bundle.put( CATALOG_SEEN, storeSeen );
		bundle.put( CATALOG_USES, storeUses );

	}

	//pre-v2.5
	private static final String CATALOG_ITEMS = "catalog_items";

	public static void restore( Bundle bundle ){

		//old logic for pre-v2.5 catalog-specific badges
		Badges.loadGlobal();
		for (Catalog cat : values()){
			if (Badges.isUnlocked(catalogBadges.get(cat))){
				for (Class<?> item : cat.items()){
					cat.seen.put(item, true);
				}
			}
		}
		if (bundle.contains(CATALOG_ITEMS)) {
			for (Class<?> cls : bundle.getClassArray(CATALOG_ITEMS)){
				for (Catalog cat : values()) {
					if (cat.seen.containsKey(cls)) {
						cat.seen.put(cls, true);
					}
				}
			}
		}
		//end of old logic

		if (bundle.contains(CATALOG_CLASSES)){
			Class<?>[] classes = bundle.getClassArray(CATALOG_CLASSES);
			boolean[] seen = bundle.getBooleanArray(CATALOG_SEEN);
			int[] uses = bundle.getIntArray(CATALOG_USES);

			for (int i = 0; i < classes.length; i++){
				for (Catalog cat : values()) {
					if (cat.seen.containsKey(classes[i])) {
						cat.seen.put(classes[i], seen[i]);
						cat.useCount.put(classes[i], uses[i]);
					}
				}

			}
		}

	}
	
}
