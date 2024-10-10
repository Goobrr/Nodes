package nodes.world.blocks.draw;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.draw.*;

public class DrawRegionStatic extends DrawDefault {
    public String suffix;
    public TextureRegion region;

    public DrawRegionStatic(String suffix){
        this.suffix = suffix;
    }

    @Override
    public void draw(Building build) {
        Draw.rect(region, build.x, build.y);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(region, plan.x, plan.y);
    }

    @Override
    public void load(Block block) {
        super.load(block);

        region = Core.atlas.find(block.name + suffix);
    }
}
