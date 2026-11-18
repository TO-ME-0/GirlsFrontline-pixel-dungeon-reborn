package com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * 查种器错误日志窗口，仿照 CrashReportScene 的列表 + 详情结构。
 * 由 SeedFindScene 第一页的入口按钮打开。
 */
public class WndSeedFinderErrors extends Window {

    private static final int GAP = 4;
    private static final float ROW_H = 22;

    private final int winW;
    private final Component content;
    private final ScrollPane list;
    private final RedButton clearBtn;
    private final ArrayList<ErrorRow> rows = new ArrayList<>();

    public WndSeedFinderErrors() {
        super();

        winW = PixelScene.landscape() ? WndJournal.WIDTH_L : WndJournal.WIDTH_P;
        int winH = PixelScene.landscape() ? WndJournal.HEIGHT_L : WndJournal.HEIGHT_P;
        resize(winW, winH);

        IconTitle titlebar = new IconTitle(Icons.get(Icons.WARNING), "查种错误记录");
        titlebar.color(0xFFCC00);
        titlebar.setRect(0, 0, winW, 0);
        add(titlebar);

        content = new Component();
        list = new ScrollPane(content) {
            @Override
            public void onClick(float x, float y) {
                for (ErrorRow row : rows) {
                    if (row.inside(x, y)) {
                        GirlsFrontlinePixelDungeon.scene().addToFront(new WndDetail(row.entry));
                        return;
                    }
                }
            }
        };
        add(list);

        clearBtn = new RedButton("清空记录", 6) {
            @Override
            protected void onClick() {
                confirmClear();
            }
        };
        clearBtn.setSize(44, 16);
        clearBtn.setPos(GAP, winH - 16 - GAP);
        add(clearBtn);

        RedButton closeBtn = new RedButton("关闭", 6) {
            @Override
            protected void onClick() {
                hide();
            }
        };
        closeBtn.setSize(40, 16);
        closeBtn.setPos(winW - 40 - GAP, winH - 16 - GAP);
        add(closeBtn);

        list.setRect(GAP, titlebar.bottom() + GAP,
                winW - GAP * 2,
                clearBtn.top() - titlebar.bottom() - GAP * 2);

        rebuild();
    }

    private void rebuild() {
        content.clear();
        rows.clear();

        float rowW = winW - GAP * 2 - 6;
        ArrayList<SeedFinderErrorLog.Entry> entries = SeedFinderErrorLog.list();
        if (entries.isEmpty()) {
            RenderedTextBlock empty = PixelScene.renderTextBlock("暂无查种错误记录", 7);
            empty.hardlight(0xCCCCCC);
            content.add(empty);
            empty.setPos(GAP, GAP);
            content.setSize(rowW, empty.height() + GAP * 2);
        } else {
            float y = 0;
            for (SeedFinderErrorLog.Entry entry : entries) {
                ErrorRow row = new ErrorRow(entry);
                row.setRect(0, y, rowW, ROW_H);
                content.add(row);
                rows.add(row);
                y += ROW_H + 2;
            }
            content.setSize(rowW, y);
        }

        list.scrollTo(0, 0);
        clearBtn.enable(!entries.isEmpty());
    }

    private void confirmClear() {
        if (rows.isEmpty()) {
            return;
        }
        GirlsFrontlinePixelDungeon.scene().addToFront(new WndOptions(
                Icons.get(Icons.WARNING),
                "清空错误记录",
                "确定清空全部查种错误记录吗？",
                "确定", "取消") {
            @Override
            protected void onSelect(int index) {
                if (index == 0) {
                    SeedFinderErrorLog.clear();
                    rebuild();
                }
            }
        });
    }

    private final class ErrorRow extends Component {
        private final SeedFinderErrorLog.Entry entry;
        private final NinePatch bg;
        private final Image icon;
        private final RenderedTextBlock title;
        private final RenderedTextBlock time;

        ErrorRow(SeedFinderErrorLog.Entry entry) {
            this.entry = entry;

            bg = Chrome.get(Chrome.Type.WINDOW_SILVER);
            add(bg);

            icon = Icons.get(Icons.WARNING);
            icon.hardlight(0xFFCC00);
            add(icon);

            title = PixelScene.renderTextBlock("查种错误", 6);
            title.hardlight(0xFFFFFF);
            add(title);

            String text = entry.time.isEmpty() ? entry.fileName : entry.time;
            time = PixelScene.renderTextBlock(text, 6);
            time.hardlight(0xCCCCCC);
            add(time);
        }

        @Override
        protected void layout() {
            icon.x = x + GAP;
            icon.y = y + (height - icon.height()) / 2f;
            PixelScene.align(icon);

            title.setPos(icon.x + icon.width + GAP, y + GAP);
            time.maxWidth((int) Math.max(1, width - (icon.width() + GAP * 3)));
            time.setPos(title.left(), title.bottom() + 2);

            height = Math.max(ROW_H, time.bottom() - y + GAP);

            bg.x = x;
            bg.y = y;
            bg.size(width, height);
        }
    }

    private final class WndDetail extends Window {
        WndDetail(final SeedFinderErrorLog.Entry entry) {
            super();

            int w = PixelScene.landscape() ? WndJournal.WIDTH_L : WndJournal.WIDTH_P;
            int h = PixelScene.landscape() ? WndJournal.HEIGHT_L : WndJournal.HEIGHT_P;
            resize(w, h);

            IconTitle titlebar = new IconTitle(Icons.get(Icons.WARNING), "错误详情");
            titlebar.color(0xFFCC00);
            titlebar.setRect(0, 0, w, 0);
            add(titlebar);

            Component content = new Component();
            ScrollPane list = new ScrollPane(content);
            add(list);

            RenderedTextBlock text = PixelScene.renderTextBlock(entry.content, 6);
            text.setHightlighting(false);
            text.maxWidth(w - GAP * 3);
            text.setPos(GAP, 0);
            content.add(text);
            content.setSize(w - GAP * 2, text.height() + GAP);

            float by = h - 16 - GAP;
            list.setRect(GAP, titlebar.bottom() + GAP,
                    w - GAP * 2,
                    by - titlebar.bottom() - GAP * 2);

            RedButton copyBtn = new RedButton("复制", 6) {
                @Override
                protected void onClick() {
                    Gdx.app.getClipboard().setContents(entry.content);
                    GirlsFrontlinePixelDungeon.scene().addToFront(
                            new WndMessage("已复制到剪贴板"));
                }
            };
            copyBtn.setSize(36, 16);
            copyBtn.setPos(GAP, by);
            add(copyBtn);

            RedButton deleteBtn = new RedButton("删除", 6) {
                @Override
                protected void onClick() {
                    GirlsFrontlinePixelDungeon.scene().addToFront(new WndOptions(
                            Icons.get(Icons.WARNING),
                            "删除错误记录",
                            "确定删除这条错误记录吗？",
                            "确定", "取消") {
                        @Override
                        protected void onSelect(int index) {
                            if (index == 0) {
                                SeedFinderErrorLog.delete(entry.fileName);
                                WndDetail.this.hide();
                                rebuild();
                            }
                        }
                    });
                }
            };
            deleteBtn.setSize(36, 16);
            deleteBtn.setPos(copyBtn.right() + GAP, by);
            add(deleteBtn);

            RedButton closeBtn = new RedButton("关闭", 6) {
                @Override
                protected void onClick() {
                    hide();
                }
            };
            closeBtn.setSize(36, 16);
            closeBtn.setPos(w - 36 - GAP, by);
            add(closeBtn);
        }
    }
}
