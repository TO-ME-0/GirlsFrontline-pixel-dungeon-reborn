package com.shatteredpixel.shatteredpixeldungeon.levels.triggers;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.P7Sprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

//金库入口：小恶魔房间中央传送点的触发器，替代 4.0 CityLevel.activateTransition 的 BRANCH_EXIT 门禁
//门禁规则照搬 4.0：旧任务/已完成/未接任务/背包遗失状态时进入无效；
//确认后暂存全部背包（存入逃脱水晶）、换上布衣并进入金库子层
public class VaultEntranceTrigger extends Trigger {

	@Override
	public void activate(Char ch) {
		if (ch != Dungeon.hero()){
			return;
		}

		Imp.Quest quest = Imp.Quest.cur();

		//与 4.0 一致：以下情况传送点沉默不生效（小恶魔就在旁边，会通过对话引导玩家）
		if (quest.oldQuest || quest.isCompleted() || !quest.given
				|| ch.buff(LostInventory.class) != null){
			return;
		}

		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndOptions( new P7Sprite(),
						Messages.titleCase(Messages.get(Imp.class, "name")),
						Messages.get(Imp.class, "enter_text"),
						Messages.get(Imp.class, "enter_yes"),
						Messages.get(Imp.class, "enter_no")){
					@Override
					protected void onSelect(int index) {
						if (index == 0){
							enterVault();
						}
					}
				} );
			}
		});
	}

	//进入金库子层：满状态、暂存背包、发布衣与逃脱水晶，ACCESS 到 1000+当前深度 的子层，落点 -1 由 switchLevel 解析为新层 entrance
	private static void enterVault(){
		Hero hero = Dungeon.hero();

		hero.live(); //清除所有非持久 buff，重置饥饿/回复
		hero.HP = hero.HT; //完全治疗

		EscapeCrystal crystal = hero.belongings.getItem(EscapeCrystal.class);
		if (crystal == null) {
			crystal = new EscapeCrystal();
		} else {
			crystal.detachAll(hero.belongings.backpack);
		}
		if (crystal.storedItems == null){
			crystal.storeHeroBelongings(hero);
		}
		crystal.collect();
		hero.belongings.armor = new ClothArmor();
		hero.belongings.armor.identify(true);
		hero.updateHT( false );

		InterlevelScene.accessPos = -1;
		InterlevelScene.accessLevelId = 1000 + Dungeon.cur().depth;
		InterlevelScene.mode = InterlevelScene.Mode.ACCESS;
		Game.switchScene(InterlevelScene.class);
	}
}
