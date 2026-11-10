package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.RabbitBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Music;

public class JournalScene extends PixelScene {
	@Override
	public void create() {
		super.create();
		if (Dungeon.hero() == null)
			Dungeon.hero(new Hero());
		if (Dungeon.level == null){
			Dungeon.level = new RabbitBossLevel();
			Dungeon.level.create(0, 0);
		}
		new GameScene().SimplyCreate();

		Music.INSTANCE.play(Assets.Music.THEME_1,true);

		int w = Camera.main.width;
		int h = Camera.main.height;

		Archs archs = new Archs();
		archs.setSize( w, h );
		add( archs );

		WndJournal.TitleScene = true;
		GirlsFrontlinePixelDungeon.scene().addToFront( new WndJournal(){
			@Override
			public void onBackPressed() {
				//Do nothing
			}
		});
		ExitButton exitBtn = new ExitButton() {
			@Override
			public void onClick() {
				GirlsFrontlinePixelDungeon.switchNoFade(TitleScene.class);
				GameScene.scene = null;
			}
		};
		exitBtn.setPos((float) Camera.main.width - exitBtn.width(), 0);
		this.add(exitBtn);
	}

	@Override
	protected void onBackPressed() {
		//Do nothing
	}
}