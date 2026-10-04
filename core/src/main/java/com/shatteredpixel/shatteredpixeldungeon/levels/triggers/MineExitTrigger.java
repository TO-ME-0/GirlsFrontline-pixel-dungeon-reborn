package com.shatteredpixel.shatteredpixeldungeon.levels.triggers;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DarkGold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BlacksmithSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

//矿洞出口：矿洞入口房 ENTRANCE 格触发器，替代 3.3.8 MiningLevel.activateTransition 的 BRANCH_ENTRANCE 门禁
//任务未完成时离开：没镐直接拦下；否则按暗金数量与 boss 状态弹出结算警告，确认后 complete() 再回主层
public class MineExitTrigger extends Trigger {

	@Override
	public void activate(Char ch) {
		if (ch != Dungeon.hero()){
			return;
		}

		Blacksmith.Quest quest = Blacksmith.Quest.cur();

		//任务已结算，之后可自由往返
		if (quest.completed()){
			leaveMine();
			return;
		}

		if (Dungeon.hero().belongings.getItem(Pickaxe.class) == null){
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndTitledMessage(new BlacksmithSprite(),
							Messages.titleCase(Messages.get(Blacksmith.class, "name")),
							Messages.get(Blacksmith.class, "lost_pick")));
				}
			});
			return;
		}

		DarkGold gold = Dungeon.hero().belongings.getItem(DarkGold.class);
		int goldAmount = gold == null ? 0 : gold.quantity();
		String warnText;
		if (goldAmount < 10){
			warnText = Messages.get(Blacksmith.class, "exit_warn_none");
		} else if (goldAmount < 20){
			warnText = Messages.get(Blacksmith.class, "exit_warn_low");
		} else if (goldAmount < 30){
			warnText = Messages.get(Blacksmith.class, "exit_warn_med");
		} else if (goldAmount < 40){
			warnText = Messages.get(Blacksmith.class, "exit_warn_high");
		} else {
			warnText = Messages.get(Blacksmith.class, "exit_warn_full");
		}

		if (!quest.bossBeaten()){
			switch (quest.Type()){
				case Blacksmith.Quest.CRYSTAL: warnText += "\n\n" + Messages.get(Blacksmith.class, "exit_warn_crystal"); break;
				case Blacksmith.Quest.GNOLL:   warnText += "\n\n" + Messages.get(Blacksmith.class, "exit_warn_gnoll"); break;
				case Blacksmith.Quest.FUNGI:   warnText += "\n\n" + Messages.get(Blacksmith.class, "exit_warn_fungi"); break;
			}
		}

		final String finalWarnText = warnText;
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(new WndOptions(new BlacksmithSprite(),
						Messages.titleCase(Messages.get(Blacksmith.class, "name")),
						finalWarnText,
						Messages.get(Blacksmith.class, "exit_yes"),
						Messages.get(Blacksmith.class, "exit_no")){
					@Override
					protected void onSelect(int index) {
						if (index == 0){
							Blacksmith.Quest.cur().complete();
							leaveMine();
						}
					}
				});
			}
		});
	}

	//返回矿洞对应的主层：returnPos=-3 由 InterlevelScene.returnTo 解析为铁匠房入口格
	private static void leaveMine(){
		InterlevelScene.returnLevel = Dungeon.cur().depth;
		InterlevelScene.returnPos = -3;
		InterlevelScene.mode = InterlevelScene.Mode.RETURN;
		Game.switchScene(InterlevelScene.class);
	}
}
