package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;

import java.util.Arrays;

/**
 * 单向行走箭头层。
 * 每格按3x3划分，子格编号1..9（左上到右下）。
 * 仅当 from 允许走向 to、且 to 不允许走回 from 时，在两端对应的子格上绘制箭头。
 *
 * 贴图尚未接入，期望帧规格：8x8。
 * 帧0为空白，帧1..8依次为 N,NE,E,SE,S,SW,W,NW。
 */
public class OneWayTilemap extends Tilemap {

	private static final String TEXTURE = "environment/one_way.png";
	private static final int CELL = DungeonTilemap.SIZE / 3;

	public static final int NONE = -1;

	public static final int N	= 1;
	public static final int NE	= 2;
	public static final int E	= 3;
	public static final int SE	= 4;
	public static final int S	= 5;
	public static final int SW	= 6;
	public static final int W	= 7;
	public static final int NW	= 8;

	//方向索引与 PathFinder 的 NEIGHBOURS8 一致：NW,N,NE,W,E,SW,S,SE
	private static final int NW_I = 0;
	private static final int N_I  = 1;
	private static final int NE_I = 2;
	private static final int W_I  = 3;
	private static final int E_I  = 4;
	private static final int SW_I = 5;
	private static final int S_I  = 6;
	private static final int SE_I = 7;

	private static final int[] DIR_BIT		= {1, 2, 4, 8, 16, 32, 64, 128};
	private static final int[] DIR_DX		= {-1, 0, 1, -1, 1, -1, 0, 1};
	private static final int[] DIR_DY		= {-1, -1, -1, 0, 0, 1, 1, 1};
	private static final int[] SRC_SUB		= {0, 1, 2, 3, 5, 6, 7, 8};
	private static final int[] DST_SUB		= {8, 7, 6, 5, 3, 2, 1, 0};
	private static final int[] DIR_FRAME	= {NW, N, NE, W, E, SW, S, SE};
	private static final int[] OPP			= {7, 6, 5, 4, 3, 2, 1, 0};

	public OneWayTilemap() {
		super(TEXTURE, new TextureFilm(TEXTURE, CELL, CELL));

		Level level = Dungeon.level;
		data = buildArrows(level.width(), level.height(), level.oneWay, level.passable);
		map(data, level.width() * 3);
	}

	public void refresh() {
		Level level = Dungeon.level;
		data = buildArrows(level.width(), level.height(), level.oneWay, level.passable);
		updateMap();
	}

	static int[] buildArrows(int width, int height, int[] oneWay, boolean[] passable) {
		int cols = width * 3;
		int[] data = new int[cols * height * 3];
		Arrays.fill(data, NONE);

		if (passable == null || passable.length != width * height
				|| oneWay == null || oneWay.length != width * height) {
			return data;
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;
				if (!passable[pos]) continue;

				for (int d = 0; d < 8; d++) {
					int nx = x + DIR_DX[d];
					int ny = y + DIR_DY[d];
					if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;

					int npos = ny * width + nx;
					if (!oneWay(oneWay, passable, pos, npos, d)) continue;

					set(data, cols, x, y, SRC_SUB[d], DIR_FRAME[d]);
					set(data, cols, nx, ny, DST_SUB[d], DIR_FRAME[d]);
				}
			}
		}

		//合并：同一2x2区域四边均为单向且方向一致时，四个中心角同步为该方向
		for (int y = 0; y < height - 1; y++) {
			for (int x = 0; x < width - 1; x++) {
				int tl = y * width + x;
				int tr = tl + 1;
				int bl = tl + width;
				int br = bl + 1;

				int frame = mergeFrame(oneWay, passable, tl, tr, bl, br);
				if (frame == NONE) continue;

				set(data, cols, x, y, 8, frame);
				set(data, cols, x + 1, y, 6, frame);
				set(data, cols, x, y + 1, 2, frame);
				set(data, cols, x + 1, y + 1, 0, frame);
			}
		}

		return data;
	}

	private static int mergeFrame(int[] oneWay, boolean[] passable, int tl, int tr, int bl, int br) {
		if (oneWay(oneWay, passable, tr, tl, W_I)
				&& oneWay(oneWay, passable, br, bl, W_I)
				&& oneWay(oneWay, passable, br, tl, NW_I)
				&& oneWay(oneWay, passable, tr, bl, SW_I)) {
			return W;
		}

		if (oneWay(oneWay, passable, tl, tr, E_I)
				&& oneWay(oneWay, passable, bl, br, E_I)
				&& oneWay(oneWay, passable, tl, br, SE_I)
				&& oneWay(oneWay, passable, bl, tr, NE_I)) {
			return E;
		}

		if (oneWay(oneWay, passable, bl, tl, N_I)
				&& oneWay(oneWay, passable, br, tr, N_I)
				&& oneWay(oneWay, passable, br, tl, NW_I)
				&& oneWay(oneWay, passable, bl, tr, NE_I)) {
			return N;
		}

		if (oneWay(oneWay, passable, tl, bl, S_I)
				&& oneWay(oneWay, passable, tr, br, S_I)
				&& oneWay(oneWay, passable, tl, br, SE_I)
				&& oneWay(oneWay, passable, tr, bl, SW_I)) {
			return S;
		}

		return NONE;
	}

	private static boolean oneWay(int[] oneWay, boolean[] passable, int from, int to, int dir) {
		return passable[from] && passable[to]
				&& (oneWay[from] & DIR_BIT[dir]) == 0
				&& (oneWay[to] & DIR_BIT[OPP[dir]]) != 0;
	}

	private static void set(int[] data, int cols, int x, int y, int sub, int frame) {
		data[(y * 3 + sub / 3) * cols + x * 3 + sub % 3] = frame;
	}
}
