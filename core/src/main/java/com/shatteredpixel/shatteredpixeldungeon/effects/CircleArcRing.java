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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.Visual;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * 空心圆环状的圆弧，用于绘制头像右下角的经验指示环。
 * 从正上方（12 点方向）开始，顺时针填充；sweep 为 0~1 的填充比例。
 */
public class CircleArcRing extends Visual {
	
	private SmartTexture texture;
	
	private FloatBuffer vertices;
	private ShortBuffer indices;
	
	private final int nTris;
	private final float inner;
	private final float outer;
	
	//0f 为空环，1f 为完整圆环
	private float sweep = 0f;
	private boolean dirty = true;
	
	public CircleArcRing( int triangles, float innerRadius, float outerRadius ) {
		
		super( 0, 0, 0, 0 );
		
		texture = TextureCache.createSolid( 0xFFFFFFFF );
		
		this.nTris = triangles;
		this.inner = innerRadius;
		this.outer = outerRadius;
		
		vertices = ByteBuffer.
				allocateDirect( (nTris + 1) * 2 * 4 * (Float.SIZE / 8) ).
				order( ByteOrder.nativeOrder() ).
				asFloatBuffer();
		
		indices = ByteBuffer.
				allocateDirect( nTris * 6 * Short.SIZE / 8 ).
				order( ByteOrder.nativeOrder() ).
				asShortBuffer();
		
		updateTriangles();
	}
	
	public CircleArcRing ringColor( int color ) {
		hardlight( color );
		return this;
	}
	
	public void setSweep( float sweep ) {
		if (this.sweep != sweep) {
			this.sweep = sweep;
			dirty = true;
		}
	}
	
	private void updateTriangles() {
		
		dirty = false;
		
		((Buffer)vertices).position( 0 );
		((Buffer)indices).position( 0 );
		
		float v[] = new float[4];
		v[2] = 0.25f;
		v[3] = 0;
		
		//从正上方开始，按顺时针方向展开
		double start = -Math.PI / 2.0;
		
		for (int i = 0; i <= nTris; i++) {
			
			double a = start + Math.PI * 2 * sweep * i / nTris;
			float cos = (float)Math.cos( a );
			float sin = (float)Math.sin( a );
			
			v[0] = cos * inner;
			v[1] = sin * inner;
			vertices.put( v );
			
			v[0] = cos * outer;
			v[1] = sin * outer;
			vertices.put( v );
		}
		
		for (int i = 0; i < nTris; i++) {
			
			short base = (short)(i * 2);
			
			indices.put( base );
			indices.put( (short)(base + 1) );
			indices.put( (short)(base + 2) );
			
			indices.put( (short)(base + 2) );
			indices.put( (short)(base + 1) );
			indices.put( (short)(base + 3) );
		}
		
		((Buffer)indices).position( 0 );
	}
	
	@Override
	public void draw() {
		
		super.draw();
		
		if (dirty) {
			updateTriangles();
		}
		
		if (sweep <= 0) {
			return;
		}
		
		Blending.setNormalMode();
		
		NoosaScript script = NoosaScript.get();
		
		texture.bind();
		
		script.uModel.valueM4( matrix );
		script.lighting(
				rm, gm, bm, am,
				ra, ga, ba, aa );
		
		script.camera( camera() );
		script.drawElements( vertices, indices, nTris * 6 );
	}
}
