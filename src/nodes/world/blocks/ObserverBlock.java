package nodes.world.blocks;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.gen.*;

public class ObserverBlock extends NodeBlock{

    public ObserverBlock(String name) {
        super(name);
    }

    public boolean checkValid(Building b){
        return true;
    }

    public class ObserverBuild extends NodeBuild {
        @Override
        public void drawBlueprint() {
            super.drawBlueprint();

            if(front() != null && checkValid(front())){
                Building b = front();
                float rad = (b.block.size * 8f) / 2f;

                Tmp.c1.set(Color.white).lerp(Color.black, 0.9f);
                Draw.color(Tmp.c1);
                Fill.square(b.x, b.y, rad);

                Lines.stroke(1f, Color.white);
                Lines.square(b.x, b.y, rad);

                Draw.rect(overlayRegion, b.x, b.y);
            }
        }
    }
}
