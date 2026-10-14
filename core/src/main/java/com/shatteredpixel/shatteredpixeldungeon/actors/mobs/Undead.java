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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SacrificialFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.ActiveAbstract;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.Buff_Statistics;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.UndeadSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Undead extends Mob {
	
	{
		spriteClass = UndeadSprite.class;
		faction = Faction.SANGVIS_FERRI;
		
		HP = HT = 45;
		defenseSkill = 20;
		
		EXP = 5;
		maxLvl = 20;
		
		SLEEPING = new Sleeping();
		WANDERING = new Wandering();
		state = SLEEPING;

		loot = Gold.class;
		lootChance = 0.2f;
		
		properties.add(Property.UNDEAD);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 16, 22 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 24;
	}

	@Override
	public int drRoll() {
		return Random.NormalIntRange(0, 4);
	}

	@Override
	public float spawningWeight() {
		return 0.5f;
	}

	private int timesDowned = 0;
	protected int partnerID = -1;

	private static final String PARTNER_ID = "partner_id";
	private static final String TIMES_DOWNED = "times_downed";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( PARTNER_ID, partnerID );
		bundle.put( TIMES_DOWNED, timesDowned );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		partnerID = bundle.getInt( PARTNER_ID );
		timesDowned = bundle.getInt( TIMES_DOWNED );
	}
	
	@Override
	protected boolean act() {
		//create a child
		if (partnerID == -1){
			
			ArrayList<Integer> candidates = new ArrayList<>();
			
			int[] neighbours = {pos + 1, pos - 1, pos + Dungeon.level.width(), pos - Dungeon.level.width()};
			for (int n : neighbours) {
				if (Dungeon.level.passable[n]
						&& Actor.findChar( n ) == null
						&& (!Char.hasProp(this, Property.LARGE) || Dungeon.level.openSpace[n])) {
					candidates.add( n );
				}
			}
			
			if (!candidates.isEmpty()){
				Undead child = new Undead();
				child.partnerID = this.id();
				this.partnerID = child.id();
				if (state != SLEEPING) {
					child.state = child.WANDERING;
				}
				
				child.pos = Random.element( candidates );

				GameScene.add( child );
				Dungeon.level.occupyCell(child);
				
				if (sprite.visible) {
					Actor.addDelayed( new Pushing( child, pos, child.pos ), -1 );
				}

				for (Buff b : buffs(ChampionEnemy.class)){
					Buff.affect( child, b.getClass());
				}

			}
			
		}
		return super.act();
	}

	private boolean beingLifeLinked = false;

	@Override
	public void die(Object cause) {
		if (cause != Chasm.class && cause != UndeadLifeLink.class && !Dungeon.level.pit[pos]){
			Undead nearby = UndeadLifeLink.searchForHost(this);
			if (nearby != null){
				beingLifeLinked = true;
				Actor.remove(this);
				Dungeon.level.mobs.remove( this );
				timesDowned++;
				Buff.append(nearby, UndeadLifeLink.class).set(timesDowned*5, this);
				((UndeadSprite)sprite).crumple();
				beingLifeLinked = false;
				return;
			}
		}

		super.die(cause);
	}

    @Override
    public void MustDie(Object cause ) {
        //不可用super.MustDie
        super.die(cause);
    }
	@Override
	protected synchronized void onRemove() {
		if (beingLifeLinked) {
			for (Buff buff : buffs()) {
				//ally buffs, champion, and king damager are preserved when removed via life link
				if(buff instanceof SacrificialFire.Marked){
					Buff.prolong(this, SacrificialFire.Marked.class, 5F);
				}else if (!(buff instanceof AllyBuff)
						&& (!(buff instanceof ChampionEnemy))
						&& !(buff instanceof Disloli.KingDamager)
						&& !(buff instanceof ActiveAbstract.NotDetach)
						&& !(buff instanceof Buff_Statistics)) {
					buff.detach();
				}
			}
		} else {
			super.onRemove();
		}
	}

	private class Sleeping extends Mob.Sleeping {
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			Undead partner = (Undead) Actor.findById( partnerID );
			if (partner != null && partner.state != partner.SLEEPING){
				state = WANDERING;
				target = partner.pos;
				return true;
			} else {
				return super.act( enemyInFOV, justAlerted );
			}
		}
	}
	
	private class Wandering extends Mob.Wandering {
		
		@Override
		protected boolean continueWandering() {
			enemySeen = false;
			
			Undead partner = (Undead) Actor.findById( partnerID );
			if (partner != null && (partner.state != partner.WANDERING || Dungeon.level.distance( pos,  partner.target) > 1)){
				target = partner.pos;
				int oldPos = pos;
				if (getCloser( target )){
					spendMove( 1 / speed() );
					return moveSprite( oldPos, pos );
				} else {
					spendMove( TICK );
					return true;
				}
			} else {
				return super.continueWandering();
			}
		}
	}

	public static class UndeadLifeLink extends Buff{

		private Undead undead;
		private int turnsToRevive;

		@Override
		public boolean act() {
			undead.sprite.visible = Dungeon.level.heroFOV[undead.pos];

			if (target.alignment != undead.alignment){
				detach();
				return true;
			}

			if (target.fieldOfView == null){
				target.fieldOfView = new boolean[Dungeon.level.length()];
				Dungeon.level.updateFieldOfView( target, target.fieldOfView );
			}

			if (!target.fieldOfView[undead.pos] && Dungeon.level.distance(undead.pos, target.pos) >= 4){
				detach();
				return true;
			}

			if (Dungeon.level.pit[undead.pos]){
				super.detach();
				undead.die(this);
				return true;
			}

			turnsToRevive--;
			if (turnsToRevive <= 0){
				if (Actor.findChar( undead.pos ) != null) {
					ArrayList<Integer> candidates = new ArrayList<>();
					for (int n : PathFinder.NEIGHBOURS8()) {
						int cell = undead.pos + n;
						if (Dungeon.level.passable[cell]
								&& Actor.findChar( cell ) == null
								&& (!Char.hasProp(undead, Property.LARGE) || Dungeon.level.openSpace[cell])) {
							candidates.add( cell );
						}
					}
					if (candidates.size() > 0) {
						int newPos = Random.element( candidates );
						Actor.addDelayed( new Pushing( undead, undead.pos, newPos ), -1 );
						undead.pos = newPos;

					} else {
						spend(TICK);
						return true;
					}
				}
				undead.HP = Math.round(undead.HT/10f);
				Actor.add(undead);
				undead.timeToNow();
				Dungeon.level.mobs.add(undead);
				Dungeon.level.occupyCell( undead );
				undead.sprite.idle();
				super.detach();
				return true;
			}

			spend(TICK);
			return true;
		}

		public void set(int turns, Undead undead){
			this.undead = undead;
			turnsToRevive = turns;
		}

		@Override
		public void fx(boolean on) {
			if (on && undead != null && undead.sprite == null){
				GameScene.addSprite(undead);
				((UndeadSprite)undead.sprite).crumple();
			}
		}

		@Override
		public void detach() {
			super.detach();
			Undead newHost = searchForHost(undead);
			if (newHost != null){
				attachTo(newHost);
				timeToNow();
			} else {
				undead.die(this);
			}
		}

		private static final String UNDEAD = "undead";
		private static final String LEFT  = "left";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(UNDEAD, undead);
			bundle.put(LEFT, turnsToRevive);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			undead = (Undead) bundle.get(UNDEAD);
			turnsToRevive = bundle.getInt(LEFT);
		}

		public static Undead searchForHost(Undead dieing){

			for (Char ch : Actor.chars()){
				if (ch != dieing && ch instanceof Undead && ch.alignment == dieing.alignment){
					if (ch.fieldOfView == null){
						ch.fieldOfView = new boolean[Dungeon.level.length()];
						Dungeon.level.updateFieldOfView( ch, ch.fieldOfView );
					}
					if (ch.fieldOfView[dieing.pos] || Dungeon.level.distance(ch.pos, dieing.pos) < 4){
						return (Undead) ch;
					}
				}
			}
			return null;
		}
	}
}
