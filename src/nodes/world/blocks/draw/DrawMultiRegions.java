package nodes.world.blocks.draw;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.draw.*;

public class DrawMultiRegions extends DrawDefault {
    public boolean rotate;
    public TextureRegion[] regions = new TextureRegion[4];
    public String name;

    public DrawMultiRegions(String name, boolean rotate){
        this.name = name;
        this.rotate = rotate;
    }

    @Override
    public void load(Block block) {
        super.load(block);

        if(rotate) {
            for (int i = 0; i < 4; i++) {
                regions[i] = Core.atlas.find(name + "-" + i);
            }
        }else{
            regions[0] = Core.atlas.find(name);
        }
    }

    @Override
    public void draw(Building build) {
        Draw.rect(regions[rotate ? build.rotation() : 0], build.x, build.y, rotate ? 0 : build.rotation() * 90);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(regions[rotate ? plan.rotation : 0], plan.x, plan.y, rotate ? 0 : plan.rotation * 90);
    }
}
