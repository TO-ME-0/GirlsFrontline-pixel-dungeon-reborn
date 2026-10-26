package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.minigames.ChessEngine;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;

import java.util.ArrayList;
import java.util.List;

/**
 * 国际象棋小游戏场景（UI层）。
 * 规则与AI逻辑全部由 {@link ChessEngine} 承担，本类只负责渲染与交互：
 * 合法着法高亮（普通移动绿点 / 吃子红点）、上一步高亮、将军红标、
 * 兵升变选择、将死/逼和/和棋结算、AI难度选择与后台异步思考。
 */
public class ChessScene extends PixelScene {
	{
		inGameScene = true;
	}

	// 资源图中每个棋子/棋盘格子大小为24*24
	private static final int TILE_SIZE = 24;

	private final ChessEngine engine = new ChessEngine();

	private boolean gameRunning;
	private boolean playerIsWhite = true;
	// AI难度：0关闭（双人对弈），1简单，2普通，3困难
	private int aiLevel = 0;
	private static final int[] AI_DEPTHS = {0, 2, 3, 4};

	// 选择状态
	private int selectedRow = -1;
	private int selectedCol = -1;
	private final List<ChessEngine.Move> selectedMoves = new ArrayList<>();
	private Image selectionIndicator;
	private final List<Image> moveIndicators = new ArrayList<>();

	// 高亮：上一步起止格、被将军的王
	private Image lastMoveFromImg, lastMoveToImg, checkImg;
	private int lastFromRow = -1, lastFromCol = -1, lastToRow = -1, lastToCol = -1;

	// 渲染组件
	private Image background;
	private StyledButton[][] chessButtons;
	private StyledButton restartButton;
	private StyledButton exitButton;
	private StyledButton aiButton;
	private RenderedTextBlock statusText;

	private float adjustedCellSize;
	private float boardX, boardY;

	// AI异步思考
	private volatile boolean aiThinking = false;
	private volatile int aiSession = 0;
	private volatile AiResult aiResult = null;

	private static class AiResult {
		final int session;
		final ChessEngine.Move move;
		AiResult(int session, ChessEngine.Move move) {
			this.session = session;
			this.move = move;
		}
	}

	@Override
	public void create() {
		super.create();

		setupBackground();
		createBoardVisual();
		createHighlights();
		addControlButtons();
		createSelectionIndicator();
		createStatusText();

		restartGame();
		fadeIn();
	}

	private String msg(String key) {
		return Messages.get(ChessScene.class, key);
	}

	// 设置背景
	private void setupBackground() {
		background = new Image(Assets.Interfaces.LOADING_PRISON);
		background.camera = uiCamera;

		float scaleX = uiCamera.width / (float) background.width;
		float scaleY = uiCamera.height / (float) background.height;
		background.scale.set(scaleX, scaleY);

		add(background);
	}

	// 布局常量：顶部状态栏 / 底部按钮栏高度（游戏像素）
	private static final float TOP_BAR = 14;
	private static final float BOTTOM_BAR = 24;

	// 创建游戏板视觉组件（棋盘在扣除顶栏与底栏后的区域内尽可能放大并居中）
	private void createBoardVisual() {
		float availW = uiCamera.width;
		float availH = uiCamera.height - TOP_BAR - BOTTOM_BAR;
		adjustedCellSize = Math.min(availW, availH) / ChessEngine.SIZE;

		float boardSize = adjustedCellSize * ChessEngine.SIZE;
		boardX = (uiCamera.width - boardSize) / 2f;
		boardY = TOP_BAR + (availH - boardSize) / 2f;

		chessButtons = new StyledButton[ChessEngine.SIZE][ChessEngine.SIZE];
		for (int row = 0; row < ChessEngine.SIZE; row++) {
			for (int col = 0; col < ChessEngine.SIZE; col++) {
				final int finalRow = row;
				final int finalCol = col;

				NinePatch bg = new NinePatch(Assets.Environment.CHESS, 0, ((row + col) % 2) * TILE_SIZE, TILE_SIZE, TILE_SIZE, 0);
				chessButtons[row][col] = new StyledButton(bg, "") {
					@Override
					protected void onClick() {
						handleChessPieceClick(finalRow, finalCol);
					}
				};

				chessButtons[row][col].setSize(adjustedCellSize, adjustedCellSize);
				chessButtons[row][col].setPos(cellX(col), cellY(row));
				chessButtons[row][col].camera = uiCamera;
				add(chessButtons[row][col]);
			}
		}
	}

	// 视角方向：玩家主控方渲染在棋盘下方（执黑时棋盘整体180°旋转）
	private boolean whiteAtBottom = true;

	private float cellX(int col) {
		return boardX + (whiteAtBottom ? col : ChessEngine.SIZE - 1 - col) * adjustedCellSize;
	}

	private float cellY(int row) {
		return boardY + (whiteAtBottom ? ChessEngine.SIZE - 1 - row : row) * adjustedCellSize;
	}

	// 视角变化后重排棋盘格子并刷新各类指示器
	private void repositionBoard() {
		for (int row = 0; row < ChessEngine.SIZE; row++) {
			for (int col = 0; col < ChessEngine.SIZE; col++) {
				chessButtons[row][col].setPos(cellX(col), cellY(row));
			}
		}
		clearSelection();
		updateHighlights();
	}

	// 上一步与将军的高亮块
	private void createHighlights() {
		lastMoveFromImg = new Image(TextureCache.createSolid(0xFFFFFF00));
		lastMoveFromImg.alpha(0.3f);
		lastMoveFromImg.visible = false;
		lastMoveFromImg.camera = uiCamera;
		add(lastMoveFromImg);

		lastMoveToImg = new Image(TextureCache.createSolid(0xFFFFFF00));
		lastMoveToImg.alpha(0.3f);
		lastMoveToImg.visible = false;
		lastMoveToImg.camera = uiCamera;
		add(lastMoveToImg);

		checkImg = new Image(TextureCache.createSolid(0xFFFF3333));
		checkImg.alpha(0.55f);
		checkImg.visible = false;
		checkImg.camera = uiCamera;
		add(checkImg);
	}

	// 顶部状态文字（回合 / 将军 / AI思考中）
	private void createStatusText() {
		statusText = renderTextBlock("", 9);
		statusText.camera = uiCamera;
		add(statusText);
		refreshStatusText();
	}

	private void refreshStatusText() {
		String text;
		if (aiThinking) {
			text = msg("thinking");
		} else {
			text = engine.whiteTurn() ? msg("turn_white") : msg("turn_black");
			if (gameRunning && engine.inCheck(engine.whiteTurn())) {
				text += " · " + msg("check");
			}
		}
		statusText.text(text);
		// 在顶部状态栏内水平垂直居中
		statusText.setPos(
				Math.round((uiCamera.width - statusText.width()) / 2f),
				Math.round((TOP_BAR - statusText.height()) / 2f)
		);
	}

	// ---------------- 交互 ----------------

	private void handleChessPieceClick(int row, int col) {
		if (!gameRunning || isAiTurn() || aiThinking) return;

		int piece = engine.get(row, col);
		boolean ownPiece = piece != ChessEngine.EMPTY
				&& ChessEngine.isWhite(piece) == engine.whiteTurn();

		if (selectedRow == -1) {
			// 未选中：只能选择当前走棋方的棋子
			if (ownPiece) select(row, col);
			return;
		}

		if (ownPiece) {
			// 改选自己的其他棋子
			select(row, col);
			return;
		}

		// 尝试移动
		List<ChessEngine.Move> candidates = new ArrayList<>();
		for (ChessEngine.Move m : selectedMoves) {
			if (m.toRow == row && m.toCol == col) candidates.add(m);
		}

		if (candidates.isEmpty()) {
			// 点了非法位置，取消选择
			clearSelection();
			return;
		}

		if (candidates.get(0).promotion != ChessEngine.EMPTY) {
			// 升变：弹出选择窗口
			addToFront(new WndPromotion(candidates));
		} else {
			engine.make(candidates.get(0));
			afterMove(candidates.get(0));
		}
	}

	private void select(int row, int col) {
		// 先清除上一个棋子的落点提示，再显示新选中棋子的
		clearMoveIndicators();
		selectedRow = row;
		selectedCol = col;
		selectedMoves.clear();
		selectedMoves.addAll(engine.generateLegalMoves(row, col));
		updateSelectionIndicator();
		showValidMoves();
	}

	private void afterMove(ChessEngine.Move m) {
		lastFromRow = m.fromRow; lastFromCol = m.fromCol;
		lastToRow = m.toRow;   lastToCol = m.toCol;
		clearSelection();
		updateChessButtons();
		updateHighlights();
		refreshStatusText();

		int status = engine.status();
		if (status != ChessEngine.ONGOING) {
			gameRunning = false;
			addToFront(new WndResult(status));
		}
	}

	// ---------------- AI ----------------

	private boolean isAiTurn() {
		return aiLevel > 0 && gameRunning && (engine.whiteTurn() != playerIsWhite);
	}

	private void startAiThinking() {
		aiThinking = true;
		refreshStatusText();
		final int session = aiSession;
		final ChessEngine snapshot = engine.copy();
		final int depth = AI_DEPTHS[aiLevel];
		Thread t = new Thread(() -> {
			long start = System.currentTimeMillis();
			ChessEngine.Move move = snapshot.findBestMove(depth);
			// 最小思考时间，避免AI落子过快显得突兀
			long elapsed = System.currentTimeMillis() - start;
			if (elapsed < 350) {
				try {
					Thread.sleep(350 - elapsed);
				} catch (InterruptedException ignored) {
				}
			}
			aiResult = new AiResult(session, move);
		});
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void update() {
		super.update();

		if (!gameRunning || !isAiTurn()) return;

		AiResult result = aiResult;
		if (result != null) {
			aiResult = null;
			aiThinking = false;
			if (result.session == aiSession && result.move != null) {
				engine.make(result.move);
				afterMove(result.move);
			} else {
				refreshStatusText();
			}
		} else if (!aiThinking) {
			startAiThinking();
		}
	}

	// ---------------- 渲染更新 ----------------

	private void updateChessButtons() {
		for (int row = 0; row < ChessEngine.SIZE; row++) {
			for (int col = 0; col < ChessEngine.SIZE; col++) {
				StyledButton button = chessButtons[row][col];
				int pieceType = engine.get(row, col);

				if (pieceType != ChessEngine.EMPTY) {
					button.icon(createPieceImage(pieceType));
				} else {
					button.icon(null);
				}
			}
		}
	}

	private Image createPieceImage(int pieceType) {
		int row = 0, col = 0;
		switch (pieceType) {
			case ChessEngine.B_PAWN:   row = 0; col = 1; break;
			case ChessEngine.B_ROOK:   row = 0; col = 2; break;
			case ChessEngine.B_KNIGHT: row = 0; col = 3; break;
			case ChessEngine.B_BISHOP: row = 0; col = 4; break;
			case ChessEngine.B_KING:   row = 0; col = 5; break;
			case ChessEngine.B_QUEEN:  row = 0; col = 6; break;
			case ChessEngine.W_PAWN:   row = 1; col = 1; break;
			case ChessEngine.W_ROOK:   row = 1; col = 2; break;
			case ChessEngine.W_KNIGHT: row = 1; col = 3; break;
			case ChessEngine.W_BISHOP: row = 1; col = 4; break;
			case ChessEngine.W_KING:   row = 1; col = 5; break;
			case ChessEngine.W_QUEEN:  row = 1; col = 6; break;
		}

		Image pieceImg = new Image(Assets.Environment.CHESS);
		pieceImg.frame(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

		float scale = (adjustedCellSize - 8) / pieceImg.width;
		pieceImg.scale.set(scale);

		return pieceImg;
	}

	private void updateHighlights() {
		if (lastFromRow != -1) {
			// 贴图为1x1纯色，scale即像素尺寸
			lastMoveFromImg.scale.set(adjustedCellSize, adjustedCellSize);
			lastMoveFromImg.setPos(cellX(lastFromCol), cellY(lastFromRow));
			lastMoveFromImg.visible = true;
			lastMoveToImg.scale.set(adjustedCellSize, adjustedCellSize);
			lastMoveToImg.setPos(cellX(lastToCol), cellY(lastToRow));
			lastMoveToImg.visible = true;
		} else {
			lastMoveFromImg.visible = false;
			lastMoveToImg.visible = false;
		}

		// 将军红标：标出当前被将一方的王
		if (gameRunning && engine.inCheck(engine.whiteTurn())) {
			int kingRow = -1, kingCol = -1;
			int king = engine.whiteTurn() ? ChessEngine.W_KING : ChessEngine.B_KING;
			for (int r = 0; r < ChessEngine.SIZE && kingRow == -1; r++) {
				for (int c = 0; c < ChessEngine.SIZE; c++) {
					if (engine.get(r, c) == king) {
						kingRow = r; kingCol = c;
						break;
					}
				}
			}
			if (kingRow != -1) {
				checkImg.scale.set(adjustedCellSize, adjustedCellSize);
				checkImg.setPos(cellX(kingCol), cellY(kingRow));
				checkImg.visible = true;
			}
		} else {
			checkImg.visible = false;
		}
	}

	// ---------------- 选择与着法提示 ----------------

	private void createSelectionIndicator() {
		selectionIndicator = new Image(Icons.get(Icons.TARGET));
		selectionIndicator.hardlight(0xFFFFFF);
		selectionIndicator.visible = false;
		selectionIndicator.camera = uiCamera;

		float scale = adjustedCellSize / selectionIndicator.width();
		selectionIndicator.scale.set(scale);

		add(selectionIndicator);
	}

	private void updateSelectionIndicator() {
		if (selectedRow == -1) {
			selectionIndicator.visible = false;
			return;
		}
		selectionIndicator.setPos(cellX(selectedCol), cellY(selectedRow));
		selectionIndicator.visible = true;
	}

	private void clearSelection() {
		selectedRow = -1;
		selectedCol = -1;
		selectedMoves.clear();
		selectionIndicator.visible = false;
		clearMoveIndicators();
	}

	// 显示合法着法：普通移动为绿点，吃子为红点
	private void showValidMoves() {
		for (ChessEngine.Move m : selectedMoves) {
			// 同一目标格的4个升变着法只画一个标记
			boolean dup = false;
			for (Image ind : moveIndicators) {
				if (((MoveDot) ind).row == m.toRow && ((MoveDot) ind).col == m.toCol) {
					dup = true;
					break;
				}
			}
			if (dup) continue;

			boolean isCapture = engine.get(m.toRow, m.toCol) != ChessEngine.EMPTY
					|| m.flag == ChessEngine.FLAG_EN_PASSANT;
			addMoveIndicator(m.toRow, m.toCol, isCapture);
		}
	}

	private static class MoveDot extends Image {
		int row, col;
		MoveDot(int row, int col) {
			super(Icons.get(Icons.TARGET));
			this.row = row;
			this.col = col;
		}
	}

	private void addMoveIndicator(int row, int col, boolean isCapture) {
		MoveDot indicator = new MoveDot(row, col);
		indicator.camera = uiCamera;
		if (isCapture) {
			indicator.hardlight(0xFF5555);
			indicator.alpha(0.85f);
			indicator.scale.set(adjustedCellSize * 0.95f / indicator.width());
			indicator.setPos(cellX(col) + adjustedCellSize * 0.025f, cellY(row) + adjustedCellSize * 0.025f);
		} else {
			indicator.hardlight(0x55FF55);
			indicator.alpha(0.8f);
			indicator.scale.set(adjustedCellSize * 0.45f / indicator.width());
			indicator.setPos(cellX(col) + adjustedCellSize * 0.275f, cellY(row) + adjustedCellSize * 0.275f);
		}

		add(indicator);
		moveIndicators.add(indicator);
	}

	private void clearMoveIndicators() {
		for (Image indicator : moveIndicators) {
			if (indicator != null) indicator.killAndErase();
		}
		moveIndicators.clear();
	}

	// ---------------- 控制按钮 ----------------

	private void addControlButtons() {
		restartButton = new StyledButton(Chrome.Type.TOAST_TR, msg("restart")) {
			@Override
			protected void onClick() {
				restartGame();
			}
		};
		add(restartButton);

		aiButton = new StyledButton(Chrome.Type.TOAST_TR, aiText()) {
			@Override
			protected void onClick() {
				aiLevel = (aiLevel + 1) % 4;
				text(aiText());
				layoutControlButtons();
				refreshStatusText();
			}
		};
		add(aiButton);

		exitButton = new StyledButton(Chrome.Type.TOAST_TR, msg("exit")) {
			@Override
			protected void onClick() {
				onBackPressed();
			}
		};
		add(exitButton);

		layoutControlButtons();
	}

	// 底部按钮栏：宽度随文字自适应，整体水平居中，位于屏幕底栏内
	private void layoutControlButtons() {
		StyledButton[] btns = {restartButton, aiButton, exitButton};
		float gap = 6, btnH = 18;
		float[] widths = new float[btns.length];
		float total = gap * (btns.length - 1);
		for (int i = 0; i < btns.length; i++) {
			widths[i] = Math.max(48, btns[i].reqWidth());
			total += widths[i];
		}
		float x = (uiCamera.width - total) / 2f;
		float y = uiCamera.height - BOTTOM_BAR + (BOTTOM_BAR - btnH) / 2f;
		for (int i = 0; i < btns.length; i++) {
			btns[i].setSize(widths[i], btnH);
			btns[i].setPos(x, y);
			btns[i].camera = uiCamera;
			x += widths[i] + gap;
		}
	}

	private String aiText() {
		switch (aiLevel) {
			case 1:  return msg("ai_easy");
			case 2:  return msg("ai_normal");
			case 3:  return msg("ai_hard");
			default: return msg("ai_off");
		}
	}

	// ---------------- 对局管理 ----------------

	private void restartGame() {
		gameRunning = false;
		aiSession++;
		aiResult = null;
		aiThinking = false;

		lastFromRow = lastFromCol = lastToRow = lastToCol = -1;
		clearSelection();
		engine.reset();

		updateChessButtons();
		updateHighlights();
		refreshStatusText();

		addToFront(new WndSideSelection());
	}

	// ---------------- 窗口 ----------------

	// 兵升变选择窗口
	private class WndPromotion extends Window {

		private static final int WIDTH = 120;
		private static final int BTN_HEIGHT = 30;
		private static final float GAP = 2;

		private final List<ChessEngine.Move> candidates;

		public WndPromotion(List<ChessEngine.Move> candidates) {
			super();

			this.candidates = candidates;

			resize(WIDTH, (int) (BTN_HEIGHT * 4 + GAP * 3));

			int[] promotionPieces = engine.whiteTurn() ?
					new int[]{ChessEngine.W_QUEEN, ChessEngine.W_ROOK, ChessEngine.W_BISHOP, ChessEngine.W_KNIGHT} :
					new int[]{ChessEngine.B_QUEEN, ChessEngine.B_ROOK, ChessEngine.B_BISHOP, ChessEngine.B_KNIGHT};
			String[] pieceNames = new String[]{msg("queen"), msg("rook"), msg("bishop"), msg("knight")};

			for (int i = 0; i < promotionPieces.length; i++) {
				final int finalPiece = promotionPieces[i];
				StyledButton btn = new StyledButton(Chrome.Type.TOAST_TR, pieceNames[i]) {
					@Override
					protected void onClick() {
						promote(finalPiece);
						hide();
					}
				};
				btn.setSize(WIDTH, BTN_HEIGHT);
				btn.setPos(0, i * (BTN_HEIGHT + GAP));
				add(btn);
			}
		}

		private void promote(int piece) {
			for (ChessEngine.Move m : candidates) {
				if (m.promotion == piece) {
					engine.make(m);
					afterMove(m);
					return;
				}
			}
		}

		// 默认升变成后
		@Override
		public void onBackPressed() {
			promote(engine.whiteTurn() ? ChessEngine.W_QUEEN : ChessEngine.B_QUEEN);
			hide();
		}
	}

	// 结算窗口（将死/逼和/和棋）
	private class WndResult extends Window {
		private static final int WIDTH = 150;
		private static final int BTN_HEIGHT = 30;
		private static final float GAP = 4;

		public WndResult(int status) {
			super();

			String text;
			switch (status) {
				case ChessEngine.CHECKMATE:
					// 被将死的是当前走棋方，胜者为另一方
					text = engine.whiteTurn() ? msg("black_win") : msg("white_win");
					break;
				case ChessEngine.STALEMATE:
					text = msg("draw_stalemate");
					break;
				case ChessEngine.DRAW_FIFTY:
					text = msg("draw_fifty");
					break;
				default:
					text = msg("draw_material");
					break;
			}

			resize(WIDTH, (int) (BTN_HEIGHT * 3 + GAP * 2));

			StyledButton resultBtn = new StyledButton(Chrome.Type.TOAST_TR, text) {
				@Override
				protected void onClick() {
					onBackPressed();
				}
			};
			resultBtn.setSize(WIDTH, BTN_HEIGHT);
			resultBtn.setPos(0, 0);
			add(resultBtn);

			StyledButton againBtn = new StyledButton(Chrome.Type.TOAST_TR, msg("again")) {
				@Override
				protected void onClick() {
					onBackPressed();
				}
			};
			againBtn.setSize(WIDTH, BTN_HEIGHT);
			againBtn.setPos(0, BTN_HEIGHT + GAP);
			add(againBtn);

			StyledButton backBtn = new StyledButton(Chrome.Type.TOAST_TR, msg("back")) {
				@Override
				protected void onClick() {
					hide();
					ChessScene.this.onBackPressed();
				}
			};
			backBtn.setSize(WIDTH, BTN_HEIGHT);
			backBtn.setPos(0, 2 * (BTN_HEIGHT + GAP));
			add(backBtn);
		}

		@Override
		public void onBackPressed() {
			restartGame();
			hide();
		}
	}

	// 选边窗口
	private class WndSideSelection extends Window {
		private static final int WIDTH = 150;
		private static final int BTN_HEIGHT = 40;
		private static final float GAP = 2;

		public WndSideSelection() {
			super();

			resize(WIDTH, (int) (BTN_HEIGHT * 2 + GAP));

			StyledButton whiteBtn = new StyledButton(Chrome.Type.TOAST_TR, msg("white_side")) {
				@Override
				protected void onClick() {
					onBackPressed();
				}
			};
			whiteBtn.setSize(WIDTH, BTN_HEIGHT);
			whiteBtn.setPos(0, 0);
			add(whiteBtn);

			StyledButton blackBtn = new StyledButton(Chrome.Type.TOAST_TR, msg("black_side")) {
				@Override
				protected void onClick() {
					playerIsWhite = false;
					whiteAtBottom = false;
					repositionBoard();
					whiteStarts();
					hide();
				}
			};
			blackBtn.setSize(WIDTH, BTN_HEIGHT);
			blackBtn.setPos(0, BTN_HEIGHT + GAP);
			add(blackBtn);
		}

		// 默认选择白棋
		@Override
		public void onBackPressed() {
			playerIsWhite = true;
			whiteAtBottom = true;
			repositionBoard();
			whiteStarts();
			hide();
		}
	}

	private void whiteStarts() {
		gameRunning = true;
		refreshStatusText();
	}

	// 处理返回键
	@Override
	protected void onBackPressed() {
		aiSession++; // 使进行中的AI结果失效
		InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
		Game.switchScene(InterlevelScene.class);
	}
}
