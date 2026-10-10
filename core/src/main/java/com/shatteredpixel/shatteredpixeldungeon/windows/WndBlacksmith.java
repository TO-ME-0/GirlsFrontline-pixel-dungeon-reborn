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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

//照搬 SPD 3.3.8：favor 点数商店（赎回镐/重铸/硬化/直升/史密斯奖励/兑现金币）
//适配：Quest 为实例（Quest.cur()）、Dungeon.hero() 路由、删除 UpgradedSetTracker 分支（mod 无此类）
public class WndBlacksmith extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 180;

	private static final int GAP  = 2;

	public WndBlacksmith( Blacksmith troll, Hero hero ) {
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		IconTitle titlebar = new IconTitle();
		titlebar.icon( troll.sprite() );
		titlebar.label( Messages.titleCase( troll.name() ) );
		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );

		RenderedTextBlock message = PixelScene.renderTextBlock( Messages.get(this, "prompt", Blacksmith.Quest.cur().favor), 6 );
		message.maxWidth( width );
		message.setPos(0, titlebar.bottom() + GAP);
		add( message );

		ArrayList<RedButton> buttons = new ArrayList<RedButton>();

		final int pickaxeCost = Blacksmith.Quest.cur().freePickaxe ? 0 : 250;
		RedButton pickaxe = new RedButton(Messages.get(this, "pickaxe", pickaxeCost), 6){
			@Override
			protected void onClick() {
				GameScene.show(new WndOptions(
						troll.sprite(),
						Messages.titleCase( troll.name() ),
						Messages.get(WndBlacksmith.class, "pickaxe_verify") + (pickaxeCost == 0 ? "\n\n" + Messages.get(WndBlacksmith.class, "pickaxe_free") : ""),
						Messages.get(WndBlacksmith.class, "pickaxe_yes"),
						Messages.get(WndBlacksmith.class, "pickaxe_no")
				){
					@Override
					protected void onSelect(int index) {
						if (index == 0){
							Item pick = Blacksmith.Quest.cur().pickaxe;
							if (pick.doPickUp( Dungeon.hero() )) {
								GLog.i( Messages.capitalize(Messages.get(Dungeon.hero(), "you_now_have", pick.name()) ));
							} else {
								Dungeon.level.drop( pick, Dungeon.hero().pos ).sprite.drop();
							}
							Blacksmith.Quest.cur().favor -= pickaxeCost;
							Blacksmith.Quest.cur().pickaxe = null;
							WndBlacksmith.this.hide();

							if (!Blacksmith.Quest.cur().rewardsAvailable()){
								Notes.remove( Notes.Landmark.TROLL );
							}
						}
					}
				});
			}
		};
		pickaxe.enable(Blacksmith.Quest.cur().pickaxe != null && Blacksmith.Quest.cur().favor >= pickaxeCost);
		buttons.add(pickaxe);

		final int reforgecost = 500 + 1000*Blacksmith.Quest.cur().reforges;
		RedButton reforge = new RedButton(Messages.get(this, "reforge", reforgecost), 6){
			@Override
			protected void onClick() {
				GameScene.show(new WndReforge(troll, WndBlacksmith.this));
			}
		};
		reforge.enable(Blacksmith.Quest.cur().favor >= reforgecost);
		buttons.add(reforge);

		final int hardenCost = 500 + 1000*Blacksmith.Quest.cur().hardens;
		RedButton harden = new RedButton(Messages.get(this, "harden", hardenCost), 6){
			@Override
			protected void onClick() {
				GameScene.selectItem(new HardenSelector());
			}
		};
		harden.enable(Blacksmith.Quest.cur().favor >= hardenCost);
		buttons.add(harden);

		final int upgradeCost = 1000 + 1000*Blacksmith.Quest.cur().upgrades;
		RedButton upgrade = new RedButton(Messages.get(this, "upgrade", upgradeCost), 6){
			@Override
			protected void onClick() {
				GameScene.selectItem(new UpgradeSelector());
			}
		};
		upgrade.enable(Blacksmith.Quest.cur().favor >= upgradeCost);
		buttons.add(upgrade);

		RedButton smith = new RedButton(Messages.get(this, "smith", 2000), 6){
			@Override
			protected void onClick() {
				GameScene.show(new WndOptions(
						troll.sprite(),
						Messages.titleCase( troll.name() ),
						Messages.get(WndBlacksmith.class, "smith_verify"),
						Messages.get(WndBlacksmith.class, "smith_yes"),
						Messages.get(WndBlacksmith.class, "smith_no")
				){
					@Override
					protected void onSelect(int index) {
						if (index == 0){
							Blacksmith.Quest.cur().favor -= 2000;
							Blacksmith.Quest.cur().smiths++;
							WndBlacksmith.this.hide();
							GameScene.show(new WndSmith(troll, hero));
						}
					}
				});
			}
		};
		smith.enable(Blacksmith.Quest.cur().favor >= 2000);
		buttons.add(smith);

		RedButton cashOut = new RedButton(Messages.get(this, "cashout"), 6){
			@Override
			protected void onClick() {
				GameScene.show(new WndOptions(
						troll.sprite(),
						Messages.titleCase( troll.name() ),
						Messages.get(WndBlacksmith.class, "cashout_verify", Blacksmith.Quest.cur().favor),
						Messages.get(WndBlacksmith.class, "cashout_yes"),
						Messages.get(WndBlacksmith.class, "cashout_no")
				){
					@Override
					protected void onSelect(int index) {
						if (index == 0){
							new Gold(Blacksmith.Quest.cur().favor).doPickUp(Dungeon.hero(), Dungeon.hero().pos);
							Blacksmith.Quest.cur().favor = 0;
							WndBlacksmith.this.hide();
						}
					}
				});
			}
		};
		cashOut.enable(Blacksmith.Quest.cur().favor > 0);
		buttons.add(cashOut);

		float pos = message.bottom() + 3*GAP;
		for (RedButton b : buttons){
			b.leftJustify = true;
			b.multiline = true;
			b.setSize(width, b.reqHeight());
			b.setRect(0, pos, width, b.reqHeight());
			b.enable(b.active); //让禁用状态在外观上体现
			add(b);
			pos = b.bottom() + GAP;
		}

		resize(width, (int)pos);

	}

	protected static class WndReforge extends Window {

		private static final int WIDTH		= 120;

		private static final int BTN_SIZE	= 32;
		private static final float GAP		= 2;
		private static final float BTN_GAP	= 5;

		private ItemButton btnPressed;

		private ItemButton btnItem1;
		private ItemButton btnItem2;
		private RedButton btnReforge;

		public WndReforge( Blacksmith troll, Window wndParent ) {
			super();

			IconTitle titlebar = new IconTitle();
			titlebar.icon( troll.sprite() );
			titlebar.label( Messages.titleCase( troll.name() ) );
			titlebar.setRect( 0, 0, WIDTH, 0 );
			add( titlebar );

			RenderedTextBlock message = PixelScene.renderTextBlock( Messages.get(this, "message"), 6 );
			message.maxWidth( WIDTH);
			message.setPos(0, titlebar.bottom() + GAP);
			add( message );

			btnItem1 = new ItemButton() {
				@Override
				protected void onClick() {
					btnPressed = btnItem1;
					GameScene.selectItem( itemSelector );
				}
			};
			btnItem1.setRect( (WIDTH - BTN_GAP) / 2 - BTN_SIZE, message.top() + message.height() + BTN_GAP, BTN_SIZE, BTN_SIZE );
			add( btnItem1 );

			btnItem2 = new ItemButton() {
				@Override
				protected void onClick() {
					btnPressed = btnItem2;
					GameScene.selectItem( itemSelector );
				}
			};
			btnItem2.setRect( btnItem1.right() + BTN_GAP, btnItem1.top(), BTN_SIZE, BTN_SIZE );
			add( btnItem2 );

			btnReforge = new RedButton( Messages.get(this, "reforge") ) {
				@Override
				protected void onClick() {

					Item item1 = btnItem1.item(), item2 = btnItem2.item();
					Item first, second;
					if (item1 instanceof ClassArmor || item2 instanceof ClassArmor) {
						if (item1 instanceof ClassArmor) {
							first = item1; second = item2;
						}
						else {
							first = item2; second = item1;
						}
					}
					else if (item1 instanceof MagesStaff || item2 instanceof MagesStaff) {
						if (item1 instanceof MagesStaff) {
							first = item1; second = item2;
						}
						else {
							first = item2; second = item1;
						}
					}
					else if (item1 instanceof MissileWeapon && ((MissileWeapon) item1).setID == 0 && item1 == item2) {
						first = item1;
						second = item1.detach(Dungeon.hero().belongings.backpack);
					}
					else if (!item1.levelKnown || !item2.levelKnown) {
						if (item2.levelKnown) {
							first = item2; second = item1;
						}
						else {
							first = item1; second = item2;
						}
					}
					else {
						if (item1.trueLevel() >= item2.trueLevel()) {
							first = item1;second = item2;
						}
						else {
							first = item2;second = item1;
						}
					}

					Sample.INSTANCE.play( Assets.Sounds.EVOKE );
					ScrollOfUpgrade.upgrade( Dungeon.hero() );
					Item.evoke( Dungeon.hero() );

					if (second.isEquipped( Dungeon.hero() )) {
						((EquipableItem)second).doUnequip( Dungeon.hero(), false );
					}
					second.detachAll( Dungeon.hero().belongings.backpack );

					if (second instanceof Armor){
						BrokenSeal seal = ((Armor) second).checkSeal();
						if (seal != null){
							Dungeon.level.drop( seal, Dungeon.hero().pos );
						}
					}

					//保留已有的好附魔/铭文
					if (first instanceof Weapon && ((Weapon) first).hasGoodEnchant()){
						((Weapon) first).upgrade(true);
					} else if (first instanceof Armor && ((Armor) first).hasGoodGlyph()){
						((Armor) first).upgrade(true);
					} else {
						first.upgrade();
					}
					Badges.validateItemLevelAquired( first );
					Item.updateQuickslot();

					Blacksmith.Quest.cur().favor -= 500 + 1000*Blacksmith.Quest.cur().reforges;
					Blacksmith.Quest.cur().reforges++;

					if (!Blacksmith.Quest.cur().rewardsAvailable()){
						Notes.remove( Notes.Landmark.TROLL );
					}

					hide();
					if (wndParent != null){
						wndParent.hide();
					}
				}
			};
			btnReforge.enable( false );
			btnReforge.setRect( 0, btnItem1.bottom() + BTN_GAP, WIDTH, 20 );
			add( btnReforge );


			resize( WIDTH, (int)btnReforge.bottom() );
		}

		protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

			@Override
			public String textPrompt() {
				return Messages.get(WndReforge.class, "prompt");
			}

			@Override
			public Class<?extends Bag> preferredBag(){
				return Belongings.Backpack.class;
			}

			@Override
			public boolean itemSelectable(Item item) {
				return !item.cursed && item.isUpgradable();
			}

			@Override
			public void onSelect( Item item ) {
				if (item != null && btnPressed.parent != null) {
					btnPressed.item(item);

					Item item1 = btnItem1.item();
					Item item2 = btnItem2.item();

					//需要两件物品
					if (item1 == null || item2 == null) {
						btnReforge.enable(false);
						return;
					}
					//不能是同一件物品
					if (item1 == item2) {
						//量产型投武采用旧的逻辑。
						if (item1 instanceof MissileWeapon && ((MissileWeapon) item1).setID == 0 && item1.quantity() > 1);
						else {
							btnReforge.enable(false);
							return;
						}
					}
					if (item1 instanceof MissileWeapon && item2 instanceof MissileWeapon) {
						//量产型投武不能与旧投武合并
						if (((MissileWeapon) item1).setID == 0 && ((MissileWeapon) item2).setID != 0
								|| ((MissileWeapon) item1).setID != 0 && ((MissileWeapon) item2).setID == 0) {
							btnReforge.enable(false);
							return;
						}
					}
					if (item1 instanceof MagesStaff || item2 instanceof MagesStaff) {
						MagesStaff staff = (MagesStaff) (item1 instanceof MagesStaff ? item1 : item2);
						btnReforge.enable(staff.wand != null && (item1.getClass() == staff.wand.getClass() || item2.getClass() == staff.wand.getClass()));
						return;
					}
					if (item1 instanceof Armor && item2 instanceof Armor) {
						btnReforge.enable(((Armor) item1).tier == ((Armor) item2).tier);
						return;
					}
					//必须同 class
					if (item1.getClass() != item2.getClass()) {
						btnReforge.enable(false);
						return;
					}
					btnReforge.enable(true);
				}
			}
		};

	}

	private class HardenSelector extends WndBag.ItemSelector {

		@Override
		public String textPrompt() {
			return Messages.get(this, "prompt");
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item.isUpgradable()
					&& item.isIdentified() && !item.cursed
					&& ((item instanceof Weapon && !((Weapon) item).enchantHardened)
					|| (item instanceof Armor && !((Armor) item).glyphHardened));
		}

		@Override
		public void onSelect(Item item) {
			if (item != null) {
				if (item instanceof Weapon){
					((Weapon) item).enchantHardened = true;
				} else if (item instanceof Armor){
					((Armor) item).glyphHardened = true;
				}

				Blacksmith.Quest.cur().favor -= 500 + 1000*Blacksmith.Quest.cur().hardens;
				Blacksmith.Quest.cur().hardens++;

				WndBlacksmith.this.hide();

				Sample.INSTANCE.play(Assets.Sounds.EVOKE);
				Item.evoke( Dungeon.hero() );

				if (!Blacksmith.Quest.cur().rewardsAvailable()){
					Notes.remove( Notes.Landmark.TROLL );
				}
			}
		}
	}

	private class UpgradeSelector extends WndBag.ItemSelector {

		@Override
		public String textPrompt() {
			return Messages.get(this, "prompt");
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item.isUpgradable()
					&& item.isIdentified()
					&& !item.cursed
					&& item.level() < 2;
		}

		@Override
		public void onSelect(Item item) {
			if (item != null) {
				item.upgrade();
				int upgradeCost = 1000 + 1000*Blacksmith.Quest.cur().upgrades;
				Blacksmith.Quest.cur().favor -= upgradeCost;
				Blacksmith.Quest.cur().upgrades++;

				WndBlacksmith.this.hide();

				Sample.INSTANCE.play(Assets.Sounds.EVOKE);
				ScrollOfUpgrade.upgrade( Dungeon.hero() );
				Item.evoke( Dungeon.hero() );

				Badges.validateItemLevelAquired( item );

				if (!Blacksmith.Quest.cur().rewardsAvailable()){
					Notes.remove( Notes.Landmark.TROLL );
				}

				Catalog.countUse(item.getClass());
			}
		}
	}

	public static class WndSmith extends Window {

		private static final int WIDTH      = 128;
		private static final int BTN_SIZE	= 28;
		private static final int BTN_GAP	= 4;
		private static final int GAP		= 2;

		public WndSmith( Blacksmith troll, Hero hero ){
			super();

			IconTitle titlebar = new IconTitle();
			titlebar.icon(troll.sprite());
			titlebar.label(Messages.titleCase(troll.name()));

			RenderedTextBlock message = PixelScene.renderTextBlock( Messages.get(this, "prompt"), 6 );

			titlebar.setRect( 0, 0, WIDTH, 0 );
			add( titlebar );

			message.maxWidth(WIDTH);
			message.setPos(0, titlebar.bottom() + GAP);
			add( message );

			if (Blacksmith.Quest.cur().smithRewards == null || Blacksmith.Quest.cur().smithRewards.isEmpty()){
				Blacksmith.Quest.cur().generateRewards();
			}

			int count = 0;
			for (final Item i : Blacksmith.Quest.cur().smithRewards){
				count++;
				ItemButton btnReward = new ItemButton(){
					@Override
					protected void onClick() {
						GameScene.show(new RewardWindow(troll, hero, i));
					}
				};
				btnReward.item( i );
				btnReward.setRect( count*(WIDTH - BTN_GAP) / Blacksmith.Quest.cur().smithRewards.size() - BTN_SIZE,
						message.top() + message.height() + BTN_GAP,
						BTN_SIZE, BTN_SIZE );
				add( btnReward );

			}

			resize(WIDTH, (int)message.bottom() + 2*BTN_GAP + BTN_SIZE);

		}

		@Override
		public void onBackPressed() {
			//空实现，防止误触关闭导致奖励丢失
		}

		private class RewardWindow extends WndInfoItem {

			public RewardWindow( Blacksmith troll, Hero hero, final Item item ) {
				super(item);

				RedButton btnConfirm = new RedButton(Messages.get(WndSadGhost.class, "confirm")){
					@Override
					protected void onClick() {
						RewardWindow.this.hide();

						if (item instanceof Weapon && Blacksmith.Quest.cur().smithEnchant != null){
							((Weapon) item).enchant(Blacksmith.Quest.cur().smithEnchant);
						} else if (item instanceof Armor && Blacksmith.Quest.cur().smithGlyph != null){
							((Armor) item).inscribe(Blacksmith.Quest.cur().smithGlyph);
						}

						item.identify(false);
						Sample.INSTANCE.play(Assets.Sounds.EVOKE);
						Item.evoke( Dungeon.hero() );
						if (item.doPickUp( Dungeon.hero() )) {
							GLog.i( Messages.capitalize(Messages.get(Dungeon.hero(), "you_now_have", item.name())) );
						} else {
							Dungeon.level.drop( item, Dungeon.hero().pos ).sprite.drop();
						}
						WndSmith.this.hide();
						Blacksmith.Quest.cur().smithRewards = null;

						if (!Blacksmith.Quest.cur().rewardsAvailable()){
							Notes.remove( Notes.Landmark.TROLL );
						}
					}
				};
				btnConfirm.setRect(0, height+2, width/2-1, 16);
				add(btnConfirm);

				RedButton btnCancel = new RedButton(Messages.get(WndSadGhost.class, "cancel")){
					@Override
					protected void onClick() {
						RewardWindow.this.hide();
					}
				};
				btnCancel.setRect(btnConfirm.right()+2, height+2, btnConfirm.width(), 16);
				add(btnCancel);

				resize(width, (int)btnCancel.bottom());
			}
		}

	}

}
