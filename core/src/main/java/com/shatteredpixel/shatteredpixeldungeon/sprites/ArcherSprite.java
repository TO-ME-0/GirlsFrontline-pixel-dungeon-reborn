package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

public class ArcherSprite extends MobSprite {

	private int cellToAttack;

	public ArcherSprite() {
		super();

		texture( Assets.Sprites.KENTAUROS );

		TextureFilm frames = new TextureFilm( texture, 24, 23 );

		idle = new Animation( 12, true );
		idle.frames( frames, 0, 1, 2, 3 );

		run = new Animation( 8, true );
		run.frames( frames, 8, 9, 10, 11, 12, 13 );

		attack = new Animation( 12, false );
		attack.frames( frames, 4, 5, 6, 7 );

		zap = attack.clone();

		die = new Animation( 8, false );
		die.frames( frames, 14, 15, 16, 17 );

		play( idle );
	}

	@Override
	public int blood() {
		return 0xFF44FF22;
	}

	@Override
	public void attack( int cell ) {
		if (!Dungeon.level.adjacent( cell, ch.pos )) {

			cellToAttack = cell;
			turnTo( ch.pos , cell );
			play( zap );

		} else {

			super.attack( cell );

		}
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();

			((MissileSprite)parent.recycle( MissileSprite.class )).
			reset( this, cellToAttack, new ArcherShot(), new Callback() {
				@Override
				public void call() {
					ch.onAttackComplete();
				}
			} );
		} else {
			super.onComplete( anim );
		}
	}

	public class ArcherShot extends Item {
		{
			image = ItemSpriteSheet.FISHING_SPEAR;
		}
	}
}
