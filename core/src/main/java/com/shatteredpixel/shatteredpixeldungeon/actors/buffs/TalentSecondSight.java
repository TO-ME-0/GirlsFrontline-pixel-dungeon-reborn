/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2020 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//A buff whose only purposes is to keep track of a count of some form
public class TalentSecondSight extends Buff {
    {
        revivePersists = true;
    }

    //使用CopyOnWriteArrayList：渲染线程（BuffIndicator刷新图标/描述）会遍历sights，
    //而Actor线程（天赋触发Set/EndCD）可能同时add，普通ArrayList会触发ConcurrentModificationException
    private List<curLevelSight> sights = new CopyOnWriteArrayList<>();

    @Override
    public boolean attachTo( Char target ){
        if (sights == null)
            sights = new CopyOnWriteArrayList<>();
        return super.attachTo(target);
    }
    @Override
    public String toString() {
        return Messages.get(this, "name");
    }
    public String desc() {
        return Messages.get(this, "desc", descA());
    }
    public int icon() {
        boolean on = false;
        for (curLevelSight sight : sights){
            if (nearbyLevel(sight.level)){
                on = true;
                break;
            }
        }
        if (on){
            return BuffIndicator.MIND_VISION;
        }else
            return super.icon();
    }

    private static boolean nearbyLevel(int level){
        if (level == 0)
            return false;
        int depth = level % 1000;
        return GameMath.gate(Dungeon.cur().depth-2,
                depth,
                Dungeon.cur().depth+2) == depth;
    }
    public void tintIcon(Image icon) {
        icon.hardlight(1F, 2F, 3F);
    }
    private String descA(){
        StringBuilder desc = new StringBuilder();
        for (curLevelSight sight : sights){
            if (!nearbyLevel(sight.level))
                continue;
            if (sight.time <= 0)
                continue;
            if (desc.length() > 0){
                desc.append("\n");
            }
            int sub = sight.level/1000;
            String level = String.valueOf(sight.level%1000);
            if (sub != 0)
                level += "/" + sub;
            desc.append("楼层").append(level).append("仍需：").append(sight.time).append(" 回合");
        }
        return desc.toString();
    }
    public void Set(int id, int cd){
        for (curLevelSight sight : sights){
            if (sight.level == id) {
                sight.time = cd;
                return;
            }
        }
        sights.add(new curLevelSight(id, cd));
    }
    public boolean EndCD(int id){
        for (curLevelSight sight : sights){
            if (sight.level == id) {
                return sight.time <= 0;
            }
        }
        sights.add(new curLevelSight(id, 0));
        return true;
    }
    @Override
    public boolean act() {
        for (curLevelSight sight: sights){
            if (sight.level == Dungeon.curFloorCode()){
                if (sight.time > 0)
                    sight.time--;
                break;
            }
        }

        spend(1);
        return true;
    }

    private static final String SIGHT   =   "sight_new";
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
        if (sights != null)
            bundle.put(SIGHT, sights);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

        if (bundle.contains(SIGHT)){
            ArrayList<curLevelSight> loaded = bundle.getArrayList(SIGHT, curLevelSight.class);
            sights = loaded != null ? new CopyOnWriteArrayList<>(loaded) : new CopyOnWriteArrayList<>();
        }
        else if (bundle.contains("ID")){
            int[] IDToLoad = bundle.getIntArray("ID");
            int[] CDToLoad = bundle.getIntArray("CD");
            if (IDToLoad != null) {
                for (int i = 0; i < IDToLoad.length; i++) {
                    if (i >= CDToLoad.length)
                        break;
                    sights.add(new curLevelSight(IDToLoad[i], CDToLoad[i]));
                }
            }
        }
        else
            sights = new CopyOnWriteArrayList<>();

	}
    public static class curLevelSight implements Bundlable{

        private int level;
        private int time;
        //Bundle 反射反序列化必须存在无参构造
        public curLevelSight(){
        }
        public curLevelSight(int level, int time){
            this.level  = level;
            this.time   = time;
        }
        private static final String ID = "sight_level";
        private static final String CD = "sight_time";
        @Override
        public void restoreFromBundle(Bundle bundle) {
            level = bundle.getInt(ID);
            time = bundle.getInt(CD);
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            bundle.put(ID, level);
            bundle.put(CD, time);
        }
    }
}
