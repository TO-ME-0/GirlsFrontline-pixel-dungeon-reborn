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
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder.Holder;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardSelector;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.VaultLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.AmbitiousImpRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.triggers.VaultEntranceTrigger;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.P7Sprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.shatteredpixel.shatteredpixeldungeon.utils.Rng;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Imp extends NPC {

	{
		spriteClass = P7Sprite.class;

		properties.add(Property.IMMOVABLE);
	}

	private boolean seenBefore = false;

	@Override
	protected boolean act() {

		//新版任务：若奖励因玩家背包满而暂存在小恶魔身上，见面时掉落在脚下
		if (Quest.cur().isCompleted() && Quest.cur().reward != null){
			Dungeon.level.drop(Quest.cur().reward, pos);
			Quest.cur().reward = null;
		}

		//新版任务：分数足以解锁商店后，小恶魔在玩家看不见时溜走
		if (Quest.cur().isCompleted() && Quest.cur().score > 2000
				&& fieldOfView != null && !fieldOfView[Dungeon.hero().pos]){
			flee();
		} else if (!Quest.cur().given && Dungeon.level.heroFOV[pos]) {
			if (!seenBefore) {
				yell( Messages.get(this, "hey", Dungeon.hero().name() ) );
			}
			Notes.add( Notes.Landmark.IMP );
			seenBefore = true;
		} else {
			seenBefore = false;
		}

		return super.act();
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage( int dmg, Object src ) {
	}

	@Override
	public void add( Buff buff ) {
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public boolean interact(Char c) {

		sprite.turnTo( pos, Dungeon.hero().pos );

		if (c != Dungeon.hero()){
			return true;
		}

		//新版金库夺宝任务：纯文本对话
		if (!Quest.cur().given){
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndQuest(Imp.this, Messages.get(Imp.this, "quest_intro_1")) {
						@Override
						public void hide() {
							super.hide();

							Quest.cur().given = true;
							Quest.cur().completed = false;
							Notes.add( Notes.Landmark.IMP );

							tell(Messages.get(Imp.this, "quest_intro_2"));
						}
					});
				}
			});
		} else if (!Quest.cur().isCompleted()) {
			if (Quest.cur().oldQuest) {
				//把旧任务状态转换为新版金库任务
				if(Quest.cur().rewardOptions.isEmpty())
					Quest.cur().migrateToNewQuest();
				//旧版存档：所在层没有金库传送点，接取后再点小恶魔即确认进入金库
				VaultEntranceTrigger.showEnterPrompt();
			} else {
				tell(Messages.get(Imp.this, "quest_in_progress"));
			}
		} else {
			//按分数三档评价
			if (Quest.cur().score <= 2000){
				tell(Messages.get(Imp.this, "quest_completed_bad"));
			} else if (Quest.cur().score < 4000){
				tell(Messages.get(Imp.this, "quest_completed_good"));
			} else {
				tell(Messages.get(Imp.this, "quest_completed_great"));
			}
		}

		return true;
	}

	private void tell( String text ) {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndQuest( Imp.this, text ));
			}
		});
	}

	public void flee() {

		yell( Messages.get(this, "cya", Dungeon.hero().name()) );

		destroy();
		sprite.die();
	}

	public static class Quest {

		public static Quest cur() {
			return Dungeons.cur().impQuest;
		}
		public static void store( Bundle bundle ) {
			Dungeons.MAIN().impQuest.storeInBundle( bundle );
		}
		public static void restore( Bundle bundle ) {
			Dungeons.MAIN().impQuest.restoreFromBundle( bundle );
		}
		public static void reset() {
			cur().resetAll();
		}

		public boolean spawned;

		//旧版（4.0 之前）任务存档：旧层没有金库房间/传送点，接取后与小恶魔对话进入金库；
		//迁移只执行一次（见 migrateIfNeeded），迁移后本标记作为“旧档入口方式”继续保留
		public boolean oldQuest = false;

		//新旧任务共用字段
		public boolean given;
		public boolean completed;
		public Item reward; //新版任务中仅用于玩家背包满时暂存奖励

		//新版金库任务的专有字段
		//最终奖励固定 6 件：0 神器（init 时为占位）、1 戒指、2 近战、3 投掷、4 护甲、5 法杖
		public ArrayList<Item> rewardOptions = new ArrayList<>();
		//金库外围装备与奖励分离，按档位独立预生成：每档容量 = 该档极端布局下的最高请求次数（整循环向上取整）
		//预生成量必然大于实际落地量，这是刻意的：查种剪枝允许假阳性，但绝不能漏掉任何可能落地的装备
		public final ArrayList<ArrayList<Item>> vaultEquipOptions = newVaultEquipLists();
		private static ArrayList<ArrayList<Item>> newVaultEquipLists(){
			ArrayList<ArrayList<Item>> lists = new ArrayList<>();
			for (int i = 0; i < VaultLevel.EQUIP_TIERS; i++)
				lists.add(new ArrayList<>());
			return lists;
		}
		public int hazardFreebies; //玩家前两次触发机关不扣分的豁免次数
		public boolean mirrorUsed = false;
		public int score; //非排行榜分数，未计算惩罚

		private void resetAll() {
			spawned = false;
			oldQuest = false;
			given = false;
			completed = false;

			reward = null;
			hazardFreebies = 2;
			mirrorUsed = false;
			score = 0;
			//预生成以列表非空为已生成标记，新开局必须清空
			rewardOptions.clear();
			for (ArrayList<Item> list : vaultEquipOptions) list.clear();
		}

		private static final String NODE        = "demon";

		private static final String SPAWNED     = "spawned";

		private static final String OLD_QUEST   = "old_quest";
		private static final String REWARD      = "reward";

		private static final String GIVEN       = "given";
		private static final String COMPLETED   = "completed";

		private static final String HAZRD_FREEBIES = "hazard_freebies";
		private static final String SCORE       = "score";
		private static final String REWARD_OPTIONS = "reward_options";
		//外围装备每档一个列表，键名后接档位下标
		private static final String VAULT_EQUIP_OPTIONS = "vault_equip_";
		private static final String MIRROR_USED = "mirror_used";

		private void storeInBundle( Bundle bundle ) {

			Bundle node = new Bundle();

			node.put( SPAWNED, spawned );

			//奖励已在 init 预生成，与 spawn 无关，未 spawn 时必须同样入档
			//第 0 位用可序列化的 ArtifactHolder（而非 null）占位，读档后仍保留位置供 genArtifact 填写
			node.put( REWARD_OPTIONS, rewardOptions );
			//外围装备与奖励分开入档：各档容量不同，合表时无法还原档位
			for (int i = 0; i < vaultEquipOptions.size(); i++){
				node.put( VAULT_EQUIP_OPTIONS + i, vaultEquipOptions.get(i) );
			}

			if (spawned) {
				//写入 old_quest 键作为格式标记：新版存档一律 false，旧版存档无此键
				node.put( OLD_QUEST, oldQuest );

				node.put( GIVEN, given );
				node.put( COMPLETED, completed );
				node.put( REWARD, reward );

				node.put( HAZRD_FREEBIES, hazardFreebies );
				node.put( SCORE, score );
				node.put( MIRROR_USED, mirrorUsed );
			}

			bundle.put( NODE, node );
		}

		private void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );

			if (node.isNull()) {
				resetAll();
				return;
			}

			//奖励与 spawn 无关，未 spawn 时同样保留，不能走 reset 清掉
			rewardOptions = node.contains( REWARD_OPTIONS )
					? node.getArrayList( REWARD_OPTIONS, Item.class )
					: new ArrayList<>();
			for (ArrayList<Item> list : vaultEquipOptions) list.clear();
			for (int i = 0; i < vaultEquipOptions.size(); i++){
				String key = VAULT_EQUIP_OPTIONS + i;
				if (node.contains( key )) vaultEquipOptions.get(i).addAll( node.getArrayList( key, Item.class ) );
			}

			spawned = node.getBoolean( SPAWNED );

			if (!spawned){
				//未 spawn：只清 transient 状态
				oldQuest = false;
				given = false;
				completed = false;
				reward = null;
				hazardFreebies = 2;
				mirrorUsed = false;
				score = 0;
				return;
			}

			//新版存档写有 old_quest 键；旧版存档没有该键，读取时视为旧版任务
			oldQuest = !node.contains( OLD_QUEST ) || node.getBoolean( OLD_QUEST );

			if (oldQuest){
				//旧版任务只继承接取/完成状态；旧版奖励（诅咒戒指）作废，改由新版奖励池重新生成
				given = node.getBoolean( GIVEN );
				completed = node.getBoolean( COMPLETED );
				reward = null;
				hazardFreebies = 2;
				mirrorUsed = false;
				score = 0;
				//旧版奖励作废，迁移时按新版流程重新生成
				rewardOptions.clear();

				if (completed){
					//已完成的旧任务视为满分，保证商店仍按 earnedShop() 解锁；无需迁移，也没有金库入口需求
					score = 4000;
					oldQuest = false;
				}
			} else {
				hazardFreebies = node.getInt( HAZRD_FREEBIES );
				mirrorUsed = node.getBoolean( MIRROR_USED );
				score = node.getInt( SCORE );

				reward = (Item)node.get( REWARD );
				given = node.getBoolean( GIVEN );
				completed = node.getBoolean( COMPLETED );
			}
		}

		//新版任务：以 9×9 特殊房间的形式生成于 17~19 层（城市区）
		public ArrayList<Room> spawn( ArrayList<Room> rooms ) {
			if (!spawned && Dungeon.depth() > 16 && Random.Int( 20 - Dungeon.depth() ) == 0) {

				rooms.add(new AmbitiousImpRoom());
				spawned = true;

				reward = null;
				score = 0;

				given = false;
				mirrorUsed = false;

				//装备奖励已在 init 预生成则直接使用，否则（旧存档/读档缺失）现场补生成
				genReward();
				//神器项推迟到 spawn 生成（详见 genArtifact 注释）
				genArtifact();
			}

			return rooms;
		}

		//任务奖励预生成：以独立子种子在存档创建（init）时定型奖励，不消耗主随机流，
		//查种器因此在 init 阶段即可读取奖励内容提前剪枝（挂于 Dungeons.init 末尾）
		public void genReward() {
			if (!rewardOptions.isEmpty()) return;
			//子种子异或常量（ASCII "IMP4"），保证与其他任务的奖励流互不相同
			try (Rng.Sub s = Rng.substream( Dungeon.seed() ^ 0x494D5034L )) {
				//0 位占位：神器项推迟到 spawn 由 genArtifact 填入；
				//用可序列化的 ArtifactHolder（而非 null）占位，未 spawn 的存档读回后位置不会丢失
				rewardOptions.add(new Holder.ArtifactHolder().level(-10));

				//戒指在 init 预生成；与神器同类的极小概率冲突在 genArtifact 中重 roll
				//戒指延迟到击败 BOSS 后才鉴定
				Item ring = Generator.random(Generator.Category.RING);
				ring.level(Random.IntRange(2, 4));
				rewardOptions.add(ring);

				Item mis, wep;
				int random = Random.Int(12);
				//0-5给T5投武，6-11给T4投武，0-3给T4近战，4-7给T5，8-11给T6
				//4-7要覆盖，T5近战与T4和T5投武的组合、T5取值2-4和3-5的组合
				switch (random) {
					default: case 0: case 1: case 2: case 3:
						wep = Generator.random(Generator.Category.WEP_T4);
						wep.level(Random.IntRange(3, 5));
						mis = Generator.random(Generator.Category.MIS_T5);
						mis.level(Random.IntRange(2, 4));
						break;
					case 4://T5高
						wep = Generator.random(Generator.Category.WEP_T5);
						wep.level(Random.IntRange(3, 5));
						mis = Generator.random(Generator.Category.MIS_T5);
						mis.level(Random.IntRange(2, 4));
						break;
					case 5://T5低
						wep = Generator.random(Generator.Category.WEP_T5);
						wep.level(Random.IntRange(2, 4));
						mis = Generator.random(Generator.Category.MIS_T5);
						mis.level(Random.IntRange(2, 4));
						break;
					case 6://T5高
						wep = Generator.random(Generator.Category.WEP_T5);
						wep.level(Random.IntRange(3, 5));
						mis = Generator.random(Generator.Category.MIS_T4);
						mis.level(Random.IntRange(2, 4));
						break;
					case 7://T5低
						wep = Generator.random(Generator.Category.WEP_T5);
						wep.level(Random.IntRange(2, 4));
						mis = Generator.random(Generator.Category.MIS_T4);
						mis.level(Random.IntRange(2, 4));
						break;
					case 8: case 9: case 10: case 11:
						wep = Generator.random(Generator.Category.WEP_T6);
						wep.level(Random.IntRange(2, 4));
						mis = Generator.random(Generator.Category.MIS_T4);
						mis.level(Random.IntRange(2, 4));
						break;
				}
				((Weapon) wep).enchant().identify(false);
				((Weapon) mis).enchant().identify(false);

				rewardOptions.add(wep);
				rewardOptions.add(mis);
				PlateArmor armor = new PlateArmor();
				armor.inscribe().identify(false);
				armor.level(Random.IntRange(2, 4));
				rewardOptions.add(armor);

				Wand w = (Wand) Generator.random(Generator.Category.WAND);
				w.identify(false);
				w.level(Random.IntRange(2, 4));
				w.curCharges = w.maxCharges;
				rewardOptions.add(w);

				for (Item i : rewardOptions)
					if (i != null)
						i.cursed = false;
				genVaultEquip();
			}
		}

		//金库外围装备按档位独立预生成（4 个列表，长度取各档最高请求次数 5/13/13/14）。
		//以第 0 档列表非空作为已生成标记：各档有充足的数量，一旦生成就不可能为空。
		//生成过程自带子种子 push/pop，因此与主随机流和 6 件奖励的掷点完全无关
		public void genVaultEquip() {
			if (!vaultEquipOptions.get(0).isEmpty()) return;

			Item[][] pool = VaultLevel.generateEquipPool();
			for (int i = 0; i < pool.length; i++){
				vaultEquipOptions.get(i).clear();
				for (Item item : pool[i]) vaultEquipOptions.get(i).add(item);
			}
		}

		//神器项在 spawn 时生成：randomArtifact 在神器牌堆耗尽后返回 null 并以戒指替代，
		//其结果依赖运行时牌堆状态，故不纳入 init 预生成
		private void genArtifact() {
			Item artif = Generator.randomArtifact();
			if (artif != null){
				artif.identify(false);
				((Artifact)artif).transferUpgrade(5);
			} else {
				//神器牌堆耗尽则改出一枚戒指
				artif = Generator.random(Generator.Category.RING);
				//戒指延迟到击败 BOSS 后才鉴定
				artif.level(Random.IntRange(2, 4));
			}
			artif.cursed = false;
			rewardOptions.set(0, artif);

			//与 init 预生成的戒指同类则重 roll 戒指
			Item ring = rewardOptions.get(1);
			if (artif.getClass() == ring.getClass()){
				do {
					ring = Generator.random(Generator.Category.RING);
				} while (ring.getClass() == artif.getClass());
				//戒指延迟到击败 BOSS 后才鉴定
				ring.level(Random.IntRange(2, 4));
				ring.cursed = false;
				rewardOptions.set(1, ring);
			}
		}

		public boolean given(){
			return given;
		}

		//旧版任务存档迁移：清除旧版令牌、作废旧版奖励，直接转入新版任务
		//旧版存档没有 9×9 金库房间，故不补传送点：接取后改为与小恶魔对话进入金库（见 interact），
		//出库由 EscapeCrystal 按进库时记下的入口格原路返回
		public void migrateToNewQuest() {

			DwarfToken tokens = Dungeon.hero().belongings.getItem( DwarfToken.class );
			if (tokens != null){
				tokens.detachAll( Dungeon.hero().belongings.backpack );
			}

			//旧版进度作废，按新版流程重新接取
			reward = null;
			given = false;
			completed = false;
			score = 0;
			hazardFreebies = 2;
			mirrorUsed = false;

			//新版奖励按本档种子预生成
			genReward();
			genArtifact();
		}

		//新版任务结算：分数由 EscapeCrystal 计算后传入
		public void complete( int score ){
			completed = true;

			this.score = score;
			if (Dungeon.isSearch()) return;
			Statistics.questScores[3] += score;
			CardSelector.INSTANCE().coolDown(1000);
			Notes.remove( Notes.Landmark.IMP );
		}

		public boolean isCompleted() {
			return spawned && completed;
		}

		//商店解锁条件：分数超过 2000（旧任务完成迁移时按满分处理）
		public boolean earnedShop() {
			return completed && score > 2000;
		}
	}
}
