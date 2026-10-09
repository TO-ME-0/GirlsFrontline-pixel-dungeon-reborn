/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.Visual;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class ScrollingGridPane extends ScrollPane {

    private ArrayList<Component> items = new ArrayList<>();
    private ArrayList<ColorBlock> separators = new ArrayList<>();

    private static final int ITEM_SIZE	= 17;
    private static final int MIN_GROUP_SIZE = 3*(ITEM_SIZE+1);
    private final boolean small;
    public ScrollingGridPane(boolean small){
        super(new Component());
        this.small = small;
    }

    public void addItem( ScrollingGridPane.GridItem item ){
        content.add(item);
        items.add(item);
    }

    public void addHeader( String text ){
        addHeader( text, 7, false );
    }

    public void addHeader( String text, int size, boolean center ){
        GridHeader header = new GridHeader(text, size, center);
        content.add(header);
        items.add(header);
    }

    //带点击行为的标题，点击区仅覆盖文字本身，不吞掉整行的滚动拖动
    public void addHeader( String text, Runnable onClick ){
        GridHeader header = new GridHeader(text, 7, false, onClick);
        content.add(header);
        items.add(header);
    }

    @Override
    public synchronized void clear() {
        //content.clear() 只会把子节点从成员表里摘除并置 parent=null，并不会注销子节点内部的
        //PointerArea 监听。配合 Gizmo.camera() 的缓存与 isActive() 在 parent==null 时返回自身
        //active，被摘除的 GridItem 仍能被点中，形成残留的幽灵点击区域（切分类/关窗口后依旧响应）。
        //因此必须在摘除前显式 destroy，真正移除监听并解除状态。
        for (Component item : items) {
            if (item != null) {
                item.destroy();
            }
        }
        content.clear();
        items.clear();
        separators.clear();
    }

    @Override
    protected void layout() {

        float left = 0;
        float top = 0;

        int sepsUsed = 0;

        //these variables help control logic for laying out multiple grid groups on one line
        boolean freshRow = true; //whether the previous group is still on its first row
        boolean lastWasSmallheader = false; //whether the last UI element was a header on its own
        float widthThisGroup = 0; //how wide the current group is (we use a min of 3 items)

        for (int i = 0; i < items.size(); i++){
            Component item = items.get(i);
            if (item instanceof GridHeader){
                //we can sometimes get two smaller headers next to each other if a group has no items in it
                //so we need to treat it as if there were grid items for proper layout
                if (left > 0 || lastWasSmallheader){

                    //this bit of logic exists so that multiple headers can be on one row
                    // if all of their groups have a small number of items, with a min space for 3
                    float spacing = Math.max(0, MIN_GROUP_SIZE - widthThisGroup);
                    float spaceLeft = width() - (left + spacing);
                    int spaceReq = 0;
                    for (int j = i+1; j < items.size(); j++){
                        if (items.get(j) instanceof GridItem){
                            spaceReq += ITEM_SIZE+1;
                        } else {
                            break;
                        }
                    }
                    spaceReq = Math.max(spaceReq, MIN_GROUP_SIZE);
                    if (!((GridHeader) item).center && freshRow && spaceLeft >= spaceReq && small){
                        left = left + spacing;
                        top -= item.height()+1;
                        ColorBlock sep;
                        if (separators.size() > sepsUsed){
                            sep = separators.get(sepsUsed++);
                        } else {
                            sep = new ColorBlock(1, 1, 0xFF222222);
                            separators.add(sep);
                            content.add(sep);
                            sepsUsed++;
                        }
                        sep.size(1, item.height()+1+ITEM_SIZE);
                        sep.x = left-1;
                        sep.y = top;
                    } else {
                        left = 0;
                        top += ITEM_SIZE + 2;
                        freshRow = true;
                    }
                }
                item.setRect(left, top, width(), item.height());
                top += item.height()+1;
                widthThisGroup = 0;

                if (!((GridHeader) item).center){
                    lastWasSmallheader = true;
                } else {
                    lastWasSmallheader = false;
                }

            }
            if (item instanceof GridItem){
                if (left + ITEM_SIZE > width()) {
                    left = 0;
                    widthThisGroup = 0;
                    top += ITEM_SIZE+1;
                    freshRow = false;
                }
                item.setRect(left, top, ITEM_SIZE, ITEM_SIZE);
                left += ITEM_SIZE+1;
                widthThisGroup += ITEM_SIZE+1;
                lastWasSmallheader = false;
            }

        }
        if (left > 0){
            left = 0;
            top += ITEM_SIZE+1;
        }

        while (separators.size() > sepsUsed){
            ColorBlock sep = separators.remove(sepsUsed);
            content.remove(sep);
        }

        content.setSize(width, top);
        super.layout();
    }

    public static class GridItem extends IconButton {

        protected Visual secondIcon;

        protected ColorBlock bg;
        boolean hitColor;
        public GridItem( Image icon ) {
            this(icon, true);
        }
        public GridItem( Image icon, boolean hitColor ) {
            super(icon);
            hotArea.blockLevel = PointerArea.NEVER_BLOCK;
            this.hitColor = hitColor;
        }
        public void addSecondIcon( Visual icon ){
            secondIcon = icon;
            add(secondIcon);
            layout();
        }
        public void hardLightBG( float r, float g, float b ){
            bg.hardlight(r, g, b);
        }
        @Override
        protected void createChildren() {
            super.createChildren();
            bg = new ColorBlock( 1, 1, 0x9953564D);
            add(bg);
        }

        @Override
        protected void onPointerDown() {
            if (hitColor)
                super.onPointerDown();
        }

        @Override
        protected void onPointerUp() {
            if (hitColor)
                super.onPointerUp();
        }

        @Override
        protected void layout() {
            bg.x = x;
            bg.y = y;
            bg.size(width(), height());
            super.layout();

            if (secondIcon != null){
                secondIcon.x = x + width()-secondIcon.width();
                secondIcon.y = y;
            }
        }
    }

    public static class GridHeader extends Component {

        protected RenderedTextBlock text;
        boolean center;
        private PointerArea clickArea; //非空时标题可点击，点击区只覆盖文字
        private final Runnable onClickAction;

        public GridHeader( String text ){
            this(text, 7, false);
        }

        public GridHeader( String text, int size, boolean center ){
            this(text, size, center, null);
        }

        public GridHeader( String text, int size, boolean center, Runnable onClickAction ){
            super();

            this.center = center;
            this.onClickAction = onClickAction;
            this.text = PixelScene.renderTextBlock(text, size);
            add(this.text);

            if (onClickAction != null){
                clickArea = new PointerArea(0, 0, 0, 0){
                    @Override
                    protected void onClick( PointerEvent event ) {
                        if (GridHeader.this.onClickAction != null) {
                            GridHeader.this.onClickAction.run();
                        }
                    }
                };
                add(clickArea);
            }
        }

        @Override
        protected void layout() {
            super.layout();

            if (center){
                text.align(RenderedTextBlock.CENTER_ALIGN);
                text.maxWidth((int)width());
                text.setPos(x + (width() - text.width()) / 2, y+1);
            } else {
                text.maxWidth((int)width());
                text.setPos(x, y+1);
            }

            if (clickArea != null){
                clickArea.x = text.x;
                clickArea.y = text.y;
                clickArea.width = text.width();
                clickArea.height = text.height();
            }
        }

        @Override
        public float height() {
            if (center){
                return text.height() + 3;
            } else {
                return text.height() + 2;
            }
        }
    }

}
