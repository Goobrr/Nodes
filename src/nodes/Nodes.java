
package nodes;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.type.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import nodes.node.*;
import nodes.ui.*;
import nodes.world.blocks.*;
import nodes.world.blocks.draw.*;

public class Nodes extends Mod{
    public Nodes(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            NodeOverlay.build(Vars.ui.hudGroup);
        });

        Events.on(EventType.WorldLoadEndEvent.class, e -> {
            Log.info("World Load");
        });
    }
    @Override
    public void loadContent(){
        new NodeBlock("test-node-block"){{
            addNode(false, "Test Output", "Outputs Signals", node -> node.build.nodes.get(1).getSignal());
            addNode(true, "Test Input", "Accepts Signals", node -> {
                float v = 0;
                for(Node source : node.sources){
                    v += source.getSignal();
                }
                return v;
            });
            requirements(Category.logic, BuildVisibility.sandboxOnly, ItemStack.with(Items.copper, 1));
        }};

        new NodeBlock("test-node-source"){{
            addNode(false, "Test Output", "Outputs a constant signal of 10", node -> 10f);
            requirements(Category.logic, BuildVisibility.sandboxOnly, ItemStack.with(Items.copper, 1));
        }};

        new SignalDisplay("test-node-display"){{
            requirements(Category.logic, BuildVisibility.sandboxOnly, ItemStack.with(Items.copper, 1));
        }};

        new NodeBlock("item-observer"){{
            addNode(false, "Output", "Amount of items in the container in front of the observer.", node -> {
                Building b = node.build.front();
                if(b != null && b.block.hasItems && b.items() != null){
                    return (float) b.items().total();
                }
                return 0f;
            });
            requirements(Category.logic, BuildVisibility.sandboxOnly, ItemStack.with(Items.copper, 1));
            rotate = true;

            drawer = new DrawMulti(
                    new DrawMultiRegions("nodes-observer", true),
                    new DrawRegionStatic("-overlay")
            );

            blueprintDrawer = new DrawMulti(
                    new DrawMultiRegions("nodes-item-observer-blueprint", false),
                    new DrawRegionStatic("-overlay")
            );
        }};
    }
}
