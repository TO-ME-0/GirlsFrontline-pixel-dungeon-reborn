/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.update.GDChangesButton;
import com.shatteredpixel.shatteredpixeldungeon.update.UpdateChecker;
import com.shatteredpixel.shatteredpixeldungeon.utils.Color;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndComputer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndError;
import com.badlogic.gdx.Net;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PlatformSupport;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;

import java.io.File;
import java.util.ArrayList;

/**
 * 新版关于界面（v2）。
 * 与旧版 {@link AboutScene} 并存：本界面为数据驱动的自动排版，
 * 人员变动时只需修改 SECTIONS 数组即可增删成员，无需手动调坐标。
 * 旧版界面保留，可通过本界面底部的按钮进入。
 */
public class AboutSceneV2 extends PixelScene {

	//每个成员卡片的宽度与水平间距
	private static final float CARD_W = 56;
	private static final float CARD_GAP = 6;

	//成员数据格式：{ 职务文字, 高亮颜色, 图标, 名字 }
	//图标传 null 则不引用贴图，仅预留头像占位
	//人员变动时只需增删这里的条目，排版会自动换行
	private static final Object[][] TEAM_MAIN = {
			{"程 序 编 码", -1, Icons.ONWARD, "to me", true},
			{"贴 图 美 术", 0xe6739b, Icons.CHOCOSUKI, "choco", false},
			{"文 案 编 写", 0x4a76c2, Icons.LANGLING, "言凌", false},
	};
	//协力人员名单暂未确定，先保留占位条目，后续编辑此处即可
	private static final Object[][] TEAM_HELP = {
			{"协力", 0xB9F0FD, null, "提供文本和建议的群友、以及提供代码和美术支持的 其他地牢开发者", false},
			{" ", 0xB9F0FD, null, "待编辑", false},
	};

	//项目开源仓库地址（点击可跳转）
	private static final String REPO_CURRENT =
			"https://github.com/TO-ME-0/GirlsFrontline-pixel-dungeon-reborn";
	private static final String[] REPO_LEGACY = {
			"https://github.com/Cat-Zs/GirlsFrontline-pixel-dungeon-reborn",
			"https://github.com/tamamoqian/GirlsFrontline-pixel-dungeon-pla56/tree/main",
	};

	//官方 Q 群加群链接（与 GDChangesButton"加入"按钮使用同一短链）
	private static final String QGROUP_URL = "https://qm.qq.com/q/9DnEp6FpNS";

	//辉梦服务器的其他地牢的在线更新配置 JSON（点击时联网解析出 DownloadLink1 后跳转浏览器）
	private static final String MLPD_CONFIG_URL =
			"https://gameupdate.insrv.mlpd.spldream.com/MLPD/GameUpdate.json";
	private static final String RAPD_CONFIG_URL =
			"https://gameupdate.insrv.mlpd.spldream.com/RAPD/RDGameUpdate.json";

	//卡牌地牢王的友情链接（仅官网地址，点击直接用浏览器打开）
	private static final String CARDKING_URL = "https://dms.spldream.com/";

	//仓库地址超链接的暂存列表：文本行在内容构建期创建，
	//热区须等滚动面板创建后再注册（指针事件按创建逆序分发，后注册者先响应）
	private final ArrayList<RenderedTextBlock> repoLinkBlocks = new ArrayList<>();
	private final ArrayList<String> repoLinkUrls = new ArrayList<>();

	private Image logo;

	@Override
	public void create() {
		super.create();

		int w = Camera.main.width;
		int h = Camera.main.height;

		Archs archs = new Archs();
		archs.setSize(w, h);
		add(archs);

		//darkens the arches
		add(new ColorBlock(w, h, 0x88000000));

		Component content = new Component();

		float y = 10;

		//*** 头部：徽标 + 标题 + 版本号 ***
		logo = Icons.get(Icons.GIRLPD);
		logo.x = (w - logo.width()) / 2f;
		logo.y = y;
		PixelScene.align(logo);
		content.add(logo);

		Flare logoFlare = new Flare(7, 24).color(Window.TITLE_COLOR, true).show(logo, 0);
		logoFlare.angularSpeed = 20;

		y = logo.y + logo.height() + 4;

		RenderedTextBlock title = PixelScene.renderTextBlock("少前地牢", 9);
		title.hardlight(Window.TITLE_COLOR);
		title.maxWidth(w - 20);
		title.setPos((w - title.width()) / 2f, y);
		content.add(title);
		y += title.height() + 1;

		RenderedTextBlock version = PixelScene.renderTextBlock(
				"v" + Game.version + "  (" + Game.versionCode + ")", 6);
		version.hardlight(0x888888);
		version.maxWidth(w - 20);
		version.setPos((w - version.width()) / 2f, y);
		content.add(version);
		y += version.height() + 6;

		//*** 项目简介 ***
		y = addTextBlock(content, y,
				"少前地牢是一款_完全开源且完全免费_的\n少女前线及破碎地牢同人游戏。\n" +
						"如果您从任何渠道_花费资金购买_了这款游戏，\n您已经遭遇了盗版诈骗，请立即退款维权！\n"+
						"\n您任何合理的建议都将会被采纳参考，欢迎您的加入与游玩！\n",
				Window.TITLE_COLOR);

		//*** 官方 Q 群（点击跳转加群页面）***
		y = addRepoLine(content, y, "点击加Q群：897141018", QGROUP_URL) + 8;

		//*** 成员分组（数据驱动，自动换行）***
		y = addSection(content, y, "—— 近期版本制作 ——", TEAM_MAIN);
		y = addSection(content, y, "—— 协力人员 ——", TEAM_HELP);

		//*** 项目仓库入口（点击弹出仓库窗口，避免在滚动列表中误触链接）***
		//按钮本体须在滚动面板创建后构造（见下方 repoBtn 处），此处仅记录位置
		float repoBtnY = y + 4;
		y = repoBtnY + 18 + 4;

		//*** 下载支持说明（源码仓库按钮正下方，品红色）***
		RenderedTextBlock dlNote = PixelScene.renderTextBlock(
				Messages.get(AboutSceneV2.class, "download_support"), 6);
		dlNote.align(RenderedTextBlock.CENTER_ALIGN);
		dlNote.hardlight(0xFF00FF);
		dlNote.maxWidth(w - 20);
		dlNote.setPos((w - dlNote.width()) / 2f, y);
		content.add(dlNote);
		y = dlNote.bottom() + 8;

		//*** 友情链接入口（下载支持说明正下方，点击弹出二级窗口）***
		//按钮本体须在滚动面板创建后构造（见下方 friendBtn 处），此处仅记录位置
		float friendBtnY = y;
		y = friendBtnY + 18 + 4;

		//*** 音乐版权 ***
		y = addTextBlock(content, y,
				"_少女前线的像素地牢使用了以下歌曲作为游戏音乐_:\n\n" +

						"唱片集：Girls Frontline Original Soundtrack Vol.1 \n\n" +
						"主界面音乐\n" +
						"_-_ Horizon\n" +
						"_-_ Make Sense\n\n" +

						"第一大区音乐\n" +
						"_-_ Safety First-a\n" +
						"_-_ Safety First-b\n\n" +
						"第一大区Boss音乐\n" +
						"_-_ Made in Heaven\n\n" +

						"第三大区音乐\n" +
						"_-_ machines are talking\n\n" +
						"第四大区Boss音乐\n" +
						"_-_ Cury\n\n" +
						"第五大区Boss音乐\n" +
						"_-_ What i am fight for\n\n" +

						"唱片集：少女前线诡疫狂潮BGM \n\n" +
						"第二大区音乐\n" +
						"_-_ m-Halloween19-host\n\n" +
						"第二大区Boss音乐\n" +
						"_-_ m-Halloween19-made in heaven\n\n" +

						"唱片集：Girls Frontline Original Soundtrack Vol.2 \n\n" +
						"第三大区Boss音乐\n" +
						"_-_ cradle of fear\n\n" +
						"第六大区Boss音乐\n" +
						"_-_ mind hack\n\n" +
						"通关音乐\n" +
						"_-_ Vacance 6.64\n\n" +
						"唱片集：少女前线碧海秘闻BGM\n\n" +
						"第四大区音乐\n" +
						"_-_ Event summer combat\n\n" +
						"唱片集：Slow Shock (游戏《少女前线》活动「慢休克」原声音乐)\n\n" +
						"第五大区音乐\n" +
						"_-_ Tactical Operation\n\n" +
						"唱片集：未知\n\n" +
						"第六大区音乐\n" +
						"_-_ See you Again",
				Window.TITLE_COLOR);

		//滚动面板必须先于其内容中的按钮创建：
		//指针事件按创建逆序分发（后注册者先收到），
		//PointerController 覆盖整个面板且会拦截 DOWN 事件，
		//若按钮先创建，点击会先被控制器吞掉，按钮将无法响应
		ScrollPane list = new ScrollPane(content) {
			@Override
			public void onClick(float x, float y) {
				//彩蛋：连点徽标解锁（与旧版致谢界面逻辑一致）
				if (x >= logo.x
						&& x <= logo.x + logo.width()
						&& y >= logo.y
						&& y <= logo.y + logo.height()) {
					switch (++Game.unlockClickTime) {
						case 100:
							Badges.validateHappyEnd();
						case 50:
							Badges.unlockForPlay();
						case 10:
							Game.isDebug = true;
					}
				}
			}
		};
		add(list);

		//为仓库地址注册超链接热区（须在滚动面板之后注册，否则会被其拦截）
		for (int i = 0; i < repoLinkBlocks.size(); i++) {
			RenderedTextBlock linkBlock = repoLinkBlocks.get(i);
			String linkUrl = repoLinkUrls.get(i);
			PointerArea link = new PointerArea(0, 0, 0, 0) {
				@Override
				protected void onClick(PointerEvent event) {
					Game.platform.openURI(linkUrl);
				}
			};
			//RenderedTextBlock 不是 Visual，无法直接作为热区 target，改用其矩形
			link.x = linkBlock.x;
			link.y = linkBlock.y;
			link.width = linkBlock.width();
			link.height = linkBlock.height();
			content.add(link);
		}

		//滚动内容中的按钮须在滚动面板之后创建，否则点击会被其拦截
		//项目仓库入口：弹出模态仓库窗口
		StyledButton repoBtn = new StyledButton(Chrome.Type.GREY_BUTTON, "源码仓库") {
			@Override
			protected void onClick() {
				GirlsFrontlinePixelDungeon.scene().addToFront(new WndRepo());
			}
		};
		repoBtn.setRect((w - 80) / 2f, repoBtnY, 80, 18);
		content.add(repoBtn);

		//友情链接入口：弹出友情链接窗口（辉梦服务器的其他像素地牢下载）
		StyledButton friendBtn = new StyledButton(Chrome.Type.GREY_BUTTON, "友情链接") {
			@Override
			protected void onClick() {
				GirlsFrontlinePixelDungeon.scene().addToFront(new WndFriendLinks());
			}
		};
		friendBtn.setRect((w - 80) / 2f, friendBtnY, 80, 18);
		content.add(friendBtn);

		//*** 旧版入口 ***
		StyledButton oldBtn = new StyledButton(Chrome.Type.GREY_BUTTON, "查看旧版致谢界面") {
			@Override
			protected void onClick() {
				GirlsFrontlinePixelDungeon.switchNoFade(AboutScene.class);
			}
		};
		oldBtn.setRect((w - 80) / 2f, y + 4, 80, 18);
		content.add(oldBtn);
		y = oldBtn.bottom() + 4;

		//*** 开源声明 ***
		y = addTextBlock(content, y,
				"本程序为自由软件，基于_GPL-3.0_协议发布。\n" +
						"原始代码版权 © 2012-2015 Oleg Dolya\n" +
						"© 2014-2024 Evan Debenham",
				0x888888);

		content.setSize(w, y + 8);

		//内容尺寸确定后再设置面板矩形，保证滚动条可见性计算正确
		list.setRect(0, 0, w, h);
		list.scrollTo(0, 0);

		ExitButton btnExit = new ExitButton() {
			@Override
			protected void onClick() {
				exitScene();
			}
		};
		btnExit.setPos(Camera.main.width - btnExit.width(), 0);
		add(btnExit);

		fadeIn();
	}

	// 退出关于界面：从0层电脑进入则读档返回0层，否则返回标题页
	private void exitScene() {
		if (WndComputer.launchedFromGame) {
			WndComputer.backFromComputerApp();
		} else {
			Game.switchScene(TitleScene.class);
		}
	}

	@Override
	protected void onBackPressed() {
		exitScene();
	}

	/** 添加一个居中的富文本块（支持 _文字_ 高亮语法），返回新的 y 坐标 */
	private float addTextBlock(Group content, float y, String text, int color) {
		int w = Camera.main.width;

		RenderedTextBlock block = PixelScene.renderTextBlock(text, 6);
		block.align(RenderedTextBlock.CENTER_ALIGN);
		block.setHightlighting(true, color);
		block.maxWidth(w - 20);
		block.setPos((w - block.width()) / 2f, y);
		content.add(block);
		return block.bottom() + 8;
	}

	/**
	 * 添加一行居中的仓库文本，返回该行底部 y 坐标。
	 * url 非空时整行高亮并暂存为待注册的超链接热区（热区统一在滚动面板创建后挂载）。
	 */
	private float addRepoLine(Group content, float y, String text, String url) {
		int w = Camera.main.width;

		RenderedTextBlock block = PixelScene.renderTextBlock(text, 6);
		block.align(RenderedTextBlock.CENTER_ALIGN);
		block.setHightlighting(true, Window.TITLE_COLOR);
		block.maxWidth(w - 20);
		if (url != null) {
			block.hardlight(Window.TITLE_COLOR);
			repoLinkBlocks.add(block);
			repoLinkUrls.add(url);
		}
		block.setPos((w - block.width()) / 2f, y);
		content.add(block);
		return block.bottom();
	}

	/** 添加一个成员分组：分组标题 + 自动换行的成员卡片，返回新的 y 坐标 */
	private float addSection(Group content, float y, String title, Object[][] members) {
		int w = Camera.main.width;

		int color = (Integer) members[0][1];
		if (color == -1)
			color = Color.random();

		RenderedTextBlock header = PixelScene.renderTextBlock(title, 8);
		header.hardlight(color);
		header.maxWidth(w - 20);
		header.setPos((w - header.width()) / 2f, y);
		content.add(header);
		y += header.height() + 6;

		//两侧仅预留 8px，保证最窄屏（135px）下两张卡片也能并排
		int perRow = (int) ((w - 8) / (CARD_W + CARD_GAP));
		if (perRow < 1) perRow = 1;

		int i = 0;
		while (i < members.length) {
			int inRow = Math.min(perRow, members.length - i);
			float startX = (w - inRow * CARD_W - (inRow - 1) * CARD_GAP) / 2f;
			float rowBottom = y;
			for (int c = 0; c < inRow; c++, i++) {
				Object[] m = members[i];
				MemberCard card = new MemberCard(
						(String) m[0], (Integer) m[1], (Icons) m[2], (String) m[3], (boolean) m[4]);
				card.setSize(CARD_W, 0);
				card.setPos(startX + c * (CARD_W + CARD_GAP), y);
				content.add(card);
				rowBottom = Math.max(rowBottom, card.bottom());
			}
			y = rowBottom + 8;
		}
		return y;
	}

	//成员卡片：职务（彩色）+ 头像（带光晕）+ 名字
	private static class MemberCard extends Component {

		private final RenderedTextBlock role;
		private final Image avatar;
		private final RenderedTextBlock name;
		private final Flare flare;

		MemberCard(String roleText, int color, Icons icon, String nameText, boolean flare_change) {
			super();

			role = PixelScene.renderTextBlock(roleText, 6);
			if (color == -1)
				color = Color.random();
			role.hardlight(color);
			add(role);

			avatar = icon == null ? null : Icons.get(icon);
			if (avatar != null) {
				add(avatar);

				//show() 会自动把光晕挂到 avatar 的 parent 上，需在 add(avatar) 之后调用
				flare = new Flare(7, 24).color(color, true).show(avatar, 0).change(flare_change);
				flare.angularSpeed = 20;
			} else {
				flare = null;
			}

			name = PixelScene.renderTextBlock(nameText, 7);
			add(name);
		}

		@Override
		protected void layout() {
			super.layout();

			float topY = top();

			role.maxWidth((int) width());
			role.setPos(x + (width() - role.width()) / 2f, topY);
			topY += role.height() + 2;

			if (avatar != null) {
				avatar.x = x + (width() - avatar.width()) / 2f;
				avatar.y = topY;
				PixelScene.align(avatar);
				if (flare != null) {
					flare.point(avatar.center());
				}
				topY = avatar.y + avatar.height() + 2;
			} else {
				//无头像时预留 16px 占位，与其他卡片保持一致高度
				topY += 16;
			}

			name.maxWidth((int) width());
			name.setPos(x + (width() - name.width()) / 2f, topY);
			topY += name.height() + 2;

			height = Math.max(height, topY - top());
		}
	}

	//项目仓库窗口：模态展示各仓库入口，点击跳转系统浏览器
	//（窗口内点击不会影响背后的滚动列表，避免滚动时误触链接）
	public static class WndRepo extends Window {

		private static final int WIDTH_P = 120;
		private static final int WIDTH_L = 144;
		private static final int MARGIN = 2;
		private static final int BUTTON_HEIGHT = 18;

		WndRepo() {
			super();

			int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

			IconTitle title = new IconTitle();
			title.icon(Icons.get(Icons.CHANGESLOG));
			title.label("项目开源仓库地址");
			title.color(Window.TITLE_COLOR);
			title.setRect(0, 0, width, 0);
			add(title);

			float pos = title.bottom() + MARGIN;

			pos = addLabel(pos, width, "目前使用：");
			pos = addLinkButton(pos, width, "TO-ME-0", REPO_CURRENT);
			pos = addLabel(pos, width, "曾经的仓库：");
			pos = addLinkButton(pos, width, "Cat-Zs", REPO_LEGACY[0]);
			pos = addLinkButton(pos, width, "tamamoqian", REPO_LEGACY[1]);

			resize(width, (int) pos + MARGIN);
		}

		private float addLabel(float pos, int width, String text) {
			RenderedTextBlock label = PixelScene.renderTextBlock(text, 6);
			label.hardlight(0x888888);
			label.maxWidth(width - MARGIN * 2);
			label.setPos(MARGIN, pos);
			add(label);
			return pos + label.height() + MARGIN;
		}

		private float addLinkButton(float pos, int width, String label, String url) {
			StyledButton btn = new StyledButton(Chrome.Type.GREY_BUTTON, label) {
				@Override
				protected void onClick() {
					Game.platform.openURI(url);
				}
			};
			btn.setRect(MARGIN, pos, width - MARGIN * 2, BUTTON_HEIGHT);
			add(btn);
			return btn.bottom() + MARGIN;
		}
	}

	//友情链接窗口：展示辉梦服务器的其他像素地牢的下载入口
	//点击按钮时联网拉取对应地牢的更新配置 JSON，解析出版本信息和 DownloadLink1/3，
	//然后弹出下载窗口（WndFriendUpdate）执行带进度条的下载与安装
	public static class WndFriendLinks extends Window {

		private static final int WIDTH_P = 120;
		private static final int WIDTH_L = 144;
		private static final int MARGIN = 2;
		private static final int BUTTON_HEIGHT = 18;

		WndFriendLinks() {
			super();

			int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

			IconTitle title = new IconTitle();
			title.icon(Icons.get(Icons.INFO));
			title.label("友情链接");
			title.color(Window.TITLE_COLOR);
			title.setRect(0, 0, width, 0);
			add(title);

			float pos = title.bottom() + MARGIN;

			pos = addLabel(pos, width, "辉梦服务器的其他像素地牢：");
			pos = addFetchButton(pos, width, "魔绫地牢", MLPD_CONFIG_URL);
			pos = addFetchButton(pos, width, "萝卜地牢", RAPD_CONFIG_URL);
			pos = addUriButton(pos, width, "卡牌地牢王", CARDKING_URL);

			resize(width, (int) pos + MARGIN);
		}

		private float addLabel(float pos, int width, String text) {
			RenderedTextBlock label = PixelScene.renderTextBlock(text, 6);
			label.hardlight(0x888888);
			label.maxWidth(width - MARGIN * 2);
			label.setPos(MARGIN, pos);
			add(label);
			return pos + label.height() + MARGIN;
		}

		//点击时联网解析 JSON 中的版本信息和下载链接，然后打开下载窗口；失败则回退到 JSON 地址
		//注意：Gdx.net 的 HTTP 回调在后台线程执行，UI 操作（改文字、创建窗口）必须切回渲染线程
		private float addFetchButton(float pos, int width, String label, String configUrl) {
			StyledButton btn = new StyledButton(Chrome.Type.GREY_BUTTON, label) {
				@Override
				protected void onClick() {
					final String originalLabel = text();
					text("加载中...");
					UpdateChecker.getHttpStringFromUrl(configUrl, new Net.HttpResponseListener() {
						@Override
						public void handleHttpResponse(Net.HttpResponse httpResponse) {
							//先在后台线程完成 JSON 解析
							final String versionName;
							final String url1;
							final String url3;
							final String changeLog;
							try {
								JsonNode config = new ObjectMapper().readTree(httpResponse.getResultAsString());
								versionName = config.has("MLPDGameVersion") ?
										config.get("MLPDGameVersion").asText() :
										config.get("RAPDGameVersion").asText();
								url1 = config.get("DownloadLink1").asText();
								url3 = config.has("DownloadLink3") ?
										config.get("DownloadLink3").asText() : url1;
								changeLog = config.has("changeLog") ?
										config.get("changeLog").asText() : "";
							} catch (Exception e) {
								//解析失败，切回渲染线程后回退到打开 JSON 地址
								Game.runOnRenderThread(() -> {
									try { text(originalLabel); } catch (Exception ignored) {}
									Game.platform.openURI(configUrl);
								});
								return;
							}
							//解析成功，切回渲染线程执行 UI 操作
							Game.runOnRenderThread(() -> {
								try { text(originalLabel); } catch (Exception ignored) {}
								GirlsFrontlinePixelDungeon.scene().addToFront(
										new WndFriendUpdate(label, versionName, url1, url3, changeLog));
							});
						}
						@Override
						public void failed(Throwable t) {
							Game.runOnRenderThread(() -> {
								try { text(originalLabel); } catch (Exception ignored) {}
								Game.platform.openURI(configUrl);
							});
						}
						@Override
						public void cancelled() {
							Game.runOnRenderThread(() -> {
								try { text(originalLabel); } catch (Exception ignored) {}
							});
						}
					});
				}
			};
			btn.setRect(MARGIN, pos, width - MARGIN * 2, BUTTON_HEIGHT);
			add(btn);
			return btn.bottom() + MARGIN;
		}

		//直接打开外部网址的按钮：无更新配置 JSON，点击即用浏览器访问官网
		private float addUriButton(float pos, int width, String label, final String url) {
			StyledButton btn = new StyledButton(Chrome.Type.GREY_BUTTON, label) {
				@Override
				protected void onClick() {
					Game.platform.openURI(url);
				}
			};
			btn.setRect(MARGIN, pos, width - MARGIN * 2, BUTTON_HEIGHT);
			add(btn);
			return btn.bottom() + MARGIN;
		}
	}


	//友情链接下载窗口：带进度条的下载 UI，参照 GDChangesButton.WndUpdate 的视觉风格
	//使用实例状态（非静态），避免与 GRPD 自身更新按钮的状态互相干扰
	public static class WndFriendUpdate extends Window {

		private static final int WIDTH_P = 120;
		private static final int WIDTH_L = 144;
		private static final int MARGIN = 2;
		private static final int BUTTON_HEIGHT = 18;

		//实例状态（每个窗口独立，不与 GRPD 更新按钮共享）
		private float progressValue = 0f;
		private String progressText = "";
		private boolean downloadStart = false;
		private boolean downloadSuccess = false;
		private boolean downloadFailure = false;
		private File downloadedFile = null;

		private final String gameName;
		private final String versionName;
		private final String url1;    //移动端 APK 下载链接
		private final String url3;    //桌面端 jar 下载链接
		private final String changeLog;

		private PlatformSupport.UpdateCallback listener = new PlatformSupport.UpdateCallback() {
			@Override
			public void onDownloading(boolean isDownloading) {}

			@Override
			public void onStart(String url) {
				progressText = Messages.get(GDChangesButton.class, "downloading");
				downloadStart = true;
			}

			@SuppressWarnings("DefaultLocale")
			@Override
			public void onProgress(long progress, long total, boolean isChanged) {
				progressValue = (float) progress / total;
				progressText = String.format("%.2f", progressValue * 100) + "%";
			}

			@Override
			public void onFinish(File file) {
				downloadSuccess = true;
				downloadFailure = false;
				downloadStart = false;
				downloadedFile = file;
				progressText = Messages.get(GDChangesButton.class, "downloadsuccess");
				GLog.p(Messages.get(GDChangesButton.class, "downloadsuccessling"));
			}

			@Override
			public void onError(Exception e) {
				progressText = Messages.get(GDChangesButton.class, "downloadingfailed");
				downloadFailure = true;
				downloadStart = false;
			}

			@Override
			public void onCancel() {
				progressText = Messages.get(GDChangesButton.class, "downloadingfailed");
				downloadFailure = true;
				downloadStart = false;
			}
		};

		WndFriendUpdate(String gameName, String versionName, String url1, String url3, String changeLog) {
			super();
			this.gameName = gameName;
			this.versionName = versionName;
			this.url1 = url1;
			this.url3 = url3;
			this.changeLog = changeLog;

			int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

			IconTitle tfTitle = new IconTitle(Icons.get(Icons.INFO),
					Messages.get(GDChangesButton.class, "versioned_title", versionName));
			tfTitle.color(Window.TITLE_COLOR);
			tfTitle.setRect(0, 0, width, 0);
			add(tfTitle);

			float pos = tfTitle.bottom() + 2 * MARGIN;

			//进度条背景
			Image bg = new Image(Assets.Interfaces.UPBARS) {
				@Override
				public synchronized void update() {
					super.update();
					visible = !progressText.isEmpty();
				}
			};
			bg.setPos(0, pos - 2);
			bg.visible = false;
			add(bg);

			//进度百分比文字
			BitmapText progressTextDisplay = new BitmapText(PixelScene.pixelFont) {
				@Override
				public void update() {
					if (downloadSuccess) {
						text("100%");
					} else if (!progressText.isEmpty()) {
						text(progressText);
					}
				}
			};
			progressTextDisplay.x = width / 1.23f;
			progressTextDisplay.y = pos - 2;
			add(progressTextDisplay);

			//进度填充条
			Image download = new Image(Assets.Interfaces.DOWNLOAD) {
				@Override
				public synchronized void update() {
					super.update();
					scale.x = progressValue;
					visible = true;
				}
			};
			download.setPos(bg.x, bg.y);
			download.visible = false;
			add(download);

			//变更说明
			String message = changeLog.isEmpty() ?
					("下载 " + gameName + " 最新版本") : changeLog;
			RenderedTextBlock tfMessage = PixelScene.renderTextBlock(6);
			tfMessage.text(message, width);
			tfMessage.setPos(0, pos + 8);
			add(tfMessage);

			pos = tfMessage.bottom() + 2 * MARGIN * 2;

			//下载/安装按钮
			RedButton btn = new RedButton(
					DeviceCompat.isDesktop() ?
							Messages.get(GDChangesButton.class, "downloadpc") :
							Messages.get(GDChangesButton.class, "download1")) {
				@Override
				public void update() {
					if (downloadSuccess) {
						text(Messages.get(GDChangesButton.class, "downloadsuccess"));
					}
				}

				@Override
				protected void onClick() {
					if ("null".equals(WndFriendUpdate.this.url1) && !downloadSuccess) {
						GirlsFrontlinePixelDungeon.scene().add(
								new WndError(Messages.get(GDChangesButton.class, "null")));
					} else if (DeviceCompat.isDesktop()) {
						GirlsFrontlinePixelDungeon.platform.openURI(WndFriendUpdate.this.url3);
					} else if (!downloadSuccess) {
						if (downloadFailure) downloadFailure = false;
						Game.platform.updateGame(WndFriendUpdate.this.url1, listener);
					} else {
						Game.platform.install(downloadedFile);
					}
				}
			};
			btn.setRect(0, pos, width, BUTTON_HEIGHT);
			add(btn);
			pos += BUTTON_HEIGHT + MARGIN;

			if (!DeviceCompat.isDesktop()) {
				add(btn);
			}

			//关闭按钮
			RedButton btnClose = new RedButton("关闭") {
				@Override
				protected void onClick() {
					hide();
				}
			};
			btnClose.setRect(0, pos, width, BUTTON_HEIGHT);
			add(btnClose);
			pos += BUTTON_HEIGHT + MARGIN;

			resize(width, (int) (pos - MARGIN));
		}
	}
}
