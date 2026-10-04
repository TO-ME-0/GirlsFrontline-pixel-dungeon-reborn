package com.shatteredpixel.shatteredpixeldungeon.levels.triggers;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BlacksmithSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

//矿洞入口：铁匠房里的 EXIT 格触发器，替代 3.3.8 CavesLevel.activateTransition 的 BRANCH_EXIT 门禁
//门禁规则照搬 3.3.8：已领任务且未完成，首次进入需持镐确认（确认后任务 start）
public class MineEntranceTrigger extends Trigger {

	@Override
	public void activate(Char ch) {
		if (ch != Dungeon.hero()){
			return;
		}

		Blacksmith.Quest quest = Blacksmith.Quest.cur();

		//已领任务、未完成、已开始：直接进入
		if (quest.given() && !quest.completed() && quest.started()){
			enterMine();
			return;
		}

		//其余情况先判断铁匠是否还在、任务是否已领取/完成
		Blacksmith smith = null;
		for (Char c : Actor.chars()){
			if (c instanceof Blacksmith){
				smith = (Blacksmith) c;
				break;
			}
		}

		if (smith == null || !quest.given() || quest.completed()) {
			GLog.w(Messages.get(Blacksmith.class, "entrance_blocked"));
			return;
		}

		//已领任务但尚未开始：没镐提示丢镐，有镐则二次确认
		final Pickaxe pick = Dungeon.hero().belongings.getItem(Pickaxe.class);
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				if (pick == null){
					GameScene.show(new WndTitledMessage(new BlacksmithSprite(),
							Messages.titleCase(Messages.get(Blacksmith.class, "name")),
							Messages.get(Blacksmith.class, "lost_pick"))
					);
				} else {
					GameScene.show(new WndOptions(new BlacksmithSprite(),
							Messages.titleCase(Messages.get(Blacksmith.class, "name")),
							Messages.get(Blacksmith.class, "quest_start_prompt"),
							Messages.get(Blacksmith.class, "enter_yes"),
							Messages.get(Blacksmith.class, "enter_no")){
						@Override
						protected void onSelect(int index) {
							if (index == 0){
								Blacksmith.Quest.cur().start();
								enterMine();
							}
						}
					});
				}
			}
		});
	}

	//进入矿洞子层：关卡 id 为 当前主层深度 +1000，落点 -1 由 switchLevel 解析为新层 entrance
	private static void enterMine(){
		InterlevelScene.accessPos = -1;
		InterlevelScene.accessLevelId = 1000 + Dungeon.cur().depth;
		InterlevelScene.mode = InterlevelScene.Mode.ACCESS;
		Game.switchScene(InterlevelScene.class);
	}
}
