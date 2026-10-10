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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PinCushion;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.HuntressTalent;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ParchmentScrap;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.RogueTalent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

abstract public class MissileWeapon extends Weapon {

	{
		stackable = true;
		quantity = defaultQuantity();
		bones = true;

		defaultAction = AC_THROW;
		usesTargeting = true;
	}
	
	//标识投掷武器“套装”。同一套升级投武共享该 ID；0 表示无数量限制（兼容旧版投武/飞镖等）
	public long setID = new SecureRandom().nextLong();

	//是否仅用于触发效果而不掉落（如技能召唤的投武）
	public boolean spawnedForEffect = false;

	//主要用于跟踪“投掷最后一个升级投武”的警告
	public boolean extraThrownLeft = false;
	
	protected boolean sticky = true;
	
	public static final float MAX_DURABILITY = 100;
	protected float durability = MAX_DURABILITY;
	protected float baseUses = 8;
	
	public boolean holster;
	
	//used to reduce durability from the source weapon stack, rather than the one being thrown.
	protected MissileWeapon parent;

	//迁移 3.3.8：供基类 Weapon 访问来源投武堆（异包 protected 不可见）
	public MissileWeapon getParent(){
		return parent;
	}
	
	//迁移 3.3.8：投武鉴定所需使用次数为近战一半
	@Override
	protected int usesToID(){
		return 10;
	}

	@Override
	public int min() {
		return Math.max(0, min( buffedLvl() + RingOfSharpshooting.levelDamageBonus(Dungeon.hero()) ));
	}
	
	@Override
	public int min(int lvl) {
		return  2 * tier +                      //base
				(tier == 1 ? lvl : 2*lvl);      //level scaling
	}
	
	@Override
	public int max() {
		return Math.max(0, max( buffedLvl() + RingOfSharpshooting.levelDamageBonus(Dungeon.hero()) ));
	}
	
	@Override
	public int max(int lvl) {
		return  5 * tier +                      //base
				(tier == 1 ? 2*lvl : tier*lvl); //level scaling
	}
	
	//迁移 3.3.8：投出的子武等级/加成跟随父武器
	@Override
	public int buffedLvl() {
		if (parent != null) {
			return parent.buffedLvl();
		} else {
			return super.buffedLvl();
		}
	}

	@Override
	public int buffedLvl(int lvl) {
		if (parent != null) {
			return parent.buffedLvl(lvl);
		} else {
			return super.buffedLvl(lvl);
		}
	}
	
	public int STRReq(int lvl){
		//迁移 3.3.8：1-tier 武器力量需求-1；masteryPotionBonus 由基类 Weapon.STRReq(boolean)/baseSTRReq() 统一处理，避免双重计算
		return STRReq(tier, lvl) - 1; //1 less str than normal for their tier
	}
	@Override
	public Item degrade() {
		if (Dungeon.hero() != null)
			Buff.affect(Dungeon.hero(), UpgradedSetTracker.class).levelThresholds.put(setID, trueLevel()-1);
		return super.degrade();
	}
	@Override
	public Item upgrade() {
		return upgrade(false);
	}

	//迁移 3.3.8：投武升级统一入口（setID != 0 整组升级补数量；setID == 0 分离一件升级不补数量）
	@Override
	public Item upgrade(boolean enchant) {
		if (!bundleRestoring) {
			durability = MAX_DURABILITY;

			//setID != 0（套装投武）：迁移 3.3.8 机制，整组升级并补齐数量
			if (setID != 0) {
				if (Dungeon.hero() != null) {
					Buff.affect(Dungeon.hero(), UpgradedSetTracker.class).levelThresholds.put(setID, trueLevel()+1);
				}
				extraThrownLeft = false;
				quantity = defaultQuantity();
				updateQuickslot();
				//thrown weapons don't get curse weakened
				boolean wasCursed = cursed;
				super.upgrade(enchant);
				if (wasCursed && hasCurseEnchant()){
					cursed = wasCursed;
				}
				return this;
			}

			//setID == 0（无套装限制投武）：保留分离一件升级机制，不补数量
			if (quantity > 1) {
				MissileWeapon upgraded = (MissileWeapon) split(1);
				upgraded.parent = null;
				
				upgraded = (MissileWeapon) upgraded.upgrade();
				
				//try to put the upgraded into inventory, if it didn't already merge
				if (upgraded.quantity() == 1 && !upgraded.collect()) {
					Dungeon.level.drop(upgraded, Dungeon.hero().pos);
				}
				updateQuickslot();
				return upgraded;
			} else {
				//thrown weapons don't get curse weakened
				boolean wasCursed = cursed;
				super.upgrade(enchant);
				if (wasCursed && hasCurseEnchant()){
					cursed = wasCursed;
				}
				
				Item similar = Dungeon.hero().belongings.getSimilar(this);
				if (similar != null){
					detach(Dungeon.hero().belongings.backpack);
					Item result = similar.merge(this);
					updateQuickslot();
					return result;
				}
				updateQuickslot();
				return this;
			}
			
		} else {
			return super.upgrade(enchant);
		}
	}
	
	//该套装投武的默认数量（用于 setID 防刷取的合并上限判定）
	public int defaultQuantity(){
		return 3;
	}

	@Override
	public Item virtual() {
		Item item = super.virtual();

		((MissileWeapon)item).setID = setID;

		return item;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.remove( AC_EQUIP );
		return actions;
	}
	
	@Override
	public boolean collect(Bag container) {
		if (container instanceof MagicalHolster) holster = true;
		return super.collect(container);
	}
	
	@Override
	public int throwPos(Hero user, int dst) {

		boolean projecting = hasEnchant(Projecting.class, user);
		// 女猎（隼）共享附魔：投掷时概率借用灵弓附魔（实现见 HuntressTalent）
		if (!projecting && HuntressTalent.rollSharedEnchantment(user)){
			if (this instanceof Dart && ((Dart) this).crossbowHasEnchant(Dungeon.hero())){
				//do nothing
			} else {
				SpiritBow bow = Dungeon.hero().belongings.getItem(SpiritBow.class);
				if (bow != null && bow.hasEnchant(Projecting.class, user)) {
					projecting = true;
				}
			}
		}

		if (projecting && !Dungeon.level.solid[dst] && Dungeon.level.distance(user.pos, dst) <= 4){
			return dst;
		} else {
			return super.throwPos(user, dst);
		}
	}

	@Override
	public float accuracyFactor(Char owner) {
		float accFactor = super.accuracyFactor(owner);
		// 盗贼（UMP9）弹射势能：疾跑中投掷命中（实现见 RogueTalent）
		if (owner instanceof Hero){
			accFactor = RogueTalent.projectileAccuracyFactor((Hero) owner, accFactor);
		}
		return accFactor;
	}

	//同步 3.3.8：基于攻击者与目标是否相邻的命中系数，子类可覆写（如回旋武器）
	public float adjacentAccFactor(Char owner, Char target){
		if (Dungeon.level.adjacent( owner.pos, target.pos )) {
			if (owner instanceof Hero){
				return HuntressTalent.pointBlankAdjacentAccuracy((Hero) owner);
			} else {
				return 0.5f;
			}
		} else {
			return 1.5f;
		}
	}

	@Override
	public void doThrow(Hero hero) {
		parent = null; //reset parent before throwing, just incase
		//迁移 3.3.8：投掷最后一个升级/附魔投武时弹出破坏确认框
		if (((levelKnown && level() > 0) || hasGoodEnchant() || masteryPotionBonus || enchantHardened)
				&& !extraThrownLeft && quantity() == 1 && durabilityLeft() <= durabilityPerUse()){
			GameScene.show(new WndOptions(new ItemSprite(this), Messages.titleCase(name()),
					Messages.get(MissileWeapon.class, "break_upgraded_warn_desc"),
					Messages.get(MissileWeapon.class, "break_upgraded_warn_yes"),
					Messages.get(MissileWeapon.class, "break_upgraded_warn_no")){
				@Override
				protected void onSelect(int index) {
					if (index == 0){
						MissileWeapon.super.doThrow(hero);
					} else {
						QuickSlotButton.cancel();
						InventoryPane.cancelTargeting();
					}
				}

				@Override
				public void onBackPressed() {
					super.onBackPressed();
					QuickSlotButton.cancel();
					InventoryPane.cancelTargeting();
				}
			});

		} else {
			super.doThrow(hero);
		}
	}

	@Override
	protected void onThrow( int cell ) {
		Char enemy = Actor.findChar( cell );
		if (enemy == null || enemy == curUser) {
            parent = null;
            // 女猎（隼）预知射击：投掷落空揭示落点（实现见 HuntressTalent）
            if (HuntressTalent.appliesSeerShot(curUser)) {
                HuntressTalent.applySeerShot(curUser, cell);
            }
            super.onThrow( cell );
		} else {
			if (!curUser.shoot( enemy, this )) {
				rangedMiss( cell );
			} else {
                if (overLoad == OverLoad.OVERLOADING)
				    overLoadLeft -= 10;
				rangedHit( enemy, cell );

			}
		}
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		// 女猎（隼）共享附魔：命中时概率借用灵弓附魔（实现见 HuntressTalent）
		if (attacker == Dungeon.hero() && HuntressTalent.rollSharedEnchantment(Dungeon.hero())){
			if (this instanceof Dart && ((Dart) this).crossbowHasEnchant(Dungeon.hero())){
				//do nothing
			} else {
				SpiritBow bow = Dungeon.hero().belongings.getItem(SpiritBow.class);
				if (bow != null && bow.enchantment != null && Dungeon.hero().buff(MagicImmune.class) == null) {
					damage = bow.enchantment.proc(this, attacker, defender, damage);
				}
			}
		}

		//迁移 3.3.8：命中时揭示诅咒（投武可通过韧性直觉即时鉴定）
		if ((cursed || hasCurseEnchant()) && !cursedKnown){
			GLog.n(Messages.get(this, "curse_discover"));
		}
		cursedKnown = true;
		if (parent != null) parent.cursedKnown = true;

		//instant ID with the right talent
		if (attacker == Dungeon.hero() && Dungeon.hero().pointsInTalent(Talent.SURVIVALISTS_INTUITION) == 2){
			usesLeftToID = Math.min(usesLeftToID, 0);
			availableUsesToID = Math.max(usesLeftToID, 0);
		}

		int result = super.proc(attacker, defender, damage);

		//handle ID progress over parent/child
		if (parent != null && parent.usesLeftToID > usesLeftToID){
			float diff = parent.usesLeftToID - usesLeftToID;
			parent.usesLeftToID -= diff;
			parent.availableUsesToID -= diff;
			if (usesLeftToID <= 0) {
				if (ShardOfOblivion.passiveIDDisabled()){
					parent.setIDReady();
				} else {
					parent.identify();
				}
			}
		} else if (parent != null && isIdentified() && !parent.isIdentified()){
			parent.identify();
		}

		if (attacker == Dungeon.hero() && !isIdentified() && ShardOfOblivion.passiveIDDisabled()){
			Buff.prolong(Dungeon.hero(), ShardOfOblivion.ThrownUseTracker.class, 50f);
		}

		return result;
	}

	@Override
	public Item random() {
		if (!stackable) return this;

		//迁移 3.3.8：数量由初始化块 defaultQuantity() 决定，这里仅生成等级与附魔/诅咒
		//+0: 75% (3/4)
		//+1: 20% (4/20)
		//+2: 5%  (1/20)
		int n = 0;
		if (Random.Int(4) == 0) {
			n++;
			if (Random.Int(5) == 0) {
				n++;
			}
		}
		level(n);

		//we use a separate RNG here so that variance due to things like parchment scrap
		//does not affect levelgen
		Random.pushGenerator(Random.Long());

			//30% chance to be cursed
			//10% chance to be enchanted
			float effectRoll = Random.Float();
			if (effectRoll < 0.3f * ParchmentScrap.curseChanceMultiplier()) {
				enchant(Enchantment.randomCurse());
				cursed = true;
			} else if (effectRoll >= 1f - (0.1f * ParchmentScrap.enchantChanceMultiplier())){
				enchant();
			}

		Random.popGenerator();

		return this;
	}
	
	@Override
	public float castDelay(Char user, int dst) {
		return delayFactor( user );
	}
	
	protected void rangedHit( Char enemy, int cell ){
		decrementDurability();
		if (durability > 0){
			//attempt to stick the missile weapon to the enemy, just drop it if we can't.
			if (sticky && enemy != null && enemy.isAlive() && enemy.alignment != Char.Alignment.ALLY){
				PinCushion p = Buff.affect(enemy, PinCushion.class);
				if (p.target == enemy){
					p.stick(this);
					return;
				}
			}
			Dungeon.level.drop( this, cell ).sprite.drop();
		}
	}
	
	protected void rangedMiss( int cell ) {
		parent = null;
		super.onThrow(cell);
	}

	public float durabilityLeft(){
		return durability;
	}

	public void repair( float amount ){
		durability += amount;
		durability = Math.min(durability, MAX_DURABILITY);
	}

	public void damage( float amount ){
		durability -= amount;
		durability = Math.max(durability, 1); //cannot break from doing this
	}

	public float durabilityPerUse(){
		return durabilityPerUse(level());
	}

	//迁移 3.3.8：子类可覆写带等级参数版本自行处理舍入
	protected boolean useRoundingInDurabilityCalc = true;

	public float durabilityPerUse( int level ){
		float usages;
		if (setID == 0)
			usages = baseUses * (float) Math.pow(3F, level);
		else
			usages = baseUses * (float) Math.pow(1.5F, level);

		// 女猎（隼）耐久弹药：投掷武器耐久系数（实现见 HuntressTalent）
		usages *= HuntressTalent.durableProjectilesFactor(Dungeon.hero());
		if (holster) {
			usages *= MagicalHolster.HOLSTER_DURABILITY_FACTOR;
		}

		//迁移 3.3.8：augment 联动，速度加成耐久、伤害削弱耐久
		usages /= augment.delayFactor(1f);

		if (Dungeon.hero() != null) usages *= RingOfSharpshooting.durabilityMultiplier( Dungeon.hero() );

		//at 100 uses, items just last forever.
		if (usages >= 100f) return 0;

		if (useRoundingInDurabilityCalc){
			usages = Math.round(usages);
			//add a tiny amount to account for rounding error for calculations like 1/3
			return (MAX_DURABILITY/usages) + 0.001f;
		} else {
			//rounding can be disabled for classes that override durability per use
			return MAX_DURABILITY/usages;
		}
	}
	
	protected void decrementDurability(){
		//if this weapon was thrown from a source stack, degrade that stack.
		//unless a weapon is about to break, then break the one being thrown
		if (parent != null){
			if (parent.durability <= parent.durabilityPerUse()){
				durability = 0;
				parent.durability = MAX_DURABILITY;
				parent.extraThrownLeft = false;
				if (parent.durabilityPerUse() < 100f) {
					GLog.n(Messages.get(this, "has_broken"));
				}
			} else {
				parent.durability -= parent.durabilityPerUse();
				if (parent.durability > 0 && parent.durability <= parent.durabilityPerUse()){
					if (level() <= 0)GLog.w(Messages.get(this, "about_to_break"));
					else             GLog.n(Messages.get(this, "about_to_break"));
				}
			}
			parent = null;
		} else {
			durability -= durabilityPerUse();
			if (durability > 0 && durability <= durabilityPerUse()){
				if (level() <= 0)GLog.w(Messages.get(this, "about_to_break"));
				else             GLog.n(Messages.get(this, "about_to_break"));
			} else if (durabilityPerUse() < 100f && durability <= 0){
				GLog.n(Messages.get(this, "has_broken"));
			}
		}
	}
	
	@Override
	public int damageRoll(Char owner) {
		int damage = augment.damageFactor(super.damageRoll( owner ));
		
		if (owner instanceof Hero) {
			int exStr = ((Hero)owner).STR() - STRReq();
			if (exStr > 0) {
				damage += Random.IntRange( 0, exStr );
			}
			// 盗贼（UMP9）弹射势能：疾跑中投掷伤害（实现见 RogueTalent）
			damage = RogueTalent.projectileDamage((Hero) owner, damage);
		}
		
		return damage;
	}
	
	@Override
	public void reset() {
		super.reset();
		durability = MAX_DURABILITY;
	}
	@Override
	public boolean isSimilar( Item item ) {
		if (trueLevel() != item.trueLevel())
			return false;
		if (getClass() != item.getClass())
			return false;
		if (setID != ((MissileWeapon) item).setID)
			return false;

		//量产型需查询过载状态。
		if (setID == 0)
			return overLoad == OverLoad.NONE && item.overLoad == OverLoad.NONE;
		else
			return true;
	}

	@Override
	public Item merge(Item other) {
		super.merge(other);
		if (isSimilar(other)) {
			extraThrownLeft = false;

			durability += ((MissileWeapon)other).durability;
			durability -= MAX_DURABILITY;
			while (durability <= 0){
				quantity -= 1;
				durability += MAX_DURABILITY;
			}

			//hashcode check is for old saves, 0 check is for unlimited sets (darts/legacy)
			if (quantity > defaultQuantity() && setID != 0){
				quantity = defaultQuantity();
				durability = MAX_DURABILITY;
			}

			levelKnown = levelKnown || other.levelKnown;
			cursedKnown = cursedKnown || other.cursedKnown;

			//迁移 3.3.8：合并时若对方鉴定已就绪，则本方也进入就绪状态
			if (((Weapon) other).readyToIdentify())
				setIDReady();

			masteryPotionBonus = masteryPotionBonus || ((MissileWeapon) other).masteryPotionBonus;
			enchantHardened = enchantHardened || ((MissileWeapon) other).enchantHardened;

			//if other has a curse/enchant status that's a higher priority, copy it. in the following order:
			//curse infused
			if (!curseInfusionBonus && ((MissileWeapon) other).curseInfusionBonus && ((MissileWeapon) other).hasCurseEnchant()){
				enchantment = ((MissileWeapon) other).enchantment;
				curseInfusionBonus = true;
				cursed = cursed || other.cursed;
			//enchanted
			} else if (!curseInfusionBonus && !hasGoodEnchant() && ((MissileWeapon) other).hasGoodEnchant()){
				enchantment = ((MissileWeapon) other).enchantment;
				cursed = other.cursed;
			//nothing
			} else if (!curseInfusionBonus && hasCurseEnchant() && !((MissileWeapon) other).hasCurseEnchant()){
				enchantment = ((MissileWeapon) other).enchantment;
				cursed = other.cursed;
			}
			//cursed (no copy as other cannot have a higher priority status)
		}
		return this;
	}
	
	@Override
	public Item split(int amount) {
		bundleRestoring = true;
		Item split = super.split(amount);
		bundleRestoring = false;
		
		//unless the thrown weapon will break, split off a max durability item and
		//have it reduce the durability of the main stack. Cleaner to the player this way
		if (split != null){
			MissileWeapon m = (MissileWeapon)split;
			m.durability = MAX_DURABILITY;
			m.parent = this;
			extraThrownLeft = m.extraThrownLeft = true;
		}
		
		return split;
	}
	
	@Override
	public boolean doPickUp(Hero hero, int pos) {
		parent = null;
		//setID 为 0（无数量限制/兼容旧版）时跳过防刷取判定
		if (setID != 0 && !UpgradedSetTracker.pickupValid(hero, this)) {
			Sample.INSTANCE.play( Assets.Sounds.ITEM );
			hero.spendAndNext( TIME_TO_PICK_UP );
			GLog.w(Messages.get(this, "dust"));
			quantity(0);
			return true;
		} else {
			extraThrownLeft = false;
			return super.doPickUp(hero, pos);
		}
	}
	
	@Override
	public boolean isIdentified() {
		//迁移 3.3.8：投武需同时鉴定等级与诅咒
		return levelKnown && cursedKnown;
	}
	
	@Override
	public String info() {

		String info = super.info();

		Ring.guessSignalRing(Dungeon.hero(), RingOfSharpshooting.class, false);

		//迁移 3.3.8：根据是否已鉴定显示已知/未知属性
		if (levelKnown) {
			info += "\n\n" + Messages.get(MissileWeapon.class, "stats_known",
					tier,
					Math.round(augment.damageFactor(min())),
					Math.round(augment.damageFactor(max())),
					STRReq());
			if (Dungeon.hero() != null) {
				if (STRReq() > Dungeon.hero().STR()) {
					info += " " + Messages.get(Weapon.class, "too_heavy");
				} else if (Dungeon.hero().STR() > STRReq()) {
					info += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero().STR() - STRReq());
				}
			}
		} else {
			info += "\n\n" + Messages.get(MissileWeapon.class, "stats_unknown",
					tier, min(0), max(0), STRReq(0));
			if (Dungeon.hero() != null && STRReq(0) > Dungeon.hero().STR()) {
				info += " " + Messages.get(MissileWeapon.class, "probably_too_heavy");
			}
		}

		//同步 3.3.8：允许子类追加额外属性描述（如战斧的流血区间）
		String statsInfo = statsInfo();
		if (!statsInfo.equals("")) info += " " + statsInfo;

		if (enchantment != null && (cursedKnown || !enchantment.curse())){
			info += "\n\n" + Messages.get(Weapon.class, "enchanted", enchantment.name());
			if (enchantHardened) info += " " + Messages.get(Weapon.class, "enchant_hardened");
			info += " " + Messages.get(enchantment, "desc");
		} else if (enchantHardened){
			info += "\n\n" + Messages.get(Weapon.class, "hardened_no_enchant");
		}

		if (cursed && isEquipped( Dungeon.hero() )) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
		} else if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed");
		} else if (!isIdentified() && cursedKnown){
			info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
		}

		info += "\n\n" + Messages.get(MissileWeapon.class, "distance");
		
		info += "\n\n" + Messages.get(this, "durability");
		
		if (levelKnown) {
			if (durabilityPerUse() > 0){
				info += " " + Messages.get(this, "uses_left",
						(int)Math.ceil(durability/durabilityPerUse()),
						(int)Math.ceil(MAX_DURABILITY/durabilityPerUse()));
			} else {
				info += " " + Messages.get(this, "unlimited_uses");
			}
		} else {
			if (durabilityPerUse(0) > 0) {
				info += " " + Messages.get(this, "unknown_uses", (int) Math.ceil(MAX_DURABILITY / durabilityPerUse(0)));
			} else {
				info += " " + Messages.get(this, "unlimited_uses");
			}
		}

        if (overLoad != OverLoad.NONE)
            info += "\n\n" + Messages.get(Item.class, overLoad.name(), overLoadLeft);
		return info;
	}

	//同步 3.3.8：默认无额外属性描述，子类可覆写追加
	public String statsInfo(){
		return "";
	}
	
	@Override
	public int value() {
		//迁移 3.3.8：附魔×1.5、诅咒÷2、等级乘算、最低 1
		int price = 5 * tier * quantity;
		if (hasGoodEnchant()) {
			price *= 1.5;
		}
		if (cursedKnown && (cursed || hasCurseEnchant())) {
			price /= 2;
		}
		if (levelKnown && level() > 0) {
			price *= (level() + 1);
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}
	
	private static final String DURABILITY = "durability";
	private static final String SET_ID = "set_id";
	private static final String SPAWNED = "spawned";
	private static final String EXTRA_LEFT = "extra_left";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(DURABILITY, durability);
		bundle.put(SET_ID, setID);
		bundle.put(SPAWNED, spawnedForEffect);
		bundle.put(EXTRA_LEFT, extraThrownLeft);
	}
	
	private static boolean bundleRestoring = false;
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		bundleRestoring = true;
		super.restoreFromBundle(bundle);
		bundleRestoring = false;

		if ((setID = bundle.getLong(SET_ID)) == 0)
			levelKnown = true;
		durability = bundle.getFloat(DURABILITY);
		spawnedForEffect = bundle.getBoolean(SPAWNED);
		extraThrownLeft = bundle.getBoolean(EXTRA_LEFT);
	}

	public static class PlaceHolder extends MissileWeapon {

		{
			image = ItemSpriteSheet.MISSILE_HOLDER;
		}

		@Override
		public boolean isSimilar(Item item) {
			//yes, even though it uses a dart outline
			return item instanceof MissileWeapon && !(item instanceof Dart);
		}

		@Override
		public String info() {
			return "";
		}
	}

	//also used by liquid metal crafting to track when a set is consumed
	public static class UpgradedSetTracker extends Buff {

		{
			revivePersists = true;
		}

		public HashMap<Long, Integer> levelThresholds = new HashMap<>();

		public static boolean pickupValid(Hero h, MissileWeapon w){
			if (h.buff(UpgradedSetTracker.class) != null){
				HashMap<Long, Integer> levelThresholds = h.buff(UpgradedSetTracker.class).levelThresholds;
				if (levelThresholds.containsKey(w.setID)){
					return w.trueLevel() >= levelThresholds.get(w.setID);
				}
				return true;
			}
			return true;
		}

		public static final String SET_IDS = "set_ids";
		public static final String SET_LEVELS = "set_levels";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			long[] IDs = new long[levelThresholds.size()];
			int[] levels = new int[levelThresholds.size()];
			int i = 0;
			for (Long ID : levelThresholds.keySet()){
				IDs[i] = ID;
				levels[i] = levelThresholds.get(ID);
				i++;
			}
			bundle.put(SET_IDS, IDs);
			bundle.put(SET_LEVELS, levels);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			long[] IDs = bundle.getLongArray(SET_IDS);
			int[] levels = bundle.getIntArray(SET_LEVELS);
			levelThresholds.clear();
			if (IDs != null) {
				for (int i = 0; i < IDs.length; i++){
					levelThresholds.put(IDs[i], levels[i]);
				}
			}
		}
	}
}
