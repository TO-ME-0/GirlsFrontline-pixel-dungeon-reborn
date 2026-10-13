package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MG;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * Created by LoveKirsi on 2017-11-22.
 */

public class Mg42 extends MachineGun {

    {
        image = ItemSpriteSheet.MG42;

        tier = 6;
        tag = Tag.MG;
        ACC = 0.65f;
        DLY = 0.1f;
        dmgBaseMul = 1.3F;
        dmgUpgradeMul = 0.4F;
    }

}


