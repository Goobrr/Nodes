package nodes.world.blocks;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import nodes.node.*;
import nodes.ui.*;

public class GateConveyor extends ArmoredConveyor {
    public Seq<Node> nodesTemplate = new Seq<>();

    public GateConveyor(String name) {
        super(name);

        logicConfigurable = false;
        configurable = true;
    }

    public void addNode(boolean in, String name, Func<Node, Float> func){
        Node node = new Node();
        node.in = in;
        node.name = name;
        node.func = func;

        nodesTemplate.add(node);
    }

    public class GateConveyorBuild extends ArmoredConveyorBuild implements NodeComp{
        public Seq<Node> nodes = new Seq<>();

        @Override
        public Seq<Node> nodes() {
            return nodes;
        }

        @Override
        public void update() {
            super.update();
            this.enabled = nodes.get(0).getSignal() > 0;
        }

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            Building result = super.init(tile, team, shouldAdd, rotation);

            initNodes(this, nodes, nodesTemplate);
            NodeOverlay.add(this);

            return result;
        }

        @Override
        public void draw() {
            int frame = enabled && clogHeat <= 0.5f ? (int)(((Time.time * speed * 8f * timeScale * efficiency)) % 4) : 0;

            Draw.z(Layer.block - 0.2f);
            Draw.rect(block.name + "-" + frame, x, y, rotation * 90);

            Draw.z(Layer.block - 0.1f);
            float layer = Layer.block - 0.1f, wwidth = Vars.world.unitWidth(), wheight = Vars.world.unitHeight(), scaling = 0.01f;

            for(int i = 0; i < len; i++){
                Item item = ids[i];
                Tmp.v1.trns(rotation * 90, 8, 0);
                Tmp.v2.trns(rotation * 90, -8 / 2f, xs[i] * 8 / 2f);

                float
                        ix = (x + Tmp.v1.x * ys[i] + Tmp.v2.x),
                        iy = (y + Tmp.v1.y * ys[i] + Tmp.v2.y);

                //keep draw position deterministic.
                Draw.z(layer + (ix / wwidth + iy / wheight) * scaling);
                Draw.rect(item.fullIcon, ix, iy, Vars.itemSize, Vars.itemSize);
            }

            Draw.z(Layer.block + 0.1f);
            Draw.rect(block.name + "-top-" + rotation, x, y);

            Draw.color(enabled ? Pal.heal : Pal.remove);
            Draw.rect(block.name + "-light", x, y, rotation * 90f);
        }

        @Override
        public void drawBlueprint() {
            Draw.rect(block.name + "-blueprint", x, y, rotation * 90f);
        }

        @Override
        public void drawDisabled() {
            return;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return super.acceptItem(source, item) && (Edges.getFacingEdge(source.tile(), tile).relativeTo(tile) == rotation);
        }

        @Override
        public void onRemoved() {
            clearNodes(nodes);
            NodeOverlay.remove(this);
            super.onRemoved();
        }

        @Override
        public boolean configTapped() {
            NodeOverlay.show();
            return false;
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
