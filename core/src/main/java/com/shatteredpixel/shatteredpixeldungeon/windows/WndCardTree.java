package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardSelector;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CommonCard;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.FinalCard;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.FirstCard;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.RareCard;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.WeaponToCard;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.canScrollRedButton;
import com.watabou.input.PointerEvent;
import com.watabou.input.ScrollEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ScrollArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

import java.util.ArrayList;

/**
 * 卡牌部署的新界面：以“卡池 → 阵营 → 成长阶段”的树状图展示卡池。
 * 只负责展示与交互，抽卡规则、冷却与卡牌效果全部复用 CardSelector。
 */
public class WndCardTree extends Window {

	private static final float TITLE_Y  = 2;
	private static final float STATUS_Y = 15;
	private static final float BAR_Y    = 23;
	private static final float TREE_Y   = 28;
	private static final float BOTTOM_H = 22;

	//窗口在世界坐标下的目标尺寸上限：手机上按屏幕铺满，PC/横屏上保持一个合适的面板，避免拉成整屏巨窗
	private static final int WIN_W_NARROW = 150;
	private static final int WIN_W_WIDE   = 320;
	private static final int WIN_H_LAND   = 190;
	private static final int WIN_H_PORT   = 320;
	private static final int WIN_MARGIN   = 4;

	private static final float HEADER_H = 12;

	private static final float LANE_H       = 50;
	private static final float LANE_GAP     = 5;
	private static final float LANE_LABEL_W = 46;
	private static final float CELL_W       = 64;
	private static final float CELL_GAP     = 4;
	private static final float CELLS_X      = 80;
	private static final float CHIP_W       = 29;
	private static final float CHIP_H       = 10;
	private static final int   CHIP_SLOTS   = 6;

	private static final float ROOT_X = 2;
	private static final float ROOT_Y = 1;
	private static final float ROOT_W = 26;
	private static final float ROOT_H = 10;

	private enum Faction {
		HS2000(0x7FC8FF, FirstCard.HS2000),
		VECTOR(0xFFA366, FirstCard.Vector),
		VHS(0xC9A0FF, FirstCard.VHS),
		WA2000(0xA6E07A, FirstCard.WA2000),
		LIU(0xFF8A8A, FirstCard.General_Liu),
		UNIVERSAL(0xCCCCCC, null);

		final int color;
		final FirstCard first;

		Faction(int color, FirstCard first) {
			this.color = color;
			this.first = first;
		}

		String label() {
			return Messages.get(Faction.class, name());
		}
	}

	private enum Tier {
		FIRST, COMMON, RARE, FINAL;

		String label() {
			return Messages.get(Tier.class, name());
		}
	}

	private static class Hit {
		final float x, y, w, h;
		final Runnable action;

		Hit(float x, float y, float w, float h, Runnable action) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.action = action;
		}

		boolean contains(float px, float py) {
			return px >= x && px < x + w && py >= y && py < y + h;
		}
	}

	private final CardSelector selector;

	private int winW, winH;

	private Camera treeCam;
	private Component treeLayer;
	private float contentW, contentH;

	private final ArrayList<Hit> hits = new ArrayList<>();
	private final ArrayList<ColorBlock> pulseBlocks = new ArrayList<>();

	private Component statusSlot;
	private ColorBlock barBg;
	private ColorBlock barFill;

	private RedButton drawBtn, checkBtn, addBtn, debugBtn;

	private boolean scrollInitialized = false;
	private boolean debugMode = false;
	private float pulseTime = 0f;

	public WndCardTree(CardSelector selector) {
		super();
		this.selector = selector;

		//端适配：
		//手机竖屏——窗口铺满可用宽度、尽量占满高度；
		//手机横屏/PC——限制窗口尺寸并居中，避免在宽屏上变成拉满整屏的大框。
		boolean wide = PixelScene.landscape();
		int availW = PixelScene.uiCamera.width - chrome.marginHor() - WIN_MARGIN * 2;
		int availH = PixelScene.uiCamera.height - chrome.marginVer() - WIN_MARGIN * 2;
		winW = Math.max(60, Math.min(availW, wide ? WIN_W_WIDE : WIN_W_NARROW));
		winH = Math.max(90, Math.min(availH, wide ? WIN_H_LAND : WIN_H_PORT));
		resize(winW, winH);

		buildChrome(winW, winH);
		buildCamera(winW, winH);

		refresh();
	}

	private void buildChrome(int w, int h) {
		//右上角：关闭、放大、缩小（PC 无捏合手势时也可缩放）
		float btnY = 1;
		float closeX = w - 4 - 13;
		IconButton close = new IconButton(Icons.get(Icons.CLOSE)) {
			@Override
			protected void onClick() {
				onBackPressed();
			}
		};
		close.setRect(closeX, btnY, 13, 13);
		add(close);

		RedButton zoomIn = new RedButton("+", 7) {
			@Override
			protected void onClick() {
				zoomBy(1.2f);
			}
		};
		zoomIn.setRect(closeX - 2 - 13, btnY, 13, 13);
		add(zoomIn);

		RedButton zoomOut = new RedButton("-", 7) {
			@Override
			protected void onClick() {
				zoomBy(1f / 1.2f);
			}
		};
		zoomOut.setRect(zoomIn.left() - 2 - 13, btnY, 13, 13);
		add(zoomOut);

		float titleW = Math.max(20, zoomOut.left() - 8);
		RenderedTextBlock title = winText(Messages.get(WndCardTree.class, "title"),
				8, TITLE_COLOR, titleW);
		title.setPos(4, TITLE_Y + (13 - title.height()) / 2f);

		statusSlot = new Component();
		add(statusSlot);

		barBg = new ColorBlock(w - 8, 3, 0x333333);
		barBg.setPos(4, BAR_Y);
		barBg.alpha(0.8f);
		add(barBg);

		barFill = new ColorBlock(0, 3, 0xFFD966);
		barFill.setPos(4, BAR_Y);
		add(barFill);

		float gap = 2;
		float bw = (w - 8 - gap * 3) / 4f;
		float by = h - BOTTOM_H + 4;

		drawBtn = new RedButton(Messages.get(WndCardTree.class, "draw"), 6) {
			@Override
			protected void onClick() {
				if (canDraw()) {
					Card.random(selector);
					refresh();
				}
			}
		};
		drawBtn.setRect(4, by, bw, 16);
		add(drawBtn);

		checkBtn = new RedButton(Messages.get(WndCardTree.class, "deck"), 6) {
			@Override
			protected void onClick() {
				showOwnedCards();
			}
		};
		checkBtn.setRect(4 + bw + gap, by, bw, 16);
		add(checkBtn);

		addBtn = new RedButton(Messages.get(WndCardTree.class, "insert"), 6) {
			@Override
			protected void onClick() {
				hide();
				GameScene.selectItem(WeaponToCard.weaponSelector);
			}
		};
		addBtn.setRect(4 + (bw + gap) * 2, by, bw, 16);
		add(addBtn);

		debugBtn = new RedButton(Messages.get(WndCardTree.class, "debug"), 6) {
			@Override
			protected void onClick() {
				debugMode = !debugMode;
				refresh();
			}
		};
		debugBtn.setRect(4 + (bw + gap) * 3, by, bw, 16);
		add(debugBtn);
	}

	private void buildCamera(int w, int h) {
		float treeW = w - 6;
		float treeH = h - TREE_Y - BOTTOM_H - 3;

		Point origin = camera().cameraToScreen(3, TREE_Y);

		treeCam = new Camera(origin.x, origin.y, (int) treeW, (int) treeH,
				PixelScene.cameraZoom);
		Camera.add(treeCam);

		treeLayer = new Component();
		treeLayer.camera = treeCam;
		add(treeLayer);

		TreeInput input = new TreeInput(3, TREE_Y, treeW, treeH);
		add(input);
		input.givePointerPriority();
	}

	@Override
	public void destroy() {
		if (treeCam != null) {
			Camera.remove(treeCam);
			treeCam = null;
		}
		super.destroy();
	}

	@Override
	public void update() {
		super.update();
		if (!pulseBlocks.isEmpty()) {
			pulseTime += 0.08f;
			float a = 0.35f + 0.65f * Math.abs((float) Math.sin(pulseTime));
			for (ColorBlock b : pulseBlocks) {
				if (b != null) b.alpha(a);
			}
		}
	}

	private void refresh() {
		refreshHud();
		rebuildTree();

		if (!scrollInitialized && treeCam != null) {
			scrollInitialized = true;
			focusCurrent();
		}
	}

	private void refreshHud() {
		Faction lock = lockedFaction();
		int interval = Math.max(1, selector.cardCD());

		String status;
		if (lock == null) {
			status = Messages.get(WndCardTree.class, "status",
					Math.max(0, selector.coolDownLeft), interval, selector.curCardNum);
		} else {
			status = Messages.get(WndCardTree.class, "status_faction",
					Math.max(0, selector.coolDownLeft), interval, selector.curCardNum,
					lock.label());
		}

		statusSlot.clear();
		RenderedTextBlock sb = PixelScene.renderTextBlock(status, 5);
		sb.maxWidth(winW - 8);
		sb.hardlight(0xCCCCCC);
		sb.setPos(4, STATUS_Y);
		statusSlot.add(sb);

		float ratio;
		if (!selector.curCards.isEmpty() || selector.coolDownLeft <= 0) {
			ratio = 1f;
		} else {
			ratio = 1f - Math.min(1f, selector.coolDownLeft / (float) interval);
		}
		barFill.size(Math.max(0, barBg.width() * ratio), 3);

		drawBtn.enable(canDraw());
		checkBtn.visible = !selector.FirstCards.isEmpty();
		debugBtn.visible = Dungeon.isChallenged(Challenges.TEST_MODE);
	}

	private void focusCurrent() {
		//初始比例按可用宽度自适应：窄屏放大到能看清一列，宽屏缩小到尽量一次看全四个阶段
		float fit = treeCam.screenWidth() / Math.max(1f, contentW + 8);
		float min = PixelScene.cameraZoom * (PixelScene.landscape() ? 0.7f : 0.8f);
		float max = PixelScene.cameraZoom * 1.15f;
		treeCam.zoom(Math.max(min, Math.min(max, fit)));

		Tier tier = currentTier();
		if (tier != null) {
			float cx = cellX(tier.ordinal()) + CELL_W / 2f;
			treeCam.scroll.x = cx - treeCam.width / 2f;
		}

		Faction lock = lockedFaction();
		if (lock != null) {
			float cy = laneY(lock.ordinal()) + LANE_H / 2f;
			treeCam.scroll.y = cy - treeCam.height / 2f;
		} else {
			treeCam.scroll.y = 0;
		}
		clampScroll();
	}

	private void zoomBy(float factor) {
		if (treeCam == null) return;
		float min = PixelScene.cameraZoom * 0.6f;
		float max = PixelScene.cameraZoom * 1.8f;
		float target = Math.max(min, Math.min(max, treeCam.zoom * factor));
		applyZoom(target,
				treeCam.x + treeCam.screenWidth() / 2f,
				treeCam.y + treeCam.screenHeight() / 2f);
	}

	private void applyZoom(float absZoom, float anchorScreenX, float anchorScreenY) {
		PointF anchor = treeCam.screenToCamera((int) anchorScreenX, (int) anchorScreenY);
		treeCam.zoom(absZoom);
		treeCam.scroll.x = anchor.x - (anchorScreenX - treeCam.x) / absZoom;
		treeCam.scroll.y = anchor.y - (anchorScreenY - treeCam.y) / absZoom;
		clampScroll();
	}

	// ---------------------------------------------------------------- tree

	private void rebuildTree() {
		treeLayer.clear();
		hits.clear();
		pulseBlocks.clear();

		int laneCount = Faction.values().length;
		contentH = laneY(laneCount - 1) + LANE_H + 6;
		contentW = CELLS_X + Tier.values().length * CELL_W
				+ (Tier.values().length - 1) * CELL_GAP + 6;

		// 阶段表头
		for (Tier t : Tier.values()) {
			boolean cur = isCurrentStage(t);
			treeText(t.label(), 5, cur ? 0xFFD966 : 0x808080, CELL_W - 6)
					.setPos(cellX(t.ordinal()) + 3, 1);
		}

		// 根节点
		block(ROOT_X, ROOT_Y, ROOT_W, ROOT_H, 0x2B2B2B, 1f);
		border(ROOT_X, ROOT_Y, ROOT_W, ROOT_H, 0xFFD966, 0.9f);
		treeText(Messages.get(WndCardTree.class, "root"), 4, 0xFFD966, ROOT_W - 4)
				.setPos(ROOT_X + 3, ROOT_Y + 2);

		float trunkX = ROOT_X + ROOT_W / 2f;
		float trunkTop = ROOT_Y + ROOT_H;
		float trunkBottom = laneY(laneCount - 1) + LANE_H / 2f;
		line(trunkX, trunkTop, trunkX, trunkBottom, 0x666666, 0.8f);

		for (Faction f : Faction.values()) {
			int idx = f.ordinal();
			float ly = laneY(idx);
			float cy = ly + LANE_H / 2f;

			boolean active = laneActive(f);
			int color = active ? f.color : 0x555555;
			float alpha = active ? 0.85f : 0.3f;

			line(trunkX, cy, CELLS_X - LANE_LABEL_W - 2, cy, color, alpha);

			RenderedTextBlock name = treeText(f.label(), 5, active ? f.color : 0x777777,
					LANE_LABEL_W - 2);
			name.setPos(CELLS_X - LANE_LABEL_W, cy - name.height());

			String own = ownedCount(f) + "/" + totalCount(f);
			treeText(own, 4, active ? 0xBBBBBB : 0x666666, LANE_LABEL_W - 2)
					.setPos(CELLS_X - LANE_LABEL_W, cy + 1);

			for (Tier t : Tier.values()) {
				float cx = cellX(t.ordinal());
				if (t.ordinal() > 0) {
					float prevRight = cellX(t.ordinal() - 1) + CELL_W;
					boolean grown = active && currentTier() != null
							&& t.ordinal() <= currentTier().ordinal();
					line(prevRight, cy, cx, cy, color,
							active ? (grown ? 0.7f : 0.2f) : 0.15f);
				}
				buildCell(f, t, cx, ly);
			}
		}
	}

	private void buildCell(Faction f, Tier t, float x, float y) {
		boolean active = laneActive(f);
		boolean current = isCurrentStage(t);

		block(x, y, CELL_W, LANE_H, active ? 0x1E1E1E : 0x171717, active ? 0.95f : 0.8f);
		ColorBlock[] cellBorder = border(x, y, CELL_W, LANE_H,
				current && active ? f.color : 0x3A3A3A,
				current && active ? 0.95f : active ? 0.75f : 0.35f);

		ArrayList<Card> all = allCards(f, t);
		ArrayList<Card> owned = ownedOf(f, t);
		ArrayList<Card> cands = candidatesOf(f, t);

		if (current && active && !cands.isEmpty()) {
			for (ColorBlock b : cellBorder) pulseBlocks.add(b);
		}

		int headerColor = current && active ? 0xFFD966 : active ? 0xD8D8D8 : 0x7A7A7A;
		treeText(t.label() + " " + Messages.get(WndCardTree.class, "selected",
				owned.size(), all.size()), 4,
				headerColor, CELL_W - 6).setPos(x + 3, y + 3);

		if (all.isEmpty()) {
			treeText(Messages.get(WndCardTree.class, "empty"), 5, 0x666666, CELL_W - 6)
					.setPos(x + 3, y + 16);
			return;
		}

		hits.add(new Hit(x, y, CELL_W, LANE_H, () -> browse(f, t)));

		ArrayList<Card> show = merge(owned, cands);
		boolean debugCell = debugMode && active;
		boolean more = show.size() > CHIP_SLOTS || debugCell;

		int slots = more ? CHIP_SLOTS - 1 : Math.min(show.size(), CHIP_SLOTS);
		for (int i = 0; i < slots; i++) {
			buildChip(show.get(i), chipX(x, i), chipY(y, i));
		}

		if (more) {
			float cx = chipX(x, slots);
			float cy = chipY(y, slots);
			String label = debugCell
					? Messages.get(WndCardTree.class, "pick")
					: Messages.get(WndCardTree.class, "more", show.size() - slots);
			block(cx, cy, CHIP_W, CHIP_H, 0x333333, 0.9f);
			border(cx, cy, CHIP_W, CHIP_H, 0x777777, 0.7f);
			treeText(label, 4, debugCell ? 0x7FC8FF : 0xBBBBBB, CHIP_W - 4)
					.setPos(cx + 2, cy + 2);
			hits.add(new Hit(cx, cy, CHIP_W, CHIP_H, () -> browse(f, t)));
		}
	}

	private void buildChip(Card c, float x, float y) {
		boolean owned = selector.hasCard(c);
		boolean cand = isCandidate(c);

		int fill = cand ? 0xFFD966 : owned ? c.color() : 0x2A2A2A;
		block(x, y, CHIP_W, CHIP_H, fill, cand ? 1f : owned ? 0.92f : 0.75f);

		int borderColor = cand ? 0xFFFFFF : owned ? 0xFFFFFF : 0x4A4A4A;
		ColorBlock[] bs = border(x, y, CHIP_W, CHIP_H, borderColor,
				cand ? 1f : owned ? 0.35f : 0.5f);
		if (cand) {
			for (ColorBlock b : bs) pulseBlocks.add(b);
		}

		int textColor = cand || (owned && luminance(fill) > 140) ? 0x101010 : 0xFFFFFF;
		treeText(shortName(c.cardName()), 4, textColor, CHIP_W - 4).setPos(x + 2, y + 2);

		hits.add(new Hit(x, y, CHIP_W, CHIP_H, () -> showCard(c)));
	}

	// -------------------------------------------------------------- actions

	private void showCard(Card c) {
		if (selector.hasCard(c)) {
			GirlsFrontlinePixelDungeon.scene().addToFront(
					new WndOptions(c.colorTitle(), c.info()));
			return;
		}
		if (isCandidate(c) || debugMode) {
			confirmChoose(c);
		} else {
			GirlsFrontlinePixelDungeon.scene().addToFront(
					new WndOptions(c.colorTitle(), c.info()));
		}
	}

	private void confirmChoose(Card c) {
		GirlsFrontlinePixelDungeon.scene().addToFront(
				new WndOptions(c.colorTitle(), c.info(),
						Messages.get(CardSelector.class, "entry"),
						Messages.get(CardSelector.class, "cancel")) {
					@Override
					protected void onSelect(int index) {
						if (index == 0) {
							selector.choose(c);
							refresh();
						}
					}
				});
	}

	private void browse(Faction f, Tier t) {
		ArrayList<canScrollRedButton> buttons = new ArrayList<>();
		for (Card c : allCards(f, t)) {
			String mark = selector.hasCard(c) ? "✓ " : isCandidate(c) ? "◆ " : "";
			buttons.add(new canScrollRedButton(mark + c.cardName()) {
				@Override
				public void onClick() {
					super.onClick();
					showCard(c);
				}
			});
		}
		if (buttons.isEmpty()) {
			return;
		}
		GirlsFrontlinePixelDungeon.scene().addToFront(
				new WndWithCanScrollButton(null,
						Messages.get(WndCardTree.class, "browse_title", f.label(), t.label()), null,
						buttons, 2, new ArrayList<>(), 1));
	}

	private void showOwnedCards() {
		ArrayList<canScrollRedButton> buttons = new ArrayList<>();
		addOwnedButtons(selector.FirstCards, buttons);
		addOwnedButtons(selector.CommonCards, buttons);
		addOwnedButtons(selector.RareCards, buttons);
		addOwnedButtons(selector.FinalCards, buttons);
		if (!buttons.isEmpty()) {
			GirlsFrontlinePixelDungeon.scene().addToFront(
					new WndWithCanScrollButton(null,
							Messages.get(WndCardTree.class, "owned_title"), null,
							buttons, 2, new ArrayList<>(), 1));
		}
	}

	private <T extends Card> void addOwnedButtons(ArrayList<T> cards,
			ArrayList<canScrollRedButton> buttons) {
		for (Card c : cards) {
			buttons.add(new canScrollRedButton(c.cardName()) {
				@Override
				public void onClick() {
					super.onClick();
					GirlsFrontlinePixelDungeon.scene().addToFront(
							new WndOptions(c.colorTitle(), c.info()));
				}
			});
		}
	}

	// --------------------------------------------------------------- states

	private boolean canDraw() {
		return selector.curCards.isEmpty() && selector.curCardNum < 8
				&& selector.coolDownLeft <= 0;
	}

	private boolean isCandidate(Card c) {
		return selector.curCards.contains(c);
	}

	private Faction lockedFaction() {
		if (selector.FirstCards.isEmpty()) return null;
		return factionOf(selector.FirstCards.get(0));
	}

	private boolean laneActive(Faction f) {
		Faction lock = lockedFaction();
		return lock == null || lock == f || f == Faction.UNIVERSAL;
	}

	private Tier currentTier() {
		if (selector.curCardNum >= 8) return null;
		if (selector.curCardNum == 0) return Tier.FIRST;
		if (selector.curCardNum < 5) return Tier.COMMON;
		if (selector.curCardNum < 7) return Tier.RARE;
		return Tier.FINAL;
	}

	private boolean isCurrentStage(Tier t) {
		return currentTier() == t;
	}

	private ArrayList<Card> ownedOf(Faction f, Tier t) {
		ArrayList<Card> list = new ArrayList<>();
		for (Card c : allCards(f, t)) {
			if (selector.hasCard(c)) list.add(c);
		}
		return list;
	}

	private ArrayList<Card> candidatesOf(Faction f, Tier t) {
		ArrayList<Card> list = new ArrayList<>();
		for (Card c : selector.curCards) {
			if (factionOf(c) == f && tierOf(c) == t) list.add(c);
		}
		return list;
	}

	private ArrayList<Card> merge(ArrayList<Card> owned, ArrayList<Card> cands) {
		ArrayList<Card> out = new ArrayList<>(owned);
		for (Card c : cands) {
			if (!out.contains(c)) out.add(c);
		}
		return out;
	}

	private int ownedCount(Faction f) {
		int n = 0;
		for (Tier t : Tier.values()) n += ownedOf(f, t).size();
		return n;
	}

	private int totalCount(Faction f) {
		int n = 0;
		for (Tier t : Tier.values()) n += allCards(f, t).size();
		return n;
	}

	private static Faction factionOf(Card c) {
		if (c instanceof FirstCard) {
			for (Faction f : Faction.values()) {
				if (f.first == c) return f;
			}
			return Faction.UNIVERSAL;
		}
		Class<?> d = ((Enum<?>) c).getDeclaringClass();
		if (d == CommonCard.HS2000.class || d == RareCard.HS2000.class
				|| d == FinalCard.HS2000.class)
			return Faction.HS2000;
		if (d == CommonCard.Vector.class || d == RareCard.Vector.class
				|| d == FinalCard.Vector.class)
			return Faction.VECTOR;
		if (d == CommonCard.VHS.class || d == RareCard.VHS.class
				|| d == FinalCard.VHS.class)
			return Faction.VHS;
		if (d == CommonCard.WA2000.class || d == RareCard.WA2000.class
				|| d == FinalCard.WA2000.class)
			return Faction.WA2000;
		if (d == CommonCard.General_Liu.class || d == RareCard.General_Liu.class
				|| d == FinalCard.General_Liu.class)
			return Faction.LIU;
		return Faction.UNIVERSAL;
	}

	private static Tier tierOf(Card c) {
		if (c instanceof FirstCard) return Tier.FIRST;
		if (c instanceof CommonCard) return Tier.COMMON;
		if (c instanceof RareCard) return Tier.RARE;
		return Tier.FINAL;
	}

	private ArrayList<Card> allCards(Faction f, Tier t) {
		ArrayList<Card> list = new ArrayList<>();
		switch (t) {
			case FIRST:
				if (f.first != null) list.add(f.first);
				break;
			case COMMON:
				for (CommonCard c : commonOf(f)) list.add(c);
				break;
			case RARE:
				for (RareCard c : rareOf(f)) list.add(c);
				break;
			case FINAL:
				for (FinalCard c : finalOf(f)) list.add(c);
				break;
		}
		return list;
	}

	private static CommonCard[] commonOf(Faction f) {
		if (f == Faction.UNIVERSAL) return CommonCard.UNIVERSAL.values();
		CommonCard[] arr = CommonCard.cardMap().get(f.first);
		return arr == null ? new CommonCard[0] : arr;
	}

	private static RareCard[] rareOf(Faction f) {
		if (f == Faction.UNIVERSAL) return RareCard.UNIVERSAL.values();
		RareCard[] arr = RareCard.cardMap().get(f.first);
		return arr == null ? new RareCard[0] : arr;
	}

	private static FinalCard[] finalOf(Faction f) {
		if (f == Faction.UNIVERSAL) return FinalCard.UNIVERSAL.values();
		FinalCard[] arr = FinalCard.cardMap().get(f.first);
		return arr == null ? new FinalCard[0] : arr;
	}

	// --------------------------------------------------------------- layout

	private float laneY(int index) {
		return HEADER_H + index * (LANE_H + LANE_GAP);
	}

	private float cellX(int tierIndex) {
		return CELLS_X + tierIndex * (CELL_W + CELL_GAP);
	}

	private static float chipX(float cellX, int index) {
		return cellX + 3 + (index % 2) * (CHIP_W + 2);
	}

	private static float chipY(float cellY, int index) {
		return cellY + 13 + (index / 2) * (CHIP_H + 2);
	}

	private static String shortName(String name) {
		if (name == null) return "";
		if (name.length() <= 5) return name;
		return name.substring(0, 4) + "..";
	}

	private static int luminance(int color) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		return (r * 299 + g * 587 + b * 114) / 1000;
	}

	private RenderedTextBlock winText(String s, float size, int color, float maxWidth) {
		RenderedTextBlock t = PixelScene.renderTextBlock(s, size);
		t.maxWidth((int) Math.max(8, maxWidth));
		t.hardlight(color);
		add(t);
		return t;
	}

	private RenderedTextBlock treeText(String s, float size, int color, float maxWidth) {
		RenderedTextBlock t = PixelScene.renderTextBlock(s, size);
		t.maxWidth((int) Math.max(8, maxWidth));
		t.hardlight(color);
		treeLayer.add(t);
		return t;
	}

	private ColorBlock block(float x, float y, float w, float h, int color, float alpha) {
		ColorBlock b = new ColorBlock(w, h, color);
		b.setPos(x, y);
		b.alpha(alpha);
		treeLayer.add(b);
		return b;
	}

	private ColorBlock[] border(float x, float y, float w, float h, int color, float alpha) {
		ColorBlock top = block(x, y, w, 1, color, alpha);
		ColorBlock bottom = block(x, y + h - 1, w, 1, color, alpha);
		ColorBlock left = block(x, y, 1, h, color, alpha);
		ColorBlock right = block(x + w - 1, y, 1, h, color, alpha);
		return new ColorBlock[]{top, bottom, left, right};
	}

	private void line(float x1, float y1, float x2, float y2, int color, float alpha) {
		if (Math.abs(y2 - y1) < 0.5f) {
			block(Math.min(x1, x2), y1, Math.abs(x2 - x1), 1, color, alpha);
		} else {
			block(x1, Math.min(y1, y2), 1, Math.abs(y2 - y1), color, alpha);
		}
	}

	private void clampScroll() {
		if (treeCam == null) return;
		float maxX = Math.max(0, contentW - treeCam.width);
		float maxY = Math.max(0, contentH - treeCam.height);
		treeCam.scroll.x = Math.max(0, Math.min(maxX, treeCam.scroll.x));
		treeCam.scroll.y = Math.max(0, Math.min(maxY, treeCam.scroll.y));
	}

	// --------------------------------------------------------------- input

	private class TreeInput extends ScrollArea {

		private final PointF last = new PointF();
		private boolean dragging = false;
		private boolean pinching = false;
		private PointerEvent second = null;
		private float startSpan = 1f;
		private float startZoom = 1f;

		TreeInput(float x, float y, float w, float h) {
			super(x, y, w, h);
			blockLevel = ALWAYS_BLOCK;
		}

		@Override
		protected void onScroll(ScrollEvent event) {
			//PC 端鼠标滚轮缩放（手机端无滚轮，走双指捏合）
			zoomBy(event.amount > 0 ? 1.12f : 1f / 1.12f);
		}

		@Override
		protected void onPointerDown(PointerEvent event) {
			if (event == curEvent) {
				dragging = false;
				pinching = false;
				last.set(event.current);
			} else if (second == null) {
				second = event;
				pinching = true;
				dragging = false;
				startSpan = Math.max(1f, PointF.distance(curEvent.current, second.current));
				startZoom = treeCam.zoom;
			}
		}

		@Override
		protected void onDrag(PointerEvent event) {
			if (pinching && second != null) {
				float span = Math.max(1f, PointF.distance(curEvent.current, second.current));
				float min = PixelScene.cameraZoom * 0.6f;
				float max = PixelScene.cameraZoom * 1.8f;
				float target = startZoom * (span / startSpan);
				target = Math.max(min, Math.min(max, target));
				float midX = (curEvent.current.x + second.current.x) / 2f;
				float midY = (curEvent.current.y + second.current.y) / 2f;
				applyZoom(target, midX, midY);
				last.set(curEvent.current);
				dragging = true;
				return;
			}

			if (event == null) return;

			float dx = event.current.x - last.x;
			float dy = event.current.y - last.y;
			if (Math.abs(dx) + Math.abs(dy) > 1f) {
				dragging = true;
			}
			treeCam.scroll.x -= dx / treeCam.zoom;
			treeCam.scroll.y -= dy / treeCam.zoom;
			clampScroll();
			last.set(event.current);
		}

		@Override
		protected void onPointerUp(PointerEvent event) {
			if (event == second) {
				second = null;
				pinching = false;
				dragging = true;
				if (curEvent != null) last.set(curEvent.current);
			}
		}

		@Override
		protected void onClick(PointerEvent event) {
			if (dragging) {
				dragging = false;
				return;
			}
			PointF p = treeCam.screenToCamera((int) event.start.x, (int) event.start.y);
			for (int i = hits.size() - 1; i >= 0; i--) {
				Hit hit = hits.get(i);
				if (hit.contains(p.x, p.y)) {
					hit.action.run();
					break;
				}
			}
		}
	}
}
