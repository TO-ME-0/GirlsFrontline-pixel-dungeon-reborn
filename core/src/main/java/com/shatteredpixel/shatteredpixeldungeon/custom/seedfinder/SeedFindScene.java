package com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.Constants;
import com.shatteredpixel.shatteredpixeldungeon.items.ColorItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.SecondTitleScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndRanking;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSettings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndStartGame;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTabbed;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class SeedFindScene extends PixelScene {
    public static String seedCode = "";
    private static final ArrayList<Item> wantedItems = new ArrayList<>();
    public static HeroClass currentHero = HeroClass.NONE;
    public static int currentFloor = Constants.MAX_DEPTH;
    public void create() {
        super.create();

        int w = Camera.main.width;
        int h = Camera.main.height;

        Archs archs = new Archs();
        archs.setSize(w, h);
        add(archs);

        INSTANCE = this;

        HeroClass heroClass = GamesInProgress.selectedClass;
        GamesInProgress.selectedClass = currentHero;
        Dungeon.init("");
        GamesInProgress.selectedClass = heroClass;
        
        addToFront(mainWindow = new WndFinder());

        buildSearchView();

        addExitButton();
    }

    // ======================== 搜索结果视图（scene 级，盖住 WndFinder） ========================
    private RenderedTextBlock currentSeedText;
    private ScrollPane resultScroll;
    private Component resultContent;
    private boolean searchViewVisible = false;
    private float seedDisplayCooldown = 0f;

    private void buildSearchView() {
        currentSeedText = PixelScene.renderTextBlock("", 6);
        currentSeedText.hardlight(0xFFFFFF);
        currentSeedText.visible = false;
        add(currentSeedText);

        resultContent = new Component();
        resultScroll = new ScrollPane(resultContent);
        resultScroll.visible = false;
        resultScroll.active = false;
        add(resultScroll);
    }

    // 关闭 Tab（隐藏 WndFinder），显示搜索视图（全屏，与 GitHub 版一致）
    private void showSearchView() {
        searchViewVisible = true;
        mainWindow.visible = false;
        mainWindow.active = false;

        float screenW = Camera.main.width;
        float screenH = Camera.main.height;
        float contentW = Math.min(120, screenW);

        float cx = (screenW - contentW) / 2f;

        currentSeedText.maxWidth((int) contentW);
        currentSeedText.text("正在查找……");
        currentSeedText.setRect(cx, 12, contentW, 0);
        currentSeedText.visible = true;

        resultContent.clear();
        resultContent.setSize(contentW, 0);
        float top = currentSeedText.bottom() + 2;
        resultScroll.setRect(cx, top, contentW, screenH - top);
        resultScroll.visible = true;
        resultScroll.active = true;
        resultScroll.scrollTo(0, 0);
    }

    // 显示查找结果（纯文本回退视图：NONE / 无楼层数据 / 关闭 Tab 窗后）
    private void showSearchResult(String body) {
        if (!searchViewVisible) return;
        searchDone = true;
        // 查询结束，清掉“正在查找……”状态文本
        currentSeedText.text("");
        currentSeedText.visible = false;

        resultContent.clear();
        CreditsBlock txt = new CreditsBlock(true, Window.TITLE_COLOR, body);
        resultContent.add(txt);
        float contentW = Math.min(120, (float) Camera.main.width);
        txt.setRect(0, 0, contentW, 0);
        resultContent.setSize(contentW, txt.bottom() + 4);
        resultScroll.scrollTo(0, 0);
    }

    // ======================== 结果 Tab 弹窗 ========================
    private static final class ResultTab {
        final String title;
        final String body;
        final boolean overview; // 总览页带复制按钮
        ResultTab(String title, String body, boolean overview) {
            this.title = title;
            this.body = body;
            this.overview = overview;
        }
    }

    // 楼层分组：每 5 层一页；31 层并入 26-30 页；25/1 属于 21-25 页
    private void buildResultTabs() {
        resultTabs = new ArrayList<>();
        // 总览页：各需求物品所在楼层（测试种子模式不生成）
        if (!isTestSeed) {
            StringBuilder ov = new StringBuilder();
            for (Item w : wantedItems) {
                WantedTarget t = new WantedTarget(w);
                StringBuilder fl = new StringBuilder();
                for (SeedFinder.FloorData fd : resultFloors) {
                    if (floorHas(fd, t)) {
                        if (fl.length() > 0) fl.append("、");
                        fl.append(fd.title);
                    }
                }
                ov.append(w).append("：");
                if (fl.length() > 0) ov.append(fl).append(" 楼");
                else ov.append("未找到");
                ov.append("\n\n");
            }
            if (resultTrinketSequence != null && !resultTrinketSequence.isEmpty()) {
                ov.append("饰品序列：\n");
                for (int i = 0; i < resultTrinketSequence.size(); i++)
                    ov.append(i + 1).append(". ").append(resultTrinketSequence.get(i)).append("\n");
                ov.append("\n");
            }
            resultTabs.add(new ResultTab("总览", ov.toString(), true));
        }
        StringBuilder[] groupText = new StringBuilder[6];
        for (SeedFinder.FloorData fd : resultFloors) {
            int g = Math.min(5, (fd.depth - 1) / 5);
            if (groupText[g] == null) groupText[g] = new StringBuilder();
            groupText[g].append(fd.text);
        }
        String[] titles = {"1-5", "6-10", "11-15", "16-20", "21-25", "26-30"};
        for (int g = 0; g < 6; g++)
            if (groupText[g] != null)
                resultTabs.add(new ResultTab(titles[g], groupText[g].toString(), false));
    }

    private static boolean floorHas(SeedFinder.FloorData fd, WantedTarget t) {
        for (SeedFinder.HeapItem hi : fd.heapItems)
            if (t.matches(hi.item)) return true;
        if (fd.ghostRewards != null)
            for (Item i : fd.ghostRewards)
                if (t.matches(i)) return true;
        if (fd.wandmakerRewards != null)
            for (Item i : fd.wandmakerRewards)
                if (t.matches(i)) return true;
        if (fd.impRewards != null)
            for (Item i : fd.impRewards)
                if (t.matches(i)) return true;
        return false;
    }

    private void showResultSheet(int sheet) {
        if (curResultWnd != null) {
            curResultWnd.hide();
            curResultWnd = null;
        }
        resultScroll.visible = false;
        resultScroll.active = false;
        currentSeedText.visible = false;
        curResultWnd = new WndResult(sheet);
        GirlsFrontlinePixelDungeon.scene().addToFront(curResultWnd);
        //Window 的全屏 blocker 会拦截其点击，退出按钮须重新提到最上层
        exitButton.killAndErase();
        exitButton.destroy();
        addExitButton();
    }

    // 楼层文本页：ScrollPane + 高亮楼层头
    private ScrollPane textPage(String body, float w) {
        Component content = new Component();
        RenderedTextBlock txt = PixelScene.renderTextBlock(body, 6);
        txt.setHightlighting(true, Window.TITLE_COLOR);
        txt.maxWidth((int) w - 4);
        content.add(txt);
        txt.setPos(1, 1);
        content.setSize(w, txt.height() + 2);
        ScrollPane sp = new ScrollPane(content);
        sp.scrollTo(0, 0);
        return sp;
    }

    // 总览页：物品→楼层列表 + 复制按钮
    // setRect 必须延迟到 layout()：此时页面尚未加入窗口，父链上没有 camera，
    // 而 ScrollPane.layout 依赖 camera() 做 cameraToScreen，提前 setRect 会 NPE
    private Component overviewPage(final String body, final float w) {
        return new Component() {
            final ScrollPane sp;
            final RenderedTextBlock seedLabel;
            final RedButton copyBtn;
            {
                Component scrollContent = new Component();
                RenderedTextBlock txt = PixelScene.renderTextBlock(body, 6);
                txt.setHightlighting(true, Window.TITLE_COLOR);
                txt.maxWidth((int) w - 4);
                scrollContent.add(txt);
                txt.setPos(1, 1);
                scrollContent.setSize(w, txt.height() + 2);
                sp = new ScrollPane(scrollContent);
                add(sp);

                seedLabel = PixelScene.renderTextBlock("种子码：" + SeedFindScene.seedCode, 8);
                seedLabel.hardlight(Window.TITLE_COLOR);
                seedLabel.maxWidth((int) w - 2);
                add(seedLabel);

                copyBtn = new RedButton("复制种子码") {
                    @Override
                    protected void onClick() {
                        confirmCopySeed();
                    }
                };
                copyBtn.icon(Icons.RENAME_ON.get());
                add(copyBtn);
            }

            @Override
            protected void layout() {
                sp.setRect(0, 0, width, height - 33);
                sp.scrollTo(0, 0);
                seedLabel.setPos(1, height - 31);
                copyBtn.setRect(1, height - 20, width - 2, 18);
            }
        };
    }

    private void confirmCopySeed() {
        if (SeedFindScene.seedCode != null && !SeedFindScene.seedCode.isEmpty()) {
            addToFront(new WndOptions(new Image(new ItemSprite(ItemSpriteSheet.SEED_HOLDER)),
                    Messages.get(WndSettings.class, "copy_title"),
                    Messages.get(WndSettings.class, "copy_body"),
                    Messages.get(WndSettings.class, "copy_yes"),
                    Messages.get(WndSettings.class, "copy_no")) {
                @Override
                protected void onSelect(int index) {
                    if (index == 0) SPDSettings.seedCode(SeedFindScene.seedCode);
                }
            });
        }
    }

    private class WndResult extends WndTabbed {
        WndResult(final int sheet) {
            super();
            int winW = PixelScene.landscape() ? WndJournal.WIDTH_L : WndJournal.WIDTH_P;
            int winH = PixelScene.landscape() ? WndJournal.HEIGHT_L : WndJournal.HEIGHT_P;
            resize(winW, winH);

            int sheets = (resultTabs.size() + TABS_PER_WINDOW - 1) / TABS_PER_WINDOW;
            boolean nav = sheets > 1;
            float pageH = winH - (nav ? 18 : 0);

            int first = sheet * TABS_PER_WINDOW;
            int last = Math.min(resultTabs.size(), first + TABS_PER_WINDOW);
            final Component[] pages = new Component[last - first];
            int n = 0;
            for (int i = first; i < last; i++, n++) {
                final ResultTab rt = resultTabs.get(i);
                final int idx = n;
                Component page = rt.overview
                        ? overviewPage(rt.body, winW)
                        : textPage(rt.body, winW);
                pages[n] = page;
                add(page);
                page.setRect(0, 0, winW, pageH);
                Tab tab = new LabeledTab(rt.title) {
                    @Override
                    protected void select(boolean value) {
                        super.select(value);
                        pages[idx].active = pages[idx].visible = value;
                    }
                };
                add(tab);
            }
            layoutTabs();
            for (int i = 1; i < pages.length; i++) {
                pages[i].visible = pages[i].active = false;
            }
            select(0);

            // 多弹窗导航：每弹窗最多 5 页
            if (nav) {
                float by = winH - 16;
                if (sheet > 0) {
                    StyledButton prev = new StyledButton(Chrome.Type.GEM, "◀", 7) {
                        @Override
                        protected void onClick() {
                            showResultSheet(sheet - 1);
                        }
                    };
                    add(prev);
                    prev.setRect(1, by, 20, 15);
                }
                if (sheet < sheets - 1) {
                    StyledButton next = new StyledButton(Chrome.Type.GEM, "▶", 7) {
                        @Override
                        protected void onClick() {
                            showResultSheet(sheet + 1);
                        }
                    };
                    add(next);
                    next.setRect(winW - 21, by, 20, 15);
                }
                RenderedTextBlock pl = PixelScene.renderTextBlock((sheet + 1) + " / " + sheets, 7);
                pl.hardlight(0xFFFFFF);
                add(pl);
                pl.setPos((winW - pl.width()) / 2f, by + 4);
            }
        }

        // 点击窗口外不关闭，统一走 Scene 右上角退出
        @Override
        public void onBackPressed() {
        }
    }
    private ExitButton exitButton;
    private void addExitButton() {
        exitButton = new ExitButton() {
            @Override
            public void onClick() {
                exitScene();
            }
        };
        exitButton.setPos((float) Camera.main.width - exitButton.width(), 0);
        addToFront(exitButton);
    }

    // 退出查种器：从0层电脑进入则读档返回0层，否则返回第二标题页
    private void exitScene() {
        stopSearch();
        if (com.shatteredpixel.shatteredpixeldungeon.windows.WndComputer.launchedFromGame) {
            com.shatteredpixel.shatteredpixeldungeon.windows.WndComputer.backFromComputerApp();
        } else {
            GirlsFrontlinePixelDungeon.switchNoFade(SecondTitleScene.class);
        }
    }

    @Override
    protected void onBackPressed() {
        exitScene();
    }
    // 挑战文本（始终过滤 TEST_MODE，种子查找不允许测试模式）
    private static String challengeText() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < Challenges.NAME_IDS.length; i++) {
            if (Challenges.MASKS[i] == Challenges.TEST_MODE) continue;
            if ((SPDSettings.challenges() & Challenges.MASKS[i]) != 0) {
                if (!first) sb.append("、");
                sb.append(Messages.titleCase(Messages.get(Challenges.class, Challenges.NAME_IDS[i])));
                first = false;
            }
        }
        if (first) sb.append("无");
        return sb.toString();
    }
    private volatile boolean stopThread = false;

    // ===== 查找状态（后台线程写 / 渲染线程读） =====
    private SeedFinder activeFinder;
    private long searchStartMs;
    private volatile ArrayList<SeedFinder.FloorData> resultFloors;
    private volatile ArrayList<Item> resultTrinketSequence;
    private volatile boolean isTestSeed;
    private volatile boolean searchDone;

    // ===== 结果 Tab 弹窗 =====
    private static final int TABS_PER_WINDOW = 5;
    private ArrayList<ResultTab> resultTabs;
    private Window curResultWnd;

    private static final int BTN_H = 16;

    // ======================== 新窗口（仅第一页实现，其余留空） ========================
    private final class WndFinder extends WndTabbed {
        private final int winW;
        private final int winH;
        private final Component[] pages = new Component[5];
        private final Tab[] tabs = new Tab[5];
        private RedButton testBtn;
        private RenderedTextBlock page1Info;

        WndFinder() {
            super();

            winW = PixelScene.landscape() ? WndJournal.WIDTH_L : WndJournal.WIDTH_P;
            winH = PixelScene.landscape() ? WndJournal.HEIGHT_L : WndJournal.HEIGHT_P;

            // 含 ScrollPane 的窗口必须最先 resize
            resize(winW, winH);

            // ---- 五个页签，initPage 内部同时添加页和 Tab ----
            initPage(0, settingPage(),       Icons.get(Icons.PREFS));
            initPage(1, equipmentPane(),     new ItemSprite(ItemSpriteSheet.WEAPON_HOLDER));
            initPage(2, stackablePage(),     new ItemSprite(ItemSpriteSheet.POTION_HOLDER));
            initPage(3, listPage(),          Icons.get(Icons.BACKPACK));
            initPage(4, startFindingPane(),  Icons.get(Icons.TARGET));

            layoutTabs();
            // 初始只显示第一页（select(0) 仅对 tab[0] 调 select(true)，其余需手动隐藏）
            for (int i = 1; i < pages.length; i++) {
                pages[i].visible = pages[i].active = false;
            }
            select(0);
        }

        private void initPage(final int index, Component page, Image tabIcon) {
            pages[index] = page;
            add(page);
            page.setRect(0, 0, winW, winH);
            Tab tab = new IconTab(tabIcon) {
                @Override protected void select(boolean value) {
                    super.select(value);
                    pages[index].active = pages[index].visible = value;
                }
            };
            tabs[index] = tab;
            add(tab);
        }
        @Override
        public void update() {
            super.update();
            boolean heroReady = currentHero != HeroClass.NONE;
            boolean hasItems = !wantedItems.isEmpty();
            tabs[1].active = heroReady;
            tabs[2].active = heroReady;
            tabs[3].active = heroReady;
            tabs[4].active = heroReady && hasItems;
            if (testBtn != null) testBtn.enable(heroReady);
        }

        //第一页设置
        private Component settingPage() {
            Component root = new Component();
            float w = winW - 4;

            RenderedTextBlock title = PixelScene.renderTextBlock("当前设置", 9);
            title.hardlight(TITLE_COLOR);
            root.add(title);
            title.setPos(2, 1);

            page1Info = PixelScene.renderTextBlock("", 6);
            root.add(page1Info);
            page1Info.setRect(2, 13, w, 40);
            refreshPage1Info();

            RenderedTextBlock hint = PixelScene.renderTextBlock(
                    "「设置」选择角色、最深楼层与挑战；「测试种子」以当前角色、最深楼层、挑战列出物品清单。", 6);
            root.add(hint);
            hint.maxWidth((int) w);
            hint.setRect(2, 55, w, 32);

            RedButton settingsBtn = new RedButton("设置（角色 / 最深楼层 / 挑战）", 8) {
                @Override
                protected void onClick() {
                    GirlsFrontlinePixelDungeon.scene().addToFront(new WndFinderSettings());
                }
            };
            settingsBtn.icon(Icons.get(Icons.PREFS));
            root.add(settingsBtn);
            settingsBtn.setRect(2, 89, w, 18);

            testBtn = new RedButton("测试种子（输入种子码直接生成）", 8) {
                @Override
                protected void onClick() {
                    GirlsFrontlinePixelDungeon.scene().addToFront(
                            new WndTextInput(
                                    Messages.get(WndStartGame.class, "set_seed_title"),
                                    Messages.get(WndStartGame.class, "set_seed_desc"),
                                    SPDSettings.seedCode(),
                                    20,
                                    false,
                                    Messages.get(WndStartGame.class, "set_seed_confirm"),
                                    Messages.get(WndStartGame.class, "set_seed_cancel")
                            ) {
                                @Override
                                public void onSelect(boolean check, String text) {
                                    if(check) {
                                        seedCode = DungeonSeed.formatText(text);
                                        startTestSeed();
                                    }
                                }
                            }
                    );
                }
            };
            testBtn.icon(new ItemSprite(ItemSpriteSheet.SEED_SUNGRASS));
            root.add(testBtn);
            testBtn.setRect(2, 110, w, 18);

            return root;
        }
        private void refreshPage1Info() {
            if (page1Info == null) return;
            page1Info.text(
                    "角色：" + Messages.capitalize(currentHero.title()) + "\n"
                            + "最深楼层：" + currentFloor + "\n"
                            + "挑战：" + challengeText(), winW);
        }
        //第二页装备
        @SuppressWarnings("unchecked")
        private Component equipmentPane() {
            ArrayList<ItemGroup> groups = new ArrayList<>();

            // 2-6阶近战武器
            Generator.Category category;
            ItemGroup g;
            for (int t = 1; t < Generator.wepTiers.length; t++) {
                category = Generator.wepTiers[t];
                g = new ItemGroup(Catalog.valueOf("MELEE_WEAPONS_T" + (t + 1)).title());
                g.items.add(Holder.meleeHolder[t]);
                for (int i = 0; i < category.classes.length; i++)
                    if (category.probs[i] >= 0f)
                        g.items.add((Class<? extends Item>) category.classes[i]);
                if (!g.items.isEmpty())
                    groups.add(g);
            }

            // 2-5阶投掷武器
            for (int t = 1; t < Generator.misTiers.length; t++) {
                category = Generator.misTiers[t];
                g = new ItemGroup((t + 1) + "阶" + Catalog.MISSILE_WEAPONS.title());
                g.items.add(Holder.missileHolder[t]);
                for (int i = 0; i < category.classes.length; i++)
                    if (category.probs[i] >= 0f)
                        g.items.add((Class<? extends Item>) category.classes[i]);
                if (!g.items.isEmpty())
                    groups.add(g);
            }

            // 2-5阶护甲
            category = Generator.Category.ARMOR;
            g = new ItemGroup(Catalog.ARMOR.title());
            g.items.add(Holder.ArmorHolder.class);
            for (int i = 0; i < category.classes.length; i++)
                if (category.probs[i] >= 0f)
                    g.items.add((Class<? extends Item>) category.classes[i]);
            groups.add(g);

            // wands
            category = Generator.Category.WAND;
            g = new ItemGroup(Catalog.WANDS.title());
            g.items.add(Holder.WandHolder.class);
            for (int i = 0; i < category.classes.length; i++)
                if (category.probs[i] >= 0f)
                    g.items.add((Class<? extends Item>) category.classes[i]);
            groups.add(g);

            // rings
            category = Generator.Category.RING;
            g = new ItemGroup(Catalog.RINGS.title());
            g.items.add(Holder.RingHolder.class);
            for (int i = 0; i < category.classes.length; i++)
                if (category.probs[i] >= 0f)
                    g.items.add((Class<? extends Item>) category.classes[i]);
            groups.add(g);

            // artifacts
            category = Generator.Category.ARTIFACT;
            g = new ItemGroup(Catalog.ARTIFACTS.title());
            for (int i = 0; i < category.classes.length; i++)
                if (category.defaultProbs[i] >= 0f)
                    g.items.add((Class<? extends Item>) category.classes[i]);
            if (!g.items.isEmpty())
                groups.add(g);

            ScrollingGridPane grid = new ScrollingGridPane(false);
            for (ItemGroup gr : groups) {
                grid.addHeader(gr.header);
                for (Class<? extends Item> cl : gr.items)
                    grid.addItem(new PickGridItem(cl));
            }
            grid.scrollTo(0, 0);
            return grid;
        }
        //第三页消耗品
        @SuppressWarnings("unchecked")
        private Component stackablePage() {
            ArrayList<ItemGroup> groups = new ArrayList<>();
            Generator.Category category;
            ItemGroup g;

            category = Generator.Category.POTION;
            g = new ItemGroup(Catalog.POTIONS.title());
            g.items.add(Holder.PotionHolder.class);
            for (int i = 0; i < category.classes.length; i++)
                if (category.probs[i] >= 0f)
                    g.items.add((Class<? extends Item>) category.classes[i]);
            groups.add(g);

            category = Generator.Category.SCROLL;
            g = new ItemGroup(Catalog.SCROLLS.title());
            g.items.add(Holder.ScrollHolder.class);
            for (int i = 0; i < category.classes.length; i++)
                if (category.probs[i] >= 0f)
                    g.items.add((Class<? extends Item>) category.classes[i]);
            groups.add(g);

            category = Generator.Category.STONE;
            g = new ItemGroup(Catalog.STONES.title());
            g.items.add(Holder.StoneHolder.class);
            for (int i = 0; i < category.classes.length; i++)
                if (category.probs[i] >= 0f)
                    g.items.add((Class<? extends Item>) category.classes[i]);
            groups.add(g);
            
            category = Generator.Category.TRINKET;
            g = new ItemGroup(Catalog.TRINKETS.title());
            for (int i = 0; i < category.classes.length; i++)
                if (category.defaultProbs[i] >= 0f)
                    g.items.add((Class<? extends Item>) category.classes[i]);
            if (!g.items.isEmpty())
                groups.add(g);

            category = Generator.Category.FOOD;
            g = new ItemGroup(Catalog.FOOD.title());
            for (int i = 0; i < category.classes.length; i++)
                if (category.probs[i] >= 0f)
                    g.items.add((Class<? extends Item>) category.classes[i]);
            groups.add(g);

            ScrollingGridPane grid = new ScrollingGridPane(false);
            for (ItemGroup gr : groups) {
                grid.addHeader(gr.header);
                for (Class<? extends Item> cl : gr.items)
                    grid.addItem(new PickGridItem(cl));
            }
            grid.scrollTo(0, 0);
            return grid;
        }
        //第四页物品清单
        private Component listPage() {
            return new listPageWrapper();
        }
        private class listPageWrapper extends Component {
            listPane pane;
            RedButton removeAllBtn;
            listPageWrapper() {
                super();
                pane = new listPane();
                add(pane);
                removeAllBtn = new RedButton("移除全部") {
                    @Override
                    protected void onClick() {
                        wantedItems.clear();
                        pane.lastCount = -1;
                    }
                };
                add(removeAllBtn);
            }
            @Override
            protected void layout() {
                // 下方常驻"移除全部"按钮(24px)，上方为滚动列表
                pane.setRect(0, 0, width, Math.max(0, height - 24));
                removeAllBtn.setRect(0, Math.max(0, height - 22), width, 20);
            }
        }
        private class listPane extends ScrollPane {
            int lastCount = -1;
            ArrayList<WndRanking.canScrollItemButton> buttons = new ArrayList<>();
            public listPane() {
                super(new Component());
            }
            @Override
            public void update() {
                super.update();
                int count = wantedItems.size();
                if (count != lastCount) {
                    resetButton();
                    lastCount = count;
                }
            }
            void resetButton() {
                for (WndRanking.canScrollItemButton button : buttons)
                    button.destroy();
                content.clear();
                buttons.clear();

                float pos = 0;
                for (Item item : wantedItems.toArray(new Item[0])) {
                    String[] actions = item.quantity() > 1 ? new String[]{"移除一个", "移除全部", "取消"}: new String[]{"移除", "取消"};
                    WndRanking.canScrollItemButton button = new WndRanking.canScrollItemButton(item, true) {
                        @Override
                        public void onClick() {
                            GirlsFrontlinePixelDungeon.scene().addToFront(
                                    new WndOptions(item.toString(), item.desc(), actions) {
                                        @Override
                                        public void onSelect(int index) {
                                            if (item.quantity() == 1)
                                                index++;

                                            if (index == 0) {
                                                item.quantity(item.quantity() - 1);
                                                lastCount = -1;
                                                hide();
                                            }
                                            else if (index == 1) {
                                                wantedItems.remove(item);
                                                lastCount = -1;
                                                hide();
                                            }
                                            else if (index == 2)
                                                hide();
                                        }
                                    });
                        }
                    };
                    button.setRect( 0, pos, width, 23 );
                    content.add(button);
                    buttons.add(button);
                    pos += button.height() + 1;
                }
                content.setSize(width, pos - 1);
            }
        }
        //第五页文字清单+开始查询
        private Component startFindingPane() {
            return new summaryPane();
        }
        private class summaryPane extends ScrollPane {
            int lastCount = -1;
            RedButton startBtn;
            public summaryPane() {
                super(new Component());
            }
            @Override
            public void update() {
                super.update();
                int count = wantedItems.size();
                if (count != lastCount) {
                    resetButton();
                    lastCount = count;
                }
            }
            void resetButton() {
                Component content = this.content();
                content.clear();
                float w = width;

                StringBuilder sb = new StringBuilder();
                sb.append("角色：").append(Messages.capitalize(currentHero.title())).append("\n");
                sb.append("挑战：").append(challengeText()).append("\n");
                sb.append("最深楼层：").append(currentFloor).append("\n");
                sb.append("物品需求（共 ").append(wantedItems.size()).append(" 件）：\n");
                for (int i = 0; i < wantedItems.size(); i++)
                    sb.append(i + 1).append(". ").append(wantedItems.get(i).toString()).append("\n");
                
                RenderedTextBlock summary = PixelScene.renderTextBlock("", 6);
                summary.text(sb.toString(), (int) w);
                content.add(summary);
                summary.setRect(1, 1, w - 2, 0);
                float pos = summary.bottom() + 4;

                // 线程数滑条：控制查种 worker 数量
                OptionSlider threadSlider = new OptionSlider("线程数量", "1", "8", 1, 8) {
                    @Override
                    protected void onChange() {
                        SPDSettings.seedFinderThreads(getSelectedValue());
                    }
                };
                threadSlider.setSelectedValue(SPDSettings.seedFinderThreads());
                content.add(threadSlider);
                threadSlider.setRect(1, pos, w - 2, 24);
                pos = threadSlider.bottom() + 4;

                // 允许缺少数滑条：0 表示必须集齐全部物品，N 表示可少找 N 件
                OptionSlider missingSlider = new OptionSlider("允许缺少物品数", "0", "4", 0, 4) {
                    @Override
                    protected void onChange() {
                        SPDSettings.seedFinderMissing(getSelectedValue());
                    }
                };
                missingSlider.setSelectedValue(SPDSettings.seedFinderMissing());
                content.add(missingSlider);
                missingSlider.setRect(1, pos, w - 2, 24);
                pos = missingSlider.bottom() + 4;

                startBtn = new RedButton("开始查找") {
                    @Override
                    protected void onClick() {
                        startSearch();
                    }
                };
                content.add(startBtn);
                startBtn.setRect(1, pos, w - 2, 18);
                pos = startBtn.bottom() + 4;

                content.setSize(w, pos);
            }
        }
        @Override
        public void onBackPressed() {

        }
    }
    public static class WndFinderSettings extends WndStartGame {
            // 与父类窗口同宽；底部按钮行位于 HEIGHT(150)-20
            private static final int WIN_W = 117;
            private static final int ROW_Y = 130;

            private int tempFloor = currentFloor;
            public WndFinderSettings() {
                super(WndStartGame.slot, true, WndStartGame.mode);
            }
            @Override
            public void addStartButton() {
                RedButton confirmBtn = new RedButton("确认") {
                    @Override
                    public void onClick() {
                        if (GamesInProgress.selectedClass == null) return;
                        super.onClick();
                        SeedFindScene.currentHero = GamesInProgress.selectedClass;
                        currentFloor = tempFloor;
                        Dungeon.init("");
                        mainWindow.refreshPage1Info();
                        hide();
                    }
                    @Override
                    public void update() {
                        if(!visible && GamesInProgress.selectedClass != null){
                            visible = true;
                        }
                        super.update();
                    }
                };
                confirmBtn.visible = false;
                confirmBtn.setRect(0, ROW_Y, WIN_W, 20);
                add(confirmBtn);
            }
            @Override
            public void addLeftButton(boolean ignored) {
                StyledButton floorBtn = new FloorButton();
                floorBtn.setRect(0, ROW_Y, 20, 20);
                floorBtn.visible = false;
                add(floorBtn);
            }
            @Override
            public void addRightButton(boolean ignore, boolean ignored) {
                super.addRightButton(false, true);
            }
            public final class FloorButton extends StyledButton {
                public FloorButton() {
                    super(Chrome.Type.GEM, String.valueOf(currentFloor), 8);
                }
                @Override
                protected void onClick() {
                    GirlsFrontlinePixelDungeon.scene().addToFront(new WndSelectLevel());
                }
                @Override
                public void update() {
                    // 与父类种子/挑战按钮一致：选中角色后才显现
                    if (!visible && GamesInProgress.selectedClass != null) {
                        visible = true;
                    }
                    super.update();
                }
                public final class WndSelectLevel extends Window {
                    private static final int PICKER_W = 120;
                    private static final int GAP = 2;
                    private static final int BTN_SIZE = 16;
                    private static final int PANE_MAX_HEIGHT = 96;

                    private int selectedFloor = tempFloor;
                    final public RedButton confirm;

                    WndSelectLevel() {
                        super();
                        ScrollPane sp = new ScrollPane(new Component());
                        add(sp);

                        confirm = new RedButton(Messages.get(SeedFindScene.class, "setFloor", selectedFloor)) {
                            @Override
                            protected void onClick() {
                                tempFloor = selectedFloor;
                                FloorButton.this.text(String.valueOf(selectedFloor));
                                hide();
                            }
                        };
                        add(confirm);

                        Component content = sp.content();
                        float xPos = (PICKER_W - 5 * BTN_SIZE - GAP * 8) / 2f;
                        float each = GAP * 2 + BTN_SIZE;
                        for (int i = 0; i < Constants.MAX_DEPTH; ++i) {
                            StyledButton btn = floorBtn(i);
                            btn.setRect(xPos + (i % 5) * each, (i / 5) * each, BTN_SIZE, BTN_SIZE);
                            PixelScene.align(btn);
                            content.add(btn);
                        }

                        int rows = (Constants.MAX_DEPTH - 1) / 5 + 1;
                        float contentHeight = rows * each - GAP * 2;
                        content.setSize(PICKER_W, contentHeight);
                        sp.setRect(0, 0, PICKER_W, contentHeight);
                        confirm.setRect(0, PANE_MAX_HEIGHT + GAP * 2, PICKER_W, BTN_SIZE);
                        resize(PICKER_W, (int) confirm.bottom());
                        sp.setRect(0, 0, PICKER_W, PANE_MAX_HEIGHT);
                        sp.scrollTo(0, 0);
                    }

                    private StyledButton floorBtn(int i) {
                        final int j = i + 1;
                        return new StyledButton(Chrome.Type.GEM, String.valueOf(j), 8) {
                            {
                                hotArea.blockLevel = PointerArea.NEVER_BLOCK;
                            }
                            @Override
                            protected void onClick() {
                                selectedFloor = j;
                                confirm.text(Messages.get(SeedFindScene.class, "setFloor", selectedFloor));
                            }
                        };
                    }
                }
            }
        }
        
    private static final class PickGridItem extends ScrollingGridPane.GridItem {
        private final Class<? extends Item> cls;
        private final boolean direct;   // 神器/饰品：单击直接加入，无配置窗
        private final boolean single;   // 饰品：只允许一个
        PickGridItem(Class<? extends Item> cls) {
            super(image(cls));
            this.cls = cls;
            single = Trinket.class.isAssignableFrom(cls);
            direct = single || Artifact.class.isAssignableFrom(cls);
        }
        private boolean isWanted() {
            for (Item item : wantedItems)
                if (cls.isInstance(item)) return true;
            return false;
        }
        @Override
        public void update() {
            super.update();
            if (direct && isWanted())
                hardLightBG(0.25f, 0.7f, 0.3f);
            else
                bg.resetColor();
        }

        @Override
        public void onClick() {
            super.onClick();
            if (!direct) {
                GirlsFrontlinePixelDungeon.scene().addToFront(new ItemConfigWindow(cls));
                return;
            }
            if (isWanted()) {
                for (Item item : wantedItems.toArray(new Item[0]))
                    if (cls.isInstance(item)) {
                        wantedItems.remove(item);
                        break;
                    }
                return;
            }
            // 饰品只允许一个，选中新的前先清除已有饰品
            if (single)
                for (Item item : wantedItems.toArray(new Item[0]))
                    if (item instanceof Trinket)
                        wantedItems.remove(item);
            wantedItems.add(newInstance(cls));
        }
        private final static class ItemConfigWindow extends Window {
            private static final int WIDTH = 120;
            private static final int GAP = 2;
            private static final int SLIDER_H = 22;
            private static final int AUG_MAX_LINES = 3; // 最多3行(8项/4每行=2行 + 当前1行)
            private Class<?>[][] pools;
            boolean isEnchant;
            int augRare, augId;
            RenderedTextBlock augInfo;
            ItemConfigWindow(Class<? extends Item> cls) {
                super();
                Item item = newInstance(cls);
                int maxLevel = getMaxLevelForClass(item);

                if (item instanceof Weapon) {
                    pools = enchant;
                    isEnchant = true;
                } else if (item instanceof Armor) {
                    pools = glyph;
                    isEnchant = false;
                }

                float pos = GAP;
                IconTitle title = new IconTitle(item);
                title.color(TITLE_COLOR);
                add(title);
                title.setRect(0, pos, WIDTH, 0);
                pos = title.bottom() + 2F;

                OptionSlider levelSlider = getSlider(maxLevel, item);
                add(levelSlider);
                levelSlider.setRect(0, pos, WIDTH, SLIDER_H);
                pos = levelSlider.bottom() + GAP;
                if (pools != null) {
                    augInfo = PixelScene.renderTextBlock("", 6);
                    updateAugText();
                    add(augInfo);
                    augInfo.setRect(0, pos, WIDTH, augInfo.height());
                    pos = augInfo.bottom() + GAP;

                    OptionSlider rareSlider = new OptionSlider(
                            "", "", "", 0, 4) {
                        @Override
                        protected void onChange() {
                            augRare = getSelectedValue();
                            updateAugText();
                        }
                    };
                    rareSlider.setSelectedValue(0);
                    add(rareSlider);
                    rareSlider.setRect(0, pos, WIDTH, SLIDER_H);
                    pos = rareSlider.bottom() + GAP;

                    OptionSlider idSlider = new OptionSlider(
                            "", "1", "8", 0, 7) {
                        @Override
                        protected void onChange() {
                            augId = getSelectedValue();
                            updateAugText();
                        }
                    };
                    idSlider.setSelectedValue(0);
                    add(idSlider);
                    idSlider.setRect(0, pos, WIDTH, SLIDER_H);
                    pos = idSlider.bottom() + GAP;
                }

                RedButton confirmBtn = new RedButton("添加") {
                    @Override
                    protected void onClick() {
                        if (item.stackable) {
                            Item same = null;
                            for (Item i : wantedItems)
                                if (cls.isInstance(i))
                                    same = i;
                            if (same != null)
                                same.quantity(same.quantity() + item.quantity());
                            else
                                wantedItems.add(item);
                        }
                        else {
                            if (augRare > 0) {
                                Class<?>[] pool = pools[augRare - 1];
                                if (augId < pool.length) {
                                    Object aug = Reflection.newInstance(pool[augId]);
                                    if (aug instanceof Weapon.Enchantment)
                                        ((Weapon) item).enchant((Weapon.Enchantment) aug);
                                    else if (aug instanceof Armor.Glyph)
                                        ((Armor) item).inscribe((Armor.Glyph) aug);
                                }
                            }
                            wantedItems.add(item);
                        }
                        hide();
                    }
                };
                add(confirmBtn);
                confirmBtn.setRect(0, pos, WIDTH / 2f - 1, BTN_H);

                RedButton cancelBtn = new RedButton("取消") {
                    @Override
                    protected void onClick() {
                        hide();
                    }
                };
                add(cancelBtn);
                cancelBtn.setRect(WIDTH / 2f + 1, pos, WIDTH / 2f - 1, BTN_H);
                pos = cancelBtn.bottom() + GAP;

                resize(WIDTH, (int) pos);

            }
            private OptionSlider getSlider(int maxLevel, Item item) {
                OptionSlider slider;
                if (maxLevel > 0) {
                    slider = new OptionSlider("等级", "0", "+" + maxLevel, 0, maxLevel) {
                        @Override
                        protected void onChange() {
                            item.level(getSelectedValue());
                        }
                    };
                slider.setSelectedValue(0);
                }
                else {
                    slider = new OptionSlider("数量", "1", "10", 1, 10) {
                        @Override
                        protected void onChange() {
                            int value = getSelectedValue();
                            if (value > 0)
                                item.quantity(value);
                        }
                    };
                slider.setSelectedValue(1);
            }
                return slider;
            }
            private String getAugName(Class<?> augClass) {
                return Messages.get(augClass, "name", "");
            }
            private void updateAugText() {
                StringBuilder info = new StringBuilder();
                int lines = 0;
                if (augRare == 0) {
                    info.append("无");
                    lines = 1;
                } else {
                    Class<?>[] pool = pools[augRare - 1];
                    for (int i = 0; i < pool.length; i++) {
                        info.append(i + 1).append(":").append(getAugName(pool[i])).append(" ");
                        if ((i + 1) % 4 == 0 || i == pool.length - 1) {
                            info.append("\n");
                            lines++;
                        }
                    }
                    if (augId < pool.length)
                        info.append("当前: ").append(getAugName(pool[augId]));
                    else
                        info.append("当前: 无");
                    lines++;
                }
                // 填充到最大行数，保证高度恒定不变
                while (lines < AUG_MAX_LINES) {
                    info.append("\n");
                    lines++;
                }
                augInfo.text(info.toString(), WIDTH);
                // 触发 layout 重排文字并更新高度
                augInfo.setRect(augInfo.left(), augInfo.top(), augInfo.width(), augInfo.height());
            }
        }
    }

    static final Class<?>[][] enchant = {
            Weapon.Enchantment.common,
            Weapon.Enchantment.uncommon,
            Weapon.Enchantment.rare,
            Weapon.Enchantment.curses
    };
    static final Class<?>[][] glyph = {
            Armor.Glyph.common,
            Armor.Glyph.uncommon,
            Armor.Glyph.rare,
            Armor.Glyph.curses
    };
    private static Item newInstance(Class<? extends Item> cls) {
        Item item = Reflection.newInstance(cls);
        assert item != null;
        item.quantity(1);
        if (item instanceof ColorItem)
            ((ColorItem) item).anonymize();
        else
            item.identify();
        item.levelKnown = true;
        return item;
    }
    private static Image image(Class<? extends Item> cls) {
        Item item = newInstance(cls);
        if (item instanceof ColorItem)
            return Icons.Notice((ColorItem) item);
        return new ItemSprite(item.image, item.glowing());
    }
    // ---- 物品分组（懒构建，全局复用） ----
    private static final class ItemGroup {
        final String header;
        final ArrayList<Class<? extends Item>> items = new ArrayList<>();

        ItemGroup(String header) {
            this.header = header;
        }
    }
    private static int getMaxLevelForClass(Item item) {
        if (item instanceof Wand || item instanceof Holder.WandHolder) {
            // 已选 +3 任务配件，其余配件最多 +2（一局仅一根任务杖）
            return hasQuestLevel(Wand.class) || hasQuestLevel(Holder.WandHolder.class) || currentFloor < 7
                    ? 2 : 3;
        }
        if (item instanceof Ring || item instanceof Holder.RingHolder) {
            // 已选 +3（小恶魔任务奖励 +3/+4 的下限）瞄准镜，其余只能 +2
            return hasQuestLevel(Ring.class) || hasQuestLevel(Holder.RingHolder.class) || currentFloor < 17
                    ? 2 : 4;
        }
        return item instanceof Weapon || item instanceof Armor
                ? 3 : 0;
    }
    private static boolean hasQuestLevel(Class<?> type) {
        for (Item item : wantedItems)
            if (type.isInstance(item) && item.trueLevel() >= 3)
                return true;
        return false;
    }
    // ======================== update() / 查找状态节流刷新 ========================
    @Override
    public void update() {
        super.update();
        // 查找结果（不节流——查找一结束就立刻展示）
        if (text != null && !text.isEmpty()) {
            String result = text;
            text = "";
            if (resultFloors != null) {
                // Tab 弹窗结果（搜索模式：总览+楼层分组；测试种子模式：仅楼层分组）
                buildResultTabs();
                showResultSheet(0);
            } else {
                // 无结构化数据（NONE / 异常信息）→ 纯文本回退视图
                showSearchResult(result);
            }
        }
        // 查找状态：各线程当前种子 + 累计耗时（节流到 4 次/秒，避免与查找线程争抢 CPU）
        seedDisplayCooldown += Game.elapsed;
        if (seedDisplayCooldown >= 0.25f) {
            seedDisplayCooldown = 0f;
            if (searchViewVisible && !stopThread && !searchDone && curResultWnd == null
                    && activeFinder != null && (text == null || text.isEmpty())) {
                StringBuilder sb = new StringBuilder("正在查找…… 耗时 ")
                        .append(String.format("%.1f", (System.currentTimeMillis() - searchStartMs) / 1000.0))
                        .append(" 秒");
                for (int i = 0; i < activeFinder.workerCount(); i++) {
                    long s = activeFinder.workerSeed(i);
                    if (s >= 0)
                        sb.append("\n线程 ").append(i + 1).append("：").append(s);
                    String err = activeFinder.workerError(i);
                    if (err != null)
                        sb.append("\n线程 ").append(i + 1).append(" ").append(err);
                }
                currentSeedText.text(sb.toString());
            }
        }
    }
    public static SeedFindScene INSTANCE = null;
    volatile String text = "";

    @Override
    public void destroy() {
        super.destroy();
        stopSearch();
    }

    private static WndFinder mainWindow;
    private Thread findSeedThread;
    private void startSearch() {
        // 防御：即使 TEST_MODE 曾在 debug 菜单里被写入，种子查找也一律不带它
        SPDSettings.challenges(SPDSettings.challenges() & ~Challenges.TEST_MODE);

        final int threads = SPDSettings.seedFinderThreads();
        final SeedFinder finder = new SeedFinder(wantedItems, currentFloor, currentHero) {
            @Override
            public void run() {
                Dungeon.resetTest();
                String str = findSeed(threads);
                SeedFindScene.INSTANCE.resultFloors = lastFloors;
                SeedFindScene.INSTANCE.resultTrinketSequence = lastTrinketSequence;
                SeedFindScene.INSTANCE.isTestSeed = false;
                SeedFindScene.INSTANCE.text = str;
            }
        };
        launchFinder(finder);
    }

    // 测试种子：与 startSearch 完全独立，无视已添加物品，
    // 始终单线程进入查种上下文，直接生成输入种子码的物品清单
    private void startTestSeed() {
        // 防御：测试种子同样不带 TEST_MODE
        SPDSettings.challenges(SPDSettings.challenges() & ~Challenges.TEST_MODE);

        final SeedFinder finder = new SeedFinder(new ArrayList<>(), currentFloor, currentHero) {
            @Override
            public void run() {
                Dungeon.resetTest();
                Dungeon.enterSearchContext();
                try {
                    String str = logSeedItems(DungeonSeed.convertFromText(SeedFindScene.seedCode));
                    SeedFindScene.INSTANCE.resultFloors = lastFloors;
                    SeedFindScene.INSTANCE.resultTrinketSequence = lastTrinketSequence;
                    SeedFindScene.INSTANCE.isTestSeed = true;
                    SeedFindScene.INSTANCE.text = str;
                } finally {
                    Dungeon.exitSearchContext();
                }
            }
        };
        launchFinder(finder);
    }

    // 查找/测试共用的线程启动与状态重置
    private void launchFinder(final SeedFinder finder) {
        stopThread = false;
        searchDone = false;
        resultFloors = null;
        resultTrinketSequence = null;
        isTestSeed = false;
        resultTabs = null;

        showSearchView();
        activeFinder = finder;
        searchStartMs = System.currentTimeMillis();
        findSeedThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    finder.run();
                } catch (Exception e) {
                    GirlsFrontlinePixelDungeon.saveCrashReport(e);
                    e.printStackTrace();
                    if (!stopThread) {
                        text = "查找失败：" + e.getMessage();
                    }
                }
            }
        });
        findSeedThread.start();
    }

    private void stopSearch() {
        stopThread = true;
        if (findSeedThread != null && findSeedThread.isAlive()) {
            findSeedThread.interrupt();
        }
        SeedFinder.SeedFinding = false;
        SeedFinder.running = false;
    }

    // ======================== CreditsBlock（保留原样） ========================
    public static class CreditsBlock extends Component {
        boolean large;
        RenderedTextBlock body;

        public CreditsBlock(boolean large, int highlight, String body) {
            this.large = large;
            this.body = PixelScene.renderTextBlock(body, 6);
            if (highlight != -1) {
                this.body.setHightlighting(true, highlight);
            }

            if (large) {
                this.body.align(2);
            }

            this.add(this.body);
        }

        protected void layout() {
            super.layout();
            float topY = this.top();
            if (this.large) {
                this.body.maxWidth((int) this.width());
                this.body.setPos(this.x + (this.width() - this.body.width()) / 2.0F, topY);
            } else {
                ++topY;
                this.body.maxWidth((int) this.width());
                this.body.setPos(this.x, topY);
            }

            topY += this.body.height();
            this.height = Math.max(this.height, topY - this.top());
        }
    }
}
