package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.DMR;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
public class AN94 extends DesignatedMarksmanRifle {
    {
        image = ItemSpriteSheet.AN94;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 0.5f;

        tier = 4;
        tag = Tag.AR;
        RCH = 3;
        dmgBaseDiffer = -1F;
        dmgUpgradeDiffer = -0.5F;
        enchantChance = 4F;
    }
    @Override
    public int reach(Char owner){
        int reach = super.reach(owner);
        if (hasEnchant(Projecting.class, owner))
            reach++;
        return reach;
    }
    @Override
    public int proc(Char attacker, Char defender, int damage ) {
        damage = enchantmentProc(attacker, defender, damage);
        return super.proc(attacker, defender, damage);
    }
}