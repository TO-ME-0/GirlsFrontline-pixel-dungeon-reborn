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

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.fairyitems.FairyItems;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.FairyRoom;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class LloydsBeacon extends Artifact {
	public static final float TIME_TO_USE = 1;

	private static final String AC_ZAP       = "ZAP";
	private static final String AC_SET		= "SET";
	private static final String AC_RETURN	= "RETURN";
    private static final String AC_TP       = "TP";
    private static final String AC_FAIRY    = "FAIRY";
	
	public int returnLevelId	= -1;
	public int returnPos;
    private int cd;
	
	{
		image = ItemSpriteSheet.ARTIFACT_BEACON;

		levelCap = 3;

		charge = 0;
		chargeCap = 3+level();

		defaultAction = AC_ZAP;
		usesTargeting = true;
	}
	
	private static final String DEPTH	= "depth";
	private static final String POS		= "pos";
    private static final String CD      = "cd";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DEPTH, returnLevelId );
		if (returnLevelId != -1) {
			bundle.put( POS, returnPos );
		}
        bundle.put(CD, cd);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
        returnLevelId	= bundle.getInt( DEPTH );
		returnPos   	= bundle.getInt( POS );
        cd              = bundle.getInt(CD);
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_ZAP );
		actions.add( AC_SET );
		if (returnLevelId != -1) {
			actions.add( AC_RETURN );
		}
        if (Dungeon.level instanceof RegularLevel && ((RegularLevel) Dungeon.level).getRoom(FairyRoom.class) != null){
            if (FairyItems.inFairyRoom(hero))
                actions.add(AC_TP);
            else
                actions.add(AC_FAIRY);
        }
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_SET) || action.equals(AC_RETURN)) {
			
			if (Dungeon.level.prevent || Dungeon.cur().branch == 0) {
				hero.spend( LloydsBeacon.TIME_TO_USE );
				GLog.w( Messages.get(this, "preventing") );
				return;
			}
			
			for (int i = 0; i < PathFinder.NEIGHBOURS8().length; i++) {
				Char ch = Actor.findChar(hero.pos + PathFinder.NEIGHBOURS8()[i]);
				if (ch != null && ch.alignment == Char.Alignment.ENEMY) {
					GLog.w( Messages.get(this, "creatures") );
					return;
				}
			}
		}

		if (action.equals(AC_ZAP) ){

			curUser = hero;
			int chargesToUse = Dungeon.cur().depth / 20 + 1;

			if (!isEquipped( hero )) {
				GLog.i( Messages.get(Artifact.class, "need_to_equip") );
				QuickSlotButton.cancel();

			} else if (charge < chargesToUse) {
				GLog.i( Messages.get(this, "no_charge") );
				QuickSlotButton.cancel();

			} else {
				GameScene.selectCell(zapper);
			}

		}
        else if (action.equals(AC_SET)) {

            returnLevelId = Dungeon.curFloorCode();
			returnPos   = hero.pos;
			hero.spend( LloydsBeacon.TIME_TO_USE );
			hero.busy();
			
			hero.sprite.operate( hero.pos );
			Sample.INSTANCE.play( Assets.Sounds.BEACON );
			
			GLog.i( Messages.get(this, "return") );

		}
        else if ( cd > 0 ){
            GLog.n( Messages.get(this, "cd") );
            return;
        }
        else if (action.equals(AC_RETURN)) {

            cd = 50;
            float blind = 10;
            float weak  = 50;
			if (returnLevelId == Dungeon.curFloorCode()) {
                PathFinder.buildDistanceMap(returnPos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
                if (PathFinder.distance()[hero.pos] == Integer.MAX_VALUE){
                    cd *= 4;
                }
				ScrollOfTeleportation.appear( hero, returnPos );
				for(Mob m : Dungeon.level.mobs){
					if (m.pos == hero.pos){
						//displace mob
						for(int i : PathFinder.NEIGHBOURS8()){
							if (Actor.findChar(m.pos+i) == null && Dungeon.level.passable[m.pos + i]){
								m.pos += i;
								m.sprite.point(m.sprite.worldToCamera(m.pos));
								break;
							}
						}
					}
				}
				Dungeon.level.occupyCell(hero );
				Dungeon.observe();
				GameScene.updateFog();
			}
            else {

                PathFinder.buildDistanceMap(Dungeon.level.entrance, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
                if (PathFinder.distance()[hero.pos] == Integer.MAX_VALUE){
                    cd *= 4;
                }
				TimekeepersHourglass.timeFreeze timeFreeze = hero.buff(TimekeepersHourglass.timeFreeze.class);
				if (timeFreeze != null) timeFreeze.disarmPressedTraps();
				Swiftthistle.TimeBubble timeBubble = hero.buff(Swiftthistle.TimeBubble.class);
				if (timeBubble != null) timeBubble.disarmPressedTraps();

				InterlevelScene.mode = InterlevelScene.Mode.RETURN;
				InterlevelScene.returnDepth = returnLevelId%1000;
				InterlevelScene.returnBranch = returnLevelId/1000;
				InterlevelScene.returnPos = returnPos;
				Game.switchScene( InterlevelScene.class );
                cd      *= 1.5F;
                blind   *= 1.5F;
                weak    *= 1.5F;
			}
            if (!FairyItems.inFairyRoom(hero)) {
                Buff.prolong(hero, Blindness.class, blind);
                Buff.prolong(hero, Weakness.class, weak);
            }
		}
        else if (action.equals( AC_TP )){
            GameScene.selectCell(caster);
        }
        else if (action.equals( AC_FAIRY )){
            int cd = 50;
            int front = ((RegularLevel) Dungeon.level).getRoom(FairyRoom.class).itemPos;
            PathFinder.buildDistanceMap(front, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
            if (PathFinder.distance()[curUser.pos] == Integer.MAX_VALUE){
                cd *= 4;
            }
            ScrollOfTeleportation.appear( hero, front );
            for(Mob m : Dungeon.level.mobs){
                if (m.pos == hero.pos){
                    //displace mob
                    for(int i : PathFinder.NEIGHBOURS8()){
                        if (Actor.findChar(m.pos+i) == null && Dungeon.level.passable[m.pos + i]){
                            m.pos += i;
                            m.sprite.point(m.sprite.worldToCamera(m.pos));
                            break;
                        }
                    }
                }
            }
            this.cd = cd;
            Dungeon.level.occupyCell(hero );
            Dungeon.observe();
            GameScene.updateFog();
        }
        lockchB();
	}

    private boolean outMap(int cell){
        Point p = Dungeon.level.cellToPoint(cell);
        return p.x <= 0 || p.y <= 0 || p.x >= Dungeon.level.width() - 1 || p.y >= Dungeon.level.height() - 1;
    }
    private final CellSelector.Listener caster = new CellSelector.Listener(){

        @Override
        public void onSelect(Integer target) {
            if (target == null)
                return;
            if (outMap(target))
                return;
            if (Dungeon.level.solid[target] && !Dungeon.level.passable[target] && !Dungeon.level.avoid[target])
                return;
            if (Dungeon.level.avoid[target] && Dungeon.hero().buff(Levitation.class) == null )
                return;
            if (Dungeon.level.visited[target] || Dungeon.level.mapped[target]){
                int cd = 50;
                PathFinder.buildDistanceMap(target, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
                if (PathFinder.distance()[curUser.pos] == Integer.MAX_VALUE){
                    cd *= 4;
                }
                ScrollOfTeleportation.appear( Dungeon.hero(), target );
                for(Mob m : Dungeon.level.mobs){
                    if (m.pos == Dungeon.hero().pos){
                        //displace mob
                        for(int i : PathFinder.NEIGHBOURS8()){
                            if (Actor.findChar(m.pos+i) == null && Dungeon.level.passable[m.pos + i]){
                                m.pos += i;
                                m.sprite.point(m.sprite.worldToCamera(m.pos));
                                break;
                            }
                        }
                    }
                }
                Dungeon.level.occupyCell( Dungeon.hero() );
                Dungeon.observe();
                GameScene.updateFog();
                LloydsBeacon.this.cd = cd;
                float blind = 10;
                float weak  = 50;
                if (!FairyItems.inFairyRoom(Dungeon.hero())) {
                    Buff.prolong(Dungeon.hero(), Blindness.class, blind);
                    Buff.prolong(Dungeon.hero(), Weakness.class, weak);
                }
            }

        }

        @Override
        public String prompt() {
            return Messages.get(EtherealChains.class, "prompt");
        }
    };
	protected CellSelector.Listener zapper = new  CellSelector.Listener() {

		@Override
		public void onSelect(Integer target) {

			if (target == null) return;

            final Ballistica bolt = new Ballistica( curUser.pos, target, Ballistica.MAGIC_BOLT );
            final Char ch = Actor.findChar(bolt.collisionPos);

            if (ch == null) return;
            Sample.INSTANCE.play(Assets.Sounds.ZAP);
            Invisibility.dispel();
            charge -= Dungeon.cur().depth / 20 + 1;
            updateQuickslot();
            
            PathFinder.buildDistanceMap(Dungeon.level.entrance, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
            if (ch == Dungeon.hero() && PathFinder.distance()[ch.pos] == Integer.MAX_VALUE){
                GLog.w( Messages.get(LloydsBeacon.class, "preventing") );
                return;
            }
            if (ch == curUser) {
                ScrollOfTeleportation.teleportChar(curUser);
                curUser.spendAndNext(1f);
            } else {
                curUser.sprite.zap(bolt.collisionPos);
                curUser.busy();

                MagicMissile.boltFromChar(curUser.sprite.parent,
                        MagicMissile.BEACON,
                        curUser.sprite,
                        bolt.collisionPos,
                        new Callback() {
                            @Override
                            public void call() {
                                int count = 10;
                                int pos;
                                do {
                                    pos = Dungeon.level.randomRespawnCell(ch);
                                    if (count-- <= 0) {
                                        break;
                                    }
                                } while (pos == -1);

                                if (pos == -1 || Dungeon.bossLevel()) {

                                    GLog.w(Messages.get(ScrollOfTeleportation.class, "no_tele"));

                                } else if (ch.properties().contains(Char.Property.IMMOVABLE)) {

                                    GLog.w(Messages.get(LloydsBeacon.class, "tele_fail"));

                                } else {

                                    ch.pos = pos;
                                    if (ch instanceof Mob && ((Mob) ch).state == ((Mob) ch).HUNTING) {
                                        ((Mob) ch).state = ((Mob) ch).WANDERING;
                                    }
                                    ch.sprite.place(ch.pos);
                                    ch.sprite.visible = Dungeon.level.heroFOV[pos];

                                    if (ch.alignment != curUser.alignment && FairyItems.inFairyRoom(curUser))
                                        Buff.prolong(ch, Paralysis.class, 5F);
                                }
                                Talent.onArtifactUsed(curUser);
                                curUser.spendAndNext(1f);
                            }
                        }
                );
            }
        }

		@Override
		public String prompt() {
			return Messages.get(LloydsBeacon.class, "prompt");
		}
	};

	@Override
	protected ArtifactBuff passiveBuff() {
		return new beaconRecharge();
	}
	
	@Override
	public void charge(Hero target, float amount) {
		if (charge < chargeCap){
			partialCharge += 0.25f*amount;
			while (partialCharge >= 1){
				partialCharge--;
				charge++;
				updateQuickslot();
			}
		}
	}

	@Override
	public Item upgrade() {
		if (level() == levelCap) return this;
		chargeCap ++;
		return super.upgrade();
	}

	@Override
	public String desc() {
		String desc = super.desc();
		if (returnLevelId != -1){
            int levelId = returnLevelId;
            int sub = levelId/1000 ;
            levelId %= 1000;
            String level;
            level = String.valueOf(levelId);
            if (sub!=0)
                level+="/"+sub;
			desc += "\n\n" + Messages.get(this, "desc_set", level);
		}
        if (cd > 0){
            desc += "\n\n" + Messages.get(this, "CDing", cd);
        }
		return desc;
	}
	
	private static final Glowing WHITE = new Glowing( 0xFFFFFF );
	
	@Override
	public Glowing glowing() {
		return returnLevelId != -1 ? WHITE : null;
	}

	public class beaconRecharge extends ArtifactBuff{
		@Override
		public boolean act() {
            lockcha();
			LockedFloor lock = target.buff(LockedFloor.class);
			if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
				partialCharge += 1 / (100f - (chargeCap - charge)*10f);

				while (partialCharge >= 1) {
					partialCharge --;
					charge ++;

					if (charge == chargeCap){
						partialCharge = 0;
					}
				}
			}

            if (cd > 0 && !cursed)
                cd--;
			updateQuickslot();
			spend( TICK );
			return true;
		}
	}

    protected CoolDownTracker coolDownTracker;
    @Override
    public void Tracker(Char owner){
        super.Tracker(owner);
        if (coolDownTracker == null) {
            coolDownTracker = new CoolDownTracker();
            coolDownTracker.attachTo(owner);
        }
	}
	
    @Override
    public void stopTrack(){
        super.stopTrack();
        if (coolDownTracker != null) {
            coolDownTracker.detach();
        }
    }
	public class CoolDownTracker extends Buff{
		@Override
		public void detach() {
			super.detach();
			coolDownTracker = null;
		}
		@Override
		public boolean act() {
            if (cd > 0 && !cursed)
                cd--;
			spend( 2 * TICK );
			return true;
		}
	}
    public static void proc(Char defender) {
        //复制的转移
        float procChance = 1/20f;
        if (Random.Float() < procChance && !defender.properties().contains(Char.Property.IMMOVABLE)){

            int oldpos = defender.pos;
            if (ScrollOfTeleportation.teleportChar(defender)){
                if (Dungeon.level.heroFOV[oldpos]) {
                    CellEmitter.get( oldpos ).start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
                }

                if (defender instanceof Mob && ((Mob) defender).state == ((Mob) defender).HUNTING){
                    ((Mob) defender).state = ((Mob) defender).WANDERING;
                }
            }
        }
    }

    public static void proc(Hero hero) {
        if ( Random.Int(20) == 0 ){
            ScrollOfTeleportation.teleportChar(hero);
        }
    }
}
