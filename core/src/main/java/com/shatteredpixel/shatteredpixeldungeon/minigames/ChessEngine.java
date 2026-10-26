package com.shatteredpixel.shatteredpixeldungeon.minigames;

import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * 国际象棋规则引擎（纯逻辑，与UI解耦）。
 * 参考经典开源象棋程序（chessprogramming wiki / Sunfish 等）的实现思路：
 * 伪合法着法生成 + 虚拟执行过滤送将 = 合法着法；
 * 将军 / 将死 / 逼和 / 五十回合 / 子力不足判定；
 * 王车易位、吃过路兵、兵升变等特殊规则；
 * AI 采用 negamax + alpha-beta 剪枝 + 子力/位置价值表（PST）评估 + MVV-LVA 着法排序。
 *
 * 棋盘坐标：row 0 为白方底线（屏幕上方），row 7 为黑方底线；白兵向 row 增大方向前进。
 */
public class ChessEngine {

	public static final int SIZE = 8;

	// 棋子定义（数值与旧版 ChessScene 保持一致，方便渲染层复用贴图映射）
	public static final int EMPTY = 0;
	public static final int W_KING = 1, W_QUEEN = 2, W_ROOK = 3, W_BISHOP = 4, W_KNIGHT = 5, W_PAWN = 6;
	public static final int B_KING = 7, B_QUEEN = 8, B_ROOK = 9, B_BISHOP = 10, B_KNIGHT = 11, B_PAWN = 12;

	// 对局状态
	public static final int ONGOING = 0;
	public static final int CHECKMATE = 1;      // 将死（负方为当前走棋方）
	public static final int STALEMATE = 2;      // 逼和（无子可动但未被将军）
	public static final int DRAW_FIFTY = 3;     // 五十回合规则
	public static final int DRAW_MATERIAL = 4;  // 子力不足

	// 着法标记
	public static final int FLAG_NORMAL = 0;
	public static final int FLAG_DOUBLE_PAWN = 1;   // 兵初始两格
	public static final int FLAG_EN_PASSANT = 2;    // 吃过路兵
	public static final int FLAG_CASTLE_Q = 3;      // 后翼易位
	public static final int FLAG_CASTLE_K = 4;      // 王翼易位

	// 王车易位权利位标记
	private static final int CASTLE_WQ = 1, CASTLE_WK = 2, CASTLE_BQ = 4, CASTLE_BK = 8;

	public static class Move {
		public int fromRow, fromCol, toRow, toCol;
		public int flag = FLAG_NORMAL;
		public int promotion = EMPTY; // 升变目标棋子，非升变为 EMPTY
		int orderScore; // 着法排序用（AI）

		public Move(int fromRow, int fromCol, int toRow, int toCol) {
			this.fromRow = fromRow;
			this.fromCol = fromCol;
			this.toRow = toRow;
			this.toCol = toCol;
		}

		public boolean matches(int fromRow, int fromCol, int toRow, int toCol) {
			return this.fromRow == fromRow && this.fromCol == fromCol
					&& this.toRow == toRow && this.toCol == toCol;
		}
	}

	private final int[][] board = new int[SIZE][SIZE];
	private boolean whiteTurn = true;
	// 刚完成两格起步的兵所在位置（吃过路兵用），无则 -1
	private int epRow = -1, epCol = -1;
	private int castleRights = CASTLE_WQ | CASTLE_WK | CASTLE_BQ | CASTLE_BK;
	private int halfmoveClock = 0; // 五十回合规则计数
	private int wKingRow, wKingCol, bKingRow, bKingCol;

	// 撤销记录（public 以便UI层走子/悔棋等功能复用）
	public static class Undo {
		Move move;
		int movedPiece, capturedPiece;
		int prevEpRow, prevEpCol, prevCastle, prevHalfmove;
		int prevWKingRow, prevWKingCol, prevBKingRow, prevBKingCol;
	}

	public static int colorOf(int piece) {
		if (piece >= W_KING && piece <= W_PAWN) return 1;
		if (piece >= B_KING && piece <= B_PAWN) return -1;
		return 0;
	}

	public static boolean isWhite(int piece) {
		return colorOf(piece) == 1;
	}

	public int get(int row, int col) {
		return board[row][col];
	}

	public boolean whiteTurn() {
		return whiteTurn;
	}

	public void reset() {
		for (int r = 0; r < SIZE; r++)
			for (int c = 0; c < SIZE; c++)
				board[r][c] = EMPTY;

		board[0][0] = W_ROOK;   board[0][1] = W_KNIGHT; board[0][2] = W_BISHOP; board[0][3] = W_QUEEN;
		board[0][4] = W_KING;   board[0][5] = W_BISHOP; board[0][6] = W_KNIGHT; board[0][7] = W_ROOK;
		for (int c = 0; c < SIZE; c++) board[1][c] = W_PAWN;

		board[7][0] = B_ROOK;   board[7][1] = B_KNIGHT; board[7][2] = B_BISHOP; board[7][3] = B_QUEEN;
		board[7][4] = B_KING;   board[7][5] = B_BISHOP; board[7][6] = B_KNIGHT; board[7][7] = B_ROOK;
		for (int c = 0; c < SIZE; c++) board[6][c] = B_PAWN;

		whiteTurn = true;
		epRow = epCol = -1;
		castleRights = CASTLE_WQ | CASTLE_WK | CASTLE_BQ | CASTLE_BK;
		halfmoveClock = 0;
		wKingRow = 0; wKingCol = 4;
		bKingRow = 7; bKingCol = 4;
	}

	/** 复制局面（供 AI 后台线程搜索使用） */
	public ChessEngine copy() {
		ChessEngine e = new ChessEngine();
		for (int r = 0; r < SIZE; r++)
			System.arraycopy(board[r], 0, e.board[r], 0, SIZE);
		e.whiteTurn = whiteTurn;
		e.epRow = epRow; e.epCol = epCol;
		e.castleRights = castleRights;
		e.halfmoveClock = halfmoveClock;
		e.wKingRow = wKingRow; e.wKingCol = wKingCol;
		e.bKingRow = bKingRow; e.bKingCol = bKingCol;
		return e;
	}

	// ---------------- 着法生成 ----------------

	/** 生成当前走棋方的全部合法着法（已过滤送将） */
	public List<Move> generateLegalMoves() {
		List<Move> result = new ArrayList<>();
		List<Move> pseudo = generatePseudoMoves();
		for (Move m : pseudo) {
			Undo u = make(m);
			// 走完该着后，轮次已翻转，检查走子方的王是否被攻击
			if (!inCheck(!whiteTurn)) {
				result.add(m);
			}
			unmake(u);
		}
		return result;
	}

	/** 指定棋子的合法着法（UI 选中提示用） */
	public List<Move> generateLegalMoves(int row, int col) {
		List<Move> all = generateLegalMoves();
		List<Move> result = new ArrayList<>();
		for (Move m : all) {
			if (m.fromRow == row && m.fromCol == col) result.add(m);
		}
		return result;
	}

	private List<Move> generatePseudoMoves() {
		List<Move> moves = new ArrayList<>(40);
		for (int r = 0; r < SIZE; r++) {
			for (int c = 0; c < SIZE; c++) {
				int piece = board[r][c];
				if (piece == EMPTY) continue;
				if (isWhite(piece) != whiteTurn) continue;
				switch (piece) {
					case W_PAWN: case B_PAWN:   genPawn(moves, r, c);   break;
					case W_KNIGHT: case B_KNIGHT: genLeaper(moves, r, c, KNIGHT_OFFSETS); break;
					case W_BISHOP: case B_BISHOP: genSlider(moves, r, c, BISHOP_DIRS); break;
					case W_ROOK: case B_ROOK:     genSlider(moves, r, c, ROOK_DIRS); break;
					case W_QUEEN: case B_QUEEN:   genSlider(moves, r, c, QUEEN_DIRS); break;
					case W_KING: case B_KING:     genKing(moves, r, c); break;
				}
			}
		}
		return moves;
	}

	private static final int[][] KNIGHT_OFFSETS = {
			{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}
	};
	private static final int[][] BISHOP_DIRS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
	private static final int[][] ROOK_DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
	private static final int[][] QUEEN_DIRS = {
			{-1, -1}, {-1, 1}, {1, -1}, {1, 1}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}
	};

	private static boolean inBoard(int r, int c) {
		return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
	}

	private void addMove(List<Move> moves, int fr, int fc, int tr, int tc) {
		moves.add(new Move(fr, fc, tr, tc));
	}

	private void genPawn(List<Move> moves, int r, int c) {
		boolean white = whiteTurn;
		int dir = white ? 1 : -1;
		int startRow = white ? 1 : 6;
		int promoRow = white ? 7 : 0;

		// 前进一格 / 升变
		if (inBoard(r + dir, c) && board[r + dir][c] == EMPTY) {
			if (r + dir == promoRow) {
				addPromotions(moves, r, c, r + dir, c);
			} else {
				addMove(moves, r, c, r + dir, c);
				// 初始两格
				if (r == startRow && board[r + 2 * dir][c] == EMPTY) {
					Move m = new Move(r, c, r + 2 * dir, c);
					m.flag = FLAG_DOUBLE_PAWN;
					moves.add(m);
				}
			}
		}

		// 斜吃 / 吃过路兵
		for (int dc = -1; dc <= 1; dc += 2) {
			int tr = r + dir, tc = c + dc;
			if (!inBoard(tr, tc)) continue;
			int target = board[tr][tc];
			if (target != EMPTY && isWhite(target) != white) {
				if (tr == promoRow) {
					addPromotions(moves, r, c, tr, tc);
				} else {
					addMove(moves, r, c, tr, tc);
				}
			} else if (target == EMPTY && r == epRow && tc == epCol) {
				Move m = new Move(r, c, tr, tc);
				m.flag = FLAG_EN_PASSANT;
				moves.add(m);
			}
		}
	}

	private void addPromotions(List<Move> moves, int fr, int fc, int tr, int tc) {
		int[] pieces = whiteTurn ?
				new int[]{W_QUEEN, W_ROOK, W_BISHOP, W_KNIGHT} :
				new int[]{B_QUEEN, B_ROOK, B_BISHOP, B_KNIGHT};
		for (int p : pieces) {
			Move m = new Move(fr, fc, tr, tc);
			m.promotion = p;
			moves.add(m);
		}
	}

	private void genLeaper(List<Move> moves, int r, int c, int[][] offsets) {
		for (int[] o : offsets) {
			int tr = r + o[0], tc = c + o[1];
			if (!inBoard(tr, tc)) continue;
			int target = board[tr][tc];
			if (target == EMPTY || isWhite(target) != whiteTurn) {
				addMove(moves, r, c, tr, tc);
			}
		}
	}

	private void genSlider(List<Move> moves, int r, int c, int[][] dirs) {
		for (int[] d : dirs) {
			int tr = r + d[0], tc = c + d[1];
			while (inBoard(tr, tc)) {
				int target = board[tr][tc];
				if (target == EMPTY) {
					addMove(moves, r, c, tr, tc);
				} else {
					if (isWhite(target) != whiteTurn) addMove(moves, r, c, tr, tc);
					break;
				}
				tr += d[0];
				tc += d[1];
			}
		}
	}

	private void genKing(List<Move> moves, int r, int c) {
		genLeaper(moves, r, c, QUEEN_DIRS); // 王走八方一格

		// 王车易位：王与车未动、之间无子、王不处于被将且经过/到达的格子不被攻击
		boolean white = whiteTurn;
		int row = white ? 0 : 7;
		if (r != row || c != 4) return;
		if (inCheck(white)) return;

		int kingSide = white ? CASTLE_WK : CASTLE_BK;
		int queenSide = white ? CASTLE_WQ : CASTLE_BQ;
		int rook = white ? W_ROOK : B_ROOK;

		// 王翼易位（短易位）：王 e→g，车 h→f
		if ((castleRights & kingSide) != 0 && board[row][7] == rook
				&& board[row][5] == EMPTY && board[row][6] == EMPTY
				&& !isSquareAttacked(row, 5, !white) && !isSquareAttacked(row, 6, !white)) {
			Move m = new Move(r, c, row, 6);
			m.flag = FLAG_CASTLE_K;
			moves.add(m);
		}
		// 后翼易位（长易位）：王 e→c，车 a→d
		if ((castleRights & queenSide) != 0 && board[row][0] == rook
				&& board[row][1] == EMPTY && board[row][2] == EMPTY && board[row][3] == EMPTY
				&& !isSquareAttacked(row, 3, !white) && !isSquareAttacked(row, 2, !white)) {
			Move m = new Move(r, c, row, 2);
			m.flag = FLAG_CASTLE_Q;
			moves.add(m);
		}
	}

	// ---------------- 攻击检测 ----------------

	public boolean inCheck(boolean whiteSide) {
		return whiteSide ?
				isSquareAttacked(wKingRow, wKingCol, false) :
				isSquareAttacked(bKingRow, bKingCol, true);
	}

	/** 格子 (r,c) 是否被 byWhite 一方攻击 */
	public boolean isSquareAttacked(int r, int c, boolean byWhite) {
		// 兵：白兵向下（row+方向）攻击，黑兵向上
		int pawnRow = byWhite ? r - 1 : r + 1;
		int pawn = byWhite ? W_PAWN : B_PAWN;
		if (inBoard(pawnRow, c - 1) && board[pawnRow][c - 1] == pawn) return true;
		if (inBoard(pawnRow, c + 1) && board[pawnRow][c + 1] == pawn) return true;

		// 马
		int knight = byWhite ? W_KNIGHT : B_KNIGHT;
		for (int[] o : KNIGHT_OFFSETS) {
			int tr = r + o[0], tc = c + o[1];
			if (inBoard(tr, tc) && board[tr][tc] == knight) return true;
		}

		// 王（相邻格，保证双王不相邻）
		int king = byWhite ? W_KING : B_KING;
		for (int[] o : QUEEN_DIRS) {
			int tr = r + o[0], tc = c + o[1];
			if (inBoard(tr, tc) && board[tr][tc] == king) return true;
		}

		// 车/后：直线
		int rook = byWhite ? W_ROOK : B_ROOK;
		int queen = byWhite ? W_QUEEN : B_QUEEN;
		for (int[] d : ROOK_DIRS) {
			int tr = r + d[0], tc = c + d[1];
			while (inBoard(tr, tc)) {
				int p = board[tr][tc];
				if (p != EMPTY) {
					if (p == rook || p == queen) return true;
					break;
				}
				tr += d[0]; tc += d[1];
			}
		}

		// 象/后：斜线
		int bishop = byWhite ? W_BISHOP : B_BISHOP;
		for (int[] d : BISHOP_DIRS) {
			int tr = r + d[0], tc = c + d[1];
			while (inBoard(tr, tc)) {
				int p = board[tr][tc];
				if (p != EMPTY) {
					if (p == bishop || p == queen) return true;
					break;
				}
				tr += d[0]; tc += d[1];
			}
		}

		return false;
	}

	// ---------------- 走子 / 撤销 ----------------

	// 搜索递归每层都需要独立的 Undo，因此 make() 每次返回新对象。
	// UI 层走子频率低，AI 搜索的分配量在现代设备上可接受。

	public Undo make(Move m) {
		Undo u = new Undo();
		u.move = m;
		u.movedPiece = board[m.fromRow][m.fromCol];
		u.capturedPiece = board[m.toRow][m.toCol];
		u.prevEpRow = epRow; u.prevEpCol = epCol;
		u.prevCastle = castleRights;
		u.prevHalfmove = halfmoveClock;
		u.prevWKingRow = wKingRow; u.prevWKingCol = wKingCol;
		u.prevBKingRow = bKingRow; u.prevBKingCol = bKingCol;

		// 吃过路兵：被吃的兵不在目标格
		if (m.flag == FLAG_EN_PASSANT) {
			u.capturedPiece = board[epRow][epCol];
			board[epRow][epCol] = EMPTY;
		}

		board[m.toRow][m.toCol] = (m.promotion != EMPTY) ? m.promotion : u.movedPiece;
		board[m.fromRow][m.fromCol] = EMPTY;

		// 王车易位：移动车
		if (m.flag == FLAG_CASTLE_K) {
			board[m.toRow][5] = board[m.toRow][7];
			board[m.toRow][7] = EMPTY;
		} else if (m.flag == FLAG_CASTLE_Q) {
			board[m.toRow][3] = board[m.toRow][0];
			board[m.toRow][0] = EMPTY;
		}

		// 更新王位置
		if (u.movedPiece == W_KING) { wKingRow = m.toRow; wKingCol = m.toCol; }
		if (u.movedPiece == B_KING) { bKingRow = m.toRow; bKingCol = m.toCol; }

		// 更新易位权利：王动则全失；角格作为起点或终点（吃车）则对应权利失效
		if (u.movedPiece == W_KING) castleRights &= ~(CASTLE_WQ | CASTLE_WK);
		if (u.movedPiece == B_KING) castleRights &= ~(CASTLE_BQ | CASTLE_BK);
		if (m.fromRow == 0 && m.fromCol == 0 || m.toRow == 0 && m.toCol == 0) castleRights &= ~CASTLE_WQ;
		if (m.fromRow == 0 && m.fromCol == 7 || m.toRow == 0 && m.toCol == 7) castleRights &= ~CASTLE_WK;
		if (m.fromRow == 7 && m.fromCol == 0 || m.toRow == 7 && m.toCol == 0) castleRights &= ~CASTLE_BQ;
		if (m.fromRow == 7 && m.fromCol == 7 || m.toRow == 7 && m.toCol == 7) castleRights &= ~CASTLE_BK;

		// 过路兵标记
		if (m.flag == FLAG_DOUBLE_PAWN) {
			epRow = m.toRow; epCol = m.toCol;
		} else {
			epRow = epCol = -1;
		}

		// 五十回合计数：兵走子或吃子则清零
		if (u.movedPiece == W_PAWN || u.movedPiece == B_PAWN || u.capturedPiece != EMPTY) {
			halfmoveClock = 0;
		} else {
			halfmoveClock++;
		}

		whiteTurn = !whiteTurn;
		return u;
	}

	public void unmake(Undo u) {
		Move m = u.move;
		whiteTurn = !whiteTurn;

		board[m.fromRow][m.fromCol] = u.movedPiece;
		board[m.toRow][m.toCol] = u.capturedPiece;

		// 撤销吃过路兵：目标格本为空，被吃的兵放回原位
		if (m.flag == FLAG_EN_PASSANT) {
			board[m.toRow][m.toCol] = EMPTY;
			board[u.prevEpRow][u.prevEpCol] = u.capturedPiece;
		}

		// 撤销王车易位：把车移回角格
		if (m.flag == FLAG_CASTLE_K) {
			board[m.toRow][7] = board[m.toRow][5];
			board[m.toRow][5] = EMPTY;
		} else if (m.flag == FLAG_CASTLE_Q) {
			board[m.toRow][0] = board[m.toRow][3];
			board[m.toRow][3] = EMPTY;
		}

		epRow = u.prevEpRow; epCol = u.prevEpCol;
		castleRights = u.prevCastle;
		halfmoveClock = u.prevHalfmove;
		wKingRow = u.prevWKingRow; wKingCol = u.prevWKingCol;
		bKingRow = u.prevBKingRow; bKingCol = u.prevBKingCol;
	}

	// ---------------- 对局状态 ----------------

	public int status() {
		List<Move> moves = generateLegalMoves();
		if (moves.isEmpty()) {
			return inCheck(whiteTurn) ? CHECKMATE : STALEMATE;
		}
		if (halfmoveClock >= 100) return DRAW_FIFTY;
		if (insufficientMaterial()) return DRAW_MATERIAL;
		return ONGOING;
	}

	/** 子力不足判和：双方仅剩王，或一方仅多一象/一马 */
	private boolean insufficientMaterial() {
		int minors = 0;
		for (int r = 0; r < SIZE; r++) {
			for (int c = 0; c < SIZE; c++) {
				int p = board[r][c];
				if (p == EMPTY || p == W_KING || p == B_KING) continue;
				if (p == W_BISHOP || p == B_BISHOP || p == W_KNIGHT || p == B_KNIGHT) {
					if (++minors > 1) return false;
				} else {
					return false; // 有兵/车/后
				}
			}
		}
		return true;
	}

	// ---------------- AI ----------------

	private static final float MATE_SCORE = 1_000_000f;
	private static final float INF = 2_000_000f;

	// 子力价值（经典取值）
	private static int materialOf(int piece) {
		switch (piece) {
			case W_PAWN: case B_PAWN:     return 100;
			case W_KNIGHT: case B_KNIGHT: return 320;
			case W_BISHOP: case B_BISHOP: return 330;
			case W_ROOK: case B_ROOK:     return 500;
			case W_QUEEN: case B_QUEEN:   return 900;
			case W_KING: case B_KING:     return 20_000;
			default: return 0;
		}
	}

	// 棋子位置价值表（中局），row 0 = 白方底线（rank 1），黑方镜像使用
	private static final int[][] PST_PAWN = {
			{  0,  0,  0,  0,  0,  0,  0,  0},
			{  5, 10, 10,-20,-20, 10, 10,  5},
			{  5, -5,-10,  0,  0,-10, -5,  5},
			{  0,  0,  0, 20, 20,  0,  0,  0},
			{  5,  5, 10, 25, 25, 10,  5,  5},
			{ 10, 10, 20, 30, 30, 20, 10, 10},
			{ 50, 50, 50, 50, 50, 50, 50, 50},
			{  0,  0,  0,  0,  0,  0,  0,  0}
	};
	private static final int[][] PST_KNIGHT = {
			{-50,-40,-30,-30,-30,-30,-40,-50},
			{-40,-20,  0,  0,  0,  0,-20,-40},
			{-30,  0, 10, 15, 15, 10,  0,-30},
			{-30,  5, 15, 20, 20, 15,  5,-30},
			{-30,  0, 15, 20, 20, 15,  0,-30},
			{-30,  5, 10, 15, 15, 10,  0,-30},
			{-40,-20,  0,  5,  5,  0,-20,-40},
			{-50,-40,-30,-30,-30,-30,-40,-50}
	};
	private static final int[][] PST_BISHOP = {
			{-20,-10,-10,-10,-10,-10,-10,-20},
			{-10,  0,  0,  0,  0,  0,  0,-10},
			{-10,  0,  5, 10, 10,  5,  0,-10},
			{-10,  5,  5, 10, 10,  5,  5,-10},
			{-10,  0, 10, 10, 10, 10,  0,-10},
			{-10, 10, 10, 10, 10, 10, 10,-10},
			{-10,  5,  0,  0,  0,  0,  5,-10},
			{-20,-10,-10,-10,-10,-10,-10,-20}
	};
	private static final int[][] PST_ROOK = {
			{ 0,  0,  0,  0,  0,  0,  0,  0},
			{ 5, 10, 10, 10, 10, 10, 10,  5},
			{-5,  0,  0,  0,  0,  0,  0, -5},
			{-5,  0,  0,  0,  0,  0,  0, -5},
			{-5,  0,  0,  0,  0,  0,  0, -5},
			{-5,  0,  0,  0,  0,  0,  0, -5},
			{-5,  0,  0,  0,  0,  0,  0, -5},
			{ 0,  0,  0,  5,  5,  0,  0,  0}
	};
	private static final int[][] PST_QUEEN = {
			{-20,-10,-10, -5, -5,-10,-10,-20},
			{-10,  0,  0,  0,  0,  0,  0,-10},
			{-10,  0,  5,  5,  5,  5,  0,-10},
			{ -5,  0,  5,  5,  5,  5,  0, -5},
			{  0,  0,  5,  5,  5,  5,  0, -5},
			{-10,  5,  5,  5,  5,  5,  0,-10},
			{-10,  0,  5,  0,  0,  0,  0,-10},
			{-20,-10,-10, -5, -5,-10,-10,-20}
	};
	private static final int[][] PST_KING = {
			{-30,-40,-40,-50,-50,-40,-40,-30},
			{-30,-40,-40,-50,-50,-40,-40,-30},
			{-30,-40,-40,-50,-50,-40,-40,-30},
			{-30,-40,-40,-50,-50,-40,-40,-30},
			{-20,-30,-30,-40,-40,-30,-30,-20},
			{-10,-20,-20,-20,-20,-20,-20,-10},
			{ 20, 20,  0,  0,  0,  0, 20, 20},
			{ 20, 30, 10,  0,  0, 10, 30, 20}
	};

	private static int pstOf(int piece, int row, int col) {
		// 黑方镜像：黑棋在 row r 的站位价值等价于白棋在 7-r
		int r = isWhite(piece) ? row : (SIZE - 1 - row);
		switch (piece) {
			case W_PAWN: case B_PAWN:     return PST_PAWN[r][col];
			case W_KNIGHT: case B_KNIGHT: return PST_KNIGHT[r][col];
			case W_BISHOP: case B_BISHOP: return PST_BISHOP[r][col];
			case W_ROOK: case B_ROOK:     return PST_ROOK[r][col];
			case W_QUEEN: case B_QUEEN:   return PST_QUEEN[r][col];
			case W_KING: case B_KING:     return PST_KING[r][col];
			default: return 0;
		}
	}

	/** 局面评估（子力 + 位置），返回当前走棋方视角的分数 */
	private float evaluate() {
		int score = 0;
		for (int r = 0; r < SIZE; r++) {
			for (int c = 0; c < SIZE; c++) {
				int p = board[r][c];
				if (p == EMPTY) continue;
				int v = materialOf(p) + pstOf(p, r, c);
				score += isWhite(p) ? v : -v;
			}
		}
		return whiteTurn ? score : -score;
	}

	/** MVV-LVA 着法排序：高收益吃子优先，提升 alpha-beta 剪枝效率 */
	private void orderMoves(List<Move> moves) {
		for (Move m : moves) {
			int s = 0;
			int victim = (m.flag == FLAG_EN_PASSANT) ?
					(whiteTurn ? B_PAWN : W_PAWN) : board[m.toRow][m.toCol];
			if (victim != EMPTY) s = 10 * materialOf(victim) - materialOf(board[m.fromRow][m.fromCol]);
			if (m.promotion != EMPTY) s += materialOf(m.promotion);
			m.orderScore = s;
		}
		moves.sort((a, b) -> b.orderScore - a.orderScore);
	}

	private float alphaBeta(int depth, float alpha, float beta, int ply) {
		if (depth <= 0) return evaluate();

		List<Move> moves = generateLegalMoves();
		if (moves.isEmpty()) {
			// 无子可动：被将死为负分（ply 越小惩罚越重，倾向更快将杀/更晚被将杀）
			return inCheck(whiteTurn) ? -MATE_SCORE + ply : 0;
		}

		orderMoves(moves);
		float best = -INF;
		for (Move m : moves) {
			Undo u = make(m);
			float s = -alphaBeta(depth - 1, -beta, -alpha, ply + 1);
			unmake(u);
			if (s > best) best = s;
			if (s > alpha) alpha = s;
			if (alpha >= beta) break;
		}
		return best;
	}

	/**
	 * 搜索最佳着法。depth 为搜索深度（建议 2~4）。
	 * 同分时随机选择，避免每局AI走法完全一致。无可走着返回 null。
	 */
	public Move findBestMove(int depth) {
		List<Move> moves = generateLegalMoves();
		if (moves.isEmpty()) return null;

		orderMoves(moves);
		float best = -INF;
		List<Move> bestMoves = new ArrayList<>();
		for (Move m : moves) {
			Undo u = make(m);
			float s = -alphaBeta(depth - 1, -INF, INF, 1);
			unmake(u);
			if (s > best + 0.001f) {
				best = s;
				bestMoves.clear();
				bestMoves.add(m);
			} else if (Math.abs(s - best) <= 0.001f) {
				bestMoves.add(m);
			}
		}
		return Random.element(bestMoves);
	}
}
