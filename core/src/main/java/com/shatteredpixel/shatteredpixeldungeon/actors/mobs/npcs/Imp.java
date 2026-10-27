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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Manti;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Striker;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardSelector;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.AmbitiousImpRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.P7Sprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.dialog.quest.P7_Plot_L1;
import com.shatteredpixel.shatteredpixeldungeon.ui.dialog.quest.P7_Plot_L2;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDialog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndImp;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;

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
		if (Quest.cur().isCompleted() && !Quest.cur().oldQuest && Quest.cur().score > 2000
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

		//旧版杀怪集令牌任务（仅旧存档存在，逻辑保持改版原样：P7 立绘剧情）
		if (Quest.cur().oldQuest) {
			if (Quest.cur().given) {

				DwarfToken tokens = Dungeon.hero().belongings.getItem( DwarfToken.class );
				if (tokens != null && (tokens.quantity() >= 5 || (!Quest.cur().alternative && tokens.quantity() >= 4))) {
					Game.runOnRenderThread(new Callback() {
						@Override
						public void call() {
							GameScene.show( new WndImp( Imp.this, tokens ) );
						}
					});
				} else {
					Game.runOnRenderThread(new Callback() {
						@Override
						public void call() {
							GameScene.show( (new WndDialog(Quest.cur().alternative ? new P7_Plot_L1.End() : new P7_Plot_L2.End()) ));
						}
					});
				}
			} else {
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( (new WndDialog(Quest.cur().alternative ? new P7_Plot_L1() : new P7_Plot_L2()) ));
					}
				});
				Quest.cur().given = true;
				Quest.cur().completed = false;
				Notes.add( Notes.Landmark.IMP );
			}
		} else {
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
				tell(Messages.get(Imp.this, "quest_in_progress"));
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
		public static Quest main() {
			return Dungeons.MAIN().impQuest;
		}

		public boolean spawned;

		//旧版（4.0 之前）杀怪任务的专有字段；旧存档读取时 oldQuest=true
		public boolean oldQuest = false;
		public boolean alternative; //true= Striker, false = Manti

		//新旧任务共用字段
		public boolean given;
		public boolean completed;
		public Item reward; //新版任务中仅用于玩家背包满时暂存奖励

		//新版金库任务的专有字段
		public ArrayList<Item> rewardOptions = new ArrayList<>();
		public int hazardFreebies; //玩家前两次触发机关不扣分的豁免次数
		public boolean mirrorUsed = false;
		public int score; //非排行榜分数，未计算惩罚

		public void reset() {
			spawned = false;
			oldQuest = false;
			given = false;
			completed = false;

			reward = null;
			hazardFreebies = 2;
			mirrorUsed = false;
			score = 0;
			//预生成以 rewardOptions 非空为已生成标记，新开局必须清空
			rewardOptions.clear();
		}

		private static final String NODE        = "demon";

		private static final String SPAWNED     = "spawned";

		private static final String OLD_QUEST   = "old_quest";
		private static final String ALTERNATIVE = "alternative";
		private static final String REWARD      = "reward";

		private static final String GIVEN       = "given";
		private static final String COMPLETED   = "completed";

		private static final String HAZRD_FREEBIES = "hazard_freebies";
		private static final String SCORE       = "score";
		private static final String REWARD_OPTIONS = "reward_options";
		private static final String MIRROR_USED = "mirror_used";

		public void storeInBundle( Bundle bundle ) {

			Bundle node = new Bundle();

			node.put( SPAWNED, spawned );

			if (spawned) {
				//写入 old_quest 键以区分新旧任务（旧存档无此键，读取时默认为旧任务）
				node.put( OLD_QUEST, oldQuest );
				node.put( ALTERNATIVE, alternative );

				node.put( GIVEN, given );
				node.put( COMPLETED, completed );
				node.put( REWARD, reward );

				node.put( HAZRD_FREEBIES, hazardFreebies );
				node.put( SCORE, score );
				node.put( REWARD_OPTIONS, rewardOptions );
				node.put( MIRROR_USED, mirrorUsed );
			}

			bundle.put( NODE, node );
		}

		public void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );

			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {

				if (node.contains( OLD_QUEST )){
					oldQuest = node.getBoolean( OLD_QUEST );
				} else {
					//旧存档没有 old_quest 键，一律视为旧版杀怪任务
					oldQuest = true;
				}
				if (oldQuest){
					alternative	= node.getBoolean( ALTERNATIVE );
					score = 0;
					rewardOptions.clear();
					mirrorUsed = false;
				} else {
					alternative = false;
					hazardFreebies = node.getInt( HAZRD_FREEBIES );
					mirrorUsed = node.getBoolean( MIRROR_USED );
					score = node.getInt( SCORE );
					rewardOptions = new ArrayList<>((Collection<Item>) (Collection<?>) node.getCollection( REWARD_OPTIONS ));
				}

				reward = (Item)node.get( REWARD );

				given = node.getBoolean( GIVEN );
				completed = node.getBoolean( COMPLETED );
			}
		}

		//新版任务：以 9×9 特殊房间的形式生成于 17~19 层（城市区）
		public ArrayList<Room> spawn( ArrayList<Room> rooms ) {
			if (!spawned && Dungeon.cur().depth > 16 && Random.Int( 20 - Dungeon.cur().depth ) == 0) {

				rooms.add(new AmbitiousImpRoom());
				spawned = true;

				oldQuest = false;
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
			Random.pushGenerator( Dungeon.cur().seed ^ 0x494D5034L );
			try {
				//0 位占位：神器项推迟到 spawn 由 genArtifact 填入
				rewardOptions.add(null);

				//戒指在 init 预生成；与神器同类的极小概率冲突在 genArtifact 中重 roll
				//戒指延迟到击败 BOSS 后才鉴定
				Item ring = Generator.random(Generator.Category.RING);
				ring.level(Random.IntRange(2, 4));
				rewardOptions.add(ring);

				if (Random.Int(2) == 0) {
					Weapon wep = (Weapon) Generator.random(Generator.Category.WEP_T5);
					wep.enchant().identify(false);
					wep.level(Random.IntRange(2, 4));
					rewardOptions.add(wep);
					Weapon mis = (Weapon) Generator.random(Generator.Category.MIS_T4);
					mis.enchant().identify(false);
					mis.level(Random.IntRange(3, 5));
					rewardOptions.add(mis);
				} else {
					Weapon mis = (Weapon) Generator.random(Generator.Category.MIS_T5);
					mis.enchant().identify(false);
					mis.level(Random.IntRange(2, 4));
					rewardOptions.add(mis);
					Weapon wep = (Weapon) Generator.random(Generator.Category.WEP_T4);
					wep.enchant().identify(false);
					wep.level(Random.IntRange(3, 5));
					rewardOptions.add(wep);
				}
				PlateArmor armor = new PlateArmor();
				armor.inscribe().identify(false);
				armor.level(Random.IntRange(2, 4));
				rewardOptions.add(armor);

				Wand w = (Wand) Generator.random(Generator.Category.WAND);
				w.identify(false);
				w.level(Random.IntRange(2, 4));
				w.curCharges = w.maxCharges;
				rewardOptions.add(w);

				for (Item i : rewardOptions){
					if (i != null) i.cursed = false;
				}
			} finally {
				Random.popGenerator();
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

		public boolean isOld(){
			return oldQuest;
		}

		//旧版任务的令牌掉落：仅旧存档有效；改版以 Striker/Manti 替代原版 Monk/Golem
		public void oldProcess( Mob mob ) {
			if (spawned && oldQuest && given && !completed && Dungeon.cur().depth != 20) {
				if ((alternative && mob instanceof Striker) ||
					(!alternative && mob instanceof Manti)) {

					Dungeon.level.drop( new DwarfToken(), mob.pos ).sprite.drop();
				}
			}
		}

		//旧版任务交付：固定记满分
		public void oldComplete() {
			reward = null;
			completed = true;

			if (Dungeons.cur().isSearch) return;
			Statistics.questScores[3] = 4000;
			CardSelector.INSTANCE().coolDown(1000);
			Notes.remove( Notes.Landmark.IMP );
		}

		//新版任务结算：分数由 EscapeCrystal 计算后传入
		public void complete( int score ){
			completed = true;

			this.score = score;
			if (Dungeons.cur().isSearch) return;
			Statistics.questScores[3] += score;
			CardSelector.INSTANCE().coolDown(1000);
			Notes.remove( Notes.Landmark.IMP );
		}

		public boolean isCompleted() {
			return spawned && completed;
		}

		//商店解锁条件：旧任务完成，或新任务分数超过 2000
		public boolean earnedShop() {
			return completed && (oldQuest || score > 2000);
		}
	}
}
