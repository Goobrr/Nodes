package nodes.world.blocks;

import arc.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.*;
import nodes.node.*;
import nodes.ui.*;

public interface NodeComp {
    default void connect(int index){

    }

    default void disconnect(int index){

    }

    default void drawBlueprint(){

    }

    default Seq<Node> nodes(){
        return null;
    }

    default void initNodes(Building building, Seq<Node> nodes, Seq<Node> nodesTemplate){
        int i = 0;
        for(Node node : nodesTemplate){
            Node newNode = new Node();
            newNode.in = node.in;
            newNode.build = building;
            newNode.index = i;
            newNode.name = node.name;
            newNode.func = node.func;
            newNode.track();

            nodes.add(newNode);

            i++;
        }
    }

    default Table buildNodeTable(Building b, Seq<Node> nodes) {
        return new Table(t -> {
            t.table().name("Content");
            t.row();
            t.label(() -> Core.bundle.get("block." + b.block.name + ".name")).name("Title");
            t.row();
            t.table(r -> {
                r.setBackground(Styles.black5);
                r.name = "Connectors";
                r.margin(10f, 5f, 10f, 5f);
                for(Node node : nodes){
                    r.add(new NodeOverlay.Connector(node))
                            .name(String.valueOf(node.index))
                            .size(30).pad(0f, 5f, 0f, 5f)
                            .tooltip((node.in ? "Input" : "Out") + " - " + Core.bundle.get("node." + node.name + ".name") + "\n[gray]" + Core.bundle.get("node." + node.name + ".tooltip"));
                }
            }).expand().padTop(5f);
        });
    }

    default void clearNodes(Seq<Node> nodes){
        for(Node node : nodes){
            Log.debug("----------");
            Log.debug("Clearing node connections of " + node.id);
            node.clearConnections();

            node.untrack();
        }
    }

    default void writeNodes(Writes write, Seq<Node> nodes){
        write.i(nodes.size);
        for(Node node : nodes){
            write.i(node.targets.size);
            for(Node targetNode : node.targets){
                write.i(targetNode.build.pos());
                write.i(targetNode.index);
            }
            write.i(node.sources.size);
            for(Node sourceNode : node.sources){
                write.i(sourceNode.build.pos());
                write.i(sourceNode.index);
            }
        }
    }

    default void readNodes(Reads read, Seq<Node> nodes){
        int len = read.i();
        for(int i = 0; i < len; i++){
            Node node = nodes.get(i);
            int targetLen = read.i();
            for(int j = 0; j < targetLen; j++){
                Building b = Vars.world.build(read.i());
                int index = read.i();
                if(b instanceof NodeComp n){
                    node.connect(n.nodes().get(index));
                }
            }
            int sourceLen = read.i();
            for(int j = 0; j < sourceLen; j++){
                Building b = Vars.world.build(read.i());
                int index = read.i();
                if(b instanceof NodeComp n){
                    node.connect(n.nodes().get(index));
                }
            }
        }
    }
}
