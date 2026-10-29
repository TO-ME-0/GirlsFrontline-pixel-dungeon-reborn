package com.shatteredpixel.shatteredpixeldungeon.levels.triggers;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.watabou.noosa.Game;

//金库出口：金库入口房中心格的触发器，替代 4.0 金库侧 LevelTransition 的 BRANCH_ENTRANCE
//踩上即返回主层城市：不结算分数、不动逃脱水晶里暂存的背包，任务保持进行中，可再次进入
public class VaultExitTrigger extends Trigger {

	@Override
	public void activate(Char ch) {
		if (ch != Dungeon.hero()){
			return;
		}

		//returnPos=-4 由 InterlevelScene.returnTo 解析为金库传送点格
		InterlevelScene.returnDepth = Dungeon.cur().depth;
		InterlevelScene.returnBranch = 0;
		InterlevelScene.returnPos = -4;
		InterlevelScene.mode = InterlevelScene.Mode.RETURN;
		Game.switchScene(InterlevelScene.class);
	}
}
