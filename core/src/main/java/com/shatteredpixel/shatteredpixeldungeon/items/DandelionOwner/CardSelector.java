package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.canScrollRedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndWithCanScrollButton;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class CardSelector extends Item {
    {
        image = ItemSprite.itemSpriteNeedDraw;
        unique = true;
        defaultAction = AC_CHOOSE;
    }
    public int duration;
    public int curCardNum;
    public ArrayList<FirstCard> FirstCards      = new ArrayList<>();
    public ArrayList<CommonCard> CommonCards    = new ArrayList<>();
    public ArrayList<RareCard> RareCards        = new ArrayList<>();
    public ArrayList<FinalCard> FinalCards      = new ArrayList<>();
    public ArrayList<Card> curCards   = new ArrayList<>();
    public ArrayList<Card> failureCards   = new ArrayList<>();
    public enum actionsList {
        SELECT_CARD, CHECK_CARD, ADD_CARD, DEBUG;
        public boolean addAction( CardSelector selector ) {
            switch (this) {
                case SELECT_CARD:
                    return selector.curCardNum < 8 && selector.coolDownLeft <= 0
                            || !selector.curCards.isEmpty();
                case CHECK_CARD:
                    return !selector.FirstCards.isEmpty();
                case ADD_CARD:
                    return true;
                case DEBUG:
                    return Dungeon.isChallenged(Challenges.TEST_MODE);
                default: return false;
            }
        }
        public void doAction( CardSelector selector ){
            switch (this) {
                case SELECT_CARD:
                    if (selector.curCards.isEmpty())
                        Card.random(selector);
                    selector.selectCards();
                    return;
                case CHECK_CARD:
                    selector.checkCards();
                    return;
                case ADD_CARD:
                    GameScene.selectItem(WeaponToCard.weaponSelector);
                    return;
                case DEBUG:
                    selector.debug();
            }
        }
        @Override
        public String toString() {
            return Messages.get(CardSelector.class, "ac_" + this.name());
        }
        public String bodyMessages( CardSelector selector ) {
            return this + ":\n" +Messages.get(CardSelector.class, name() + ".body", selector.cardCD());
        }
        public Image actionImage( CardSelector selector ) {
            switch (this) {
                case SELECT_CARD: return new HeroIcon(HeroIcon.CARD_DRAW_FIRST);
                default: return selector.customIcon();
            }
        }
    }
    private int cardCD() {
        if (duration < 33333)
            return 2000 + curCardNum * 500;
        else
            return 4500;
    }
    public static CardSelector INSTANCE(){
        if (Dungeon.hero() == null)
            return new CardSelector();
        CardSelector selector = Dungeon.hero().belongings.getItem(CardSelector.class);
        if (selector == null)
            selector = new CardSelector();
        return selector;
    }
    public void coolDown( int coolDown ){
        coolDownLeft -= coolDown;
    }
    public int upgradeTime(){
        return duration;
    }
    public boolean hasCard( Card card ){
        if (failureCards.contains(card))
            return false;

        if (card instanceof FirstCard)
            return FirstCards.contains(card);
        else if (card instanceof CommonCard)
            return CommonCards.contains(card);
        else if (card instanceof RareCard)
            return RareCards.contains(card);
        else if (card instanceof FinalCard)
            return FinalCards.contains(card);

        return false;
    }
    public boolean contain( Card card ){
        return hasCard(card) || curCards.contains(card) || failureCards.contains(card);
    }
    public void destroyCard(Card card ){
        failureCards.add(card);
    }
    @Override
    public String desc(){
        return Messages.get(this, "desc", coolDownLeft, curCardNum, duration);
    }
    @Override
    public ArrayList<String> actions( Hero hero ){
        ArrayList<String> actions = super.actions(hero);
        for (actionsList a : actionsList.values())
            if (a.addAction(this))
                actions.add(a.name());
        return actions;
    }
    @Override
    public void execute( Hero hero, String action ) {
        for (actionsList a : actionsList.values()) {
            if (action.equals(a.name())) {
                a.doAction(this);
                return;
            }
        }
        super.execute(hero, action);
    }
    @Override
    public void debug(){
        ArrayList<Card> cur = new ArrayList<>(curCards);
        curCards.clear();
        Card.getAllCard(this);
        selectCards();
        curCards = cur;
    }
    protected CoolDownTracker coolDownTracker;
    @Override
    public void Tracker( Char owner ){
        super.Tracker(owner);
        if (coolDownTracker == null){
            coolDownTracker = new CoolDownTracker();
            coolDownTracker.attachTo(owner);
        }
    }
    @Override
    public void stopTrack(){
        super.stopTrack();
        if (coolDownTracker != null){
            coolDownTracker.detach();
        }
    }
    private static WndWithCanScrollButton INSTANCE = null;
    private static WndOptions selecting = null;
    private void selectCards(){
        ArrayList<canScrollRedButton> buttons = new ArrayList<>();
        for (Card c : curCards)
            buttons.add(new canScrollRedButton(c.colorTitle()){
                @Override
                public void onClick(){
                    super.onClick();
                    if (selecting != null)
                        selecting.hide();
                    GirlsFrontlinePixelDungeon.scene().addToFront(
                            selecting = new WndOptions(c.colorTitle(), c.info(),
                                    Messages.get(CardSelector.class, "Entry"),
                                    Messages.get(CardSelector.class, "Cancel")){
                                @Override
                                protected void onSelect( int index ) {
                                    if (index == 0){
                                        if (c instanceof FirstCard)
                                            FirstCards.add((FirstCard) c);
                                        else if (c instanceof CommonCard)
                                            CommonCards.add((CommonCard) c);
                                        else if (c instanceof RareCard)
                                            RareCards.add((RareCard) c);
                                        else if (c instanceof FinalCard)
                                            FinalCards.add((FinalCard) c);
                                        curCardNum++;
                                        if (duration >= 33333)
                                            coolDownLeft += 4500;
                                        else if (curCardNum < 8)
                                            coolDownLeft += 1500 + 500 * curCardNum;
                                        curCards.clear();
                                        if (INSTANCE != null) {
                                            INSTANCE.hide();
                                            INSTANCE = null;
                                        }
                                        c.onSelect();
                                        hide();
                                        updateQuickslot();
                                    }
                                    else if (index == 1)
                                        hide();
                                }
                        }
                    );
                }
            });
        INSTANCE = new WndWithCanScrollButton(buttons);
        GirlsFrontlinePixelDungeon.scene().addToFront(INSTANCE);
    }
    private void checkCards(){
        ArrayList<canScrollRedButton> buttons = new ArrayList<>();
        addCheckCardsBtn(FirstCards, buttons);
        addCheckCardsBtn(CommonCards, buttons);
        addCheckCardsBtn(RareCards, buttons);
        addCheckCardsBtn(FinalCards, buttons);
        INSTANCE = new WndWithCanScrollButton(buttons);
        GirlsFrontlinePixelDungeon.scene().addToFront(INSTANCE);
    }
    private void addCheckCardsBtn(ArrayList<? extends Card> list, ArrayList<canScrollRedButton> buttons){
        for (Card c : list)
            buttons.add(new canScrollRedButton(c.colorTitle()){
                @Override
                public void onClick(){
                    super.onClick();
                    GirlsFrontlinePixelDungeon.scene().addToFront(
                            new WndOptions(c.colorTitle(), c.info()));
                }
            });
    }
    enum storeString{
        duration_CoolDown, CoolDownLeft_Card, CurCardNum,
        First, Common, Rare, Final, cur, failure;
        private static final String cardNum = "_CardsNum_";
        private static final String cardName = "Cards_Name";
        private static final String cardClass = "Cards_CLASS_";
        private static final String cardLength = "Cards_Length";
        public void store( Bundle bundle, ArrayList<? extends Card> list ){
            Bundle b = new Bundle();
            for (int i = 0; i < list.size(); i++) {
                assert list.get(i) != null;
                b.put(name() + cardClass + i, list.get(i).getClass());
                b.put(name() + cardNum + i, (Enum<?>) list.get(i));
            }
            b.put( name() + cardLength, list.size() );
            bundle.put( name() + cardName, b );
        }
        @SuppressWarnings("unchecked")
        public <T extends Card> ArrayList<T> restore( Bundle bundle, Class<T> ignored ){
            ArrayList<T> list = new ArrayList<>();
            Bundle b = bundle.getBundle( name() + cardName );
            int size = b.getInt( name() + cardLength );

            for (int i = 0; i < size; i++)
                list.add((T) b.getEnum( name() + cardNum + i, b.getClass(name() + cardClass + i)));

            return list;
        }
    }
    @Override
    public void storeInBundle( Bundle bundle ){
        super.storeInBundle(bundle);
        bundle.put(storeString.duration_CoolDown.name(), duration);
        bundle.put(storeString.CoolDownLeft_Card.name(), coolDownLeft);
        bundle.put(storeString.CurCardNum.name(), curCardNum);
        storeString.First.store(bundle, FirstCards);
        storeString.Common.store(bundle, CommonCards);
        storeString.Rare.store(bundle, RareCards);
        storeString.Final.store(bundle, FinalCards);
        storeString.cur.store(bundle, curCards);
        storeString.failure.store(bundle, failureCards);
    }
    @Override
    public void restoreFromBundle( Bundle bundle ){
        super.restoreFromBundle(bundle);
        duration = bundle.getInt(storeString.duration_CoolDown.name());
        coolDownLeft    = bundle.getInt(storeString.CoolDownLeft_Card.name());
        curCardNum      = bundle.getInt(storeString.CurCardNum.name());
        FirstCards      = storeString.First.restore(bundle, FirstCard.class);
        CommonCards     = storeString.Common.restore(bundle, CommonCard.class);
        RareCards       = storeString.Rare.restore(bundle, RareCard.class);
        FinalCards      = storeString.Final.restore(bundle, FinalCard.class);
        curCards        = storeString.cur.restore(bundle, Card.class);
        failureCards    = storeString.failure.restore(bundle, Card.class);
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    //丹德莱抽卡器的常驻图标（hero_icons 中第 27 格的“?”卡牌）
    @Override
    public Image customIcon() {
        return new HeroIcon(HeroIcon.CARD_DRAW_RANDOM);
    }
    public class CoolDownTracker extends Buff{
        {
            revivePersists = true;
        }
        @Override
        public void detach() {
            super.detach();
            coolDownTracker = null;
        }
        @Override
        public boolean act() {
            LockedFloor locked = target.buff(LockedFloor.class);
            boolean lock = locked != null && !locked.regenOn();
            if (coolDownLeft > 0 && !lock)
                coolDownLeft--;
            updateQuickslot();
            duration++;
            if (duration % 33333 == 0) {
                int i = curCards.isEmpty() ? 0 : -1;
                if (curCardNum - i < 8)
                    curCardNum = i;
            }
            if (duration % 500 == 0)
                CardAffect.halfKilo(lock);
            if (duration % 1000 == 0)
                CardAffect.kiloTimes(lock);
            spend(TICK);
            return true;
        }
    }
}
