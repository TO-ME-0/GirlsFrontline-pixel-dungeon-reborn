/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.VaultFlameTraps;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultDM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultDM200;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultElemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultGhoul;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultGolem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultShaman;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultSkeleton;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Torch;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlink;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfClairvoyance;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfDeepSleep;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfFear;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfFlock;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfShock;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.levels.builders.Builder;
import com.shatteredpixel.shatteredpixeldungeon.levels.builders.GridBuilder;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.CityPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault.VaultEntranceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault.VaultFinalRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault.VaultRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault.VaultSimpleEnemyTreasureRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault.VaultTokensRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault.treasure.VaultTreasureRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Starflower;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class VaultLevel extends CityLevel {

	@Override
	public void playLevelMusic() {
		//reborn 的 Assets.Music 无 CITY_TENSE，用 CITY_1 代替
		Music.INSTANCE.play(Assets.Music.CITY_1, true);
	}

	@Override
	protected boolean build() {
		//Level.create 用同一实例重试 build，本轮必须从预生成池头重新切循环；
		//否则 equipCyclesUsed 跨轮累加，第 2 轮必然越界（tier0 每轮固定 4 次请求，容量仅 6）。
		//房间在 paint 期也会调用 createEquipment，故归零必须放在整轮入口而非只包住下面 4 次
		Arrays.fill(equipmentLoot, null);
		Arrays.fill(equipCyclesUsed, 0);
		higherTierIdx = 0;
		lowerTierIdx = 0;

		itemsToSpawn.clear();

		for (int i = 0; i < 4; i++){
			addItemToSpawn(createEquipment(0));
		}
		addItemToSpawn(new Dart());
		for (int i = 0; i < 5; i++){
			addItemToSpawn(createConsumabe(0));
		}
		addItemToSpawn(Generator.randomUsingDefaults(Generator.Category.FOOD));
		addItemToSpawn(Generator.randomUsingDefaults(Generator.Category.FOOD));
		addItemToSpawn(Generator.randomUsingDefaults(Generator.Category.FOOD));

		boolean built = super.build();
		//清空点只能在整层 build/paint 成功之后：RegularLevel 会对 initRooms 做 Random.shuffle，
		//奖励房不保证最后 paint，故不能像 VaultFinalRoom 那样在房间里清理；
		//build 失败会被 Level.create 重试，此时必须保留整表以便下一轮继续装载
		if (built){
			Imp.Quest.cur().rewardOptions.clear();
			for (ArrayList<Item> list : Imp.Quest.cur().vaultEquipOptions) list.clear();
		}
		return built;
	}

	@Override
	protected ArrayList<Room> initRooms() {
		ArrayList<Room> initRooms = new ArrayList<>();

		initRooms.add(roomEntrance = new VaultEntranceRoom());
		//权重与待生成宝藏房列表都按本次建层持有：查种器是多线程并发生成楼层的，
		//静态状态会在线程间被扣减/取空（曾导致 nextRoom 取空表非法越界、房间组合串味）
		float[] roomChances = VaultRoom.setupChances();

		int i = 0;
		while (i < 9){
			VaultRoom r = VaultRoom.createRoom(roomChances);
			i += r.sizeFactor();
			initRooms.add(r);
		}
		initRooms.add( new VaultTokensRoom() );
		initRooms.add( new VaultSimpleEnemyTreasureRoom() );

		ArrayList<Class<? extends VaultTreasureRoom>> treasureRooms = VaultTreasureRoom.generateRoomList();
		for (i = 0; i < 7; i++){
			initRooms.add(VaultTreasureRoom.nextRoom(treasureRooms));
		}

		initRooms.add(new VaultFinalRoom());
		return initRooms;
	}

	//供 EscapeCrystal 计算探索分数时直接调用
	public float levelExplorePercent(int depth) {
		// 每看到 1% 的地块计为 1.25% 探索度，看到 80% 即为 100% 探索
		int seen = 0, total = 0;
		for (int i = 0; i < length; i++){
			if (discoverable[i]) total++;
			if (visited[i]) seen++;
		}
		return Math.min(1, (seen*1.25f)/total);
	}

	//金库房间固定 11x11 且数量多，LoopBuilder 的随机摆放无法容纳（placeRoom 死循环超时），
	// 故从 4.0 原样移植 GridBuilder：房间按网格对齐紧凑摆放，邻接连接率 0.55
	@Override
	protected Builder builder() {
		return new GridBuilder();
	}

	@Override
	protected Painter painter() {
		//reborn 的 RegularPainter 中 hiddenDoorChance 是局部变量无法重写，金库无隐藏门的设定随之舍弃
		return new CityPainter()
				.setWater(0.15f, 12) //水更少且更聚集（年久失修的少量渗漏）
				.setGrass(0.30f, 3) //草略多（随时间蔓生）
				.setTraps(nTraps(), trapClasses(), trapChances());
	}

	@Override
	protected int nTraps() {
		return 0;
	}

	//建层期的装载状态：只发生在楼层生成阶段，无需存入 bundle
	// 这里用数组以便追踪和访问下标
	// 这样可以保证战利品均匀分布
	// 具体来说，每 6 件（一个循环）由各档位生成的装备保证为：
	// 2x 近战武器，1x 护甲，1x 投掷武器，1x 法杖，1x 戒指
	//预生成池按档位独立存放在 Imp.Quest.vaultEquipOptions[tier]，槽位下标 = 循环 * 单循环件数 + 槽位；
	public static final int EQUIP_TIERS = 4;
	Item[][] equipmentLoot = new Item[EQUIP_TIERS][];

	public static final int EQUIP_CYCLE_SIZE = 6;
	//每档最多请求次数（极端布局下的峰值），池只预生成到这里为止：
	//tier0 最多 5 次（build 4 次 + 交替火力房 1 次）；
	//tier1 最多 13 次（宝藏房 3 次 + 两个象限房各 4 次 + 两个中心房各 1 次）；
	//tier2 最多 13 次（宝藏房 2 次 + 单怪宝藏房 1 次 + 象限房 8 次 + 中心房 2 次）；
	//tier3 最多 14 次（宝藏房 2 次 + 令牌房 1 次 + 单怪宝藏房 1 次 + 象限房 8 次 + 中心房 2 次）
	//峰值之后的整循环尾巴不进入池：那部分永远不会被发放，加进来只会给查种剪枝增加多余的假阳性
	public static final int[] EQUIP_PEAK = { 5, 13, 13, 14 };
	//整层最多 10 个循环，而法杖可用 10 种、戒指可用 8 种：法杖/戒指单独去重，
	//用尽一整轮后清空集合，因此最多出现两件同类，既不会三件重复也不会因类别耗尽而死循环
	private static final int USABLE_WANDS = Generator.Category.WAND.classes.length - 3;
	private static final int USABLE_RINGS = Generator.Category.RING.classes.length - 3;
	int higherTierIdx = 0;
	int lowerTierIdx = 0;
	//每档已装载的循环数，仅用于算槽位（循环 * 单循环件数 + 槽位）；
	//容量已按单轮峰值取整，单轮内不会取空；build 重试时由 build() 入口清零
	private final int[] equipCyclesUsed = new int[EQUIP_TIERS];

	//查种器因此在 init 阶段即可把金库外围装备当作宝库槽候选提前剪枝
	public static Item[][] generateEquipPool(){
		Item[][] pool = new Item[EQUIP_TIERS][];
		//近战/投掷/护甲的去重集合跨档位共享：同一次生成不允许出现两件相同的装备（护甲除外）
		HashSet<Class<? extends Item>> poolClasses = new HashSet<>();
		//法杖/戒指按整层计的独立集合，必须在档次循环之外创建，否则同类装备可在不同档位凑满三件
		HashSet<Class<? extends Item>> ringUsed = new HashSet<>();
		HashSet<Class<? extends Item>> wandUsed = new HashSet<>();
		for (int tier = 0; tier < EQUIP_TIERS; tier++){
			//循环必须整段生成（近战×2/护甲/投掷/法杖/戒指是一组），但只保留到该档峰值，
			//最后一个循环用不完的尾部直接丢弃
			pool[tier] = new Item[EQUIP_PEAK[tier]];
			int idx = 0;
			while (idx < EQUIP_PEAK[tier]){
				ArrayList<Item> lootList = generateEquipCycle(tier, poolClasses, ringUsed, wandUsed);
				for (int slot = 0; slot < EQUIP_CYCLE_SIZE && idx < EQUIP_PEAK[tier]; slot++){
					pool[tier][idx++] = lootList.get(slot);
				}
			}
		}
		return pool;
	}

	//只装载被请求的档位：不能顺手重载其它已发空的档位，
	//tier0 池容量恰为 5（build 4 次 + 交替火力房 1 次），发空后再被重载会越界
	private void setupEquipment(int tier){
		if (tierEmpty(tier)){
			loadEquipmentCycle(tier);
		}
	}

	//整档发放完才换下一个循环；只要还剩一件就沿用当前循环，避免跳过战利品
	private boolean tierEmpty(int tier){
		if (equipmentLoot[tier] == null) return true;
		for (int j = 0; j < equipmentLoot[tier].length; j++){
			if (equipmentLoot[tier][j] != null) return false;
		}
		return true;
	}

	//按固定槽位切出一个循环。池已按各档峰值预生成，正常布局下必然够用，故不设现场生成兜底；
	//列表为空只可能是旧档未写入或读档后未初始化，此时按同种子补齐。
	//最后一个循环可能不足 6 件（池只保留到峰值），按实际剩余件数切
	private void loadEquipmentCycle(int tier){
		ArrayList<Item> pool = Imp.Quest.cur().vaultEquipOptions.get(tier);
		if (pool.isEmpty()){
			Imp.Quest.cur().genVaultEquip();
			pool = Imp.Quest.cur().vaultEquipOptions.get(tier);
		}
		int cycle = equipCyclesUsed[tier];
		int start = cycle * EQUIP_CYCLE_SIZE;
		int count = Math.min(EQUIP_CYCLE_SIZE, pool.size() - start);
		Item[] cycleLoot = new Item[count];
		for (int slot = 0; slot < count; slot++){
			cycleLoot[slot] = pool.get(start + slot);
		}
		equipmentLoot[tier] = cycleLoot;
		equipCyclesUsed[tier] = cycle + 1;
	}

	private static ArrayList<Item> generateEquipCycle(int lootTier, HashSet<Class<? extends Item>> generatedClasses,
			HashSet<Class<? extends Item>> ringUsed, HashSet<Class<? extends Item>> wandUsed){
		//lootTier为item[][]的第一个下标，取值范围0-3
		//仅lootTier == 3时，低档次高强化项能产出且固定产出+4的t4近战武器。其余+4及以上仅最终奖励房产出。

		ArrayList<Item> lootList = new ArrayList<>();

		Item loot;
		//第一件武器（较低档次，更多强化）
		do {
			switch (lootTier) {
				default:
				case 0:
					loot = Generator.randomUsingDefaults(Generator.Category.WEP_T1);
					if (loot.level() == 0)
						loot = Generator.randomUsingDefaults(Generator.Category.WEP_T1);
					//1阶有一次无等级重roll
					break;
                case 1:
                    loot = Generator.randomUsingDefaults(Generator.Category.WEP_T2);
					break;
                case 2:
					loot = Generator.randomUsingDefaults(Generator.Category.WEP_T3);
					break;
				case 3:
					loot = Generator.randomUsingDefaults(Generator.Category.WEP_T4);
					break;
			}
		//T2 武器允许重复，因为生成量很大
		} while (!generatedClasses.add(loot.getClass()));

		//2-4阶至少+2
		if (lootTier != 0) loot.level(lootTier+1);
		//1阶补1级
		else loot.upgrade();
		if (Random.Int(3) >= lootTier) ((Weapon) loot).enchant(null);
		else ((Weapon) loot).enchant();
		lootList.add(loot);

		//第二件武器（较高档次，更少强化）
		do {
			switch (lootTier) {
				default:
				case 0:
					loot = Generator.randomUsingDefaults(Generator.Category.WEP_T3);
					break;
				case 1:
					loot = Generator.randomUsingDefaults(Generator.Category.WEP_T4);
					break;
				case 2:
					loot = Generator.randomUsingDefaults(Generator.Category.WEP_T5);
					break;
				case 3:
					loot = Generator.randomUsingDefaults(Generator.Category.WEP_T6);
					break;
			}
		} while (!generatedClasses.add(loot.getClass()));

		if (loot.trueLevel() < lootTier) loot.level(lootTier);
		if (Random.Int(3) >= lootTier) ((Weapon) loot).enchant(null);
		else ((Weapon) loot).enchant();
		lootList.add(loot);

		//投掷武器（与第二件武器同级同强化）
		do {
			switch (lootTier) {
				default:
				case 0:
					loot = Generator.randomUsingDefaults(Generator.Category.MIS_T2);
					break;
				case 1:
					loot = Generator.randomUsingDefaults(Generator.Category.MIS_T3);
					break;
				case 2:
					loot = Generator.randomUsingDefaults(Generator.Category.MIS_T4);
					break;
				case 3:
					loot = Generator.randomUsingDefaults(Generator.Category.MIS_T5);
					break;
			}
		} while (!generatedClasses.add(loot.getClass()));

		if (loot.trueLevel() < lootTier) loot.level(lootTier);
		if (Random.Int(3) >= lootTier) ((Weapon) loot).enchant(null);
		else ((Weapon) loot).enchant();
		lootList.add(loot);

		//护甲（与第二件武器同级同强化）
		switch (lootTier) {
			default:
			case 0:
				loot = new LeatherArmor();
				break;
			case 1:
				loot = new MailArmor();
				break;
			case 2:
				loot = new ScaleArmor();
				break;
			case 3:
				loot = new PlateArmor();
				break;
		}
		//跳过重复检查，目前每档也只有 1 件护甲
		generatedClasses.add(loot.getClass());

		if (loot.trueLevel() < lootTier) loot.level(lootTier);
		if (Random.Int(3) >= lootTier) ((Armor) loot).inscribe(null);
		else ((Armor) loot).inscribe();
		lootList.add(loot);

		//法杖（部分法杖被禁用）：法杖使用独立集合，集合装满（本轮可用种类全部取过）后立即清空重新计数，
		//因此最多出现两根同类法杖，且永远不会因类别耗尽而死循环；尺寸单调递增，故用 == 判断装满
		do {
			loot = Generator.randomUsingDefaults(Generator.Category.WAND);
		} while (loot instanceof WandOfRegrowth || loot instanceof WandOfTransfusion
				|| loot instanceof WandOfCorruption
				|| !wandUsed.add(loot.getClass()));
		if (wandUsed.size() == USABLE_WANDS) wandUsed.clear();

		if (loot.trueLevel() < lootTier) loot.level(lootTier);
		((Wand)loot).curCharges = ((Wand)loot).maxCharges;
		lootList.add(loot);

		//戒指（部分戒指被禁用）：与法杖同一规则，独立集合并于装满后清空
		do {
			loot = Generator.randomUsingDefaults(Generator.Category.RING);
		} while (loot instanceof RingOfWealth
				|| loot instanceof RingOfMight || loot instanceof RingOfForce
				|| !ringUsed.add(loot.getClass()));
		if (ringUsed.size() == USABLE_RINGS) ringUsed.clear();

		if (loot.trueLevel() < lootTier) loot.level(lootTier);
		lootList.add(loot);

		return lootList;
	}

	public Item createEquipment(int lootTier) {

		//确保所请求档位有战利品列表
		setupEquipment(lootTier);

		int idx;
		if (lootTier >= 2){
			idx = higherTierIdx;
			if (idx >= equipmentLoot[lootTier].length){
				idx = 0;
				higherTierIdx = 0;
			} else {
				higherTierIdx++;
			}

		} else {
			idx = lowerTierIdx;
			if (idx >= equipmentLoot[lootTier].length){
				idx = 0;
				lowerTierIdx = 0;
			} else {
				lowerTierIdx++;
			}

		}

		while(equipmentLoot[lootTier][idx] ==  null){
			idx++;
			if (idx >= equipmentLoot[lootTier].length){
				idx = 0;
			}
		}

		Item loot = equipmentLoot[lootTier][idx];
		equipmentLoot[lootTier][idx] = null;

		loot.cursed = false;
		if (loot instanceof Ring){
			//金库中的戒指按击败敌人数逐渐鉴定，见 Mob.destroy()
			loot.levelKnown = loot.cursedKnown = true;
		} else {
			loot.identify(false);
		}

		return loot;
	}

	//只发生在楼层生成阶段，无需存入 bundle
	ArrayList<ArrayList<Item>> consumableLoot = new ArrayList<>();

	private void setupConsumables(){
		if (consumableLoot.isEmpty()) {
			consumableLoot.add(new ArrayList<Item>());
			consumableLoot.add(new ArrayList<Item>());
			consumableLoot.add(new ArrayList<Item>());
			consumableLoot.add(new ArrayList<Item>());
		}

		//T0，地面掉落物
		if (consumableLoot.get(0).isEmpty()){
			consumableLoot.get(0).addAll(Arrays.asList(
					Reflection.newInstance(Random.oneOf(PotionOfFrost.class, PotionOfLevitation.class)),
					Reflection.newInstance(Random.oneOf(Firebloom.Seed.class, Icecap.Seed.class, Stormvine.Seed.class)),
					Reflection.newInstance(Random.oneOf(ScrollOfMirrorImage.class, ScrollOfTeleportation.class)),
					Reflection.newInstance(Random.oneOf(StoneOfFlock.class, StoneOfShock.class, StoneOfFear.class))));
			Collections.shuffle(consumableLoot.get(0));
			//每档的第一件总是治疗药水（T3 除外，它随机带一瓶）
			consumableLoot.get(0).add(0, new PotionOfHealing());
		}

		//T1
		if (consumableLoot.get(1).isEmpty()){
			consumableLoot.get(1).addAll(Arrays.asList(
					Reflection.newInstance(Random.oneOf(PotionOfToxicGas.class, PotionOfParalyticGas.class)),
					Reflection.newInstance(Random.oneOf(Firebloom.Seed.class, Sorrowmoss.Seed.class, Blindweed.Seed.class)),
					Reflection.newInstance(Random.oneOf(ScrollOfRecharging.class, ScrollOfTerror.class)),
					Reflection.newInstance(Random.oneOf(StoneOfDeepSleep.class, StoneOfClairvoyance.class, StoneOfAggression.class))));
			Collections.shuffle(consumableLoot.get(1));
			consumableLoot.get(1).add(0, new PotionOfHealing());
		}

		//T2
		if (consumableLoot.get(2).isEmpty()){
			consumableLoot.get(2).addAll(Arrays.asList(
					Reflection.newInstance(Random.oneOf(PotionOfMindVision.class, PotionOfLiquidFlame.class)),
					Reflection.newInstance(Random.oneOf(Swiftthistle.Seed.class, Sungrass.Seed.class)),
					Reflection.newInstance(Random.oneOf(ScrollOfLullaby.class, ScrollOfMagicMapping.class)),
					Reflection.newInstance(Random.oneOf(StoneOfBlast.class, StoneOfBlink.class))));
			Collections.shuffle(consumableLoot.get(2));
			consumableLoot.get(2).add(0, new PotionOfHealing());
		}

		//T3
		if (consumableLoot.get(3).isEmpty()){
			consumableLoot.get(3).addAll(Arrays.asList(
					Reflection.newInstance(Random.oneOf(PotionOfExperience.class, PotionOfInvisibility.class)),
					Reflection.newInstance(Random.oneOf(Earthroot.Seed.class, Starflower.Seed.class)),
					Reflection.newInstance(Random.oneOf(ScrollOfRetribution.class, ScrollOfTransmutation.class)),
					Reflection.newInstance(Random.oneOf(StoneOfEnchantment.class, StoneOfAugmentation.class)),
					new PotionOfHealing()));
			Collections.shuffle(consumableLoot.get(3));
		}
	}

	public Item createConsumabe(int tier){
		if (consumableLoot.isEmpty() || consumableLoot.get(tier).isEmpty()){
			setupConsumables();
		}
		Item result = consumableLoot.get(tier).remove(0);
		return result;
	}

	static Class<?extends Item>[] T3SolveItems = new Class[]{
			StoneOfBlink.class,
			PotionOfInvisibility.class
	};

	public Item findT3SolveItem(){
		Random.shuffle(T3SolveItems);
		Item result = null;
		for (Class<?extends Item> itemCls : T3SolveItems){
			result = findPrizeItem(itemCls);
			if (result != null){
				return result;
			}
		}
		return null;
	}

	static Class<?extends Item>[] T2SolveItems = new Class[]{
			StoneOfBlink.class,
			PotionOfInvisibility.class
	};

	public Item findT2SolveItem(){
		Random.shuffle(T2SolveItems);
		Item result = null;
		for (Class<?extends Item> itemCls : T2SolveItems){
			result = findPrizeItem(itemCls);
			if (result != null){
				return result;
			}
		}
		//找不到 T2 解法道具时，尝试改放 T3 解法道具（而不是让它沦为地面掉落物）
		return findT3SolveItem();
	}

	public static Class<?extends Mob>[] T1Mobs = new Class[]{
			VaultSkeleton.class,
			VaultDM100.class
	};

	public static Class<?extends Mob>[] T2Mobs = new Class[]{
			VaultShaman.class,
			VaultDM200.class,
			VaultGhoul.class //仅单独出现时
	};

	public static Class<?extends Mob>[] T3Mobs = new Class[]{
			//多于一只时为金库食尸鬼
			VaultElemental.class,
			VaultGolem.class
	};

	private ArrayList<Class<?extends Mob>> mobsToSpawn = new ArrayList<>();

	@Override
	public Mob createMob() {
		if (mobsToSpawn.isEmpty()){
			//轮换为每档 3 只怪
			Collections.addAll(mobsToSpawn, T1Mobs);
			mobsToSpawn.add(Random.oneOf(T1Mobs));
			Collections.addAll(mobsToSpawn, T2Mobs);
			Collections.addAll(mobsToSpawn, T3Mobs);
			mobsToSpawn.add(Random.oneOf(T3Mobs));
			Random.shuffle(mobsToSpawn);
		}
		Class<? extends Mob> cls = mobsToSpawn.remove(0);
		if (cls == VaultElemental.class){
			cls = VaultElemental.random();
		}
		return Reflection.newInstance(cls);
	}

	//很重要：尽量保留无法在某处（如走廊）生成的怪
	public void returnMob( Class<?extends Mob> cls){
		mobsToSpawn.add(0, cls);
	}

	@Override
	protected void createMobs() {
		//怪的生成由各个房间自行处理
	}

	@Override
	public void occupyCell(Char ch) {
		super.occupyCell(ch);
		//任务完成后的离开由 EscapeCrystal 的 RETURN 逻辑负责，此处不再强制踢出玩家
		if (ch == Dungeon.hero()) {
			Room r = room(ch.pos);
			if (r instanceof VaultFinalRoom){
				((VaultFinalRoom) r).processHeroStep((Hero) ch);
			}
		}
	}

	public Actor addRespawner() {
		return null;
	}

	@Override
	protected void createItems() {
		//复制自 super.createItems
		for (Item item : itemsToSpawn) {
			int cell = randomDropCell();
			drop( item, cell ).type = Heap.Type.HEAP;
			if (map[cell] == Terrain.HIGH_GRASS || map[cell] == Terrain.FURROWED_GRASS) {
				map[cell] = Terrain.GRASS;
				losBlocking[cell] = false;
			}
		}

		//若开启“深入黑暗”挑战则额外生成 2 支火把。用独立随机种子避免影响楼层生成的其他部分
		if (Dungeon.isChallenged(Challenges.DARKNESS)){
			Random.pushGenerator(Random.Long());
			for (int i = 0; i < 2; i++){
				int cell = randomDropCell();
				drop( new Torch(), cell ).type = Heap.Type.HEAP;
				if (map[cell] == Terrain.HIGH_GRASS || map[cell] == Terrain.FURROWED_GRASS) {
					map[cell] = Terrain.GRASS;
					losBlocking[cell] = false;
				}
			}
			Random.popGenerator();
		}

	}

	@Override
	public void seal() {
		if (!locked) {
			locked = true;
			//这里不加锁定楼层 debuff
			// 无需限制回复，且玩家确实可以离开
		}
	}

	@Override
	public void unseal() {
		super.unseal();
		for (Room r : rooms){
			if (r instanceof VaultFinalRoom){
				((VaultFinalRoom) r).unlock();
			}
		}
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
			//reborn 的 Music 无 fadeOut，直接 end()
				Music.INSTANCE.end();
			}
		});
	}

	public static class VaultFlameTrap extends Trap {

		{
			color = BLACK;
			shape = DOTS;

			canBeHidden = false;
			active = false;
		}

		@Override
		public void activate() {
			//无事发生，这个陷阱只是装饰且永远处于关闭状态
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc");
		}

		public static void setupTrap(Level level, int cell, int initialCD, int afterTriggerCD, int triggers){
			//reborn 的 Blob.seed 带 level 的签名为五参（末尾 strict），这里用 false
			VaultFlameTraps traps = Blob.seed(0, 0, VaultFlameTraps.class, level, false);
			traps.curCooldowns[cell] = initialCD;
			traps.afterTriggerCooldowns[cell] = afterTriggerCD;
			traps.triggersAfterCooldown[cell] = triggers;
			level.setTrap(new VaultLevel.VaultFlameTrap().reveal(), cell);
			Painter.set(level, cell, Terrain.INACTIVE_TRAP);
		}

	}

}
