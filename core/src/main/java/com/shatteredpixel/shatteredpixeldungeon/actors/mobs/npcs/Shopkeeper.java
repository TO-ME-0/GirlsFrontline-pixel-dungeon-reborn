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
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShopkeeperSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class Shopkeeper extends NPC {

    protected int turnsSinceHarmed = -1;

    {
		spriteClass = ShopKeeperSprite();
		properties.add(Property.IMMOVABLE);
	}

    @Override
    public Notes.Landmark landmark() {
        return Notes.Landmark.SHOP;
    }
    private static Class<? extends ShopkeeperSprite> ShopKeeperSprite(){
        if ((Dungeon.depth()-1)/5< Statistics.deepestFloor/5&&Dungeon.depth()!=16)
            return ShopkeeperSprite.MirrorShopkeeper.class;
        return ShopkeeperSprite.class;
    }

    private static final String TurnsSinceHarmed = "TurnsSinceHarmed";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( TurnsSinceHarmed, turnsSinceHarmed );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        turnsSinceHarmed = bundle.getInt( TurnsSinceHarmed );
    }

    protected boolean act() {
        if (this.turnsSinceHarmed >= 0) {
            ++this.turnsSinceHarmed;
        }
        this.sprite.turnTo(this.pos, Dungeon.hero().pos);
        this.spend(1.0F);
        return super.act();
    }

    public void damage(int dmg, Object src) {
        this.processHarm();
    }

    public void add(Buff buff) {
        if (buff.type == Buff.buffType.NEGATIVE) {
            this.processHarm();
        }else{
            super.add(buff);
        }
    }
    public String WndInfo(){
        return Messages.get(this, "wndinfo");
    }
    public void processHarm() {
            if (this.turnsSinceHarmed < 1) {
                this.turnsSinceHarmed = 0;
                Buff.prolong( this, BlobImmunity.class, BlobImmunity.DURATION );
                this.yell(Messages.get(this, "warn"));
            }else {
                this.flee();
            }
    }

    public void flee() {
		destroy();

		Notes.remove(Notes.Landmark.SHOP);
		
		sprite.killAndErase();
		CellEmitter.get( pos ).burst( ElmoParticle.FACTORY, 6 );
	}
	
	@Override
	public void destroy() {
		super.destroy();
		for (Heap heap: Dungeon.level.heaps.valueList()) {
			if (heap.type == Heap.Type.FOR_SALE) {
				CellEmitter.get( heap.pos ).burst( ElmoParticle.FACTORY, 4 );
                if (heap.items.get(heap.size()-1) instanceof Bag)
                    lostBag((Bag) heap.items.get(heap.size()-1));

				if (heap.size() == 1) {
					heap.destroy();
				}else{
					heap.items.remove(heap.size()-1);
					heap.type = Heap.Type.HEAP;
				}
			}
		}
	}
    private void lostBag(Bag bag){
        if (bag instanceof VelvetPouch){
            Dungeon.LimitedDrops.VELVET_POUCH.lost();
        } else if (bag instanceof ScrollHolder){
            Dungeon.LimitedDrops.SCROLL_HOLDER.lost();
        } else if (bag instanceof PotionBandolier){
            Dungeon.LimitedDrops.POTION_BANDOLIER.lost();
        } else if (bag instanceof MagicalHolster){
            Dungeon.LimitedDrops.MAGICAL_HOLSTER.lost();
        }
    }
	
	@Override
	public boolean reset() {
		return true;
	}

	//shopkeepers are greedy!
	@SuppressWarnings("SuspiciousIndentation")
    public static int sellPrice(Item item){
        if (item.sold)
            return item.value() * 10;

		float rate = 1F;

		if(Dungeon.hero().hasTalent(Talent.Type56Two_FOOD) && item instanceof Food)
			rate -= 0.2F * Dungeon.hero().pointsInTalent(Talent.Type56Two_FOOD);
        if(Dungeon.hero().hasTalent(Talent.BARGAIN_SKILLS) && item instanceof Food)
            rate -= 0.2F * Dungeon.hero().pointsInTalent(Talent.BARGAIN_SKILLS);

        int mul = Dungeon.depth()/5 + 1;
        //乘数等效于下一个boss层的楼层数

        return Math.max( item.value() * 2, (int)(item.value() * rate * (mul * 5) ));
    }
	
	public static WndBag sell() {
		return GameScene.selectItem( itemSelector );
	}

	public static boolean canSell(Item item){
		if (item.value() <= 0)                                              return false;
		if (item.unique && !item.stackable)                                 return false;
		//磁轨加速弹已贴附的武器无法出售（配件无法摘下，防止配件随武器被卖掉）
		if (item instanceof Weapon && ((Weapon) item).beamFocused)          return false;
		if (item instanceof Armor && ((Armor) item).checkSeal() != null)    return false;
		if (item.isEquipped(Dungeon.hero()) && item.cursed)                   return false;
		return true;
	}

	private static WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(Shopkeeper.class, "sell");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return Shopkeeper.canSell(item);
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null) {
				WndBag parentWnd = sell();
				GameScene.show( new WndTradeItem( item, parentWnd ) );
			}
		}
	};

	@Override
	public boolean interact(Char c) {
		if (c != Dungeon.hero()) {
			return true;
		}
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				sell();
			}
		});
		return true;
	}
}
