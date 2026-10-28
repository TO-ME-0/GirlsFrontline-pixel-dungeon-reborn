package com.shatteredpixel.shatteredpixeldungeon.ui.changelist.girlpd;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BlacksmithSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeInfo;

import java.util.ArrayList;

//v0.6.X 版本更新日志，内容整理自 0.5.9v2 之后的提交记录
public class v0_6_X_Changes {

    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_0_6_0_Changes(changeInfos);
    }

    public static void add_0_6_0_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.6.0", true,
                "本次更新以 4.0 内容迁移为主：新增小恶魔金库、铁匠矿洞、黑色模仿者与饰品、IOP 外骨骼、HK416、超级小爱等大量内容，并补完了阵营、0 层与国际象棋等系统。");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        //新增
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.SKULL),
                "小恶魔金库任务",
        "_-_ 移植了 4.0 的_有野心的小恶魔_支线：17~19 层会出现 9×9 的专属房间，与_小恶魔_对话后可进入独立的_金库层_。\n"+
        "_-_ 金库由 16 个宝库房、10 个宝藏房、9 种金库怪物和 4 种机关 NPC 组成；击败怪物会掉落_矮人令牌_，用于打开令牌门继续深入。\n"+
        "_-_ 金库采用计分制：带着_小恶魔雕像_撤离可直接获得 4000 分；探索楼层最多 +1000 分，收集令牌与开门最多 +1250 分。\n"+
        "_-_ 使用_逃脱水晶_撤离时结算分数，分数越高可选的奖励越好（共 6 件高级装备备选）；分数超过 2000 会解锁_小恶魔商店_。\n"+
        "_-_ 当前流程已可完整游玩，文案仍是 4.0 旧称、尚未少前化，部分金库怪物暂用替代贴图。\n"));

        changes.addButton(new ChangeButton(new BlacksmithSprite(),
                "铁匠矿洞任务",
        "_-_ 移植了 4.0 的_铁匠矿洞_任务：铁匠会在 12~14 层出现，且出现概率随深度递增。\n"+
        "_-_ 首次对话会直接送你一把_钻石镐_，用它开采本层的_暗金_交给铁匠换取_贡献度_（最多累计 2000 点，击败本层 Boss 额外 +1000）。\n"+
        "_-_ 贡献度可用于：_重铸_两件同类装备（500+1000n）、永久_硬化_附魔/铭文（500+1000n）、_免费强化_一次（1000+1000n）、_打造_随机装备（2000）。\n"+
        "_-_ 贡献度用完后铁匠会停止服务；未领取旧版奖励的存档会自动迁移到新版任务。\n"));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.MIMIC_TOOTH),
                "黑色模仿者与饰品",
        "_-_ 新增_黑色模仿者_（黑檀宝箱怪）：它伪装成宝箱，更隐蔽、战利品更丰厚，是隐藏起来的宝箱怪。\n"+
        "_-_ 同步迁移了 4.0 的_饰品_系统：饰品按等级提供成长性加成，可通过饰品生成器获得，部分饰品会与宝箱怪、子弹戒指等机制联动。\n"+
        "_-_ 新饰物_？地格召唤器_（名称待少前化）会让所有宝箱怪更常见、更难识别，掉落更好，并使每层有概率额外生成一只隐藏的黑檀宝箱怪。\n"+
        "_-_ 投掷武器也一并迁移完成，现在可以在查种器中查询。\n"));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARMOR_X4),
                "IOP X 系列外骨骼",
        "_-_ 新增_IOP X1~X4 外骨骼_，四件护甲的防护能力依次为 0-1 / 0-2 / 0-3 / 0-4，升级上限分别 +1 / +2 / +2 / +3。\n"+
        "_-_ X 系列的核心特性是_额外闪避判定_：每次闪避会多判定一次，且每 5 级再 +1 次；但其_抵挡_仅为同阶普通护甲的一半。\n"+
        "_-_ 目前数值强度尚未经过完整测试，后续会继续调整。\n"));

        changes.addButton(new ChangeButton(Icons.get(Icons.MAGNIFY),
                "推理鉴定",
        "_-_ 新增_推理鉴定_：未鉴定的物品在信息窗口中会多出一个_猜测_按钮，可以主动给出一个判断。\n"+
        "_-_ 猜对时物品名称会显示为「? 真名」；猜错时会提示_仍可能 / 已排除 / 证据不足_，并记录已经排除的选项。\n"+
        "_-_ _解谜药水_等辅助鉴定手段会提供额外证据，帮助缩小范围；相关界面与判定仍在完善中。\n"));

        changes.addButton(new ChangeButton(Icons.get(HeroClass.HK416),
                "HK416",
        "_-_ _HK416_ 已可作为自机角色选择，但正如设计文档所述，_能力尚不完整，仅供测试体验_。\n"+
        "_-_ 初始携带 _M320_ 榴弹发射器与 _HK USP_ 手枪：M320 可打击视野内 3×3 区域、破坏草地/门/木障碍并自动装填，HK USP 射程为 2。\n"+
        "_-_ 已预置两个子职业：_寄生榴弹_（HK269、侵蚀、死亡时 3×3 自爆）与_特工_（黏弹、不惊醒敌人、50 回合间隔）。\n"+
        "_-_ 解锁条件：_单局使用任意一种手雷_。数值与天赋仍在打磨。\n"));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SuperAIDLC),
                "超级小爱",
        "_-_ 隼的新转职_汉阳造88式-超级小爱_正式实装，配件与天赋已基本完善，数值仍可能继续调整。\n"+
        "_-_ 开启_飞行_后持续 2 回合，需要 20 点能量门槛；能量上限 200，飞行期间移动速度 -20%、饱食度消耗翻倍。\n"+
        "_-_ 支持以 _1:5_ 的比例用炼金能量为装备装填，并新增了专属贴图。\n"));

        changes.addButton(new ChangeButton(Icons.get(HeroClass.GSH18),
                "GSh-18 新增天赋与字体",
        "_-_ GSh-18 新增 T3 通用天赋_急救药箱_：治疗量依次为 35% / 70% / 100%。\n"+
        "_-_ 新增一种可选_字体_，现在可在融合像素 / 方正像素 / 系统字体之间切换。\n"+
        "_-_ GSh-18 的天赋与状态面板同步完善，状态面板新增_经验条_。\n"));

        changes.addButton(new ChangeButton(Icons.get(Icons.STATS),
                "查种器重制",
        "_-_ _查种器_重制为_多线程_实现，查询速度大幅提升；物品名称改为懒加载，进一步减少卡顿。\n"+
        "_-_ 新增_模糊搜索_与_饰品_查询，可查看饰品等级与生成结果。\n"+
        "_-_ 修复了楼层设置按钮引用错误与退出按钮异常的问题。\n"));

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA),
                "数据转移",
        "_-_ 新增_数据转移_功能，可将存档与设置迁移到其他设备。\n"+
        "_-_ 这是该功能的首次实现，后续会继续完善兼容性与提示。\n"));

        changes.addButton(new ChangeButton(Icons.get(Icons.BADGES),
                "徽章与战斗信息",
        "_-_ 重写了_徽章_的贴图引用与展示逻辑，徽章界面不再混乱，加载也更稳定。\n"+
        "_-_ 新增_伤害图标_显示，战斗中的伤害类型更直观。\n"+
        "_-_ 怪物获得_阵营标签_，图鉴中会标注所属阵营。\n"));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY),
                "0 层扩建",
        "_-_ 继续扩建_0 层（前进营地）_，新增了一批设施与交互点位。\n"+
        "_-_ 新增_快捷返回 0 层_开关，默认关闭，可在_前进营地实时转播终端_中开启。\n"+
        "_-_ 修复了开启返回功能后无法正常返回 0 层的问题。\n"));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHANGES),
                "国际象棋",
        "_-_ 0 层新增小游戏_国际象棋_，并针对操作手感与 AI 进行了优化。\n"+
        "_-_ 棋局界面与落子反馈已调整至正常效果，可作为纯休闲玩法体验。\n"));

        changes.addButton(new ChangeButton(Icons.get(Icons.SKULL),
                "阵营系统",
        "_-_ 怪物新增_阵营_系统：_帕拉蒂斯（PARA DEUS）_、_正规军（REGULAR ARMY）_、_铁血工造（SANGVIS FERRI）_，以及无阵营。\n"+
        "_-_ 同一阵营的怪物不会互相攻击，不同阵营的怪物会互相敌对；玩家未参与的战斗不会给予经验、击杀与图鉴记录。\n"+
        "_-_ 怪物生成器同步改为按阵营与贴图分组，原版地牢怪物贴图与空引用已清理。\n"));

        changes.addButton(new ChangeButton(Icons.get(Icons.ARROW),
                "单向行走（测试中）",
        "_-_ 新增_单向行走_地形：每格可设置 8 个方向的通行掩码，寻路与存读档均已支持。\n"+
        "_-_ 该功能目前_默认关闭_，尚无正式的关卡生成与箭头贴图，仅作为后续内容的底层支持。\n"));

        //其他新增与整理：小事件汇总为翻页内容，每页三条
        changes.addButton(new ChangeButton(
                new Object[]{Icons.get(Icons.DATA), null},
                new String[]{"其他新增", "其他整理"},
                new String[]{
                        "_-_ 补完了_全知药水_的相关逻辑与文案。\n"+
                        "_-_ 搬入_魔绫_的_护甲生成器_，护甲生成分布更合理。\n"+
                        "_-_ 补充了_德尔_的介绍文本。\n",

                        "_-_ 优化了_隼_的复合弓描述文本。\n"+
                        "_-_ 粽子生成改为与_粽子相关天赋_挂钩，而不是绑定职业。\n"+
                        "_-_ 优化了节日显示，整理过长的万圣节文本。\n"
                }, 0.8F));

        //调整
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.POSITIVE );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(
                new Object[]{Icons.get(Icons.DISPLAY), null, null, null},
                new String[]{"界面与显示", "交互与传送", "物品与天赋", "文本与整理"},
                new String[]{
                        "_-_ 万圣节节日显示与过长文本已整理。\n"+
                        "_-_ _望远镜_生成物品的透明度交互调整。\n"+
                        "_-_ 设置中新增_怪物位置显示_开关。\n",

                        "_-_ 携带_传送器_时可直接在冒险手册中跨层传送。\n"+
                        "_-_ _快捷返回 0 层_改为可开关设置，默认关闭。\n"+
                        "_-_ _灵刀_的星级显示优化。\n",

                        "_-_ _露水_拾取优化，可自动装瓶。\n"+
                        "_-_ _升级磁盘_支持连续使用。\n"+
                        "_-_ 新增天赋在读档时也会按顺序显示。\n",

                        "_-_ 为武器补全_枪种标签_（如 RF），供天赋等系统按类别判定。\n"+
                        "_-_ 粽子生成改为与_粽子相关天赋_挂钩。\n"+
                        "_-_ 对游戏内大量文本与贴图路径进行了整理与校正。\n"
                }, 0.8F));

        //增强
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "buffs"), false, null);
        changes.hardlight( CharSprite.POSITIVE );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(
                new Object[]{Icons.get(Icons.STATS), null},
                new String[]{"节日蛋糕", "医疗包"},
                new String[]{
                        "_-_ _节日蛋糕_增加强力单局 buff：命中 +20%、发光常驻、视野 +1。\n",

                        "_-_ _被诅咒的医疗包_现在可以正常使用了。\n"
                }, 0.8F));

        //削弱
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "nerfs"), false, null);
        changes.hardlight( CharSprite.NEGATIVE );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.SKULL),
                "节日蛋糕",
        "_-_ _节日蛋糕_的视野加成在_击杀 Boss 后失效_，避免后期视野过于宽裕。\n"));

        //BUG修复：翻页日志窗口，每页三条
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
        changes.hardlight( CharSprite.NEGATIVE );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(
                new Object[]{Icons.get(Icons.CHANGES), null, null, null, null},
                new String[]{"战斗与装备", "任务与楼层", "界面与贴图", "快捷栏与查种器", "种子与生成"},
                new String[]{
                        "_-_ 修复了诅咒_荆棘斗篷_触发复活时角色直接死亡的问题。\n"+
                        "_-_ 修复了用已复合的外骨骼再复合另一件外骨骼时闪退的问题。\n"+
                        "_-_ 修复了_超频瞄准镜_依旧为投掷武器提供加成的问题。\n",

                        "_-_ 修复了_铁匠任务_提交后进度被重置的问题。\n"+
                        "_-_ 修复了开启返回功能后无法正常返回_0 层_的问题。\n"+
                        "_-_ 修复了_升降器 / 跳楼器_相关操作中的空指针异常。\n",

                        "_-_ 修复了_图鉴按钮_异常重置颜色的问题。\n"+
                        "_-_ 修复了_升降器_返回窗口中按钮偏移的问题。\n"+
                        "_-_ 修复了药水与魔药随机贴图出现空白的问题。\n",

                        "_-_ 修复了新建角色时_快捷栏_顺序被重置的问题。\n"+
                        "_-_ 修复了_查种器_楼层设置按钮引用错误的问题。\n"+
                        "_-_ 修复了_查种器_退出按钮异常的问题。\n",

                        "_-_ 修复了 20、25 层商店不随种子变化而固定的问题。\n"+
                        "_-_ 修复了_荒芜挑战_下新楼层触发种子荚时崩溃的问题。\n"+
                        "_-_ 修复了滚动网格面板按钮未销毁点击区域、导致无法点击移动的问题。\n"
                }, 0.8F));
    }
}
