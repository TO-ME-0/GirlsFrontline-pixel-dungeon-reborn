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

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SMG.P90;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.function.BooleanSupplier;

//储存仓库窗口（0层）：以图标网格管理已解锁道具的局内生成开关
//点击图标切换启用状态，绿色高亮表示已启用（与种子查询器遗物选中样式一致）
public class WndWarehouse extends Window {

	private static final int WIDTH = 120;
	private static final int HEIGHT = 78;

	public WndWarehouse() {
		super();

		//含 ScrollPane 的窗口必须最先 resize
		resize(WIDTH, HEIGHT);

		//窗口标题
		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((WIDTH - title.width()) / 2f, 2);
		add(title);

		//操作提示
		RenderedTextBlock hint = PixelScene.renderTextBlock(Messages.get(this, "hint"), 6);
		hint.maxWidth(WIDTH - 4);
		hint.setPos(2, title.bottom() + 2);
		add(hint);

		//生成开关网格
		ScrollingGridPane grid = new ScrollingGridPane(false);
		add(grid);
		grid.addHeader(Messages.get(this, "spawn_section"));
		//后续新增的可开关生成项在此追加一行即可（仿照 P90）
		grid.addItem(new SpawnToggleItem(new P90(),
				SPDSettings::p90Unlocked,
				SPDSettings::p90Spawn,
				() -> SPDSettings.p90Spawn(!SPDSettings.p90Spawn())));
		grid.setRect(2, hint.bottom() + 2, WIDTH - 4, HEIGHT - hint.bottom() - 4);
		grid.scrollTo(0, 0);
	}

	//单个生成开关格：未解锁时暗红底+半透明图标；已解锁后点击切换，启用时绿色高亮
	private static class SpawnToggleItem extends ScrollingGridPane.GridItem {

		private final Item item;
		private final BooleanSupplier unlocked;
		private final BooleanSupplier enabled;
		private final Runnable toggle;

		SpawnToggleItem(Item item, BooleanSupplier unlocked, BooleanSupplier enabled, Runnable toggle) {
			super(new ItemSprite(item));
			this.item = item;
			this.unlocked = unlocked;
			this.enabled = enabled;
			this.toggle = toggle;
		}

		@Override
		public void update() {
			super.update();
			if (!unlocked.getAsBoolean()) {
				//未解锁：暗红底色 + 图标半透明
				hardLightBG(0.4f, 0.2f, 0.2f);
				icon.alpha(0.4f);
			} else if (enabled.getAsBoolean()) {
				//已启用：绿色高亮，与种子查询器选中样式一致
				hardLightBG(0.25f, 0.7f, 0.3f);
				icon.alpha(1f);
			} else {
				bg.resetColor();
				icon.alpha(1f);
			}
		}

		@Override
		public void onClick() {
			super.onClick();
			if (!unlocked.getAsBoolean()) {
				GLog.w(Messages.get(WndWarehouse.class, "locked_hint", item.name()));
				return;
			}
			toggle.run();
			//立即刷新生成池权重，使下一局生效
			Generator.refreshUnlockables();
			if (enabled.getAsBoolean()) {
				GLog.i(Messages.get(WndWarehouse.class, "toggle_on", item.name()));
			} else {
				GLog.p(Messages.get(WndWarehouse.class, "toggle_off", item.name()));
			}
		}
	}
}
