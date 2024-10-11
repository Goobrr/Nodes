package nodes.node;

import arc.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;

public class Node {
    private static int nextid = 0;
    private static Seq<Node> tmp = new Seq<>();
    public static Seq<Node> tracker = new Seq<>();

    public Seq<Node> sources = new Seq<>();
    public Seq<Node> targets = new Seq<>();
    public boolean in = true;

    public Building build;
    public int index;
    public int id;

    public String name = "test-name";
    public Func<Node, Float> func;

    private float lastFrame = -1;
    private float lastResult = 0;

    public Node(){
        this.id = nextid;
        nextid++;
    }

    public void track(){
        tracker.add(this);
    }

    public void untrack(){
        tracker.remove(this);
    }
    
    public boolean connect(Node otherNode){
        if(otherNode.in == this.in) return false;

        if(otherNode.in){
            Log.debug("----------");
            Log.debug("Adding node source " + this.id + " to " + otherNode.id);
            otherNode.sources.add(this);
            Log.debug("Adding node target " + otherNode.id + " to " + this.id);
            this.targets.add(otherNode);
            Log.debug(targets);
        }else{
            Log.debug("----------");
            Log.debug("Adding node target " + this.id + " to " + otherNode.id);
            otherNode.targets.add(this);
            Log.debug("Adding node source " + otherNode.id + " to " + this.id);
            this.sources.add(otherNode);
            Log.debug(sources);
        }

        return true;
    }

    public void disconnect(Node otherNode){
        Log.debug("----------");
        if(otherNode.sources.remove(n -> n.id == id)) Log.debug("Removed node source " + this.id + " from " + otherNode.id);
        if(otherNode.targets.remove(n -> n.id == id)) Log.debug("Removed node target " + this.id + " from " + otherNode.id);
        if(this.sources.remove(n -> n.id == otherNode.id)) Log.debug("Removed node source " + otherNode.id + " from " + this.id);
        if(this.targets.remove(n -> n.id == otherNode.id)) Log.debug("Removed node target " + otherNode.id + " from " + this.id );
    }

    public void clearConnections(){
        Log.debug("----------");
        Log.debug("Removing source nodes of " + id);
        Log.debug(sources);
        tmp.set(sources);
        for(Node otherNode : tmp){
            Log.debug(otherNode);
            otherNode.disconnect(this);
        }

        Log.debug("----------");
        Log.debug("Removing target nodes of " + id);
        Log.debug(targets);
        tmp.set(targets);
        for(Node otherNode : tmp){
            Log.debug(otherNode);
            otherNode.disconnect(this);
        }
    }

    public float getSignal(){
        if(Core.graphics.getFrameId() == lastFrame) return lastResult;

        lastFrame = Core.graphics.getFrameId();
        lastResult = func.get(this);
        return lastResult;
    }
}
