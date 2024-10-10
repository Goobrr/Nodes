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
import nodes.node.*;
import nodes.world.blocks.*;

public class NodeOverlay {
    public static Table main;
    public static Seq<NodeBlock.NodeBuild> buildings = new Seq<>();
    public static ObjectMap<NodeBlock.NodeBuild, NodeTable> nodeTables = new ObjectMap<>();

    public static Connector connecting;

    public static void build(Group parent){
        // blueprint overlays + background
        parent.addChild(new Table(t -> {
            t.setBackground(Styles.black5);
            t.setFillParent(true);
            t.visible(() -> main.visible);
        }){
            @Override
            public void draw() {
                super.draw();

                Tmp.m1.set(Draw.proj());
                Draw.proj(Core.camera);

                Draw.color(Color.white);

                for(NodeBlock.NodeBuild building : buildings){
                    building.drawBlueprint();
                }

                Draw.proj(Tmp.m1);
                Draw.reset();
            }
        });

        // Nodes
        parent.fill(t -> {
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
            t.touchable(() -> Touchable.childrenOnly);
            t.visible(() -> main.visible);
            t.left().bottom();
            t.margin(10f);

            t.button("Back", Icon.exit, NodeOverlay::hide).width(100f);
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
        Log.info("Shown");
        main.visible(() -> true);
    }

    public static void hide(){
        main.visible(() -> false);
    }

    public static void addBuilding(NodeBlock.NodeBuild b){
        buildings.add(b);
        Table t = b.buildNodeTable();

        main.add(t).expand();

        nodeTables.put(b, new NodeTable(b, t));

        Log.info("Added building");
    }

    public static void removeBuilding(NodeBlock.NodeBuild b){
        buildings.remove(b);

        main.removeChild(nodeTables.get(b).table);
        nodeTables.remove(b);
    }

    public static class NodeTable {
        public Table table;
        public NodeBlock.NodeBuild build;

        public NodeTable(NodeBlock.NodeBuild build, Table table){
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
            this.setZIndex(100);

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
                        if(con.node.build.id == node.build.id){
                            connecting = null;
                            return;
                        }

                        if(con.node.sources.contains(node) || con.node.targets.contains(node)){
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

            boolean disconnecting = targetNode != null && (targetNode.sources.contains(node) || targetNode.targets.contains(node));
            boolean invalid = targetNode != null && (targetNode.build == node.build || targetNode.in == node.in);

            float cx = x + width / 2f;
            float cy = y + width / 2f;

            Draw.color(Pal.accent);
            Lines.stroke(3f, Pal.accent);

            Fill.square(cx, cy, 9f, 45f);

            for(Node target : node.targets){
                Table connectors = nodeTables.get(target.build).table.find("Connectors");
                Connector targetConnector = connectors.find(String.valueOf(target.index));

                Vec2 pos = targetConnector.localToAscendantCoordinates(main, Tmp.v1.set(targetConnector.width / 2f, targetConnector.height / 2f).add(main.x, main.y));
                Lines.line(cx, cy, pos.x, pos.y);
                Fill.square(pos.x, pos.y, 7.5f, 45f);
            }

            if(connecting == this){
                if(disconnecting){
                    Draw.color(Pal.remove);
                    Lines.stroke(3f, Pal.remove);
                }

                if(invalid){
                    Draw.color(Pal.lightishGray);
                    Lines.stroke(3f, Pal.lightishGray);
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
        }

        public void old_draw(){
            validate();

            float oldz = Draw.z();
            Draw.z(95);

            boolean dc = targetNode != null && (targetNode.sources.contains(node) || targetNode.targets.contains(node));
            boolean valid = targetNode != null && (targetNode.build != node.build && targetNode.in != node.in);

            float cx = x + width / 2f;
            float cy = y + height / 2f;

            Lines.stroke(3f, Pal.accent);
            Lines.square(cx, cy, 9f, 45f);

            if(!node.in && node.targets.isEmpty() && connecting != this){
                Fill.square(cx, cy, 7.5f, 45f);
            }

            if(targetNode != null && !valid && connecting == this) {
                Lines.stroke(3f, Pal.lightishGray);
                Draw.color(Pal.lightishGray);
            }

            if(dc){
                Lines.stroke(3f, Pal.remove);
                Draw.color(Pal.remove);
            }else if(connecting == this){
                Lines.line(cx, cy, x + pointX, y + pointY);
                Fill.square(x + pointX, y + pointY, 6f, 45f);
            }

            if(targetNode != null && !valid && connecting == this){
                Draw.rect(Core.atlas.find("nodes-invalid"), Mathf.lerp(cx, x + pointX, 0.5f), Mathf.lerp(cy, y + pointY, 0.5f));
                Lines.stroke(3f, Pal.accent);
                Draw.color(Pal.accent);
            }

            for(Node target : node.targets){
                Table connectors = nodeTables.get(target.build).table.find("Connectors");
                Connector targetConnector = connectors.find(String.valueOf(target.index));

                if(dc && targetNode == targetConnector.node){
                    Lines.stroke(3f, Pal.remove);
                    Draw.color(Pal.remove);
                }else{
                    Lines.stroke(3f, Pal.accent);
                    Draw.color(Pal.accent);


                }

                targetConnector.validate();
                Vec2 pos = targetConnector.localToAscendantCoordinates(main, Tmp.v1.set(targetConnector.width / 2f, targetConnector.height / 2f).add(main.x, main.y));
                Lines.line(cx, cy, pos.x, pos.y);
                Fill.square(pos.x, pos.y, 7.5f, 45f);

                if(dc && targetNode == targetConnector.node){
                    Draw.rect(Core.atlas.find("nodes-disconnect"), Mathf.lerp(cx, pos.x, 0.5f), Mathf.lerp(cy, pos.y, 0.5f));
                }
            }

            Draw.flush();
            Draw.z(oldz);
        }
    }
}
