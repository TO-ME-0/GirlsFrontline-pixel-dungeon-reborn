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
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.AutoIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.IdentifyKnowledge;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CustomNoteButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.WndTextInput;

public class WndInfoItem extends Window {
	
	private static final float GAP	= 2;

	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_MAX = 220;
    protected IconButton noteButton;

	//only one WndInfoItem can appear at a time
	private static WndInfoItem INSTANCE;
    public Item item = null;
    public Heap heap = null;

	public WndInfoItem( Heap heap ) {

		super();

		if (INSTANCE != null){
			INSTANCE.hide();
		}
		INSTANCE = this;

        item = null;
        this.heap = heap;

		if (heap.type == Heap.Type.HEAP && heap.room == Heap.Room.NONE)
			fillFields( heap.peek() );
        else
			fillFields( heap );
	}
	
	public WndInfoItem( Item item ) {
		super();

		if (INSTANCE != null){
			INSTANCE.hide();
		}
		INSTANCE = this;

        heap = null;
        this.item = item;

        fillFields( item );
	}

	@Override
	public void hide() {
		super.hide();
		if (INSTANCE == this){
			INSTANCE = null;
		}
	}

	private void fillFields(Heap heap ) {
		
		IconTitle titlebar = new IconTitle( heap );
		titlebar.color( TITLE_COLOR );
		
		RenderedTextBlock txtInfo = PixelScene.renderTextBlock( heap.info(), 6 );

        if(heap.type == Heap.Type.FOR_SALE) {
            noteButton = new ItemNote(heap.peek(), this);
            noteButton.enable(heap.peek().canNote);
            noteButton.visible = heap.peek().canNote;
            layoutFields(titlebar, txtInfo, noteButton);
        }
        else
            layoutFields(titlebar, txtInfo);
	}

	//统一布局：右上角依次排开 0~2 个按钮（每个 16px）
	private void layoutFields(IconTitle title, RenderedTextBlock info, IconButton... buttons){
		int width = WIDTH_MIN;

		info.maxWidth(width);

		//window can go out of the screen on landscape, so widen it as appropriate
		while (PixelScene.landscape()
				&& info.height() > 100
				&& width < WIDTH_MAX){
			width += 20;
			info.maxWidth(width);
		}

		int btnCount = 0;
		for (IconButton b : buttons) {
			if (b != null) btnCount++;
		}

		title.setRect( 0, 0, width - 16*btnCount, 0 );
		add( title );

		info.setPos(title.left(), title.bottom() + GAP);
		add( info );

		int btnX = width - 16*btnCount;
		for (IconButton b : buttons) {
			if (b == null) continue;
			b.setRect(btnX, 0, 16, 16);
			add(b);
			btnX += 16;
		}

		resize( width, (int)(info.bottom() + 2) );
	}
	private void fillFields( Item item ) {
        this.item = item;

		int color = TITLE_COLOR;
		if (item.levelKnown && item.level() > 0) {
			color = ItemSlot.UPGRADED;
		} else if (item.levelKnown && item.level() < 0) {
			color = ItemSlot.DEGRADED;
		}

		IconTitle titlebar = new IconTitle( item );
		titlebar.color( color );

		RenderedTextBlock txtInfo = PixelScene.renderTextBlock( item.info(), 6 );
        noteButton = new ItemNote(item, this);
        noteButton.enable(item.canNote);
        noteButton.visible=item.canNote;

		//猜测按钮：仅“未鉴定 + 可推断 + 在背包内”时显示
		IconButton guessButton = null;
		if (!item.isIdentified()
				&& AutoIdentify.canGuess(item)
				&& Dungeon.hero() != null
				&& Dungeon.hero().belongings.contains(item)) {
			guessButton = new GuessButton(item);
		}

		layoutFields(titlebar, txtInfo, noteButton, guessButton);
	}

	//猜测按钮：点击后对该物品的标签做约束求解
	public class GuessButton extends IconButton {
		private final Item item;

		public GuessButton(Item item){
			super(Icons.INFO.get());
			this.item = item;
		}

		@Override
		protected void onClick() {
			IdentifyKnowledge.SolveOutcome out = AutoIdentify.guess(item);
			if (out == null) return;
			if (out.status == IdentifyKnowledge.Status.POSITIVE){
				//正面命中：刷新窗口，名称与描述变为“? 真名”
				WndInfoItem.this.hide();
				GameScene.show(new WndInfoItem(item));
			} else {
				//排除/证据不足：展示仍可能的类与已排除的类
				StringBuilder sb = new StringBuilder();
				if (!out.possibleClasses.isEmpty()){
					sb.append("仍可能：").append(classNames(out.possibleClasses));
				}
				if (!out.excludedClasses.isEmpty()){
					if (sb.length() > 0) sb.append('\n');
					sb.append("已排除：").append(classNames(out.excludedClasses));
				}
				if (out.cause != null && !out.cause.isEmpty()){
					if (sb.length() > 0) sb.append('\n');
					sb.append(out.cause);
				}
				if (sb.length() == 0) sb.append("证据不足，无法推断。");
				GameScene.show(new WndTitledMessage(Icons.get(Icons.INFO), item.name(), sb.toString()));
			}
		}

		private String classNames(java.util.LinkedHashSet<Class<? extends Item>> set){
			StringBuilder sb = new StringBuilder();
			for (Class<? extends Item> c : set){
				if (sb.length() > 0) sb.append("、");
				sb.append(Messages.get(c, "name"));
			}
			return sb.toString();
		}
	}
    public static class ItemNote extends IconButton {
        Item item;
        Window parentWnd;

        public ItemNote(Item item, Window parentWnd){
            super(Icons.RENAME_ON.get());
            this.item = item;
            this.parentWnd = parentWnd;
        }
        @Override
        protected void onClick() {
            customNote();
        }
        private void customNote(){
            Notes.CustomRecord note;
            if (item.stackable)
                note = Notes.findCustomRecord(item);
            else
                note = Notes.findCustomRecord(item.customNoteID);
            if (note == null){
                if (item.stackable)
                    note = new Notes.CustomRecord(item.getClass(), "", "");
                else
                    note = new Notes.CustomRecord(item, "", "");

                addNote(parentWnd, note, Messages.get(CustomNoteButton.class, "new_inv"),
                        Messages.get(CustomNoteButton.class, "new_item_title", Messages.titleCase(item.name())), item);
            }
            else {
                GameScene.show(new CustomNoteButton.CustomNoteWindow(note, parentWnd));
            }
        }
        private static void addNote(Window parentWindow, Notes.CustomRecord note, String promptTitle, String promptText, Item curItem){
            GameScene.show(new WndTextInput(promptTitle,
                    promptText,
                    "",
                    50,
                    false,
                    Messages.get(CustomNoteButton.CustomNoteWindow.class, "confirm"),
                    Messages.get(CustomNoteButton.CustomNoteWindow.class, "cancel")){
                @Override
                public void onSelect(boolean positive, String text) {
                    if (positive && !text.isEmpty()){
                        note.assignID();
                        curItem.customNoteID = note.ID();
                        note.editText(text, "");
                        Notes.add(note);
                        hide();
                        resetParentWindow(parentWindow);
                    }
                }
            });
        }
    }
    public static Window resetParentWindow(Window parentWindow){
        Window newParent = parentWindow;
        if (parentWindow != null){
            if (parentWindow.getClass() == WndInfoItem.class) {
                if (((WndInfoItem) parentWindow).item != null)
                    newParent = new WndInfoItem(((WndInfoItem) parentWindow).item);
                else if (((WndInfoItem) parentWindow).heap != null)
                    newParent = new WndInfoItem(((WndInfoItem) parentWindow).heap);
            }
            else if (parentWindow.getClass() == WndUseItem.class)
                newParent = new WndUseItem(((WndUseItem) parentWindow).owner, ((WndUseItem) parentWindow).item);
            else if (parentWindow.getClass() == WndSadGhost.RewardWindow.class)
                newParent = WndSadGhost.INSTANCE. new RewardWindow(((WndSadGhost.RewardWindow) parentWindow).item);
            else if (parentWindow.getClass() == WndWandmaker.RewardWindow.class)
                newParent = WndWandmaker.INSTANCE. new RewardWindow(((WndWandmaker.RewardWindow) parentWindow).item);
            else if (parentWindow.getClass() == WndJournal.class)
                newParent = new WndJournal();
            else if (parentWindow.getClass() == WndTradeItem.class)
                newParent = new WndTradeItem(((WndTradeItem) parentWindow).heap);
            if (newParent != parentWindow){
                parentWindow.hide();
                if (GirlsFrontlinePixelDungeon.scene() instanceof GameScene)
                    GameScene.show(newParent);
                else
                    GirlsFrontlinePixelDungeon.scene().addToFront(newParent);
            }
        }
        return newParent;
    }
}
