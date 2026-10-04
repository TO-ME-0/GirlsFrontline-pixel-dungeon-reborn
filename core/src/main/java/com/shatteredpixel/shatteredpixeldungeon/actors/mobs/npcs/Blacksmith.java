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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Dungeons;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DarkGold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ParchmentScrap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.BlacksmithRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.triggers.MineEntranceTrigger;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BlacksmithSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBlacksmith;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;

public class Blacksmith extends NPC {

	{
		spriteClass = BlacksmithSprite.class;

		properties.add(Property.IMMOVABLE);
	}

	@Override
	public Notes.Landmark landmark() {
		return (!Quest.cur().completed() || Quest.cur().rewardsAvailable()) ? Notes.Landmark.TROLL : null;
	}

	@Override
	protected boolean act() {
		//奖励全部结清后移除路标（mod 的 Hero 会自动添加 landmark，这里只负责移除）
		if (!Quest.cur().rewardsAvailable() && Quest.cur().completed()){
			Notes.remove( landmark() );
		}
		return super.act();
	}

	@Override
	public boolean interact(Char c) {

		sprite.turnTo( pos, c.pos );

		if (c != Dungeon.hero()){
			return true;
		}

		//旧版任务存档迁移：未领取旧版奖励的存档统一转接新版任务
		if (Quest.cur().needMigration){
			Quest.cur().migrateToNewQuest();
		}

		if (!Quest.cur().given) {

			String msg1 = "";
			String msg2 = "";

			//mod 仅有四个原职业，其余职业不追加职业专属开场白
			switch (Dungeon.hero().heroClass){
				case WARRIOR:   msg1 += Messages.get(Blacksmith.this, "intro_quest_warrior"); break;
				case MAGE:      msg1 += Messages.get(Blacksmith.this, "intro_quest_mage"); break;
				case ROGUE:     msg1 += Messages.get(Blacksmith.this, "intro_quest_rogue"); break;
				case HUNTRESS:  msg1 += Messages.get(Blacksmith.this, "intro_quest_huntress"); break;
				default: break;
			}

			msg1 += "\n\n" + Messages.get(Blacksmith.this, "intro_quest_start");

			switch (Quest.cur().type){
				case Quest.CRYSTAL: msg2 += Messages.get(Blacksmith.this, "intro_quest_crystal"); break;
				case Quest.GNOLL:   msg2 += Messages.get(Blacksmith.this, "intro_quest_gnoll"); break;
				case Quest.FUNGI:   msg2 += Messages.get(Blacksmith.this, "intro_quest_fungi"); break;
			}

			final String msg1Final = msg1;
			final String msg2Final = msg2;
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndQuest(Blacksmith.this, msg1Final) {
						@Override
						public void hide() {
							super.hide();

							Quest.cur().given = true;
							Quest.cur().completed = false;
							Item pick = Quest.cur().pickaxe != null ? Quest.cur().pickaxe : new Pickaxe();
							if (pick.doPickUp( Dungeon.hero() )) {
								GLog.i( Messages.capitalize(Messages.get(Dungeon.hero(), "you_now_have", pick.name()) ));
							} else {
								Dungeon.level.drop( pick, Dungeon.hero().pos ).sprite.drop();
							}
							Quest.cur().pickaxe = null;

							if (msg2Final != ""){
								GameScene.show(new WndQuest(Blacksmith.this, msg2Final));
							}

						}
					} );
				}
			});

		} else if (!Quest.cur().completed) {

			String msg = Messages.get(this, "reminder") + "\n\n";
			switch (Quest.cur().type){
				case Quest.CRYSTAL: msg += Messages.get(Blacksmith.this, "reminder_crystal"); break;
				case Quest.GNOLL:   msg += Messages.get(Blacksmith.this, "reminder_gnoll"); break;
				case Quest.FUNGI:   msg += Messages.get(Blacksmith.this, "reminder_fungi"); break;
			}
			tell(msg);

		} else if (Quest.cur().rewardsAvailable()) {

			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					//游戏可能在史密斯奖励选择中途关闭
					if (Quest.cur().smithRewards != null && Quest.cur().smiths > 0){
						GameScene.show( new WndBlacksmith.WndSmith( Blacksmith.this, Dungeon.hero() ) );
					} else {
						GameScene.show(new WndBlacksmith(Blacksmith.this, Dungeon.hero()));
					}
				}
			});

		} else {

			tell( Messages.get(this, "get_lost") );

		}

		return true;
	}

	private void tell( String text ) {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndQuest( Blacksmith.this, text ) );
			}
		});
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage( int dmg, Object src ) {
		//do nothing
	}

	@Override
	public void add( Buff buff ) {
		//do nothing（mod 的 add(Buff) 返回 void，免疫所有 buff 即不处理）
	}

	@Override
	public boolean reset() {
		return true;
	}

	/*
	// ===== 旧版重铸规则（verify/upgrade），照搬 3.3.8 后由 WndBlacksmith.WndReforge 内联实现，此处整段保留备查 =====
	public static String verify( Item item1, Item item2 ) {

		if (item1 == item2 && (item1.quantity() == 1 && item2.quantity() == 1)) {
			return Messages.get(Blacksmith.class, "same_item");
		}

		if (item1 instanceof Armor
				&& item2 instanceof Armor && ((Armor) item2).tier == ((Armor) item1).tier) {
			return null;
		}

		if (item1 instanceof MagesStaff && item2.getClass() == ((MagesStaff) item1).wand.getClass()
				|| item2 instanceof MagesStaff && item1.getClass() == ((MagesStaff) item2).wand.getClass()) {
			return null;
		}

		if (item1.getClass() != item2.getClass()) {
			return Messages.get(Blacksmith.class, "diff_type");
		}

		if (!item1.cursedKnown || !item2.cursedKnown) {
			return Messages.get(Blacksmith.class, "un_ided");
		}

		if (item1.cursed || item2.cursed ||
				(item1 instanceof Armor && ((Armor) item1).hasCurseGlyph()) ||
				(item2 instanceof Armor && ((Armor) item2).hasCurseGlyph()) ||
				(item1 instanceof Weapon && ((Weapon) item1).hasCurseEnchant()) ||
				(item2 instanceof Weapon && ((Weapon) item2).hasCurseEnchant())) {
			return Messages.get(Blacksmith.class, "cursed");
		}

		if (item1.level() < 0 || item2.level() < 0) {
			return Messages.get(Blacksmith.class, "degraded");
		}

		if (!item1.isUpgradable() || !item2.isUpgradable()) {
			return Messages.get(Blacksmith.class, "cant_reforge");
		}

		return null;
	}

	public static void upgrade( ArrayList<Item> items, ArrayList<Class<? extends Item>> itemsClass ) {

		Item first, second;
		if (itemsClass.contains(ClassArmor.class)){
			first = items.remove(itemsClass.indexOf(ClassArmor.class));
			second = items.get(0);
		}
		else if (itemsClass.contains(MagesStaff.class)) {
			first = items.remove(itemsClass.indexOf(MagesStaff.class));
			second = items.get(0);
		}
		else {
			Item item1 = items.get(0);
			Item item2 = items.get(1);
			if (item1.levelKnown && item2.levelKnown) {
				if (item2.level() > item1.level()) {
					first = item2;
					second = item1;
				} else {
					first = item1;
					second = item2;
				}
			} else if (!item1.levelKnown) {
				first = item2;
				second = item1;
			} else {
				first = item1;
				second = item2;
			}
		}

		Sample.INSTANCE.play( Assets.Sounds.EVOKE );
		ScrollOfUpgrade.upgrade( Dungeon.hero() );
		Item.evoke( Dungeon.hero() );

		if (second.isEquipped( Dungeon.hero() )) {
			((EquipableItem)second).doUnequip( Dungeon.hero(), false );
		}
		second.detach( Dungeon.hero().belongings.backpack );

		if (second instanceof Armor){
			BrokenSeal seal = ((Armor) second).checkSeal();
			if (seal != null){
				Dungeon.level.drop( seal, Dungeon.hero().pos );
			}
		}

		//preserves enchant/glyphs if present
		if (first instanceof Weapon && ((Weapon) first).hasGoodEnchant()){
			((Weapon) first).upgrade(true);
		} else if (first instanceof Armor && ((Armor) first).hasGoodGlyph()){
			((Armor) first).upgrade(true);
		} else {
			first.upgrade();
		}
		Catalog.countUse(first.getClass());
		Dungeon.hero().spendAndNext( 2f );
		Badges.validateItemLevelAquired( first );
		Item.updateQuickslot();

		Quest.cur().reforged = true;

		Notes.remove( Notes.Landmark.TROLL );
	}
	*/

	public static class Quest {

		public static Quest cur() {
			return Dungeons.cur().blacksmithQuest;
		}
		//仅用于存档读写（始终发生在主线程），避免误序列化 worker 状态
		public static Quest main() {
			return Dungeons.main().blacksmithQuest;
		}

		//矿洞生成相关房间/关卡使用的任务类型便捷访问（对齐 3.3.8 Quest.Type()）
		public static int type(){
			return cur().type;
		}

		public int getType(){
			return type;
		}

		public void setType(int type){
			this.type = type;
		}

		private int type = 0;
		public static final int CRYSTAL = 1;
		public static final int GNOLL = 2;
		public static final int FUNGI = 3; //真菌任务未实装，仅保留代码路径（不可被 roll）

		//任务状态
		public boolean spawned;
		public boolean given;
		public boolean started;
		public boolean bossBeaten;
		public boolean completed;

		//奖励状态：剩余 favor、暂存的镐、各项服务已使用次数
		public int favor;
		public Item pickaxe;
		public boolean freePickaxe;
		public int reforges;
		public int hardens;
		public int upgrades;
		public int smiths;

		//预生成奖励，保证同种子下结果一致
		public ArrayList<Item> smithRewards;
		public Weapon.Enchantment smithEnchant;
		public Armor.Glyph smithGlyph;

		//旧版任务存档迁移标记（不写入存档）：首次交互时清暗金、补矿洞入口并转接新任务
		public transient boolean needMigration;

		public void reset() {
			type        = 0;

			spawned		= false;
			given		= false;
			started     = false;
			bossBeaten  = false;
			completed	= false;

			favor       = 0;
			pickaxe     = new Pickaxe().identify(false);
			freePickaxe = false;
			reforges    = 0;
			hardens     = 0;
			upgrades    = 0;
			smiths      = 0;

			smithRewards = null;
			smithEnchant = null;
			smithGlyph = null;

			needMigration = false;
		}

		private static final String NODE	= "blacksmith";

		private static final String TYPE    	= "type";
		private static final String ALTERNATIVE	= "alternative";

		private static final String SPAWNED		= "spawned";
		private static final String GIVEN		= "given";
		private static final String STARTED		= "started";
		private static final String BOSS_BEATEN	= "boss_beaten";
		private static final String COMPLETED	= "completed";

		private static final String FAVOR	    = "favor";
		private static final String PICKAXE	    = "pickaxe";
		private static final String FREE_PICKAXE= "free_pickaxe";
		private static final String REFORGES	= "reforges";
		private static final String HARDENS	    = "hardens";
		private static final String UPGRADES	= "upgrades";
		private static final String SMITHS	    = "smiths";
		private static final String SMITH_REWARDS = "smith_rewards";
		private static final String ENCHANT		= "enchant";
		private static final String GLYPH		= "glyph";

		//旧版存档键，仅用于识别旧存档（REFORGED 在新版中不再写入）
		private static final String REFORGED	= "reforged";

		public void storeInBundle( Bundle bundle ) {

			Bundle node = new Bundle();

			node.put( SPAWNED, spawned );

			if (spawned) {
				node.put( TYPE, type );

				node.put( GIVEN, given );
				node.put( STARTED, started );
				node.put( BOSS_BEATEN, bossBeaten );
				node.put( COMPLETED, completed );

				node.put( FAVOR, favor );
				if (pickaxe != null) node.put( PICKAXE, pickaxe );
				node.put( FREE_PICKAXE, freePickaxe );
				node.put( REFORGES, reforges );
				node.put( HARDENS, hardens );
				node.put( UPGRADES, upgrades );
				node.put( SMITHS, smiths );

				if (smithRewards != null) {
					node.put( SMITH_REWARDS, smithRewards );
					if (smithEnchant != null) {
						node.put(ENCHANT, smithEnchant);
						node.put(GLYPH, smithGlyph);
					}
				}
			}

			bundle.put( NODE, node );
		}

		public void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );

			if (node.isNull() || !(spawned = node.getBoolean( SPAWNED ))) {
				reset();
				return;
			}

			//识别旧版任务存档（旧版写 reforged 键，新版不再写）
			if (node.contains( REFORGED )) {
				boolean oldReforged = node.getBoolean( REFORGED );
				reset();
				spawned = true;
				if (oldReforged) {
					//已领取旧版重铸奖励：视为新版任务已完成、无剩余 favor
					type = CRYSTAL;
					given = true;
					completed = true;
				} else {
					//未领取旧版奖励：转接新版任务，首次交互时执行迁移
					type = Random.IntRange(1, 2);
					given = false;
					generateRewards();
					needMigration = true;
				}
				return;
			}

			type = node.getInt(TYPE);

			given = node.getBoolean( GIVEN );
			started = node.getBoolean( STARTED );
			bossBeaten = node.getBoolean( BOSS_BEATEN );
			completed = node.getBoolean( COMPLETED );

			favor = node.getInt( FAVOR );
			if (node.contains(PICKAXE)) {
				pickaxe = (Item) node.get(PICKAXE);
			} else {
				pickaxe = null;
			}
			if (node.contains(FREE_PICKAXE)){
				freePickaxe = node.getBoolean(FREE_PICKAXE);
			} else {
				//3.1 之前存档及早期错误值的兼容
				if (favor >= 2500){
					freePickaxe = true;
				} else {
					freePickaxe = false;
				}
			}
			reforges = node.getInt( REFORGES );
			hardens = node.getInt( HARDENS );
			upgrades = node.getInt( UPGRADES );
			smiths = node.getInt( SMITHS );

			if (node.contains( SMITH_REWARDS )){
				smithRewards = new ArrayList<Item>((Collection<Item>) ((Collection<?>) node.getCollection( SMITH_REWARDS )));
				if (node.contains(ENCHANT)) {
					smithEnchant = (Weapon.Enchantment) node.get(ENCHANT);
					smithGlyph   = (Armor.Glyph) node.get(GLYPH);
				}
			} else {
				smithRewards = null;
			}

		}

		public ArrayList<Room> spawn( ArrayList<Room> rooms ) {
			if (!spawned && Dungeon.cur().depth > 11 && Random.Int( 15 - Dungeon.cur().depth ) == 0) {

				rooms.add(new BlacksmithRoom());
				spawned = true;

				//真菌任务目前无法被 roll（尚未实装完成）
				type = Random.IntRange(1, 2);

				given = false;
				generateRewards();

			}
			return rooms;
		}

		public void generateRewards(){
			smithRewards = new ArrayList<Item>();
			smithRewards.add(Generator.randomWeapon(3));
			smithRewards.add(Generator.randomWeapon(3));
			//两件武器不能同 class
			while (smithRewards.get(0).getClass() == smithRewards.get(1).getClass()) {
				smithRewards.remove(1);
				smithRewards.add(Generator.randomWeapon(3));
			}
			smithRewards.add(Generator.randomMissile(3));
			smithRewards.add(Generator.randomArmor(3));

			//30%:+0, 45%:+1, 20%:+2, 5%:+3
			int rewardLevel;
			float itemLevelRoll = Random.Float();
			if (itemLevelRoll < 0.3f){
				rewardLevel = 0;
			} else if (itemLevelRoll < 0.75f){
				rewardLevel = 1;
			} else if (itemLevelRoll < 0.95f){
				rewardLevel = 2;
			} else {
				rewardLevel = 3;
			}

			for (Item i : smithRewards){
				i.level(rewardLevel);
				if (i instanceof Weapon) {
					((Weapon) i).enchant(null);
				} else if (i instanceof Armor){
					((Armor) i).inscribe(null);
				}
				i.cursed = false;
			}

			// 30% 基础附魔/铭文概率，单独存储以免提前暴露
			//先生成再判定置空，保证 RNG 消耗次数与 3.3.8 一致
			smithEnchant = Weapon.Enchantment.random();
			smithGlyph = Armor.Glyph.random();

			float enchantRoll = Random.Float();
			if (enchantRoll > 0.3f * ParchmentScrap.enchantChanceMultiplier()){
				smithEnchant = null;
				smithGlyph = null;
			}

		}

		public int Type(){
			return type;
		}

		public boolean given(){
			return given;
		}

		public boolean started(){
			return started;
		}

		public void start(){
			started = true;
		}

		public boolean beatBoss(){
			return bossBeaten = true;
		}

		public boolean bossBeaten(){
			return bossBeaten;
		}

		public boolean completed(){
			return given && completed;
		}

		public void complete(){
			completed = true;

			favor = 0;
			DarkGold gold = Dungeon.hero().belongings.getItem(DarkGold.class);
			if (gold != null){
				favor += Math.min(2000, gold.quantity()*50);
				gold.detachAll(Dungeon.hero().belongings.backpack);
			}

			Pickaxe pick = Dungeon.hero().belongings.getItem(Pickaxe.class);
			if (pick.isEquipped(Dungeon.hero())) {
				boolean wasCursed = pick.cursed;
				pick.cursed = false; //保证总能卸下
				pick.doUnequip(Dungeon.hero(), false);
				pick.cursed = wasCursed;
			}
			pick.detach(Dungeon.hero().belongings.backpack);
			Quest.cur().pickaxe = pick;

			if (bossBeaten) favor += 1000;

			if (favor >= 2500){
				freePickaxe = true;
			}
		}

		public boolean rewardsAvailable(){
			return favor > 0
					|| (smithRewards != null && smiths > 0)
					|| (pickaxe != null && freePickaxe);
		}

		//旧版任务存档迁移：清除背包暗金，在铁匠房补放矿洞入口（触发器+EXIT 地形+刷新地图）
		public void migrateToNewQuest(){
			DarkGold gold = Dungeon.hero().belongings.getItem(DarkGold.class);
			if (gold != null){
				gold.detachAll(Dungeon.hero().belongings.backpack);
			}

			Level level = Dungeon.level;
			if (level instanceof RegularLevel) {
				Room smithRoom = null;
				for (Room r : ((RegularLevel) level).rooms()){
					if (r instanceof BlacksmithRoom){
						smithRoom = r;
						break;
					}
				}

				if (smithRoom != null) {
					int entrancePos = -1;
					for (int i = 0; i < 30 && entrancePos == -1; i++){
						int cell = level.pointToCell(smithRoom.random(2));
						if ((level.map[cell] == Terrain.EMPTY || level.map[cell] == Terrain.EMPTY_SP)
								&& level.heaps.get(cell) == null
								&& Actor.findChar(cell) == null
								&& level.triggers.get(cell) == null){
							entrancePos = cell;
						}
					}

					if (entrancePos != -1){
						Painter.set(level, entrancePos, Terrain.EXIT);
						level.placeTrigger(new MineEntranceTrigger().create(entrancePos));
						GameScene.updateMap(entrancePos);
					}
				}
			}

			needMigration = false;
		}

	}
}
