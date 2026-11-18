package com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.watabou.noosa.ui.Component;

/**
 * 仅用于显示一段文本的提示窗口，超长内容可滚动。
 */
public class WndNotice extends Window {

    private static final int WIDTH = 120;
    private static final int MAX_HEIGHT = 153;
    private static final int GAP = 4;
    private static final String notice;
    static {
        notice = "一、上限等级：" +
                "\n1.子弹模块，常规+2，7层及以上可通过M16A1任务获得+3，17层及以上可通过P7任务获得+4。" +
                "\n2.武器：二阶及以上常规+3，17层及以上可通过P7任务，1阶近战+3，4阶+5，5阶近战+5，5阶投武+4，6阶+4" +
                "\n3.外骨骼：常规+3，17层以上可通过P7任务获得UX +4。" +
                "\n4.瞄准镜：常规+2，17层及以上可通过P7任务获得+4。" +
                "\n5.模糊查找：取其覆盖范围的最大上限等级。" +
                "\n\n二、查找逻辑：" +
                "\n1.PPSh-41任务的随机装备不参与匹配，仅在结果页进行展示。" +
                "\n2.M16A1任务、P7任务各只能获取一个奖励，所以一旦有需求占用了奖励，相关物品由此得到的上限等级自然会被禁止选择。" +
                "\n3.在选择了+3子弹模块、未占用P7奖励且最深楼层17层及以上时，可能会出现子弹模块在M16A1任务没有匹配，在P7任务匹配的情况。" +
                "\n4.目前加入了饰品系统，而这个需要炼金，所以饰品序列前四个出现了目标饰品，会固定在第5层收入+3的饰品。";// +
//                "\n5.如需检查某一饰品对应情况的生成，请选中目标饰品，当饰品序列前四个出现了目标饰品，则会在第5层收入该饰品。" +
//                "如需测试在更早的楼层让饰品对楼层生效，请自行用测试模式尝试。" +
//                "由于可能存在跨层影响，在更早楼层令饰品生效后，由查种器检查的后续楼层结果可能存在不同。" +
//                "\n6.本地牢将一些生成绑定了天赋、局内物品，【特指行窃预知天赋和炼金箱】，而这是当前查种器难以处理的。" +
//                "所以我们约定不让这些因素影响，即不蜕变获得/失去影响生成的天赋，不给这些天赋加点，【特指增加隐藏房数量的行窃预知】，不启用这些局内物品，【特指炼金箱的将炼金房转化】。" +
//                "\n7.上述约定不影响各角色的一阶额外生成4/6个食物的天赋，这是 独立随机流，不会造成影响，可自由加点、蜕变。" +
//                "\n8.我还将粽子的生成从绑定56-1角色改成了绑定其某个一阶天赋。这也不会造成对生成的影响，但你可以指定丹德莱初始随机到这个一阶天赋。" +
//                "\n9.由于空降妖精的生成逻辑做了改动，所以如果刽子手掉落了空降妖精，那么后续的遗物序列可能会被打乱，这个目前我还没有好的想法处理，所以自求多福吧……" +
//                "不过庆幸的是，这个打乱只影响遗物序列，不影响其他物品。";
    }
    public WndNotice() {
        this(notice);
    }
    public WndNotice(String text) {
        this("注意事项", text);
    }

    public WndNotice(String title, String text) {
        ScrollPane pane = new ScrollPane(new Component());
        Component content = pane.content();
        add(pane);

        IconTitle titlebar = new IconTitle(Icons.get(Icons.INFO), title);
        titlebar.color(TITLE_COLOR);
        content.add(titlebar);
        titlebar.setRect(0, 0, WIDTH, 0);

        RenderedTextBlock message = PixelScene.renderTextBlock(6);
        message.text(text, WIDTH);
        content.add(message);
        message.setPos(titlebar.left(), titlebar.bottom() + GAP);

        content.setSize(WIDTH, message.bottom() + GAP);

        int height = (int) Math.min(MAX_HEIGHT, content.height() + 1);
        resize(WIDTH, height);
        pane.setRect(0, 0, WIDTH, height);
    }
}
