package nodes.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import nodes.*;
import nodes.node.*;
import nodes.world.blocks.*;

public class NodeOverlay {
    public static Table main, debug;
    public static Seq<Building> buildings = new Seq<>();
    public static ObjectMap<Integer, NodeTable> nodeTables = new ObjectMap<>();
    public static boolean hudWasShown;

    public static Connector connecting;

    public static void build(Group parent){
        // blueprint overlays + background

        parent.addChildBefore(parent.find("paused"), new Table(t -> {
            t.name = "Background";
            t.setBackground(Styles.black8);
            t.setFillParent(true);
            t.visible(() -> main.visible);
        }){

            @Override
            public void draw() {
                super.draw();

                Tmp.m1.set(Draw.proj());
                Draw.proj(Core.camera);

                Draw.color(Tmp.c1.set(86, 86, 102));
                Draw.alpha(0.3f);

                float camX = Core.camera.position.x - Core.camera.width / 2f;
                float camY = Core.camera.position.y - Core.camera.height / 2f;

                float offsetX = camX % 32f;
                float offsetY = camY % 32f;

                for(int x = 0; x < Core.camera.width + 64; x += 32){
                    for(int y = 0; y < Core.camera.height + 64; y += 32){
                        Draw.rect("nodes-edit-grid", camX + x + 12 - offsetX - 32, camY + y + 12 - offsetY - 32);
                    }
                }

                Draw.alpha(1f);

                for(Building building : buildings){
                    if(building instanceof NodeComp n) n.drawBlueprint();
                }

                Draw.proj(Tmp.m1);
                Draw.reset();
            }
        });

        // Nodes
        parent.fill(t -> {
            t.name = "Main";
            main = t;

            t.update(() -> {
                for(NodeTable n : nodeTables.values().toSeq()){
                    Vec2 pos = Core.input.mouseScreen(n.build.x, n.build.y - n.build.block.size * Vars.tilesize / 2f - 1.5f);
                    n.table.setPosition(pos.x, pos.y, Align.top);
                }
            });
        });

        // buttons and shit
        parent.fill(t -> {
            t.name = "Overlay";
            t.touchable(() -> Touchable.childrenOnly);
            t.visible(() -> main.visible);
            t.left().bottom();
            t.margin(30f);

            t.table(r -> {
                r.bottom().left();
                r.table(b -> {
                    b.setBackground(Tex.whiteui);
                    b.setColor(Pal.darkestGray);
                    b.touchable(() -> Touchable.enabled);
                    b.hovered(() -> b.setColor(Pal.darkerGray));
                    b.exited(() -> b.setColor(Pal.darkestGray));
                    b.clicked(NodeOverlay::hide);
                    b.margin(10f);

                    b.image(Icon.exit).left().size(40f).padLeft(10f);
                    b.label(() -> Core.bundle.get("nodes.overlay.exit")).center().right().growX().get().setAlignment(Align.center);

                }).expand().width(200f).bottom().left().padRight(15f);

                r.table(b -> {
                    b.setBackground(Tex.whiteui);
                    b.setColor(Pal.darkestGray);
                    b.touchable(() -> Touchable.enabled);
                    b.hovered(() -> b.setColor(Pal.darkerGray));
                    b.exited(() -> b.setColor(Pal.darkestGray));
                    b.clicked(() -> {
                        debug.visible(() -> !debug.visible);
                        Nodes.debug = !Nodes.debug;
                    });
                    b.margin(10f);

                    b.image(Icon.terminal).left().size(40f).padLeft(10f);
                    b.label(() -> Core.bundle.get("nodes.overlay.debug")).center().right().growX().get().setAlignment(Align.center);

                }).expand().width(200f).bottom().left();
            }).expand().bottom().left();
        });

        parent.fill(t -> {
            debug = t;
            t.visible(() -> Nodes.debug);
        });

        main.touchable(() -> Touchable.enabled);

        Events.on(EventType.ResetEvent.class, e -> {
            buildings.clear();
            nodeTables.clear();
            main.clearChildren();

            connecting = null;
        });

        hide();
    }

    public static void show(){
        Log.debug("Node Overlay Shown");

        main.visible(() -> true);
        hudWasShown = Vars.ui.hudfrag.shown;
        Vars.ui.hudfrag.shown = false;
    }

    public static void hide(){
        Log.debug("Node Overlay Hidden");
        main.visible(() -> false);
        Vars.ui.hudfrag.shown = hudWasShown;
    }

    public static void add(Building b){
        if(b instanceof NodeComp n){
            buildings.add(b);
            Table t = n.buildNodeTable(b, n.nodes());

            main.add(t).expand();

            nodeTables.put(b.id, new NodeTable(b, t));

            Log.debug("Added building " + b);
        }
    }

    public static void remove(Building b){
        buildings.remove(b);
        nodeTables.get(b.id).table.clearChildren();
        main.removeChild(nodeTables.get(b.id).table);
        nodeTables.remove(b.id);
    }

    public static class NodeTable {
        public Table table;
        public Building build;

        public NodeTable(Building build, Table table){
            this.table = table;
            this.build = build;
        }
    }

    public static class Connector extends Button{
        public float pointX, pointY;
        public Node targetNode;

        public Node node;
        public Connector(Node node){
            this.node = node;
            this.toFront();

            addCaptureListener(new InputListener(){
                int conPointer = -1;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    if(conPointer != -1) return false;
                    conPointer = pointer;
                    if(connecting != null) return false;

                    connecting = Connector.this;
                    pointX = x;
                    pointY = y;
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    if(conPointer != pointer) return;
                    pointX = x;
                    pointY = y;

                    Vec2 pos = Connector.this.localToAscendantCoordinates(main, Tmp.v1.set(x, y));
                    if(main.hit(pos.x, pos.y, true) instanceof Connector con && con.node != null){
                        targetNode = con.node;
                    }else{
                        targetNode = null;
                    }
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    if(conPointer != pointer || connecting != Connector.this) return;
                    conPointer = -1;

                    Vec2 pos = Connector.this.localToAscendantCoordinates(main, Tmp.v1.set(x, y));
                    if(main.hit(pos.x, pos.y, true) instanceof Connector con && con.node != null){
                        if(con.node.build.id() == node.build.id()){
                            connecting = null;
                            return;
                        }

                        if(con.node.sources.contains(n -> n.id == node.id) || con.node.targets.contains(n -> n.id == node.id)){
                            node.disconnect(con.node);
                        }else{
                            node.connect(con.node);
                        }
                    }

                    connecting = null;
                    targetNode = null;
                }
            });
        }


        @Override
        public void draw() {
            validate();

            Draw.sort(true);
            Draw.z(100);

            boolean disconnecting = targetNode != null && (targetNode.sources.contains(node) || targetNode.targets.contains(node));
            boolean invalid = targetNode != null && (targetNode.build == node.build || targetNode.in == node.in);

            float cx = x + width / 2f;
            float cy = y + width / 2f;

            Draw.color(Pal.accent);
            Lines.stroke(4f, Pal.accent);

            Fill.square(cx, cy, 9f, 45f);

            for(Node target : node.targets){
                NodeTable tab = nodeTables.get(target.build.id);
                if(tab != null) {
                    Table connectors = tab.table.find("Connectors");
                    Connector targetConnector = connectors.find(String.valueOf(target.index));

                    Vec2 pos = targetConnector.localToAscendantCoordinates(main, Tmp.v1.set(targetConnector.width / 2f, targetConnector.height / 2f).add(main.x, main.y));
                    float progress = Interp.pow3.apply((Time.time % (60 * 4)) / (60 * 4));

                    Lines.line(cx, cy, pos.x, pos.y);
                    Fill.circle(Mathf.lerp(cx, pos.x, progress), Mathf.lerp(cy, pos.y, progress), 6f);

                    //DrawUtils.drawCurve(Draw.getColor(), cx, cy, pos.x, pos.y);
                }
            }

            if(connecting == this){
                if(disconnecting){
                    Draw.color(Pal.remove);
                    Lines.stroke(4f, Pal.remove);
                }

                if(invalid){
                    Draw.color(Pal.lightishGray);
                    Lines.stroke(4f, Pal.lightishGray);
                }

                Fill.square(cx, cy, 9f, 45f);
                Lines.line(cx, cy, x + pointX, y + pointY);
                Fill.square(x + pointX, y + pointY, 9f, 45f);

                float mx = (cx + x + pointX) / 2f;
                float my = (cy + y + pointY) / 2f;

                Draw.rect(Core.atlas.find(invalid ? "nodes-invalid" : disconnecting ? "nodes-disconnect" : "clear"), mx, my, 30f, 30f);
            }

            if(node.in){
                Draw.color(Pal.darkerGray);
                Fill.square(cx, cy, 6f, 45f);
            }

            if(Nodes.debug) DrawUtils.text(cx, cy, false, Color.white, "" + node.id, 2);

            Draw.sort(false);
        }
    }
}
