package nodes.world.blocks;

import nodes.node.*;

public class SignalDisplay extends NodeBlock{
    public SignalDisplay(String name) {
        super(name);

        addNode(true, "Signal Input", "Input signal to display", node -> {
            float v = 0;
            for(Node source : node.sources){
                v += source.getSignal();
            }
            return v;
        });
    }

    public class SignalDisplayBuild extends NodeBuild{
        float signal;

        @Override
        public void update() {
            super.update();

            signal = nodes.get(0).getSignal();
        }

        @Override
        public void draw() {
            super.draw();

            drawPlaceText("Signal: " + signal, tile.x, tile.y, true);
        }
    }
}
