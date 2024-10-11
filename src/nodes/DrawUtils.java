package nodes;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.ui.*;

public class DrawUtils{
    private static final Font font = Fonts.outline;
    private static final GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);

    // shamelessly stolen from PM
    public static float text(float x, float y, Color color, CharSequence text){
        return text(x, y, false, color, text, 0.25f);
    }

    public static float text(float x, float y, boolean underline, Color color, CharSequence text, float scale){
        boolean ints = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        font.getData().setScale(scale);
        layout.setText(font, text);

        font.setColor(color);
        font.draw(text, x, y + (underline ? layout.height + 1 : layout.height / 2f), Align.center);
        if(underline){
            y -= 1f;
            Lines.stroke(2f, Color.darkGray);
            Lines.line(x - layout.width / 2f - 2f, y, x + layout.width / 2f + 1.5f, y);
            Lines.stroke(1f, color);
            Lines.line(x - layout.width / 2f - 2f, y, x + layout.width / 2f + 1.5f, y);
        }

        float width = layout.width;

        font.setUseIntegerPositions(ints);
        font.setColor(Color.white);
        font.getData().setScale(1f);
        Draw.reset();
        Pools.free(layout);

        return width;
    }

    public static void drawCurve(Color color, float x1, float y1, float x2, float y2){
        Lines.stroke(4f);
        Draw.color(color);

        float progress = Interp.pow3.apply((Time.time % (60 * 4)) / (60 * 4));

        // handle some edge cases here
        if(x1 == x2 || y1 == y2){
            Lines.line(x1, y1, x2, y2);
            Fill.circle(Mathf.lerp(x1, x2, progress), Mathf.lerp(y1, y2, progress), 6f);
            return;
        }

        float dist = Math.abs(x1 - x2) / 2f;
        float cx1 = x1 + dist;
        float cx2 = x2 - dist;
        Lines.curve(x1, y1, cx1, y1, cx2, y2, x2, y2, Math.max(4, (int) (Mathf.dst(x1, y1, x2, y2) / 4f)));

        float t2 = progress * progress;
        float t3 = progress * t2;
        float t1 = 1 - progress;
        float t13 = t1 * t1 * t1;
        float kx1 = t13 * x1 + 3 * progress * t1 * t1 * cx1 + 3 * t2 * t1 * cx2 + t3 * x2;
        float ky1 = t13  *y1 + 3 * progress * t1 * t1 * y1 + 3 * t2 * t1 * y2 + t3 * y2;

        Fill.circle(kx1, ky1, 6f);
    }
}