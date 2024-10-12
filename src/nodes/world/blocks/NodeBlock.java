package nodes.world.blocks;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import nodes.node.*;
import nodes.ui.*;

public class NodeBlock extends Block{
    public String baseRegion = "nodes-fallback-node", overlayRegion = "clear", blueprintRegion = "nodes-fallback-blueprint";

    public Seq<Node> nodesTemplate = new Seq<>();

    public NodeBlock(String name){
        super(name);

        update = true;
        solid = true;
        configurable = true;
        logicConfigurable = false;
        size = 1;
    }

    public void addNode(boolean in, String name, Func<Node, Float> func){
        Node node = new Node();
        node.in = in;
        node.name = name;
        node.func = func;

        nodesTemplate.add(node);
    }

    @Override
    public void drawPlan(BuildPlan plan, Eachable<BuildPlan> list, boolean valid) {
        super.drawPlan(plan, list, valid);

        Draw.rect(baseRegion + (rotate ? "-" + plan.rotation : ""), plan.x, plan.y);
        Draw.rect(overlayRegion, plan.x, plan.y);
    }

    @Override
    protected TextureRegion[] icons() {
        return new TextureRegion[]{Core.atlas.find(baseRegion), Core.atlas.find(overlayRegion)};
    }

    public class NodeBuild extends Building implements NodeComp{
        public Seq<Node> nodes = new Seq<>();
        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            Building result = super.init(tile, team, shouldAdd, rotation);

            initNodes(this, nodes, nodesTemplate);
            NodeOverlay.add(this);

            return result;
        }

        @Override
        public void onRemoved() {
            clearNodes(nodes);
            NodeOverlay.remove(this);
            super.onRemoved();
        }

        @Override
        public Seq<Node> nodes() {
            return nodes;
        }

        @Override
        public boolean configTapped() {
            NodeOverlay.show();
            return false;
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion + (rotate ? "-" + rotation() : ""), x, y);
            Draw.rect(overlayRegion, x, y);
        }

        @Override
        public void drawBlueprint() {
            Draw.rect(blueprintRegion, x, y, rotate ? rotation() * 90 : 0);
            Draw.rect(overlayRegion, x, y);
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);

            writeNodes(write, nodes);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            readNodes(read, nodes);
        }
    }
}
