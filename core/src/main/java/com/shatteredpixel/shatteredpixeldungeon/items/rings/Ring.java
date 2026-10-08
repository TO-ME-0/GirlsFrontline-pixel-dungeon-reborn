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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemStatusHandler;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.ColorItem;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndStartGame;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Ring extends KindofMisc implements ColorItem {
	
	protected Buff buff;

	private static final HashMap<String, Integer> gems = new HashMap<String, Integer>() {
		{
			put("garnet",ItemSpriteSheet.RING_GARNET);
			put("ruby",ItemSpriteSheet.RING_RUBY);
			put("topaz",ItemSpriteSheet.RING_TOPAZ);
			put("emerald",ItemSpriteSheet.RING_EMERALD);
			put("onyx",ItemSpriteSheet.RING_ONYX);
			put("opal",ItemSpriteSheet.RING_OPAL);
			put("tourmaline",ItemSpriteSheet.RING_TOURMALINE);
			put("sapphire",ItemSpriteSheet.RING_SAPPHIRE);
			put("amethyst",ItemSpriteSheet.RING_AMETHYST);
			put("quartz",ItemSpriteSheet.RING_QUARTZ);
			put("agate",ItemSpriteSheet.RING_AGATE);
			put("diamond",ItemSpriteSheet.RING_DIAMOND);
		}
	};
	
	private static ItemStatusHandler<Ring> handler;
	
	private String gem;
	
	//rings cannot be 'used' like other equipment, so they ID purely based on exp
	private float levelsToID = 1;
	
	@SuppressWarnings("unchecked")
	public static void initGems() {
		handler = new ItemStatusHandler<>( (Class<? extends Ring>[])Generator.Category.RING.classes, gems );
	}
	
	public static void save( Bundle bundle ) {
		handler.save( bundle );
	}

	public static void saveSelectively( Bundle bundle, ArrayList<Item> items ) {
		handler.saveSelectively( bundle, items );
	}
	
	@SuppressWarnings("unchecked")
	public static void restore( Bundle bundle ) {
		handler = new ItemStatusHandler<>( (Class<? extends Ring>[])Generator.Category.RING.classes, gems, bundle );
	}
	
	public Ring() {
		super();
		reset();
	}
	@Override
	public int TextGuessingLevel(){
		int lvl = super.TextGuessingLevel() + 1;
		if (cursed && cursedKnown)
			lvl = Math.min(0, lvl - 3);
		return lvl;
	}

	@Override
	public int buffedVisiblyUpgraded() {
		return buffedLvl(visiblyUpgraded());
	}

	//anonymous rings are always IDed, do not affect ID status,
	//and their sprite is replaced by a placeholder if they are not known,
	//useful for items that appear in UIs, or which are only spawned for their effects
	protected boolean anonymous = false;
	public void anonymize(){
		if (!isKnown()) image = ItemSpriteSheet.RING_HOLDER;
		anonymous = true;
	}
	
	public void reset() {
		super.reset();
		levelsToID = 1;
		if (handler != null && handler.contains(this)){
			image = handler.image(this);
			gem = handler.label(this);
		}
	}
	
	public void activate( Char ch ) {
		buff = buff();
		buff.attachTo( ch );
		Tracker(ch);
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {

			hero.remove( buff );
			buff = null;

			return true;

		} else {

			return false;

		}
	}
	
	public boolean isKnown() {
		return anonymous || (handler != null && handler.isKnown( this ));
	}
	
	public void setKnown() {
		if (!anonymous) {
			if (!isKnown()) {
				handler.know(this);
			}

			if (Dungeon.hero().isAlive()) {
				Catalog.setSeen(getClass());
			}
		}
	}

    public void setIgnore(){
        if (levelKnown || cursedKnown){
            levelKnown = false;
            cursedKnown = false;
			resetGuessingLevel();
			Dungeon.guessType.remove(getClass());
        }else if (isKnown()){
            handler.ignore(this);
        }else {
            initGems();
        }
    }

	@Override
	public String name() {
		if (isKnown() || Dungeon.isGameMode(WndStartGame.GameMode.IDENTIFY))
			return super.name();
		if (showGuess())
			return "? " + super.name();
		return Messages.get(this, gem);
	}

	@Override
	public String info(){
        String note = NoteGet();

		String desc = isKnown() ?
                super.info() :
                note+Messages.get(this, "unknown_desc");
		
		if (cursed && isEquipped( Dungeon.hero() )) {
			desc += "\n\n" + Messages.get(Ring.class, "cursed_worn");
			
		} else if (cursed && cursedKnown) {
			desc += "\n\n" + Messages.get(Ring.class, "curse_known");
			
		} else if (!isIdentified() && cursedKnown){
			desc += "\n\n" + Messages.get(Ring.class, "not_cursed");
			
		}
		
		if (isKnown()) {
			desc += "\n\n" + statsInfo();
		}
        if (overLoad != OverLoad.NONE)
            desc += "\n\n" + Messages.get(Item.class, overLoad.name(), overLoadLeft);
		
		return desc;
	}
    public int combinedBonus(Hero hero) {
        int bonus = 0;
        if (hero.belongings.ring() != null && hero.belongings.ring().getClass() == this.getClass()) {
            bonus += hero.belongings.ring().soloBonus();
        }

        if (hero.belongings.misc() != null && hero.belongings.misc().getClass() == this.getClass()) {
            bonus += ((Ring)hero.belongings.misc()).soloBonus();
        }

        return bonus;
    }
    public int combinedBuffedBonus(Hero hero) {
        int bonus = 0;
        if (hero.belongings.ring() != null && hero.belongings.ring().getClass() == this.getClass()) {
            bonus += hero.belongings.ring().soloBuffedBonus();
        }

        if (hero.belongings.misc() != null && hero.belongings.misc().getClass() == this.getClass()) {
            bonus += ((Ring)hero.belongings.misc()).soloBuffedBonus();
        }

        return bonus;
    }
	protected boolean canGuessType(){
        Ring other;
		if (Dungeon.hero().belongings.ring() == this) {
            if (Dungeon.hero().belongings.misc() instanceof Ring)
				other = (Ring) Dungeon.hero().belongings.misc();
			else
				other = null;
		}
		else if (Dungeon.hero().belongings.misc() == this){
            if (Dungeon.hero().belongings.ring() == null)
				return false;
			other = Dungeon.hero().belongings.ring();
		}
		else
			return false;
		if (other == null)
			return true;
		if (other.getClass() == getClass())
			return true;
		if (other.isKnown() || other.isGuess())
			return true;
		return false;
	}
	protected String statsInfo(){
		if (isIdentified() && isEquipped(Dungeon.hero())){
			Ring other = null;
			if (Dungeon.hero().belongings.ring() == this){
				if (Dungeon.hero().belongings.misc() instanceof Ring)
					other = (Ring) Dungeon.hero().belongings.misc();
			}
			else
				other = Dungeon.hero().belongings.ring();
			if (other != null && other.getClass() == getClass()){
				String cause;
				int lvl;
				if (other.cursed) {
					cause = "装备两个同类瞄准镜后，以已鉴定的瞄准镜的文案判断。";
					lvl = Math.min(2, other.level());
				}
				else {
					cause = "装备两个同类瞄准镜后，以已鉴定的瞄准镜的文案精准判断。";
					lvl = other.level();
				}
				other.guessLevel(lvl, cause);
			}
		}
		return "";
	}
	public static <T extends Ring> void guessSignalRing(Hero hero, Class<T> type, boolean guessByTime){
		if (Dungeon.hero() == null)
			return;
		T ring = null;
		if (hero.belongings.ring() != null && hero.belongings.ring().getClass() == type
				&& ( hero.belongings.misc() == null || hero.belongings.misc().getClass() != type) ){
			ring = (T) hero.belongings.ring();
		} else if (hero.belongings.misc() != null && hero.belongings.misc().getClass() == type
				&& ( hero.belongings.ring() == null || hero.belongings.ring().getClass() != type) ) {
			ring = (T) hero.belongings.misc();
		}

		if (ring == null)
			return;

		String cause = null;
		int lvl		 = Integer.MIN_VALUE;
		if (!guessByTime || hero.wholeTime()){
			if (!ring.cursed || ring.level() < 2) {
				cause = "以瞄准镜有实际生效等级精准判断。";
				lvl = ring.level();
				ring.guessType(guessByTime ? "在回合盘完整的情况下对回合盘造成影响，从而判断瞄准镜类别。" : "以信息获取判断瞄准镜类别。");
			}
			else if ( ring.isKnown() || ring.isGuess() ) {
				cause = "以知道瞄准镜类别而粗略判断。";
				lvl = Math.min(2, ring.level());
			}
			ring.guessLevel(lvl, cause);
		}
	}
	@Override
	public Item upgrade() {
		super.upgrade();
		
		if (Random.Int(3) == 0) {
			cursed = false;
		}
		
		return this;
	}
	
	@Override
	public boolean isIdentified() {
		return super.isIdentified() && isKnown();
	}
	
	@Override
	public Item identify( boolean byHero ) {
		setKnown();
		levelsToID = 0;
		return super.identify(byHero);
	}

	//迁移 3.3.8：鉴定进度已达标（可在遗忘碎片下令被动鉴定就绪）
	public void setIDReady(){
		levelsToID = -1;
	}

	public boolean readyToIdentify(){
		return !isIdentified() && levelsToID <= 0;
	}

	@Override
	public Item random() {
		//+0: 66.67% (2/3)
		//+1: 26.67% (4/15)
		//+2: 6.67%  (1/15)
		int n = 0;
		if (Random.Int(3) == 0) {
			n++;
			if (Random.Int(5) == 0){
				n++;
			}
		}
		level(n);
		
		//30% chance to be cursed
		if (Random.Float() < 0.3f) {
			cursed = true;
		}
		
		return this;
	}
	
	public static HashSet<Class<? extends Ring>> getKnown() {
		return handler.known();
	}
	
	public static HashSet<Class<? extends Ring>> getUnknown() {
		return handler.unknown();
	}
	
	public static boolean allKnown() {
		return handler.known().size() == Generator.Category.RING.classes.length;
	}
	
	@Override
	public int value() {
		int price = 75;
		if (cursed && cursedKnown) {
			price /= 2;
		}
		if (levelKnown) {
			if (level() > 0) {
				price *= (level() + 1);
			} else if (level() < 0) {
				price /= (1 - level());
			}
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}
	
	protected RingBuff buff() {
		return null;
	}

	private static final String LEVELS_TO_ID    = "levels_to_ID";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVELS_TO_ID, levelsToID );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		levelsToID = bundle.getFloat( LEVELS_TO_ID );
	}
	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		if (isIdentified() || !isEquipped(hero)) return;
		levelPercent *= Talent.itemIDSpeedFactor(hero, this);
		//becomes IDed after 1 level
		levelsToID -= levelPercent;
		if (levelsToID <= 0){
			if (ShardOfOblivion.passiveIDDisabled()){
				if (levelsToID > -1){
					GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready"), name());
				}
				setIDReady();
			} else {
				identify();
				GLog.p( Messages.get(Ring.class, "identify", toString()) );
				Badges.validateItemLevelAquired( this );
			}
		}
	}

	@Override
	public int buffedLvl(int lvl) {
		int level = super.buffedLvl(lvl);
        EnhancedRings buff = Dungeon.hero().buff(EnhancedRings.class);
		if ( buff != null ){
			Ring other = null;
			if (Dungeon.hero().belongings.ring() == this) {
				if (Dungeon.hero().belongings.misc() instanceof Ring)
					other = (Ring) Dungeon.hero().belongings.misc();
			}
			else if (Dungeon.hero().belongings.misc() == this)
				other = Dungeon.hero().belongings.ring();

			if (other == null || buff.level != 3)
			    level += buff.level;
            else {
				if (level() > other.level())
					level += 3;
				else if (level() < other.level())
					level += 1;
				else {
					if (Dungeon.hero().belongings.misc() == this)
						level += 3;
					else
						level += 1;
				}
            }
		}
		return level;
	}

	public static int getBonus(Char target, Class<?extends RingBuff> type){
		int bonus = 0;
		for (RingBuff buff : target.buffs(type)) {
			bonus += buff.level();
		}
		return bonus;
	}

	public static int getBuffedBonus(Char target, Class<?extends RingBuff> type){
		int bonus = 0;
		for (RingBuff buff : target.buffs(type)) {
			bonus += buff.buffedLvl();
		}
		return bonus;
	}
	
	public int soloBonus(){
		if (cursed){
			return Math.min( 0, Ring.this.level()-2 );
		} else {
			int lvl = Ring.this.level()+1;
            if (Ring.this.level()<0) {
                lvl = Ring.this.level()-1;
            }
            return lvl;
		}
	}

	public int soloBuffedBonus(){
		if (cursed){
			return Math.min( 0, buffedLvl() - 2 );
		} else {
            int lvl = buffedLvl() + 1;
            if (buffedLvl() < 0) {
                lvl = buffedLvl() - 1;
            }
            //-1、0的buff等级去除，以立刻获得对应收益
			return lvl;
		}
	}

	public class RingBuff extends Buff {
		public Ring ring(){
			return Ring.this;
		}
		@Override
		public boolean act() {
			
			spend( TICK );
			
			return true;
		}

		public int level(){
			return Ring.this.soloBonus();
		}

		public int buffedLvl(){
			return Ring.this.soloBuffedBonus();
		}

	}
}
