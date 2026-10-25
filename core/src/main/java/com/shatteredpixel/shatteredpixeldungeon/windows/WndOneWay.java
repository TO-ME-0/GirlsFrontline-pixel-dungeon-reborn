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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

//开发者调试窗口：编辑单个地格的单向行走禁止方向（仅调试构建可进入）
public class WndOneWay extends Window {

	private static final int WIDTH = 120;
	private static final int BTN_W = 38;
	private static final int BTN_H = 20;
	private static final int GAP = 2;

	//位序 NW,N,NE,W,E,SW,S,SE
	private static final int[] BITS = {1, 2, 4, 8, 16, 32, 64, 128};
	private static final String[] LABELS = {"NW", "N", "NE", "W", "E", "SW", "S", "SE"};
	//各方向在 3x3 网格中的列/行，中心格留给不可点的占位按钮
	private static final int[] COLS = {0, 1, 2, 0, 2, 0, 1, 2};
	private static final int[] ROWS = {0, 0, 0, 1, 1, 2, 2, 2};

	private final int cell;
	private final RedButton[] dirButtons = new RedButton[8];

	public WndOneWay( int cell ) {

		super();

		this.cell = cell;

		Level level = Dungeon.level;
		//首次写入时按当前关卡尺寸分配单向数组
		level.ensureOneWay();

		RenderedTextBlock title = PixelScene.renderTextBlock( "单向调试：格 " + cell, 7 );
		title.hardlight( TITLE_COLOR );
		title.maxWidth( WIDTH );
		title.setPos( 0, GAP );
		add( title );

		RenderedTextBlock hint = PixelScene.renderTextBlock( 6 );
		hint.text( "点击方向 = 禁止从该格向该方向离开；黄色为已禁止" );
		hint.maxWidth( WIDTH );
		hint.setPos( 0, title.bottom() + GAP );
		add( hint );

		float top = hint.bottom() + GAP;

		for (int i = 0; i < 8; i++) {
			final int dir = i;
			RedButton btn = new RedButton( LABELS[i] ) {
				@Override
				protected void onClick() {
					Level lv = Dungeon.level;
					lv.oneWay[cell] ^= BITS[dir];
					GameScene.updateMap( cell );
					refreshButtons();
				}
			};
			btn.setRect( COLS[i] * (BTN_W + GAP), top + ROWS[i] * (BTN_H + GAP), BTN_W, BTN_H );
			dirButtons[i] = btn;
			add( btn );
		}

		//中心占位，避免误触
		RedButton center = new RedButton( "·" );
		center.setRect( BTN_W + GAP, top + BTN_H + GAP, BTN_W, BTN_H );
		center.enable( false );
		add( center );

		float bottom = top + 3 * BTN_H + 2 * GAP + GAP;

		RedButton clear = new RedButton( "清除该格全部限制" ) {
			@Override
			protected void onClick() {
				Dungeon.level.oneWay[cell] = 0;
				GameScene.updateMap( cell );
				refreshButtons();
			}
		};
		clear.setRect( 0, bottom, WIDTH, BTN_H );
		add( clear );

		RedButton close = new RedButton( "关闭" ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		close.setRect( 0, bottom + BTN_H + GAP, WIDTH, BTN_H );
		add( close );

		refreshButtons();

		resize( WIDTH, (int)(close.bottom()) + GAP );
	}

	//按当前位值刷新各方向按钮颜色：黄色=禁止离开该方向
	private void refreshButtons() {
		int bits = Dungeon.level.oneWay != null && cell < Dungeon.level.oneWay.length
				? Dungeon.level.oneWay[cell] : 0;
		for (int i = 0; i < 8; i++) {
			dirButtons[i].textColor( (bits & BITS[i]) != 0 ? TITLE_COLOR : WHITE );
		}
	}
}
