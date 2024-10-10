package nodes.world.blocks;

import arc.func.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import nodes.node.*;
import nodes.ui.*;
import nodes.world.blocks.draw.*;

public class NodeBlock extends Block{
    public Seq<Node> nodesTemplate = new Seq<>();
    public DrawBlock drawer = new DrawMultiRegions("nodes-fallback-node", false);
    public DrawBlock blueprintDrawer = new DrawMultiRegions("nodes-fallback-blueprint", false);

    public NodeBlock(String name){
        super(name);

        update = true;
        solid = true;
        configurable = true;
        logicConfigurable = false;
        size = 1;
    }

    public void addNode(boolean in, String name, String tooltip, Func<Node, Float> func){
        Node newNode = new Node();
        newNode.in = in;
        newNode.name = name;
        newNode.tooltip = tooltip;
        newNode.func = func;

        nodesTemplate.add(newNode);
    }

    @Override
    public void load() {
        super.load();

        drawer.load(this);
        blueprintDrawer.load(this);
    }

    public class NodeBuild extends Building {
        public Seq<Node> nodes = new Seq<>();

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            int i = 0;
            for(Node node : nodesTemplate){
                Node newNode = new Node();
                newNode.in = node.in;
                newNode.build = this;
                newNode.index = i;
                newNode.name = node.name;
                newNode.func = node.func;
                newNode.tooltip = node.tooltip;

                nodes.add(newNode);

                i++;
            }

            NodeOverlay.addBuilding(this);
            Log.info("Init");
            return super.init(tile, team, shouldAdd, rotation);
        }

        @Override
        public void onRemoved() {
            super.onRemoved();

            for(Node node : nodes){
                for(Node otherNode : node.sources){
                    node.disconnect(otherNode);
                }
                for(Node otherNode : node.targets){
                    node.disconnect(otherNode);
                }
            }

            NodeOverlay.removeBuilding(this);
        }

        @Override
        public boolean configTapped() {
            NodeOverlay.show();
            return false;
        }

        public void buildNodeMenu(Table t){

        }

        public Table buildNodeTable() {
            return new Table(t -> {
                t.background(Styles.black5);
                t.table(this::buildNodeMenu).name("Content");
                t.row();
                t.label(() -> "Nodes").name("Title");
                t.row();
                t.table(r -> {
                    r.name = "Connectors";
                    r.margin(10f, 5f, 10f, 5f);
                    for(Node node : nodes){
                        r.add(new NodeOverlay.Connector(node))
                                .name(String.valueOf(node.index))
                                .size(30).pad(0f, 5f, 0f, 5f)
                                .tooltip((node.in ? "Input" : "Out") + " - " + node.name + "\n" + node.tooltip);
                    }
                }).expand();
            });
        }

        @Override
        public void draw() {
            ((NodeBlock) block).drawer.draw(this);
        }

        public void drawBlueprint(){
            ((NodeBlock) block).blueprintDrawer.draw(this);
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);

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

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            int len = read.i();
            for(int i = 0; i < len; i++){
                Node node = nodes.get(i);
                int targetLen = read.i();
                for(int j = 0; j < targetLen; j++){
                    NodeBuild b = (NodeBuild) Vars.world.build(read.i());
                    int index = read.i();
                    if(b != null){
                        node.connect(b.nodes.get(index));
                    }
                }
                int sourceLen = read.i();
                for(int j = 0; j < sourceLen; j++){
                    NodeBuild b = (NodeBuild) Vars.world.build(read.i());
                    int index = read.i();
                    if(b != null){
                        node.connect(b.nodes.get(index));
                    }
                }
            }
        }
    }
}
