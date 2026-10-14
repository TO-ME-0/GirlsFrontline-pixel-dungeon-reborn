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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DEL;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.GeminiMissile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.GeminiShield;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Noel;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NoelShopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.RatKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SentryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.AlarmTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.BlazingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ChillingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ConfusionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.CorrosionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.CursingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisarmingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisintegrationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DistortionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ExplosiveTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlashingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlockTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FrostTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GatewayTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrippingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GuardianTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OozeTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PoisonDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.RockfallTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ShockingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.StormTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.SummoningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TeleportationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WarpingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WeakeningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WornDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.BlandfruitBush;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Dreamfoil;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Fadeleaf;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Starflower;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

public enum Bestiary {

    REGIONAL,
    BOSSES,
    UNIVERSAL,
    RARE,
    QUEST,
    NEUTRAL,
    ALLY,
    TRAP,
    PLANT;

    //tracks whether an entity has been encountered
    private final LinkedHashMap<Class<?>, Boolean> seen = new LinkedHashMap<>();
    //tracks enemy kills, trap activations, plant tramples, or just sets to 1 for seen on allies
    private final LinkedHashMap<Class<?>, Integer> encounterCount = new LinkedHashMap<>();

    //should only be used when initializing
    private void addEntities(Class<?>... classes ){
        for (Class<?> cls : classes){
            seen.put(cls, false);
            encounterCount.put(cls, 0);
        }
    }

    public Collection<Class<?>> entities(){
        return seen.keySet();
    }

    public String title(){
        return Messages.get(Journal.class, name() + ".title");
    }

    public int totalEntities(){
        return seen.size();
    }

    public int totalSeen(){
        int seenTotal = 0;
        for (boolean entitySeen : seen.values()){
            if (entitySeen) seenTotal++;
        }
        return seenTotal;
    }

    static {

        REGIONAL.addEntities(Rat.class, Snake.class, Gnoll.class, Brute.class, Swarm.class,
                Golyat.class, Thief.class, Shaman.RedShaman.class, Shaman.BlueShaman.class, Shaman.PurpleShaman.class, Mg5.class, GolyatFactory.class, GolyatFactory.FactoryGolyat.class,
                Bat.class, Vespid.class, Spinner.class, Nemeum.class,
                Ghoul.class, Elemental.FireElemental.class, Elemental.FrostElemental.class, Elemental.ShockElemental.class, Warlock.class, Monk.class, Golem.class,
                Guard.class, Dragun.class, Jupiter.class,
                Cyclops.class, Hydra.class, Succubus.class,
                Kentauros.class,
                RipperDemon.class, DemonSpawner.class);

        BOSSES.addEntities(Goo.class,
                Tengu.class, Elphelt.class,
                Pylon.class, DM300.class,
                DwarfKing.class,
                YogDzewa.Larva.class,
                YogFist.BurningFist.class, YogFist.SoiledFist.class,
                YogFist.RottingFist.class, YogFist.RustedFist.class,
                YogFist.BrightFist.class, YogFist.DarkFist.class,
                YogDzewa.class);

        UNIVERSAL.addEntities(Wraith.class, Piranha.class,
                Mimic.class, GoldenMimic.class,
                Statue.class, GuardianTrap.Guardian.class,
                SentryRoom.Sentry.class);

        RARE.addEntities(RatXMAS.class, Albino.class, GnollSWAP.class,
                Bandit.class, GolyatPlus.class,
                SpinnerCat.class,
                Elemental.ChaosElemental.class, Senior.class,
                Typhoon.class,
                CrystalMimic.class, ArmoredStatue.class,
                ArmoredBrute.class);

        QUEST.addEntities(FetidRat.class, GnollTrickster.class, GreatCrab.class,
                Elemental.NewbornFireElemental.class, RotLasher.class, RotHeart.class);

        NEUTRAL.addEntities(Ghost.class, DEL.class, RatKing.class,
                Shopkeeper.class, Wandmaker.class, Noel.class,
                Blacksmith.class, Imp.class,
                NoelShopkeeper.class,
                Sheep.class, Bee.class);

        ALLY.addEntities(MirrorImage.class, PrismaticImage.class,
                DriedRose.GhostHero.class,
                GeminiShield.class, GeminiMissile.class,
                WandOfWarding.Ward.class, WandOfWarding.Ward.WardSentry.class,
                WandOfLivingEarth.EarthGuardian.class,
                ShadowClone.ShadowAlly.class, SmokeBomb.NinjaLog.class, SpiritHawk.HawkAlly.class);

        TRAP.addEntities(WornDartTrap.class, PoisonDartTrap.class, DisintegrationTrap.class, GatewayTrap.class,
                ChillingTrap.class, BurningTrap.class, ShockingTrap.class, AlarmTrap.class, GrippingTrap.class, TeleportationTrap.class, OozeTrap.class,
                FrostTrap.class, BlazingTrap.class, StormTrap.class, GuardianTrap.class, FlashingTrap.class, WarpingTrap.class,
                ConfusionTrap.class, ToxicTrap.class, CorrosionTrap.class,
                FlockTrap.class, SummoningTrap.class, WeakeningTrap.class, CursingTrap.class,
                GeyserTrap.class, ExplosiveTrap.class, RockfallTrap.class,
                DistortionTrap.class, DisarmingTrap.class, GrimTrap.class);

        PLANT.addEntities(Rotberry.class, Sungrass.class, Fadeleaf.class, Icecap.class,
                Firebloom.class, Sorrowmoss.class, Swiftthistle.class, Blindweed.class,
                Stormvine.class, Earthroot.class, Dreamfoil.class, Starflower.class,
                BlandfruitBush.class,
                WandOfRegrowth.Dewcatcher.class, WandOfRegrowth.Seedpod.class, WandOfRegrowth.Lotus.class);

    }

    //some mobs and traps have different internal classes in some cases, so need to convert here
    private static final HashMap<Class<?>, Class<?>> classConversions = new HashMap<>();
    static {
        classConversions.put(CorpseDust.DustWraith.class,      Wraith.class);

        classConversions.put(TenguDartTrap.class,              PoisonDartTrap.class);

        classConversions.put(DwarfKing.DKGhoul.class,          Ghoul.class);
        classConversions.put(DwarfKing.DKWarlock.class,        Warlock.class);
        classConversions.put(DwarfKing.DKMonk.class,           Monk.class);
        classConversions.put(DwarfKing.DKGolem.class,          Golem.class);

        classConversions.put(YogDzewa.YogRipper.class,         RipperDemon.class);
    }

    public static boolean isSeen(Class<?> cls){
        if (DeviceCompat.isDebug() && Dungeon.isChallenged(Challenges.TEST_MODE))
            return true;
        for (Bestiary cat : values()) {
            if (cat.seen.containsKey(cls)) {
                return cat.seen.get(cls);
            }
        }
        return false;
    }

    public static void setSeen(Class<?> cls){
        if(Dungeon.isChallenged(Challenges.TEST_MODE))
            return;
        if (classConversions.containsKey(cls)){
            cls = classConversions.get(cls);
        }
        for (Bestiary cat : values()) {
            if (cat.seen.containsKey(cls) && !cat.seen.get(cls)) {
                cat.seen.put(cls, true);
                Journal.saveNeeded = true;
            }
        }
    }

    public static int encounterCount(Class<?> cls) {
        for (Bestiary cat : values()) {
            if (cat.encounterCount.containsKey(cls)) {
                return cat.encounterCount.get(cls);
            }
        }
        return 0;
    }

    //used primarily when bosses are killed and need to clean up their minions
    public static boolean skipCountingEncounters = false;

    public static void countEncounter(Class<?> cls){
        if(Dungeon.isChallenged(Challenges.TEST_MODE))
            return;
        countEncounters(cls, 1);
    }

    public static void countEncounters(Class<?> cls, int encounters){
        if (skipCountingEncounters){
            return;
        }
        if (classConversions.containsKey(cls)){
            cls = classConversions.get(cls);
        }
        for (Bestiary cat : values()) {
            if (cat.encounterCount.containsKey(cls) && cat.encounterCount.get(cls) != Integer.MAX_VALUE){
                cat.encounterCount.put(cls, cat.encounterCount.get(cls)+encounters);
                if (cat.encounterCount.get(cls) < -1_000_000_000){ //to catch cases of overflow
                    cat.encounterCount.put(cls, Integer.MAX_VALUE);
                }
                Journal.saveNeeded = true;
            }
        }
    }

    private static final String BESTIARY_CLASSES    = "bestiary_classes";
    private static final String BESTIARY_SEEN       = "bestiary_seen";
    private static final String BESTIARY_ENCOUNTERS = "bestiary_encounters";

    public static void store( Bundle bundle ){

        ArrayList<Class<?>> classes = new ArrayList<>();
        ArrayList<Boolean> seen = new ArrayList<>();
        ArrayList<Integer> encounters = new ArrayList<>();

        for (Bestiary cat : values()) {
            for (Class<?> entity : cat.entities()) {
                if (cat.seen.get(entity) || cat.encounterCount.get(entity) > 0){
                    classes.add(entity);
                    seen.add(cat.seen.get(entity));
                    encounters.add(cat.encounterCount.get(entity));
                }
            }
        }

        Class<?>[] storeCls = new Class[classes.size()];
        boolean[] storeSeen = new boolean[seen.size()];
        int[] storeEncounters = new int[encounters.size()];

        for (int i = 0; i < storeCls.length; i++){
            storeCls[i] = classes.get(i);
            storeSeen[i] = seen.get(i);
            storeEncounters[i] = encounters.get(i);
        }

        bundle.put( BESTIARY_CLASSES, storeCls );
        bundle.put( BESTIARY_SEEN, storeSeen );
        bundle.put( BESTIARY_ENCOUNTERS, storeEncounters );

    }

    public static void restore( Bundle bundle ){

        if (bundle.contains(BESTIARY_CLASSES)
                && bundle.contains(BESTIARY_SEEN)
                && bundle.contains(BESTIARY_ENCOUNTERS)){
            Class<?>[] classes = bundle.getClassArray(BESTIARY_CLASSES);
            boolean[] seen = bundle.getBooleanArray(BESTIARY_SEEN);
            int[] encounters = bundle.getIntArray(BESTIARY_ENCOUNTERS);

            for (int i = 0; i < classes.length; i++){
                for (Bestiary cat : values()){
                    if (cat.seen.containsKey(classes[i])){
                        cat.seen.put(classes[i], seen[i]);
                        cat.encounterCount.put(classes[i], encounters[i]);
                    }
                }
            }
        }

    }

    public static ArrayList<Class<? extends Mob>> getMobRotation( int depth ) {

		ArrayList<Class<? extends Mob>> mobs = getRotation( depth );
		addRareMobs(depth, mobs);
		swapMobAlts(mobs);
		swapRareMobAlts(mobs);
		Random.shuffle(mobs);
		return mobs;
	}

	public static void addRareMobs( int depth, ArrayList<Class<?extends Mob>> rotation ){

		switch (depth){

			case 4: case 9: case 14: case 19: case 24:
                if (Random.Float() < 0.025F)
                    rotation.add(Random.element(getRotation(depth + 2))); break;
		}
	}

	//switches out regular mobs for their alt versions when appropriate
    private static final HashMap<Class<? extends Mob>, Class<? extends Mob>> swapMob = new HashMap<>();
    static {
        //1-5
        swapMob.put(Rat.class, Albino.class);
        swapMob.put(Gnoll.class, GnollSWAP.class);
        swapMob.put(Slime.class, CausticSlime.class);

        //6-10
        swapMob.put(Golyat.class, GolyatPlus.class);
        swapMob.put(Thief.class, Bandit.class);
        swapMob.put(Necromancer.class, SpectralNecromancer.class);

        //11-15
        swapMob.put(Vespid.class, ArmoredBrute.class);
        swapMob.put(DM200.class, DM201.class);

        //16-20
        swapMob.put(Elemental.FireElemental.class, Elemental.ChaosElemental.class);
        swapMob.put(Elemental.FrostElemental.class, Elemental.ChaosElemental.class);
        swapMob.put(Elemental.ShockElemental.class, Elemental.ChaosElemental.class);
        swapMob.put(Monk.class, Senior.class);

        //-
        swapMob.put(Scorpio.class, Acidic.class);
    }
	private static void swapMobAlts(ArrayList<Class<?extends Mob>> rotation){
		for (int i = 0; i < rotation.size(); i++){

            Class<? extends Mob> cl = rotation.get(i);
            int j = Random.Int(100);
            if (!swapMob.containsKey(cl))
                continue;
            //鼠头骨饰品：增加元素生物的异化概率（实现见 RatSkull）
            int chance = Dungeon.mobRan;
            float ratSkull = RatSkull.exoticChanceMultiplier();
            if (cl == Golyat.class)
                chance *= ratSkull > 1F ? 2 : 4;
            chance *= ratSkull;
            if (j < chance)
                rotation.set(i, swapMob.get(cl));
		}
	}

	private static void swapRareMobAlts(ArrayList<Class<?extends Mob>> rotation) {
		for (int i = 0; i < rotation.size(); i++){
			Class<? extends Mob> cl = rotation.get(i);
			if (Random.Int( 500 ) == 0)
				if (cl == Spinner.class)
					cl = SpinnerCat.class;
			if (Random.Int( 100 ) == 0)
				if (cl == Hydra.class)
					cl = Typhoon.class;
            rotation.set(i, cl);
		}
	}
    private static void add(ArrayList<Class<? extends Mob>> list, int num, Class<? extends Mob> mob){
        list.addAll(Collections.nCopies(num, mob));
    }
	public static ArrayList<Class<? extends Mob>> getRotation(int floor) {
        ArrayList<Class<? extends Mob>> list = new ArrayList<>();
        switch (floor){
            default:
                add(list, 1, Rat.class);
                break;
            case 1:
                add(list, 6, Rat.class);
                add(list, 2, Snake.class);
                break;

            case 2:
                add(list, 3, Rat.class);
                add(list, 3, Gnoll.class);
                add(list, 1, Snake.class);
                break;

            case 3:
                add(list, 2, Rat.class);
                add(list, 4, Gnoll.class);
                add(list, 1, Brute.class);
                add(list, 1, Swarm.class);
                break;

            case 4:
            case 5:
                add(list, 1, Rat.class);
                add(list, 2, Gnoll.class);
                add(list, 3, Brute.class);
                add(list, 1, Swarm.class);
                break;

            case 6:
                add(list, 3, Golyat.class);
                add(list, 1, Thief.class);
                add(list, 1, Swarm.class);
                break;

            case 7:
                add(list, 3, Golyat.class);
                add(list, 1, Thief.class);
                add(list, 1, Mg5.class);

                list.add(Shaman.random());
                break;

            case 8:
                add(list, 2, Golyat.class);
                add(list, 1, Thief.class);
                add(list, 2, Mg5.class);
                add(list, 1, GolyatFactory.class);

                list.add(Shaman.random());
                list.add(Shaman.random());
                break;

            case 9:
            case 10:
                add(list, 1, Golyat.class);
                add(list, 1, Thief.class);
                add(list, 2, Mg5.class);
                add(list, 2, GolyatFactory.class);

                list.add(Shaman.random());
                list.add(Shaman.random());
                break;

            case 11:
                add(list, 5, Bat.class);
                add(list, 1, Vespid.class);
                break;

            case 12:
                add(list, 4, Bat.class);
                add(list, 4, Vespid.class);
                add(list, 2, Spinner.class);
                add(list, 1, Nemeum.class);
                break;

            case 13:
                add(list, 2, Bat.class);
                add(list, 3, Vespid.class);
                add(list, 2, Spinner.class);
                add(list, 2, Nemeum.class);
                break;

            case 14:
            case 15:
                add(list, 1, Bat.class);
                add(list, 3, Vespid.class);
                add(list, 2, Spinner.class);
                add(list, 3, Nemeum.class);
                break;

            case 16:
                add(list, 2, Ghoul.class);

                list.add(Elemental.random());
                list.add(Elemental.random());
                list.add(Elemental.random());
                break;

            case 17:
                add(list, 3, Ghoul.class);
                add(list, 2, Monk.class);

                list.add(Elemental.random());
                list.add(Elemental.random());
                break;

            case 18:
                add(list, 3, Monk.class);
                add(list, 2, Warlock.class);
                add(list, 1, Golem.class);
                break;

            case 19:
            case 20:
                add(list, 1, Monk.class);
                add(list, 3, Warlock.class);
                add(list, 2, Golem.class);
                break;

            case 21:
                add(list, 1, Guard.class);
                break;

            case 22:
                add(list, 3, Guard.class);
                add(list, 1, Dragun.class);
                add(list, 1, Jupiter.class);
                break;

            case 23:
            case 24:
            case 25:
                add(list, 4, Guard.class);
                add(list, 2, Dragun.class);
                add(list, 1, Jupiter.class);
                break;

            case 26:
                add(list, 4, Cyclops.class);
                add(list, 2, Succubus.class);
                add(list, 1, Jupiter.class);
                break;

            case 27:
                add(list, 3, Cyclops.class);
                add(list, 2, Succubus.class);
                add(list, 2, Jupiter.class);
                add(list, 1, Hydra.class);
                add(list, 2, Kentauros.class);
                break;

            case 28:
                add(list, 2, Cyclops.class);
                add(list, 1, Succubus.class);
                add(list, 1, Jupiter.class);
                add(list, 6, Hydra.class);
                add(list, 2, Kentauros.class);
                break;

            case 29:
                add(list, 1, Cyclops.class);
                add(list, 6, Hydra.class);
                add(list, 3, Kentauros.class);
                break;

            case 30:
                add(list, 1, Cyclops.class);
                add(list, 9, Hydra.class);
                break;
        }
        return list;
	}
}