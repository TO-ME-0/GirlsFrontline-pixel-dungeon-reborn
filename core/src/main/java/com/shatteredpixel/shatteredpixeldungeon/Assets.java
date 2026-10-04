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

public class Assets {
	//unsed 未使用
	public static final String ELID         = "elid.png";
	public static final String TOW 			= "tow.png";

	public static class Effects {
		public static final String EFFECTS      = "effects/effects.png";
		public static final String FIREBALL     = "effects/fireball.png";
		public static final String SPECKS       = "effects/specks.png";
		public static final String SPELL_ICONS  = "effects/spell_icons.png";
		public static final String SHAKCO       = "effects/shockwave.png";
		public static final String TEXT_ICONS 	= "effects/text_icons.png";
	}

	public static class Environment {
		public static final String TERRAIN_FEATURES = "environment/terrain_features_1.png";

		public static final String VISUAL_GRID 		= "environment/visual_grid.png";
		public static final String PLAY_SNAKE  		= "environment/play_snake1.png";
		public static final String TILES_ZERO_LEVEL	="environment/tiles_zero_level.png";
		public static final String TILES_SEWERS    	="environment/tiles_sewers.png";
		public static final String TILES_PRISON    	="environment/tiles_prison.png";
		public static final String TILES_CAVES     	="environment/tiles_caves.png";
		public static final String TILES_CAVES_BOSS	="environment/tiles_caves_boss.png";
		public static final String TILES_CITY      	="environment/tiles_city.png";
		public static final String TILES_RECAVES   	="environment/tiles_recaves.png";
		public static final String TILES_HALLS     	="environment/tiles_halls.png";
        public static final String CHESS         	="environment/Chess.png";
		
		public static final String WATER_SEWERS 	= "environment/water0.png";
		public static final String WATER_PRISON 	= "environment/water1.png";
		public static final String WATER_CAVES  	= "environment/water2.png";
		public static final String WATER_CITY   	= "environment/water3.png";
		public static final String WATER_HALLS  	= "environment/water4.png";

		//need Organize 需要整理
		public static final String WEAK_FLOOR       = "environment/custom_tiles/weak_floor.png";
		public static final String ZERO_LEVEL       = "environment/custom_tiles/zero_level.png";
		public static final String ROOM             = "environment/custom_tiles/room.png";
		public static final String ROOM404_1        = "environment/custom_tiles/room404-1.png";
		public static final String COFFEE_ROOM      = "environment/custom_tiles/CoffeeRoom.png";
		public static final String COFFEE_ROOM_1    = "environment/custom_tiles/CoffeeRoom-1.png";
		public static final String SEWER_BOSS       = "environment/custom_tiles/sewer_boss.png";
		public static final String PRISON_QUEST     = "environment/custom_tiles/prison_quests.png";
		public static final String PRISON_EXIT      = "environment/custom_tiles/prison_exit.png";
		public static final String CAVES_BOSS       = "environment/custom_tiles/caves_boss.png";
		public static final String CITY_BOSS        = "environment/custom_tiles/city_boss.png";
		public static final String HALLS_SP         = "environment/custom_tiles/halls_special.png";
		public static final String FORWARD_CAMP     = "environment/custom_tiles/Forward_Camp.png";
		public static final String FORWARD_CAMP_1   = "environment/custom_tiles/Forward_Camp-1.png";
	}
	
	//TODO include other font assets here? Some are platform specific though... 一些平台特定的字体资产
	public static class Fonts {
		public static final String PIXELFONT= "fonts/pixel_font.png";
	}

	public static class Interfaces {
		public static final String SAVESLOT	= "interfaces/save_slot_ui.png";
		public static final String PORTRAIT1= "interfaces/portrait1.png";
		
		public static final String ARCS_BG  = "interfaces/arcs1.png";
		public static final String ARCS_FG  = "interfaces/arcs2.png";

		public static final String BANNERS  = "interfaces/banners.png";
		public static final String BADGES   = "interfaces/badges.png";
		public static final String LOCKED   = "interfaces/locked_badge.png";

		public static final String CHROME   = "interfaces/chrome.png";
		public static final String CARD     = "interfaces/card.png";	//卡牌合并
		public static final String ICONS    = "interfaces/icons.png";
		public static final String STATUS   = "interfaces/status_pane.png";
		public static final String STATUS_1 = "interfaces/status_pane1.png";
		public static final String STATUS_2 = "interfaces/status_pane2.png";
		public static final String MENU     = "interfaces/menu_pane.png";
		public static final String MENU_BTN = "interfaces/menu_button.png";
		public static final String TOOLBAR  = "interfaces/toolbar.png";
		public static final String TOOLBAR_1 = "interfaces/toolbar1.png";
		public static final String TOOLBAR_2 = "interfaces/toolbar2.png";
		public static final String SHADOW   = "interfaces/shadow.png";
		public static final String BOSSHP   = "interfaces/boss_hp.png";

		public static final String SURFACE  = "interfaces/surface.png";

		public static final String LOADING_SEWERS   = "interfaces/loading_sewers.png";
		public static final String LOADING_PRISON   = "interfaces/loading_prison.png";
		public static final String LOADING_CAVES    = "interfaces/loading_caves.png";
		public static final String LOADING_CITY     = "interfaces/loading_city.png";
		public static final String LOADING_HALLS    = "interfaces/loading_halls.png";

		public static final String BUFFS_SMALL  = "interfaces/buffs.png";
		public static final String BUFFS_LARGE  = "interfaces/large_buffs.png";

		public static final String TALENT_ICONS  = "interfaces/talent_icons.png";
		public static final String TALENT_BUTTON = "interfaces/talent_button.png";
		public static final String TALENT_BUTTON_1 = "interfaces/talent_button_1.png";
		public static final String TALENT_BUTTON_2 = "interfaces/talent_button_2.png";

		public static final String HERO_ICONS   = "interfaces/hero_icons.png";

		public static final String DASHBORD = "interfaces/dashboard.png";

		public static final String UPBARS  = "interfaces/progress.png";
		public static final String DOWNLOAD    = "interfaces/downloadbar.png";
		public static final String LOADING  = "interfaces/loading.png";
		public static final String ZEROSHOP_UI = "interfaces/ZeroshopUI.png";
	}

	//these points to resource bundles, not raw asset files
	public static class Messages {
		public static final String ACTORS   = "messages/actors/actors";
		public static final String ITEMS    = "messages/items/items";
		public static final String JOURNAL  = "messages/journal/journal";
		public static final String LEVELS   = "messages/levels/levels";
		public static final String MISC     = "messages/misc/misc";
		public static final String PLANTS   = "messages/plants/plants";
		public static final String SCENES   = "messages/scenes/scenes";
		public static final String UI       = "messages/ui/ui";
		public static final String WINDOWS  = "messages/windows/windows";
        public static final String MINIGAMES  = "messages/minigames/minigames";
	}

	public static class Music {
		public static final String THEME_1      = "music/MakeSense.ogg";
		public static final String THEME_2      = "music/Horizon.ogg";

		public static final String END        = "music/Vacance.ogg";

		public static final String SEWERS_1     = "music/SafetyFirst-a.ogg";
		public static final String SEWERS_2     = "music/SafetyFirst-b.ogg";
		public static final String SEWERS_BOSS  = "music/Made-in-Heaven.ogg";

		public static final String PRISON_1     = "music/M_halloween19_host.ogg";
		public static final String PRISON_BOSS  = "music/M_halloween19_made_in_heaven.ogg";

		public static final String CAVES_1      = "music/Machines-AreTalking.ogg";
		public static final String CAVES_BOSS   = "music/Cradle-of-Fear.ogg";

		public static final String CITY_1       = "music/Event_summer_combat.ogg";
		public static final String CITY_BOSS    = "music/Cury.ogg";


		public static final String RECAVES_1       = "music/Tactical-Operation.ogg";
		public static final String RECAVES_BOSS    = "music/What-amIfightingfor.ogg";

		public static final String HALLS_1      = "music/SeeYouAgain.ogg";
		public static final String HALLS_BOSS   = "music/MindHack.ogg";
	}

	public static class Sounds {
		public static final String BADGE    = "sounds/badge.mp3";
		public static final String CLICK    = "sounds/click.mp3";
		public static final String GOLD     = "sounds/gold.mp3";

		public static final String DEWDROP  = "sounds/dewdrop.mp3";
		public static final String GRASS    = "sounds/grass.mp3";
		public static final String ITEM     = "sounds/item.mp3";
		public static final String OPEN     = "sounds/door_open.mp3";
		public static final String STEP     = "sounds/step.mp3";
		public static final String STURDY   = "sounds/sturdy.mp3";
		public static final String TRAMPLE  = "sounds/trample.mp3";
		public static final String UNLOCK   = "sounds/unlock.mp3";
		public static final String WATER    = "sounds/water.mp3";

		public static final String ATK_SPIRITBOW    = "sounds/atk_spiritbow.mp3";
		public static final String ATK_CROSSBOW     = "sounds/atk_crossbow.mp3";
		public static final String HEALTH_WARN      = "sounds/health_warn.mp3";
		public static final String HEALTH_CRITICAL  = "sounds/health_critical.mp3";
		public static final String HIT              = "sounds/hit.mp3";
		public static final String HIT_SLASH        = "sounds/hit_slash.mp3";
		public static final String HIT_STAB         = "sounds/hit_stab.mp3";
		public static final String HIT_CRUSH        = "sounds/hit_crush.mp3";
		public static final String HIT_MAGIC        = "sounds/hit_magic.mp3";
		public static final String HIT_STRONG       = "sounds/hit_strong.mp3";
		public static final String HIT_PARRY        = "sounds/hit_parry.mp3";
		public static final String HIT_ARROW        = "sounds/hit_arrow.mp3";
		public static final String MISS             = "sounds/miss.mp3";

		public static final String ALERT    	= "sounds/alert.mp3";
		public static final String BOSS     	= "sounds/boss.mp3";
		public static final String BLAST    	= "sounds/blast.mp3";
		public static final String BEACON   	= "sounds/beacon.mp3";
		public static final String BONES    	= "sounds/bones.mp3";
		public static final String BEE      	= "sounds/bee.mp3";
		public static final String BURNING  	= "sounds/burning.mp3";
		public static final String CHARMS   	= "sounds/charms.mp3";
		public static final String CHARGEUP 	= "sounds/chargeup.mp3";
		public static final String CHAINS   	= "sounds/chains.mp3";
		public static final String CURSED   	= "sounds/cursed.mp3";
		public static final String CHALLENGE	= "sounds/challenge.mp3";
		public static final String DESCEND  	= "sounds/descend.mp3";
		public static final String DEATH    	= "sounds/death.mp3";
		public static final String DRINK    	= "sounds/drink.mp3";
		public static final String DEGRADE  	= "sounds/degrade.mp3";
		public static final String DEBUFF   	= "sounds/debuff.mp3";
		public static final String DOGCHONG 	= "sounds/dogchong.mp3";
		public static final String EVOKE    	= "sounds/evoke.mp3";
		public static final String EAT      	= "sounds/eat.mp3";
		public static final String FALLING  	= "sounds/falling.mp3";
		public static final String GAS      	= "sounds/gas.mp3";
		public static final String GHOST    	= "sounds/ghost.mp3";
		public static final String LULLABY  	= "sounds/lullaby.mp3";
		public static final String LIGHTNING	= "sounds/lightning.mp3";
		public static final String LEVELUP  	= "sounds/levelup.mp3";
		public static final String MIMIC    	= "sounds/mimic.mp3";
		public static final String MELD     	= "sounds/meld.mp3";
		public static final String MASTERY  	= "sounds/mastery.mp3";
		public static final String PUFF     	= "sounds/puff.mp3";
		public static final String PLANT    	= "sounds/plant.mp3";
		public static final String READ     	= "sounds/read.mp3";
		public static final String ROCKS    	= "sounds/rocks.mp3";
		public static final String RAY      	= "sounds/ray.mp3";
		public static final String SHATTER  	= "sounds/shatter.mp3";
		public static final String SECRET   	= "sounds/secret.mp3";
		public static final String SCAN     	= "sounds/scan.mp3";
		public static final String SHEEP    	= "sounds/sheep.mp3";
		public static final String TRAP     	= "sounds/trap.mp3";
		public static final String TOMB     	= "sounds/tomb.mp3";
		public static final String TELEPORT 	= "sounds/teleport.mp3";
		public static final String ZAP      	= "sounds/zap.mp3";

		public static final String[] all = new String[]{
				CLICK, BADGE, GOLD,

				OPEN, UNLOCK, ITEM, DEWDROP, STEP, WATER, GRASS, TRAMPLE, STURDY,

				HIT, MISS, HIT_SLASH, HIT_STAB, HIT_CRUSH, HIT_MAGIC, HIT_STRONG, HIT_PARRY,
				HIT_ARROW, ATK_SPIRITBOW, ATK_CROSSBOW, HEALTH_WARN, HEALTH_CRITICAL,

				DESCEND, EAT, READ, LULLABY, DRINK, SHATTER, ZAP, LIGHTNING, LEVELUP, DEATH,
				CHALLENGE, CURSED, TRAP, EVOKE, TOMB, ALERT, MELD, BOSS, BLAST, PLANT, RAY, BEACON,
				TELEPORT, CHARMS, MASTERY, PUFF, ROCKS, BURNING, FALLING, GHOST, SECRET, BONES,
				BEE, DEGRADE, MIMIC, DEBUFF, CHARGEUP, GAS, CHAINS, SCAN, SHEEP, DOGCHONG
		};
	}



	public static class Sprites {
		public static final String ITEMS        = "interfaces/items.png";
		public static final String ITEM_ICONS   = "interfaces/item_icons.png";

		// ===== sprites/ (direct children) =====            
		
		// TODO  破碎中待删除的贴图        因为我不知道有哪些还在被引用 所以没删掉。看到的话请帮忙清理一下这些！！感谢！
		
		public static final String PET      = "sprites/pet.png";
		public static final String AMULET   = "sprites/amulet.png";
		public static final String DM300      = "sprites/dm300.png";
		public static final String WRAITH     = "sprites/wraith.png";
		public static final String KING       = "sprites/king.png";
		public static final String GNOLL      = "sprites/gnoll.png";
		public static final String CRAB       = "sprites/crab.png";
		public static final String GOO        = "sprites/goo.png";
		public static final String SKELETON   = "sprites/skeleton.png";
		public static final String SHAMAN     = "sprites/shaman.png";
		public static final String TENGU      = "sprites/tengu.png";
		public static final String MONK       = "sprites/monk.png";
		public static final String WARLOCK    = "sprites/warlock.png";
		public static final String GOLEM      = "sprites/golem.png";
		public static final String STATUE     = "sprites/statue.png";
		public static final String SCORPIO    = "sprites/scorpio.png";
		public static final String FISTS      = "sprites/yog_fists.png";
		public static final String YOG        = "sprites/yog.png";
		public static final String RATKING    = "sprites/ratking.png";
		public static final String SLIME      = "sprites/slime.png";
		public static final String NECRO      = "sprites/necromancer.png";
		public static final String DM100      = "sprites/dm100.png";
		public static final String DM200      = "sprites/dm200.png";
		public static final String NINJA_LOG  = "sprites/ninja_log.png";

		// ===== sprites/boss/ =====              		-地牢boss-
		public static final String PYLON        = "sprites/boss/pylon.png";
		public static final String TYPHOOTIN	= "sprites/boss/typhootin.png";
		public static final String INTRUDER 	= "sprites/boss/intruder.png";
		public static final String ELPHELT 		= "sprites/boss/elphelt.png";
		public static final String DISLOLI		= "sprites/boss/disloli.png";
		public static final String GARM			= "sprites/boss/garm.png";
		public static final String DREAMER		= "sprites/boss/dreamer.png";
		public static final String AGENT		= "sprites/boss/Agent.png";
		public static final String ALCHEMIST	= "sprites/boss/Alchemist.png";
		public static final String EXCU			= "sprites/boss/excutioner.png";
		public static final String MASTAR_HEAD	= "sprites/boss/master_head.png";
		public static final String DISTORY		= "sprites/boss/disloli.png";
		public static final String URO			= "sprites/boss/uro.png";
		public static final String GAGER		= "sprites/boss/gager.png";

		// ===== sprites/char/ =====              		-自机角色-
		public static final String WARRIOR  		= "sprites/char/warrior.png";
		public static final String MAGE     		= "sprites/char/mage.png";
		public static final String ROGUE    		= "sprites/char/rogue.png";
		public static final String HUNTRESS 		= "sprites/char/falcon.png";
		public static final String TYPE561  		= "sprites/char/type56.png";
		public static final String GSH18    		= "sprites/char/gsh18.png";
		public static final String HK416    		= "sprites/char/hk416.png";
		public static final String FALCON   		= "sprites/char/falcon.png";
		public static final String SUPERAI  		= "sprites/char/superAI.png"; // 超级小爱
		public static final String DANDELION		= "sprites/char/dandelion.png";
		public static final String AVATARS  		= "sprites/char/avatars.png";
		public static final String EMOTION 			= "sprites/char/emotion.png";

		// ===== sprites/girlpd/ =====              		-少前地牢的中立生物-
		public static final String SPINNER   		= "sprites/girlpd/spinner.png";
		public static final String SPINNERCAT		= "sprites/girlpd/spinner_cat.png";
		public static final String PIRANHA   		= "sprites/girlpd/piranha.png";
		public static final String THIEF     		= "sprites/girlpd/thief.png";
		public static final String ELEMENTAL 		= "sprites/girlpd/elemental.png";
		public static final String LARVA       		= "sprites/girlpd/larva.png";
		public static final String BEE	     		= "sprites/girlpd/bee.png";
		public static final String MIMIC      		= "sprites/girlpd/mimic.png";
		public static final String ROT_LASH  		= "sprites/girlpd/rot_lasher.png";
		public static final String ROT_HEART  		= "sprites/girlpd/rot_heart.png";
		public static final String SNAKE       		= "sprites/girlpd/snake.png";
		public static final String SKS       		= "sprites/girlpd/sks.png";
        public static final String SKS_ARM   		= "sprites/girlpd/sks_arm.png";
		public static final String PYRO		    	= "sprites/girlpd/pyro.png";
		public static final String MG5	   			= "sprites/girlpd/mg5.png";
		public static final String FETID_RAT  		= "sprites/girlpd/fetid_rat.png";

		// ===== sprites/PARA_DEUS/ =====              		-帕拉蒂斯-
		public static final String RED_SENTRY 		= "sprites/PARA_DEUS/red_pointer.png";

		// ===== sprites/REGULAR_ARMY/ =====              		-正规军-
		public static final String SUCCUBUS     	= "sprites/REGULAR_ARMY/succubus.png";
		public static final String RIPPER    		= "sprites/REGULAR_ARMY/ripper.png";
		public static final String SPAWNER    		= "sprites/REGULAR_ARMY/spawner.png";
		public static final String KENTAUROS		= "sprites/REGULAR_ARMY/archer.png";
		public static final String TYPHOON			= "sprites/REGULAR_ARMY/typhoon.png";
		public static final String ACYCLOPS			= "sprites/REGULAR_ARMY/acyclops.png";
		public static final String HYDRA			= "sprites/REGULAR_ARMY/hydra.png";

		// ===== sprites/SANGVIS_FERRI/ =====              		-铁血工造-
		public static final String RAT        		= "sprites/SANGVIS_FERRI/rat.png";
		public static final String BRUTE      		= "sprites/SANGVIS_FERRI/brute.png";
		public static final String UNDEAD      		= "sprites/SANGVIS_FERRI/undead.png";
		public static final String SWARM      		= "sprites/SANGVIS_FERRI/swarm.png";
		public static final String BAT        		= "sprites/SANGVIS_FERRI/bat.png";
		public static final String GUARD      		= "sprites/SANGVIS_FERRI/guard.png";
        public static final String GOLYATFACTORY    = "sprites/SANGVIS_FERRI/golyatfactory.png";
        public static final String GNOLLSWAP 		= "sprites/SANGVIS_FERRI/GnollSWAP.png";
		public static final String AEGIS    		= "sprites/SANGVIS_FERRI/aegis.png";
		public static final String JAEGER    		= "sprites/SANGVIS_FERRI/jaeger.png";
		public static final String JAEGER_LOCK    	= "sprites/SANGVIS_FERRI/jager_lock.png";
		public static final String MANTI  			= "sprites/SANGVIS_FERRI/manti.png";
		public static final String STRIKER			= "sprites/SANGVIS_FERRI/striker.png";
		public static final String ESTRIKER			= "sprites/SANGVIS_FERRI/elitestriker.png";
		public static final String SVESPID			= "sprites/SANGVIS_FERRI/shieldvespid.png";
		public static final String JUPITER			= "sprites/SANGVIS_FERRI/jupiter.png";
		public static final String NEMEUM 			= "sprites/SANGVIS_FERRI/nemeum.png";
		public static final String JAGUAR      		= "sprites/SANGVIS_FERRI/Jaguar.png";
		public static final String DRAGUN      		= "sprites/SANGVIS_FERRI/dragun.png";
		public static final String PROWLER      	= "sprites/SANGVIS_FERRI/prowler.png";
		public static final String GOLYAT			= "sprites/SANGVIS_FERRI/golyat.png";
		public static final String VESPID			= "sprites/SANGVIS_FERRI/vespid.png";
		public static final String GOLYATPLUS      	= "sprites/SANGVIS_FERRI/golyatplus.png";
		public static final String EYE        		= "sprites/SANGVIS_FERRI/eye.png";
		public static final String GHOUL        	= "sprites/SANGVIS_FERRI/undead.png";

		// ===== sprites/zeroNPC/ =====              		-NPC角色-
		public static final String NOEL      		= "sprites/zeroNPC/noel.png";
		public static final String DEELE     		= "sprites/zeroNPC/deele.png";
		public static final String SHEEP     		= "sprites/zeroNPC/sheep.png";
		public static final String KEEPER    		= "sprites/zeroNPC/shopkeeper.png";
        public static final String GHOST     		= "sprites/zeroNPC/ghost.png";
        public static final String POT      		= "sprites/zeroNPC/pot.png";
		public static final String MAKER     		= "sprites/zeroNPC/wandmaker.png";
		public static final String TROLL     		= "sprites/zeroNPC/blacksmith.png";
		public static final String WARDS     		= "sprites/zeroNPC/wards.png";
		public static final String GUARDIAN  		= "sprites/zeroNPC/SAT8.png";
		public static final String LOTUS     		= "sprites/zeroNPC/lotus1.png";
		public static final String SPIRIT_HAWK		= "sprites/zeroNPC/spirit_hawk.png";
		public static final String P7				= "sprites/zeroNPC/p7.png";
		public static final String M4A1				= "sprites/zeroNPC/m4a1.png";
		public static final String FNC				= "sprites/zeroNPC/fnc.png";

	}
}
