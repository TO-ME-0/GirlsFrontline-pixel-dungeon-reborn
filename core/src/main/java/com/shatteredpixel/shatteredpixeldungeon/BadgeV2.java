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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBadgeV2;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * 徽章系统 V2：上半部分为徽章贴图索引，下半部分（嵌套类 BadgesGrid）为徽章界面。
 * 界面采用可展开分类行的形式：点击行标题展开/折叠该行的徽章卡片。
 * 解锁条件仍沿用原 Badges 系统。
 * 贴图文件 interfaces/badges1.png，每格 16*16 像素，每行 16 格，共 17 行。
 * 行语义（自上而下）：
 *   1  角色解锁徽章行（解锁角色的角色图标盾徽）
 *   2  物品徽章行
 *   3  钻石获取徽章行
 *   4  击杀徽章行
 *   5  食物徽章行
 *   6  等级徽章行
 *   7  炼药徽章行
 *   8  力量徽章行
 *   9  职业精通徽章行
 *   10 探索徽章行
 *   11 鉴定徽章行
 *   12 死因徽章行
 *   13 boss徽章行
 *   14 结局徽章行
 *   15 挑战徽章行
 *   16 特殊徽章行
 *   17 徽章背景行
 */
public class BadgeV2 {

	public static final int WIDTH = 16; //每行16格
	public static final int SIZE  = 16; //每格16*16像素

	public static final String BADGE_SHEET = "interfaces/badges1.png";

	public static TextureFilm film = new TextureFilm( BADGE_SHEET, SIZE, SIZE );

	//按贴图索引创建一个徽章图像
	public static Image image( int index ){
		Image im = new Image( BADGE_SHEET );
		im.frame( film.get( index ) );
		return im;
	}

	private static int xy(int x, int y){
		x -= 1; y -= 1;
		return x + WIDTH*y;
	}

	private static void assignItemRect( int item, int width, int height ){
		int x = (item % WIDTH) * SIZE;
		int y = (item / WIDTH) * SIZE;
		film.add( item, x, y, x+width, y+height);
	}

	//第一行 角色解锁徽章行（解锁角色的角色图标盾徽）
	private static final int HERO_ICONS             =                               xy(1, 1);   //共16格
	public static final int UNLOCK_WARRIOR          = HERO_ICONS+0;   //解锁UMP45
	public static final int UNLOCK_MAGE             = HERO_ICONS+1;   //解锁G11
	public static final int UNLOCK_ROGUE            = HERO_ICONS+2;   //解锁UMP9
	public static final int UNLOCK_HK416            = HERO_ICONS+3;   //解锁HK416
	public static final int UNLOCK_TYPE561          = HERO_ICONS+4;   //解锁56-1式
	public static final int UNLOCK_GSH18            = HERO_ICONS+5;   //解锁GSH-18
	public static final int UNLOCK_HUNTRESS         = HERO_ICONS+6;   //解锁隼（图标背景占位，贴图后续补充）
	public static final int UNLOCK_DANDELION        = HERO_ICONS+7;   //解锁丹德莱（图标背景占位，贴图后续补充）
	//空闲8格

	//第二行 物品徽章行（物品升级等级）
	private static final int ITEM_BADGES            =                               xy(1, 2);   //共16格
	public static final int ITEM_LEVEL_1            = ITEM_BADGES+0;
	public static final int ITEM_LEVEL_2            = ITEM_BADGES+1;
	public static final int ITEM_LEVEL_3            = ITEM_BADGES+2;
	public static final int ITEM_LEVEL_4            = ITEM_BADGES+3;
	public static final int ITEM_LEVEL_5            = ITEM_BADGES+4;
	//空闲11格

	//第三行 钻石获取徽章行（单局收集钻石）
	private static final int DIAMOND_BADGES         =                               xy(1, 3);   //共16格
	public static final int GOLD_COLLECTED_1        = DIAMOND_BADGES+0;   //250钻石
	public static final int GOLD_COLLECTED_2        = DIAMOND_BADGES+1;   //1000钻石
	public static final int GOLD_COLLECTED_3        = DIAMOND_BADGES+2;   //2500钻石
	public static final int GOLD_COLLECTED_4        = DIAMOND_BADGES+3;   //5000钻石
	public static final int GOLD_COLLECTED_5        = DIAMOND_BADGES+4;   //10000钻石
	//空闲11格

	//第四行 击杀徽章行
	private static final int KILL_BADGES            =                               xy(1, 4);   //共16格
	public static final int MONSTERS_SLAIN_1        = KILL_BADGES+0;
	public static final int MONSTERS_SLAIN_2        = KILL_BADGES+1;
	public static final int MONSTERS_SLAIN_3        = KILL_BADGES+2;
	public static final int MONSTERS_SLAIN_4        = KILL_BADGES+3;
	public static final int MONSTERS_SLAIN_5        = KILL_BADGES+4;
	public static final int NO_MONSTERS_SLAIN       = KILL_BADGES+5;
	public static final int GRIM_WEAPON             = KILL_BADGES+6;
	public static final int KILL_EXCUTION           = KILL_BADGES+7;
	public static final int KILL_SNAKE              = KILL_BADGES+8;
	public static final int KILL_CALC               = KILL_BADGES+9;
	public static final int KILL_DISPORE            = KILL_BADGES+10;
	public static final int KILL_ELPHELT            = KILL_BADGES+11;
	public static final int ELPHELT_WEAPON          = KILL_BADGES+12;
	//空闲3格

	//第五行 食物徽章行
	private static final int FOOD_BADGES            =                               xy(1, 5);   //共16格
	public static final int FOOD_EATEN_1            = FOOD_BADGES+0;
	public static final int FOOD_EATEN_2            = FOOD_BADGES+1;
	public static final int FOOD_EATEN_3            = FOOD_BADGES+2;
	public static final int FOOD_EATEN_4            = FOOD_BADGES+3;
	public static final int FOOD_EATEN_5            = FOOD_BADGES+4;
	//空闲11格

	//第六行 等级徽章行（英雄等级）
	private static final int LEVEL_BADGES           =                               xy(1, 6);   //共16格
	public static final int LEVEL_REACHED_1         = LEVEL_BADGES+0;
	public static final int LEVEL_REACHED_2         = LEVEL_BADGES+1;
	public static final int LEVEL_REACHED_3         = LEVEL_BADGES+2;
	public static final int LEVEL_REACHED_4         = LEVEL_BADGES+3;
	public static final int LEVEL_REACHED_5         = LEVEL_BADGES+4;
	//空闲11格

	//第七行 炼药徽章行
	private static final int ALCHEMY_BADGES         =                               xy(1, 7);   //共16格
	public static final int ITEMS_CRAFTED_1         = ALCHEMY_BADGES+0;
	public static final int ITEMS_CRAFTED_2         = ALCHEMY_BADGES+1;
	public static final int ITEMS_CRAFTED_3         = ALCHEMY_BADGES+2;
	public static final int ITEMS_CRAFTED_4         = ALCHEMY_BADGES+3;
	public static final int ITEMS_CRAFTED_5         = ALCHEMY_BADGES+4;
	//空闲11格

	//第八行 力量徽章行
	private static final int STRENGTH_BADGES        =                               xy(1, 8);   //共16格
	public static final int STRENGTH_ATTAINED_1     = STRENGTH_BADGES+0;
	public static final int STRENGTH_ATTAINED_2     = STRENGTH_BADGES+1;
	public static final int STRENGTH_ATTAINED_3     = STRENGTH_BADGES+2;
	public static final int STRENGTH_ATTAINED_4     = STRENGTH_BADGES+3;
	public static final int STRENGTH_ATTAINED_5     = STRENGTH_BADGES+4;
	//空闲11格

	//第九行 职业精通徽章行（全职业成就+组合技）
	private static final int CLASS_BADGES           =                               xy(1, 9);   //共16格
	public static final int BOSS_SLAIN_1_ALL_CLASSES = CLASS_BADGES+0;   //多面手
	public static final int BOSS_SLAIN_3_ALL_SUBCLASSES = CLASS_BADGES+1;   //全面手
	public static final int VICTORY_ALL_CLASSES     = CLASS_BADGES+2;   //全能大师
	public static final int MASTERY_COMBO           = CLASS_BADGES+3;   //角斗士之怒
	//空闲12格

	//第十行 探索徽章行
	private static final int EXPLORE_BADGES         =                               xy(1, 10);  //共16格
	public static final int PIRANHAS                = EXPLORE_BADGES+0;
	public static final int BAG_BOUGHT_VELVET_POUCH     = EXPLORE_BADGES+1;
	public static final int BAG_BOUGHT_SCROLL_HOLDER    = EXPLORE_BADGES+2;
	public static final int BAG_BOUGHT_POTION_BANDOLIER = EXPLORE_BADGES+3;
	public static final int BAG_BOUGHT_MAGICAL_HOLSTER  = EXPLORE_BADGES+4;
	public static final int ALL_BAGS_BOUGHT         = EXPLORE_BADGES+5;
	public static final int FOUND_RATMOGRIFY        = EXPLORE_BADGES+6;
	//空闲9格

	//第十一行 鉴定徽章行
	private static final int IDENTIFY_BADGES        =                               xy(1, 11);  //共16格
	public static final int ALL_POTIONS_IDENTIFIED  = IDENTIFY_BADGES+0;
	public static final int ALL_SCROLLS_IDENTIFIED  = IDENTIFY_BADGES+1;
	public static final int ALL_WEAPONS_IDENTIFIED  = IDENTIFY_BADGES+2;
	public static final int ALL_ARMOR_IDENTIFIED    = IDENTIFY_BADGES+3;
	public static final int ALL_WANDS_IDENTIFIED    = IDENTIFY_BADGES+4;
	public static final int ALL_RINGS_IDENTIFIED    = IDENTIFY_BADGES+5;
	public static final int ALL_ARTIFACTS_IDENTIFIED= IDENTIFY_BADGES+6;
	public static final int ALL_ITEMS_IDENTIFIED    = IDENTIFY_BADGES+7;
	public static final int IDENTIFY                = IDENTIFY_BADGES+8;
	public static final int DEGRADE_EQUIPMENT       = IDENTIFY_BADGES+9;
	//空闲6格

	//第十二行 死因徽章行
	private static final int DEATH_BADGES           =                               xy(1, 12);  //共16格
	public static final int DEATH_FROM_FIRE         = DEATH_BADGES+0;
	public static final int DEATH_FROM_POISON       = DEATH_BADGES+1;
	public static final int DEATH_FROM_GAS          = DEATH_BADGES+2;
	public static final int DEATH_FROM_HUNGER       = DEATH_BADGES+3;
	public static final int DEATH_FROM_FALLING      = DEATH_BADGES+4;
	public static final int DEATH_FROM_GLYPH        = DEATH_BADGES+5;
	//空闲10格

	//第十三行 boss徽章行
	//TODO: BOSS_SLAIN_3_* 子职业系列（共9个）超出本行容量，待分配到其他行
	private static final int BOSS_BADGES            =                               xy(1, 13);  //共16格
	public static final int BOSS_SLAIN_1            = BOSS_BADGES+0;
	public static final int BOSS_SLAIN_2            = BOSS_BADGES+1;
	public static final int BOSS_SLAIN_3            = BOSS_BADGES+2;
	public static final int BOSS_SLAIN_4            = BOSS_BADGES+3;
	public static final int BOSS_SLAIN_1_WARRIOR    = BOSS_BADGES+4;
	public static final int BOSS_SLAIN_1_MAGE       = BOSS_BADGES+5;
	public static final int BOSS_SLAIN_1_ROGUE      = BOSS_BADGES+6;
	public static final int BOSS_SLAIN_1_HUNTRESS   = BOSS_BADGES+7;
	public static final int BOSS_SLAIN_1_TYPE561    = BOSS_BADGES+8;
	public static final int BOSS_SLAIN_1_GSH18      = BOSS_BADGES+9;
	public static final int BOSS_SLAIN_1_HK416      = BOSS_BADGES+10;
	//空闲5格

	//第十四行 结局徽章行
	private static final int ENDING_BADGES          =                               xy(1, 14);  //共16格
	public static final int VICTORY                 = ENDING_BADGES+0;
	public static final int VICTORY_WARRIOR         = ENDING_BADGES+1;
	public static final int VICTORY_MAGE            = ENDING_BADGES+2;
	public static final int VICTORY_ROGUE           = ENDING_BADGES+3;
	public static final int VICTORY_HUNTRESS        = ENDING_BADGES+4;
	public static final int VICTORY_TYPE561         = ENDING_BADGES+5;
	public static final int VICTORY_GSH18           = ENDING_BADGES+6;
	public static final int VICTORY_HK416           = ENDING_BADGES+7;
	public static final int HAPPY_END               = ENDING_BADGES+8;
	public static final int YASD                    = ENDING_BADGES+9;
	//空闲6格

	//第十五行 挑战徽章行
	private static final int CHALLENGE_BADGES       =                               xy(1, 15);  //共16格
	public static final int CHAMPION_1              = CHALLENGE_BADGES+0;
	public static final int CHAMPION_2              = CHALLENGE_BADGES+1;
	public static final int CHAMPION_3              = CHALLENGE_BADGES+2;
	public static final int CHAMPION_4              = CHALLENGE_BADGES+3;
	//空闲13格

	//第十六行 特殊徽章行（游戏局数+极限挑战+节日）
	private static final int SPECIAL_BADGES         =                               xy(1, 16);  //共16格
	public static final int GAMES_PLAYED_1          = SPECIAL_BADGES+0;
	public static final int GAMES_PLAYED_2          = SPECIAL_BADGES+1;
	public static final int GAMES_PLAYED_3          = SPECIAL_BADGES+2;
	public static final int GAMES_PLAYED_4          = SPECIAL_BADGES+3;
	public static final int GAMES_PLAYED_5          = SPECIAL_BADGES+4;
	public static final int XMAS_GIFT               = SPECIAL_BADGES+5;
	public static final int CRYSTAL_TROPHY          = SPECIAL_BADGES+6;
	//空闲9格

	//第十七行 徽章背景行（各品质底框，供徽章图标叠加使用）
	private static final int BADGE_BACKGROUNDS      =                               xy(1, 17);  //共16格
	//空闲16格（背景框按迭代需要再命名）

	static{
		//全部17*16个格子均为16*16像素
		for (int i = 0; i < WIDTH*17; i++)
			assignItemRect(i, SIZE, SIZE);
	}

	//==========================================================================
	// 徽章界面：可展开分类行 + 4列徽章卡片
	//==========================================================================
	public static class BadgesGrid extends ScrollPane {

		private static final float SCALE = 0.8f; //整个控件统一缩放到原来的80%

		private static final int N_COLS       = 8;    //每行4张卡片
		private static final float HEADER_H   = 12 ;
		private static final float CARD_ICON  = 14 ;  //卡片内图标显示尺寸
		private static final float CARD_H     = 28 ;
		private static final float CAT_GAP    = 1 * SCALE;
		private static final float HEADER_FONT = 6;   //条头标题字号
		private static final float CARD_FONT  = 4 ;    //卡片名称字号
		//条头内部排版偏移
		private static final float ARROW_X   = 6 * SCALE;
		private static final float TITLE_X   = 22 * SCALE;
		private static final float ICON_Y    = 1 * SCALE;
		private static final float NAME_PAD  = 2 * SCALE;

		private final ArrayList<Category> categories = new ArrayList<>();

		public BadgesGrid( boolean global ) {
			super( new Component() );

			//已解锁徽章集合
			ArrayList<Badges.Badge> unlocked = new ArrayList<>(
					global ? Badges.allUnlocked() : Badges.localBadges());

			//全局页：未解锁徽章只显示最低阶占位（与旧版行为一致）
			ArrayList<Badges.Badge> lockedPlaceholders = new ArrayList<>();
			if (global) {
				for (Badges.Badge b : Badges.Badge.values()) {
					if (!unlocked.contains(b) && !Badges.hidden.contains(b)) {
						lockedPlaceholders.add(b);
					}
				}
				Badges.filterHigherIncrementalBadges(lockedPlaceholders);
			}

			for (CatDef catDef : CATEGORY_DEFS) {
				Category category = new Category(catDef, categories.isEmpty());
				for (Def def : catDef.defs) {
					boolean isUnlocked = unlocked.contains(def.badge);
					if (isUnlocked || lockedPlaceholders.contains(def.badge)) {
						category.cards.add(new Card(def, isUnlocked));
					}
				}
				content.add(category);
				categories.add(category);
			}
		}

		@Override
		protected void layout() {
			float pos = 0;
			for (Category category : categories) {
				category.setRect(0, pos, width, category.heightForWidth(width));
				pos += category.heightForWidth(width) + CAT_GAP;
			}
			content.setSize(width, pos);
			super.layout();
		}

		//---------------- 数据定义 ----------------

		private static final class Def {
			final int sprite;
			final Badges.Badge badge;
			Def(int sprite, Badges.Badge badge) {
				this.sprite = sprite;
				this.badge = badge;
			}
		}

		private static Def e( int sprite, Badges.Badge badge ) {
			return new Def(sprite, badge);
		}

		private static final class CatDef {
			final String titleKey;
			final Def[] defs;
			CatDef(String titleKey, Def... defs) {
				this.titleKey = titleKey;
				this.defs = defs;
			}
		}

		private static final ArrayList<CatDef> CATEGORY_DEFS = new ArrayList<>();
		static {
			//1 角色解锁徽章行
			CATEGORY_DEFS.add(new CatDef("hero_v2",
					e(UNLOCK_WARRIOR,   Badges.Badge.UNLOCK_WARRIOR),
					e(UNLOCK_MAGE,      Badges.Badge.UNLOCK_MAGE),
					e(UNLOCK_ROGUE,     Badges.Badge.UNLOCK_ROGUE),
					e(UNLOCK_HK416,     Badges.Badge.UNLOCK_HK416),
					e(UNLOCK_TYPE561,   Badges.Badge.UNLOCK_TYPE561),
					e(UNLOCK_GSH18,     Badges.Badge.UNLOCK_GSH18),
					e(UNLOCK_HUNTRESS,  Badges.Badge.UNLOCK_HUNTRESS),
					e(UNLOCK_DANDELION, Badges.Badge.UNLOCK_DANDELION)));

			//2 物品徽章行
			CATEGORY_DEFS.add(new CatDef("item_v2",
					e(ITEM_LEVEL_1, Badges.Badge.ITEM_LEVEL_1),
					e(ITEM_LEVEL_2, Badges.Badge.ITEM_LEVEL_2),
					e(ITEM_LEVEL_3, Badges.Badge.ITEM_LEVEL_3),
					e(ITEM_LEVEL_4, Badges.Badge.ITEM_LEVEL_4),
					e(ITEM_LEVEL_5, Badges.Badge.ITEM_LEVEL_5)));

			//3 钻石获取徽章行
			CATEGORY_DEFS.add(new CatDef("diamond_v2",
					e(GOLD_COLLECTED_1, Badges.Badge.GOLD_COLLECTED_1),
					e(GOLD_COLLECTED_2, Badges.Badge.GOLD_COLLECTED_2),
					e(GOLD_COLLECTED_3, Badges.Badge.GOLD_COLLECTED_3),
					e(GOLD_COLLECTED_4, Badges.Badge.GOLD_COLLECTED_4),
					e(GOLD_COLLECTED_5, Badges.Badge.GOLD_COLLECTED_5)));

			//4 击杀徽章行
			CATEGORY_DEFS.add(new CatDef("kill_v2",
					e(MONSTERS_SLAIN_1,  Badges.Badge.MONSTERS_SLAIN_1),
					e(MONSTERS_SLAIN_2,  Badges.Badge.MONSTERS_SLAIN_2),
					e(MONSTERS_SLAIN_3,  Badges.Badge.MONSTERS_SLAIN_3),
					e(MONSTERS_SLAIN_4,  Badges.Badge.MONSTERS_SLAIN_4),
					e(MONSTERS_SLAIN_5,  Badges.Badge.MONSTERS_SLAIN_5),
					e(NO_MONSTERS_SLAIN, Badges.Badge.NO_MONSTERS_SLAIN),
					e(GRIM_WEAPON,       Badges.Badge.GRIM_WEAPON),
					e(KILL_EXCUTION,     Badges.Badge.KILL_EXCUTION),
					e(KILL_SNAKE,        Badges.Badge.KILL_SNAKE),
					e(KILL_CALC,         Badges.Badge.KILL_CALC),
					e(KILL_DISPORE,      Badges.Badge.KILL_DISPORE),
					e(KILL_ELPHELT,      Badges.Badge.KILL_ELPHELT),
					e(ELPHELT_WEAPON,    Badges.Badge.ELPHELT_WEAPON)));

			//5 食物徽章行
			CATEGORY_DEFS.add(new CatDef("food_v2",
					e(FOOD_EATEN_1, Badges.Badge.FOOD_EATEN_1),
					e(FOOD_EATEN_2, Badges.Badge.FOOD_EATEN_2),
					e(FOOD_EATEN_3, Badges.Badge.FOOD_EATEN_3),
					e(FOOD_EATEN_4, Badges.Badge.FOOD_EATEN_4),
					e(FOOD_EATEN_5, Badges.Badge.FOOD_EATEN_5)));

			//6 等级徽章行
			CATEGORY_DEFS.add(new CatDef("level_v2",
					e(LEVEL_REACHED_1, Badges.Badge.LEVEL_REACHED_1),
					e(LEVEL_REACHED_2, Badges.Badge.LEVEL_REACHED_2),
					e(LEVEL_REACHED_3, Badges.Badge.LEVEL_REACHED_3),
					e(LEVEL_REACHED_4, Badges.Badge.LEVEL_REACHED_4),
					e(LEVEL_REACHED_5, Badges.Badge.LEVEL_REACHED_5)));

			//7 炼药徽章行
			CATEGORY_DEFS.add(new CatDef("alchemy_v2",
					e(ITEMS_CRAFTED_1, Badges.Badge.ITEMS_CRAFTED_1),
					e(ITEMS_CRAFTED_2, Badges.Badge.ITEMS_CRAFTED_2),
					e(ITEMS_CRAFTED_3, Badges.Badge.ITEMS_CRAFTED_3),
					e(ITEMS_CRAFTED_4, Badges.Badge.ITEMS_CRAFTED_4),
					e(ITEMS_CRAFTED_5, Badges.Badge.ITEMS_CRAFTED_5)));

			//8 力量徽章行
			CATEGORY_DEFS.add(new CatDef("strength_v2",
					e(STRENGTH_ATTAINED_1, Badges.Badge.STRENGTH_ATTAINED_1),
					e(STRENGTH_ATTAINED_2, Badges.Badge.STRENGTH_ATTAINED_2),
					e(STRENGTH_ATTAINED_3, Badges.Badge.STRENGTH_ATTAINED_3),
					e(STRENGTH_ATTAINED_4, Badges.Badge.STRENGTH_ATTAINED_4),
					e(STRENGTH_ATTAINED_5, Badges.Badge.STRENGTH_ATTAINED_5)));

			//9 职业精通徽章行
			CATEGORY_DEFS.add(new CatDef("class_v2",
					e(BOSS_SLAIN_1_ALL_CLASSES,  Badges.Badge.BOSS_SLAIN_1_ALL_CLASSES),
					e(BOSS_SLAIN_3_ALL_SUBCLASSES, Badges.Badge.BOSS_SLAIN_3_ALL_SUBCLASSES),
					e(VICTORY_ALL_CLASSES,       Badges.Badge.VICTORY_ALL_CLASSES),
					e(MASTERY_COMBO,             Badges.Badge.MASTERY_COMBO)));

			//10 探索徽章行
			CATEGORY_DEFS.add(new CatDef("explore_v2",
					e(PIRANHAS,                    Badges.Badge.PIRANHAS),
					e(BAG_BOUGHT_VELVET_POUCH,     Badges.Badge.BAG_BOUGHT_VELVET_POUCH),
					e(BAG_BOUGHT_SCROLL_HOLDER,     Badges.Badge.BAG_BOUGHT_SCROLL_HOLDER),
					e(BAG_BOUGHT_POTION_BANDOLIER,  Badges.Badge.BAG_BOUGHT_POTION_BANDOLIER),
					e(BAG_BOUGHT_MAGICAL_HOLSTER,   Badges.Badge.BAG_BOUGHT_MAGICAL_HOLSTER),
					e(ALL_BAGS_BOUGHT,              Badges.Badge.ALL_BAGS_BOUGHT),
					e(FOUND_RATMOGRIFY,             Badges.Badge.FOUND_RATMOGRIFY)));

			//11 鉴定徽章行
			CATEGORY_DEFS.add(new CatDef("identify_v2",
					e(ALL_POTIONS_IDENTIFIED,   Badges.Badge.ALL_POTIONS_IDENTIFIED),
					e(ALL_SCROLLS_IDENTIFIED,   Badges.Badge.ALL_SCROLLS_IDENTIFIED),
					e(ALL_WEAPONS_IDENTIFIED,   Badges.Badge.ALL_WEAPONS_IDENTIFIED),
					e(ALL_ARMOR_IDENTIFIED,     Badges.Badge.ALL_ARMOR_IDENTIFIED),
					e(ALL_WANDS_IDENTIFIED,     Badges.Badge.ALL_WANDS_IDENTIFIED),
					e(ALL_RINGS_IDENTIFIED,     Badges.Badge.ALL_RINGS_IDENTIFIED),
					e(ALL_ARTIFACTS_IDENTIFIED, Badges.Badge.ALL_ARTIFACTS_IDENTIFIED),
					e(ALL_ITEMS_IDENTIFIED,     Badges.Badge.ALL_ITEMS_IDENTIFIED),
					e(IDENTIFY,                 Badges.Badge.Identify),
					e(DEGRADE_EQUIPMENT,        Badges.Badge.Degrade_Equipment)));

			//12 死因徽章行
			CATEGORY_DEFS.add(new CatDef("death_v2",
					e(DEATH_FROM_FIRE,    Badges.Badge.DEATH_FROM_FIRE),
					e(DEATH_FROM_POISON,  Badges.Badge.DEATH_FROM_POISON),
					e(DEATH_FROM_GAS,     Badges.Badge.DEATH_FROM_GAS),
					e(DEATH_FROM_HUNGER,  Badges.Badge.DEATH_FROM_HUNGER),
					e(DEATH_FROM_FALLING, Badges.Badge.DEATH_FROM_FALLING),
					e(DEATH_FROM_GLYPH,   Badges.Badge.DEATH_FROM_GLYPH)));

			//13 boss徽章行
			CATEGORY_DEFS.add(new CatDef("boss_v2",
					e(BOSS_SLAIN_1,           Badges.Badge.BOSS_SLAIN_1),
					e(BOSS_SLAIN_2,           Badges.Badge.BOSS_SLAIN_2),
					e(BOSS_SLAIN_3,           Badges.Badge.BOSS_SLAIN_3),
					e(BOSS_SLAIN_4,           Badges.Badge.BOSS_SLAIN_4),
					e(BOSS_SLAIN_1_WARRIOR,   Badges.Badge.BOSS_SLAIN_1_WARRIOR),
					e(BOSS_SLAIN_1_MAGE,      Badges.Badge.BOSS_SLAIN_1_MAGE),
					e(BOSS_SLAIN_1_ROGUE,     Badges.Badge.BOSS_SLAIN_1_ROGUE),
					e(BOSS_SLAIN_1_HUNTRESS,  Badges.Badge.BOSS_SLAIN_1_HUNTRESS),
					e(BOSS_SLAIN_1_TYPE561,   Badges.Badge.BOSS_SLAIN_1_TYPE561),
					e(BOSS_SLAIN_1_GSH18,     Badges.Badge.BOSS_SLAIN_1_GSH18),
					e(BOSS_SLAIN_1_HK416,     Badges.Badge.BOSS_SLAIN_1_HK416)));

			//14 结局徽章行
			CATEGORY_DEFS.add(new CatDef("ending_v2",
					e(VICTORY,             Badges.Badge.VICTORY),
					e(VICTORY_WARRIOR,     Badges.Badge.VICTORY_WARRIOR),
					e(VICTORY_MAGE,        Badges.Badge.VICTORY_MAGE),
					e(VICTORY_ROGUE,       Badges.Badge.VICTORY_ROGUE),
					e(VICTORY_HUNTRESS,    Badges.Badge.VICTORY_HUNTRESS),
					e(VICTORY_TYPE561,     Badges.Badge.VICTORY_TYPE561),
					e(VICTORY_GSH18,       Badges.Badge.VICTORY_GSH18),
					e(VICTORY_HK416,       Badges.Badge.VICTORY_HK416),
					e(HAPPY_END,           Badges.Badge.HAPPY_END),
					e(YASD,                Badges.Badge.YASD)));

			//15 挑战徽章行
			CATEGORY_DEFS.add(new CatDef("challenge_v2",
					e(CHAMPION_1, Badges.Badge.CHAMPION_1),
					e(CHAMPION_2, Badges.Badge.CHAMPION_2),
					e(CHAMPION_3, Badges.Badge.CHAMPION_3)));

			//16 特殊徽章行
			CATEGORY_DEFS.add(new CatDef("special_v2",
					e(GAMES_PLAYED_1, Badges.Badge.GAMES_PLAYED_1),
					e(GAMES_PLAYED_2, Badges.Badge.GAMES_PLAYED_2),
					e(GAMES_PLAYED_3, Badges.Badge.GAMES_PLAYED_3),
					e(GAMES_PLAYED_4, Badges.Badge.GAMES_PLAYED_4),
					e(GAMES_PLAYED_5, Badges.Badge.GAMES_PLAYED_5),
					e(XMAS_GIFT, Badges.Badge.XMASGift),
					e(CRYSTAL_TROPHY, Badges.Badge.CRYSTAL_TROPHY)));
		}

		//---------------- 可展开分类 ----------------

		private class Category extends Component {

			final CatDef def;
			final ArrayList<Card> cards = new ArrayList<>();
			boolean expanded;

			private Button header;
			private NinePatch bar;
			private Image arrow;
			private RenderedTextBlock title;

			Category(CatDef def, boolean expanded) {
				this.def = def;
				this.expanded = expanded;

				header = new Button() {
					@Override
					protected void onClick() {
						Category.this.expanded = !Category.this.expanded;
						Sample.INSTANCE.play(Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f);
						//必须显式调用 BadgesGrid.layout()：
						//匿名 Button 内部直接写 layout() 会解析成 Button.layout，
						//而不是外层 Category/BadgesGrid 的 layout，导致展开完全不生效
						BadgesGrid.this.layout();
					}
				};
				bar = Chrome.get(Chrome.Type.BLANK);
				bar.hardlight(0x33373B);
				header.add(bar);

				arrow = Icons.ARROW.get();
				arrow.hardlight(0xDDDDDD);
				arrow.originToCenter();  //必须在设置scale前用原始尺寸确定旋转中心
				arrow.scale.set(SCALE);
				header.add(arrow);

				title = PixelScene.renderTextBlock(HEADER_FONT);
				title.text(Messages.get(BadgeV2.class, def.titleKey));
				title.hardlight(0xFFFFFF);
				header.add(title);

				add(header);
			}

			float heightForWidth( float w ) {
				if (!expanded || cards.isEmpty()) return HEADER_H;
				int rows = (cards.size() + N_COLS - 1) / N_COLS;
				return HEADER_H + 1 + rows * CARD_H;
			}

			@Override
			protected void layout() {
				header.setRect(x, y, width, HEADER_H);

				//条头内部排版
				bar.x = header.x;
				bar.y = header.y;
				bar.size(header.width(), header.height());
				arrow.x = header.x + ARROW_X;
				arrow.y = header.y + (HEADER_H - arrow.height()) / 2f;
				PixelScene.align(arrow);
				title.setPos(header.x + TITLE_X, header.y + (HEADER_H - title.height()) / 2f);
				PixelScene.align(title);

				//三角箭头：收起朝右，展开朝下
				arrow.angle = expanded ? 90f : 0f;

				//移除旧卡片（未挂在本组时remove无副作用）
				for (Card card : cards) {
					remove(card);
				}

				if (expanded && !cards.isEmpty()) {
					float cardW = width / N_COLS;
					float top = header.bottom() + 1;
					for (int i = 0; i < cards.size(); i++) {
						int row = i / N_COLS;
						int col = i % N_COLS;
						Card card = cards.get(i);
						card.setRect(x + col * cardW, top + row * CARD_H, cardW, CARD_H);
						add(card);
					}
				}
			}
		}

		//---------------- 徽章卡片 ----------------

		private class Card extends Button {

			final Def def;
			final boolean unlocked;

			private NinePatch bg;
			private Image icon;
			private RenderedTextBlock name;

			Card(Def def, boolean unlocked) {
				this.def = def;
				this.unlocked = unlocked;

				bg = Chrome.get(Chrome.Type.BLANK);
				bg.hardlight(unlocked ? 0x232629 : 0x17191C);
				add(bg);

				icon = image(def.sprite);
				icon.scale.set(CARD_ICON / SIZE);
				if (!unlocked) icon.brightness(0.4f);
				add(icon);

				name = PixelScene.renderTextBlock(CARD_FONT);
				if (unlocked) {
					name.text(def.badge.title());
					name.hardlight(0xCBCBCB);
				} else {
					name.text(Messages.get(BadgeV2.class, "locked_name_v2"));
					name.hardlight(0x666666);
				}
				name.align(RenderedTextBlock.CENTER_ALIGN);
				add(name);
			}

			@Override
			protected void layout() {
				bg.x = x;
				bg.y = y;
				bg.size(width, height);

				icon.x = x + (width - icon.width()) / 2f;
				icon.y = y + ICON_Y;
				PixelScene.align(icon);

				name.maxWidth((int) (width - NAME_PAD));
				name.align(RenderedTextBlock.CENTER_ALIGN);
				name.setPos(x + (width - name.width()) / 2f, y + CARD_ICON + NAME_PAD);
				PixelScene.align(name);
			}

			@Override
			protected void onClick() {
				Sample.INSTANCE.play(Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f);
				Game.scene().add(new WndBadgeV2(def.sprite, def.badge, unlocked));
			}

			@Override
			protected String hoverText() {
				return unlocked ? def.badge.title() : null;
			}
		}
	}
}
