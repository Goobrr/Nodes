package nodes.node;

import arc.*;
import arc.func.*;
import arc.struct.*;
import nodes.world.blocks.*;

public class Node {
    public Seq<Node> sources = new Seq<>();
    public Seq<Node> targets = new Seq<>();
    public boolean in = true;

    public NodeBlock.NodeBuild build;
    public int index;

    public String name = "Test Name";
    public String tooltip = "Test Tooltip";
    public Func<Node, Float> func;

    private float lastFrame = -1;
    private float lastResult = 0;

    public boolean connect(Node otherNode){
        if(otherNode.in == this.in) return false;

        if(otherNode.in){
            otherNode.sources.add(this);
            this.targets.add(otherNode);
        }else{
            otherNode.targets.add(this);
            this.sources.add(otherNode);
        }

        return true;
    }

    public void disconnect(Node otherNode){
        otherNode.sources.remove(this);
        otherNode.targets.remove(this);
        sources.remove(otherNode);
        targets.remove(otherNode);
    }

    public float getSignal(){
        if(Core.graphics.getFrameId() == lastFrame) return 0;

        lastFrame = Core.graphics.getFrameId();
        lastResult = func.get(this);
        return lastResult;
    }
}
