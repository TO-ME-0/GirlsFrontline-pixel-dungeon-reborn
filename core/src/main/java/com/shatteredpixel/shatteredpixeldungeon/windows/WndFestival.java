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

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.Holidays;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;

/**
 * 节日介绍窗口：在任意层点击带节日配色的日期文字时弹出，
 * 展示当前节日的名称与一段简介。点击窗口外部即可关闭。
 */
public class WndFestival extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 160;
	private static final int MARGIN  = 2;

	public WndFestival() {
		super( 0, 0, Chrome.get( Chrome.Type.SCROLL ) );

		int width = PixelScene.landscape() ? WIDTH_L - MARGIN * 2 : WIDTH_P - MARGIN * 2;

		//优先展示玩家自定义的特殊日（生日/纪念日），其次展示系统节日
		String name;
		String desc;
		if (SPDSettings.isSpecialDay()) {
			name = Messages.get(WndFestival.class, "special_day.name");
			desc = SPDSettings.getSpecialDay_Message();
			if (desc == null || desc.isEmpty()) {
				desc = Messages.get(WndFestival.class, "special_day.desc");
			}
		} else {
			String key = holidayKey(Holidays.holiday);
			name = Messages.get(WndFestival.class, key + ".name");
			desc = Messages.get(WndFestival.class, key + ".desc");

			//现实日期无节日但圣诞彩蛋已生效：改显圣诞介绍
			if (Holidays.holiday == Holidays.Holiday.NONE && Dungeon.isXMAS()) {
				name = Messages.get(WndFestival.class, "xmas.name");
				desc = Messages.get(WndFestival.class, "xmas.desc");
				//彩蛋由营地NPC主动开启（CHRISTMAS模式位）时，标注"主动开启"
				if (Dungeon.isGameMode(WndStartGame.GameMode.CHRISTMAS)) {
					desc += Messages.get(WndFestival.class, "manual_note");
				}
			}
		}

		float y = MARGIN;

		IconTitle ttl = new IconTitle(Icons.get(Icons.INFO), name);
		ttl.setRect(MARGIN, y, width - 2 * MARGIN, 0);
		y = ttl.bottom() + MARGIN;
		add(ttl);

		RenderedTextBlock tf = PixelScene.renderTextBlock(desc, 6);
		tf.maxWidth(width);
		tf.invert();
		tf.setPos(MARGIN, y);
		add(tf);

		//点击窗口外部任意位置关闭
		PointerArea blocker = new PointerArea(0, 0,
				PixelScene.uiCamera.width, PixelScene.uiCamera.height) {
			@Override
			protected void onClick(PointerEvent event) {
				onBackPressed();
			}
		};
		blocker.camera = PixelScene.uiCamera;
		add(blocker);

		resize((int) (tf.width() + MARGIN * 2),
				(int) Math.min(tf.bottom() + MARGIN, 180));
	}

	/**
	 * 将节日枚举映射为 properties 中使用的小写 key。
	 */
	private static String holidayKey(Holidays.Holiday h) {
		if (h == null) return "none";
		switch (h) {
			case SPRING_FESTIVAL:    return "spring_festival";
			case LANTERN_FESTIVAL:   return "lantern_festival";
			case QINGMING:           return "qingming";
			case DRAGON_BOAT:        return "dragon_boat";
			case QIXI:               return "qixi";
			case DOUBLE_NINTH:       return "double_ninth";
			case NATIONAL_DAY:       return "national_day";
			case midAutumnFestival:  return "mid_autumn";
			case XMAS:               return "xmas";
			case HWEEN:              return "hween";
			case BREAD_INDEPENDENT:  return "bread";
			case EASTER:             return "easter";
			case NONE:
			default:                 return "none";
		}
	}
}
