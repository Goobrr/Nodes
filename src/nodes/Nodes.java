
package nodes;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.type.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.meta.*;
import nodes.node.*;
import nodes.ui.*;
import nodes.world.blocks.*;

public class Nodes extends Mod{
    public static boolean debug = true;

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
        new NodeBlock("node-sum"){{
            baseRegion = "nodes-node-addition";
            addNode(false, "sum-output", node -> ((NodeComp) node.build).nodes().get(1).getSignal());
            addNode(true, "sum-input", node -> {
                float v = 0;
                for(Node source : node.sources){
                    v += source.getSignal();
                }
                return v;
            });
            requirements(Category.logic, BuildVisibility.sandboxOnly, ItemStack.with(Items.copper, 1));
        }};

        new NodeBlock("node-sub"){{
            baseRegion = "nodes-node-subtraction";
            addNode(true, "sub-input-a", node -> {
                float v = 0;
                for(Node source : node.sources){
                    v += source.getSignal();
                }
                return v;
            });
            addNode(false, "sub-output", node -> {
                Seq<Node> nodes = ((NodeComp) node.build).nodes();
                return nodes.get(0).getSignal() - nodes.get(2).getSignal();
            });
            addNode(true, "sub-input-b", node -> {
                float v = 0;
                for(Node source : node.sources){
                    v += source.getSignal();
                }
                return v;
            });
            requirements(Category.logic, BuildVisibility.sandboxOnly, ItemStack.with(Items.copper, 1));
        }};

        new NodeBlock("node-multiply"){{
            baseRegion = "nodes-node-multiply";
            addNode(true, "multiply-input-a", node -> {
                float v = 0;
                for(Node source : node.sources){
                    v += source.getSignal();
                }
                return v;
            });
            addNode(false, "multiply-output", node -> {
                Seq<Node> nodes = ((NodeComp) node.build).nodes();
                return nodes.get(0).getSignal() * nodes.get(2).getSignal();
            });
            addNode(true, "multiply-input-b", node -> {
                float v = 0;
                for(Node source : node.sources){
                    v += source.getSignal();
                }
                return v;
            });
            requirements(Category.logic, BuildVisibility.sandboxOnly, ItemStack.with(Items.copper, 1));
        }};

        new NodeBlock("node-compare"){{
            baseRegion = "nodes-node-comparison";
            addNode(true, "compare-input-a", node -> {
                float v = 0;
                for(Node source : node.sources){
                    v += source.getSignal();
                }
                return v;
            });
            addNode(false, "compare-output", node -> {
                Seq<Node> nodes = ((NodeComp) node.build).nodes();
                return nodes.get(0).getSignal() < nodes.get(2).getSignal() ? 1f : 0f;
            });
            addNode(true, "compare-input-b", node -> {
                float v = 0;
                for(Node source : node.sources){
                    v += source.getSignal();
                }
                return v;
            });
            requirements(Category.logic, BuildVisibility.sandboxOnly, ItemStack.with(Items.copper, 1));
        }};

        new ObserverBlock("item-observer"){{
            baseRegion = "nodes-observer";
            overlayRegion = "nodes-observer-item";
            blueprintRegion = "nodes-observer-blueprint";

            addNode(false, "item-observer-output", node -> {
                Building b = node.build.front();
                if(checkValid(b)){
                    return (float) b.items().total();
                }
                return 0f;
            });
            requirements(Category.logic, BuildVisibility.sandboxOnly, ItemStack.with(Items.copper, 1));
            rotate = true;
        }

            @Override
            public boolean checkValid(Building b) {
                return b != null && b.block.hasItems && b.items() != null;
            }
        };

        new GateConveyor("gated-conveyor"){{
            speed = ((Conveyor) Blocks.conveyor).speed;

            addNode(true, "gated-conveyor-input", node -> {
                float v = 0;
                for(Node source : node.sources){
                    v += source.getSignal();
                }
                return v;
            });
            requirements(Category.logic, BuildVisibility.sandboxOnly, ItemStack.with(Items.copper, 1));
        }};
    }
}
