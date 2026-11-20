package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ItemBuff;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndCardTree;
import com.watabou.noosa.Image;

public class CardSelectorBuff extends ItemBuff implements ActionIndicator.Action {
    {
        item = new CardSelector();
    }
    private CardSelector selector() {
        assert item != null;
        return (CardSelector) item;
    }

    @Override
    public void fx( boolean on ) {
        if (on)
            ActionIndicator.setAction(this);
        else
            ActionIndicator.clearAction(this);
    }
    @Override
    public String actionName() {
        return selector().name();
    }
    @Override
    public Image actionIcon() {
        //常驻动作按钮使用 hero_icons 中的卡牌图标，而非物品贴图占位（变色核心）
        return selector().customIcon();
    }

    @Override
    public void doAction() {
        GameScene.show(new WndCardTree(selector()));
    }

}
