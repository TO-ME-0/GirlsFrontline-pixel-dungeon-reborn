package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

/**
 * 标记接口：表示该伤害来源来自怪物本身。
 *
 * 经验结算使用：怪物(MobDamage)直接造成的伤害不视为玩家方参与，
 * 其余来源（环境、陷阱、物品、Buff、友方等）造成的伤害都视为玩家方参与。
 */
public interface MobDamage {
}
