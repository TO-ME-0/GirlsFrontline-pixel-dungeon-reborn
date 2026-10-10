/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Foliage;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SacrificialFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WaterOfAwareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WaterOfHealth;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bandit;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DemonSpawner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollSWAP;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GolyatPlus;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SpinnerCat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Typhoon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DEL;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.ImpShopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NoelShopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.RatKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.MapEditor;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.LostBackpack;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ItemHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.BeaconOfReturning;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.WeakFloorRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BanditSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BlacksmithSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DELSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhostSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollSWAPSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GolyatPlusSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FncSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.NoelShopKeeperSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.P7Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShopkeeperSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpawnerSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpinnerCatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TyphoonSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WandmakerSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Notes {

	public static abstract class Record implements Comparable<Record>, Bundlable {

		//TODO currently notes can only relate to branch = 0, add branch support here if that changes
		protected int levelId;

		public int depth(){
			return levelId;
		}

		public Image icon() { return Icons.STAIRS.get(); }

		public Visual secondIcon() { return null; }

		public int quantity() { return 1; }

		protected abstract int order();

		public abstract String title();

		public abstract String desc();

		@Override
		public abstract boolean equals(Object obj);

		@Override
		public int compareTo( Record another ) {
			return another.depth() - depth();
		}

		private static final String DEPTH	= "depth";

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			levelId = bundle.getInt( DEPTH );
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
			bundle.put( DEPTH, levelId );
		}
	}

	public enum Landmark {
		CHASM_FLOOR,
		WATER_FLOOR,
		GRASS_FLOOR,
		DARK_FLOOR,
		LARGE_FLOOR,
		TRAPS_FLOOR,
		SECRETS_FLOOR,

		SHOP,
		ALCHEMY,
		GARDEN,
		DISTANT_WELL,
		WELL_OF_HEALTH,
		WELL_OF_AWARENESS,
		WELL_OF_TRANSMUTATION,
		SACRIFICIAL_FIRE,
		STATUE,

		LOST_PACK,
		BEACON_LOCATION,

		GHOST,
		RAT_KING,
		WANDMAKER,
		TROLL,
		IMP,
		DEL,

		DEMON_SPAWNER,

		GNOLL_SWAP,
		GOLYAT_SWAP,
		THIEF_SWAP,
		SPINNER_CAT,
		Elemental_SWAP,
		TYPHOON

	}

	public static class LandmarkRecord extends Record {

		protected Landmark landmark;

		public LandmarkRecord() {}

		public LandmarkRecord(Landmark landmark, int levelId ) {
			this.landmark = landmark;
			this.levelId = levelId;
		}
		public Image icon(){
			Image image = image();
			if (image.width() != 16 || image.height() != 16){
				float size = Math.max(image.width(), image.height());
				image.scale.set(16/size);
			}
			return image;
		}
		private Image image(){
			switch (landmark){
				default:
					return Icons.STAIRS.get();
				case CHASM_FLOOR:
					return Icons.get(Level.Feeling.CHASM);
				case WATER_FLOOR:
					return Icons.get(Level.Feeling.WATER);
				case GRASS_FLOOR:
					return Icons.get(Level.Feeling.GRASS);
				case DARK_FLOOR:
					return Icons.get(Level.Feeling.DARK);
				case LARGE_FLOOR:
					return Icons.get(Level.Feeling.LARGE);
				case TRAPS_FLOOR:
					return Icons.get(Level.Feeling.TRAPS);
				case SECRETS_FLOOR:
					return Icons.get(Level.Feeling.SECRETS);

				case SHOP:
					if (levelId % 1000 == 25)								return new NoelShopKeeperSprite();
					else if (levelId % 1000 == 20)    						return new P7Sprite();
					else if (Statistics.deepestFloor % 1000 > levelId % 1000 + 5)	return new ShopkeeperSprite.MirrorShopkeeper();
					else 											return new ShopkeeperSprite();
				case ALCHEMY:
					return MapEditor.TerrainIcon(Terrain.ALCHEMY);
				case GARDEN:
					return MapEditor.TerrainIcon(Terrain.HIGH_GRASS);
				case DISTANT_WELL:
					return MapEditor.TerrainIcon(Terrain.EMPTY_WELL);
				case WELL_OF_HEALTH:
					return Icons.Notice(new PotionOfHealing());
				case WELL_OF_AWARENESS:
					return Icons.Notice(new ScrollOfIdentify());
				case SACRIFICIAL_FIRE:
					return MapEditor.TerrainIcon(Terrain.PEDESTAL);
				case STATUE:
					return new StatueSprite();

				case LOST_PACK:
					return Icons.get(Icons.BACKPACK_LRG);
				case BEACON_LOCATION:
					return new ItemSprite(ItemSpriteSheet.RETURN_BEACON);

				case GHOST:
					return new GhostSprite();
				case RAT_KING:
					return new FncSprite();
				case WANDMAKER:
					return new WandmakerSprite();
				case TROLL:
					return new BlacksmithSprite();
				case IMP:
					return new P7Sprite();
				case DEL:
					return new DELSprite();

				case GNOLL_SWAP:
					return new GnollSWAPSprite();
				case GOLYAT_SWAP:
					return new GolyatPlusSprite();
				case THIEF_SWAP:
					return new BanditSprite();
				case SPINNER_CAT:
					return new SpinnerCatSprite();
				case Elemental_SWAP:
					return new Elemental.ChaosElemental().sprite();
				case TYPHOON:
					return new TyphoonSprite();
				case DEMON_SPAWNER:
					return new SpawnerSprite();
			}
		}

		@Override
		public String title() {
			switch (landmark) {
				default:            return Messages.get(Landmark.class, landmark.name());
				case CHASM_FLOOR:   return Messages.get(Level.Feeling.class, "chasm_title");
				case WATER_FLOOR:   return Messages.get(Level.Feeling.class, "water_title");
				case GRASS_FLOOR:   return Messages.get(Level.Feeling.class, "grass_title");
				case DARK_FLOOR:    return Messages.get(Level.Feeling.class, "dark_title");
				case LARGE_FLOOR:   return Messages.get(Level.Feeling.class, "large_title");
				case TRAPS_FLOOR:   return Messages.get(Level.Feeling.class, "traps_title");
				case SECRETS_FLOOR: return Messages.get(Level.Feeling.class, "secrets_title");

				case LOST_PACK:     return Messages.get(LostBackpack.class, "name");
				case BEACON_LOCATION:return Messages.get(BeaconOfReturning.class, "name");
				case GNOLL_SWAP:	return Messages.get(GnollSWAP.class, "name");
				case GOLYAT_SWAP:	return Messages.get(GolyatPlus.class, "name");
				case THIEF_SWAP:	return Messages.get(Bandit.class, "name");
				case SPINNER_CAT:	return Messages.get(SpinnerCat.class, "name");
				case Elemental_SWAP:return Messages.get(Elemental.ChaosElemental.class, "name");
				case TYPHOON:		return Messages.get(Typhoon.class, "name");
			}
		}

		@Override
		public String desc() {
			switch (landmark) {
				default:            return "";

				case CHASM_FLOOR:   return Messages.get(Level.Feeling.class, "chasm_desc");
				case WATER_FLOOR:   return Messages.get(Level.Feeling.class, "water_desc");
				case GRASS_FLOOR:   return Messages.get(Level.Feeling.class, "grass_desc");
				case DARK_FLOOR:    return Messages.get(Level.Feeling.class, "dark_desc");
				case LARGE_FLOOR:   return Messages.get(Level.Feeling.class, "large_desc");
				case TRAPS_FLOOR:   return Messages.get(Level.Feeling.class, "traps_desc");
				case SECRETS_FLOOR: return Messages.get(Level.Feeling.class, "secrets_desc");

				case SHOP:
					if (levelId % 1000 == 25)    return Messages.get(NoelShopkeeper.class, "desc");
					else if (levelId % 1000 == 20)    return Messages.get(ImpShopkeeper.class, "desc");
					else                return Messages.get(Shopkeeper.class, "desc");
				case ALCHEMY:           return Messages.get(Level.class, "alchemy_desc");
				case GARDEN:            return Messages.get(Foliage.class, "desc");
				case DISTANT_WELL:      return Messages.get(WeakFloorRoom.HiddenWell.class, "desc");
				case WELL_OF_HEALTH:    return Messages.get(WaterOfHealth.class, "desc");
				case WELL_OF_AWARENESS: return Messages.get(WaterOfAwareness.class, "desc");
				case SACRIFICIAL_FIRE:  return Messages.get(SacrificialFire.class, "desc");
				case STATUE:            return Messages.get(Statue.class, "desc");

				case LOST_PACK:         return Messages.get(LostBackpack.class, "desc");
				case BEACON_LOCATION:   return Messages.get(BeaconOfReturning.class, "desc");

				case GHOST:         return Messages.get(Ghost.class, "desc");
				case RAT_KING:      return new RatKing().description(); //variable description based on holiday/run state
				case WANDMAKER:     return Messages.get(Wandmaker.class, "desc");
				case TROLL:         return Messages.get(Blacksmith.class, "desc");
				case IMP:           return Messages.get(Imp.class, "desc");
				case DEL:			return Messages.get(DEL.class, "desc");

				case GNOLL_SWAP:	return Messages.get(GnollSWAP.class, "desc");
				case GOLYAT_SWAP:	return Messages.get(GolyatPlus.class, "desc");
				case THIEF_SWAP:	return Messages.get(Bandit.class, "desc");
				case SPINNER_CAT:	return Messages.get(SpinnerCat.class, "desc");
				case Elemental_SWAP:return Messages.get(Elemental.ChaosElemental.class, "desc");
				case TYPHOON:		return Messages.get(Typhoon.class, "desc");
				case DEMON_SPAWNER: return Messages.get(DemonSpawner.class, "desc");
			}
		}

		@Override
		protected int order(){
			return landmark.ordinal();
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof LandmarkRecord)
					&& landmark == ((LandmarkRecord) obj).landmark
					&& depth() == ((LandmarkRecord) obj).depth();
		}

		private static final String LANDMARK	= "landmark";

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			landmark = Landmark.valueOf(bundle.getString(LANDMARK));
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( LANDMARK, landmark.name() );
		}
	}

	public static class KeyRecord extends Record {

		protected Key key;

		public KeyRecord() {}

		public KeyRecord( Key key ){
			this.key = key;
		}

		@Override
		public int depth() {
			return key.depth;
		}

		@Override
		public Image icon() {
			return new ItemSprite(key);
		}

		@Override
		public Visual secondIcon() {
			if (quantity() > 1){
				BitmapText text = new BitmapText(Integer.toString(quantity()), PixelScene.pixelFont);
				text.measure();
				return text;
			} else {
				return null;
			}
		}

		@Override
		public String title() {
			return key.name();
		}

		@Override
		public String desc() {
			return key.desc();
		}

		public Class<? extends Key> type(){
			return key.getClass();
		}

		@Override
		protected int order() {
			return 1000 + Generator.Category.order(key);
		}

		public int quantity(){
			return key.quantity();
		}

		public void quantity(int num){
			key.quantity(num);
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof KeyRecord)
					&& key.isSimilar(((KeyRecord) obj).key);
		}

		private static final String KEY	= "key";

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			key = (Key) bundle.get(KEY);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( KEY, key );
		}
	}

	public enum CustomType {
		TEXT,
		DEPTH,
		ITEM_TYPE,
		SPECIFIC_ITEM,
		ITEM //for pre-3.1 save conversion
	}

	public static class CustomRecord extends Record {

		protected CustomType type;

		protected int ID = -1;
		protected Class<? extends Item> itemClass;

		protected String title;
		protected String body;

		public CustomRecord() {}

		public CustomRecord(String title, String desc) {
			type = CustomType.TEXT;
			this.title = title;
			body = desc;
		}

		public CustomRecord(int levelId, String title, String desc) {
			type = CustomType.DEPTH;
			this.levelId = levelId;
			this.title = title;
			body = desc;
		}

		public CustomRecord(Class<? extends Item> itemCls, String title, String desc) {
			type = CustomType.ITEM_TYPE;
			itemClass = itemCls;
			this.title = title;
			body = desc;
		}

		public CustomRecord(Item item, String title, String desc) {
			type = CustomType.SPECIFIC_ITEM;
			itemClass = item.getClass();
			this.title = title;
			body = desc;
		}
		public static int nextCustomID(){
			return nextCustomID++;
		}
		public void assignID(){
			if (ID == -1) {
				ID = nextCustomID();
			}
		}
		public CustomType type(){
			return type;
		}
		public Class<? extends Item> itemClass(){
			return itemClass;
		}

		public int ID(){
			return ID;
		}

		@Override
		public int depth() {
			if (type == CustomType.DEPTH){
				return levelId;
			} else {
				return 0;
			}
		}

		@Override
		public Image icon() {
			switch (type){
				case TEXT: default:
					return Icons.RENAME_ON.get();
				case DEPTH:
					return Icons.STAIRS.get();
				case ITEM_TYPE:
				case SPECIFIC_ITEM:
					Item i = Reflection.newInstance(itemClass);
					return new ItemSprite(i);
			}
		}

		@Override
		public Visual secondIcon() {
			switch (type){
				case TEXT: default:
					return null;
				case DEPTH:
					String depth = String.valueOf(depth() % 1000);
					if (depth() > 1000)
						depth += "/" + depth()/1000;
					BitmapText text = new BitmapText(depth, PixelScene.pixelFont);
					text.measure();
					return text;
				case ITEM_TYPE:
				case SPECIFIC_ITEM:
					Item item = Reflection.newInstance(itemClass);
					if (item.isIdentified() && item.icon != -1) {
						Image secondIcon = new Image(Assets.Sprites.ITEM_ICONS);
						secondIcon.frame(ItemSpriteSheet.Icons.film.get(item.icon));
						return secondIcon;
					}
					return null;
			}
		}

		@Override
		protected int order() {
			return 2000 + ID;
		}

		public void editText(String title, String desc){
			this.title = title;
			this.body = desc;
		}

		@Override
		public String title() {
			return title;
		}

		@Override
		public String desc() {
			return body;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof CustomRecord && ((CustomRecord) obj).ID == ID;
		}

		private static final String TYPE        = "type";
		private static final String ID_NUMBER   = "id_number";

		private static final String ITEM_CLASS   = "item_class";

		private static final String TITLE       = "title";
		private static final String BODY        = "body";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TYPE, type);
			bundle.put(ID_NUMBER, ID);
			if (itemClass != null) bundle.put(ITEM_CLASS, itemClass);
			bundle.put(TITLE, title);
			bundle.put(BODY, body);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			type = bundle.getEnum(TYPE, CustomType.class);
			ID = bundle.getInt(ID_NUMBER);

			if (bundle.contains(ITEM_CLASS)) {
				itemClass = bundle.getClass(ITEM_CLASS);
				if (type == CustomType.ITEM){
					//prior to v3.1 specific item notes and item type notes were the same
					//we assume notes are for a specific item if they're for an equipment
					if (EquipableItem.class.isAssignableFrom(itemClass)){
						type = CustomType.SPECIFIC_ITEM;
					} else {
						type = CustomType.ITEM_TYPE;
					}
				}
			}

			title = bundle.getString(TITLE);
			body = bundle.getString(BODY);
		}
	}

	private static ArrayList<Record> records;

	public static void reset() {
		records = new ArrayList<>();
	}

	private static final String RECORDS	        = "records";
	private static final String NEXT_CUSTOM_ID	= "next_custom_id";

	protected static int nextCustomID = 0;

	public static void storeInBundle( Bundle bundle ) {
		bundle.put( RECORDS, records );
		bundle.put( NEXT_CUSTOM_ID, nextCustomID );
	}

	public static void restoreFromBundle( Bundle bundle ) {
		records = new ArrayList<>();
		nextCustomID = bundle.getInt( NEXT_CUSTOM_ID );
		for (Bundlable rec : bundle.getCollection( RECORDS ) ) {
			records.add( (Record) rec );
		}
	}

	public static boolean add( Landmark landmark ) {
		return add( landmark, Dungeon.levelId );
	}

	public static boolean add( Landmark landmark, int levelId) {
		LandmarkRecord l = new LandmarkRecord( landmark, levelId);
		if (!records.contains(l)) {
			boolean result = records.add(l);
			Collections.sort(records, comparator);
			return result;
		}
		return false;
	}

	public static boolean contains( Landmark landmark ){
		return contains( landmark, Dungeon.levelId );
	}

	public static boolean contains( Landmark landmark, int levelId ){
		return records.contains(new LandmarkRecord( landmark, levelId));
	}

	public static boolean remove( Landmark landmark ) {
		return remove( landmark, Dungeon.levelId );
	}

	public static boolean remove( Landmark landmark, int levelId ) {
		return records.remove( new LandmarkRecord(landmark, levelId) );
	}

	public static boolean add( Key key ){
		KeyRecord k = new KeyRecord(key);
		if (!records.contains(k)){
			boolean result = records.add(k);
			Collections.sort(records, comparator);
			return result;
		} else {
			k = (KeyRecord) records.get(records.indexOf(k));
			k.quantity(k.quantity() + key.quantity());
			return true;
		}
	}

	public static boolean remove( Key key ){
		KeyRecord k = new KeyRecord( key );
		if (records.contains(k)){
			Catalog.countUses(key.getClass(), key.quantity());
			k = (KeyRecord) records.get(records.indexOf(k));
			k.quantity(k.quantity() - key.quantity());
			if (k.quantity() <= 0){
				records.remove(k);
			}
			return true;
		}
		return false;
	}

	public static int keyCount( Key key ){
		KeyRecord k = new KeyRecord( key );
		if (records.contains(k)){
			k = (KeyRecord) records.get(records.indexOf(k));
			return k.quantity();
		} else {
			return 0;
		}
	}

	public static boolean add( CustomRecord rec ){
		rec.assignID();
		if (!records.contains(rec)){
			boolean result = records.add(rec);
			Collections.sort(records, comparator);
			return result;
		}
		return false;
	}

	public static boolean remove( CustomRecord rec ){
		if (records.contains(rec)){
			records.remove(rec);
			return true;
		}
		return false;
	}

	public static <T extends Record> ArrayList<T> getRecords( Class<T> recordType ){
		ArrayList<T> filtered = new ArrayList<>();
		for (Record rec : records){
			if (recordType.isInstance(rec)){
				filtered.add((T)rec);
			}
		}
		return filtered;
	}

	public static ArrayList<Record> getRecords(int levelId){
		ArrayList<Record> filtered = new ArrayList<>();
		for (Record rec : records){
			if (rec.depth() == levelId && !(rec instanceof CustomRecord)){
				filtered.add(rec);
			}
		}

		Collections.sort(filtered, comparator);

		return filtered;
	}

	public static CustomRecord findCustomRecord( int ID ){
		for (Record rec : records){
			if (rec instanceof CustomRecord && ((CustomRecord) rec).ID == ID) {
				return (CustomRecord) rec;
			}
		}
		return null;
	}

	public static CustomRecord findCustomRecord( Item item ){
		if (item == null)
			return null;
		Class<? extends Item> itemClass;
		if (item instanceof ExoticPotion)
			itemClass = ExoticPotion.exoToReg.get(item.getClass());
		else if (item instanceof ExoticScroll)
			itemClass = ExoticScroll.exoToReg.get(item.getClass());
		else
			itemClass = item.getClass();
		return findCustomRecord(itemClass);
	}
	public static CustomRecord findCustomRecord( Class<? extends Item> itemClass ){
		for (Record rec : records){
			if (rec instanceof CustomRecord
					&& ((CustomRecord) rec).type == CustomType.ITEM_TYPE
					&& ((CustomRecord) rec).itemClass == itemClass) {
				return (CustomRecord) rec;
			}
		}
		return null;
	}
	private static final Comparator<Record> comparator = new Comparator<Record>() {
		@Override
		public int compare(Record r1, Record r2) {
			return r1.order() - r2.order();
		}
	};

}
