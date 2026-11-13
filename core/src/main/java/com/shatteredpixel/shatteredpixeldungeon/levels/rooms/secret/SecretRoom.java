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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Dungeons;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;


public abstract class SecretRoom extends SpecialRoom {

    public boolean Found(){
        if (Dungeon.level == null || Dungeon.level.map.length == 0)
            return false;
        return Dungeon.level.map[Dungeon.level.pointToCell(entrance())] != Terrain.SECRET_DOOR;
    }

	private static final ArrayList<Class<? extends SecretRoom>> ALL_SECRETS = new ArrayList<>( Arrays.asList(
			SecretGardenRoom.class, SecretLaboratoryRoom.class, SecretLibraryRoom.class,
			SecretLarderRoom.class, SecretWellRoom.class, SecretRunestoneRoom.class,
			SecretArtilleryRoom.class, SecretChestChasmRoom.class, SecretHoneypotRoom.class,
			SecretHoardRoom.class, SecretMazeRoom.class, SecretSummoningRoom.class));

	//this is the number of secret rooms per region (whole value),
	// plus the chance for an extra secret room (fractional value)
	private static final float[] baseRegionSecrets = new float[]{2f, 2.25f, 2.5f, 2.75f, 3.0f, 3.25f};
	public static final int BASE_REGION_SECRETS_LEN = baseRegionSecrets.length;

	public static final class State {
		ArrayList<Class<? extends SecretRoom>> runSecrets = new ArrayList<>();
		int[] regionSecretsThisRun = new int[BASE_REGION_SECRETS_LEN];
		float[] regionSecretsThisRandom = new float[BASE_REGION_SECRETS_LEN];

		private void initForRun(){
			float[] regionChances = baseRegionSecrets.clone();
			for (int i = 0; i < regionSecretsThisRun.length; i++){
				regionSecretsThisRun[i] = (int)regionChances[i];
				regionSecretsThisRandom[i] = Random.Float();
			}

			runSecrets.clear();
			runSecrets.addAll(ALL_SECRETS);
			Random.shuffle(runSecrets);
		}

		private int secretsForFloor(int depth){
			if (depth == 1 || depth > BASE_REGION_SECRETS_LEN * 5 ) return 0;

			int region = depth/5;
			int floor = depth%5;

			int floorsLeft = 5 - floor;

			float secrets= regionSecretsThisRun[region];

			// so that it will not change old files
			if (regionSecretsThisRandom.length != BASE_REGION_SECRETS_LEN) {
				float regionRandom = regionSecretsThisRandom[region];
				float choice = baseRegionSecrets[region] % 1F;
				if (Dungeon.hero().hasTalentB(Talent.ROGUES_FORESIGHT) ||
						Dungeon.hero().pointsInTalentA(Talent.ROGUES_FORESIGHT_V2) == 0 ||
						Dungeon.hero().hasTalentB(Talent.ROGUES_FORESIGHT_V3)) {
					choice += 0.6F;
				}
				else if (Dungeon.hero().hasTalentB(Talent.ROGUES_FORESIGHT_V2)) {
					choice += 0.5F + 0.5F * Dungeon.hero().pointsInTalent(Talent.ROGUES_FORESIGHT_V2);
				}
				if (regionRandom < choice % 1F) {
					secrets += (float) Math.ceil(choice);
				} else {
					secrets += (float) Math.floor(choice);
				}
			}

			if (secrets <= 0)
				return 0;

			secrets /= floorsLeft;
			if (Random.Float() < secrets % 1f){
				secrets = (float)Math.ceil(secrets);
			} else {
				secrets = (float)Math.floor(secrets);
			}

			regionSecretsThisRun[region] -= (int)secrets;
			return (int)secrets;
		}

		private SecretRoom createRoom(){
			SecretRoom r = null;
			int index = runSecrets.size();
			for (int i = 0; i < 4; i++){
				int newidx = Random.Int( runSecrets.size() );
				if (newidx < index) index = newidx;
			}
			try {
				r = runSecrets.get( index ).getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				GirlsFrontlinePixelDungeon.reportException(e);
			}

			runSecrets.add(runSecrets.remove(index));

			return r;
		}

		private void restoreFromBundle( Bundle bundle ) {
			runSecrets.clear();
			if (bundle.contains( ROOMS )) {
				for (Class<? extends SecretRoom> type : bundle.getClassArray(ROOMS)) {
					if (type != null) runSecrets.add(type);
				}
				regionSecretsThisRun = bundle.getIntArray(REGIONS);
				if (bundle.contains(RANDOM))
					regionSecretsThisRandom = bundle.getFloatArray(RANDOM);
				else
					regionSecretsThisRandom = new float[0];
			} else {
				initForRun();
				GirlsFrontlinePixelDungeon.reportException(new Exception("secrets array didn't exist!"));
			}
		}

		private void storeInBundle( Bundle bundle ) {
			bundle.put( ROOMS, runSecrets.toArray(new Class[0]) );
			bundle.put( REGIONS, regionSecretsThisRun );
			bundle.put( RANDOM, regionSecretsThisRandom );
		}
	}

	private static State cur() {
		return Dungeons.cur().secretRoom;
	}

	public static void initForRun(){
		cur().initForRun();
	}

	public static int secretsForFloor(int depth){
		return cur().secretsForFloor(depth);
	}

	public static SecretRoom createRoom(){
		return cur().createRoom();
	}

	private static final String ROOMS	= "secret_rooms";
	private static final String REGIONS	= "region_secrets";
    private static final String RANDOM  = "region_random";

	public static void restore( Bundle bundle ) {
		Dungeons.MAIN().secretRoom.restoreFromBundle(bundle);
	}

	public static void store( Bundle bundle ) {
		Dungeons.MAIN().secretRoom.storeInBundle(bundle);
	}

}