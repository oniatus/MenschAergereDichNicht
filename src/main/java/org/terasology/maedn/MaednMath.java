
package org.terasology.maedn;

import java.util.ArrayList;
import java.util.List;

import org.terasology.math.geom.Vector2i;

public class MaednMath {

    private static final List<Vector2i> indexLookup;
    static {
        indexLookup = new ArrayList<>();
        //red corner
        indexLookup.add(new Vector2i(-5, -1));
        indexLookup.add(new Vector2i(-4, -1));
        indexLookup.add(new Vector2i(-3, -1));
        indexLookup.add(new Vector2i(-2, -1));
        indexLookup.add(new Vector2i(-1, -1));
        indexLookup.add(new Vector2i(-1, -2));
        indexLookup.add(new Vector2i(-1, -3));
        indexLookup.add(new Vector2i(-1, -4));
        indexLookup.add(new Vector2i(-1, -5));

        indexLookup.add(new Vector2i(0, -5));

        //blue corner
        indexLookup.add(new Vector2i(1, -5));
        indexLookup.add(new Vector2i(1, -4));
        indexLookup.add(new Vector2i(1, -3));
        indexLookup.add(new Vector2i(1, -2));
        indexLookup.add(new Vector2i(1, -1));
        indexLookup.add(new Vector2i(2, -1));
        indexLookup.add(new Vector2i(3, -1));
        indexLookup.add(new Vector2i(4, -1));
        indexLookup.add(new Vector2i(5, -1));

        indexLookup.add(new Vector2i(5, 0));

        //green corner
        indexLookup.add(new Vector2i(5, 1));
        indexLookup.add(new Vector2i(4, 1));
        indexLookup.add(new Vector2i(3, 1));
        indexLookup.add(new Vector2i(2, 1));
        indexLookup.add(new Vector2i(1, 1));
        indexLookup.add(new Vector2i(1, 2));
        indexLookup.add(new Vector2i(1, 3));
        indexLookup.add(new Vector2i(1, 4));
        indexLookup.add(new Vector2i(1, 5));

        indexLookup.add(new Vector2i(0, 5));

        //yellow corner
        indexLookup.add(new Vector2i(-1, 5));
        indexLookup.add(new Vector2i(-1, 4));
        indexLookup.add(new Vector2i(-1, 3));
        indexLookup.add(new Vector2i(-1, 2));
        indexLookup.add(new Vector2i(-1, 1));
        indexLookup.add(new Vector2i(-2, 1));
        indexLookup.add(new Vector2i(-3, 1));
        indexLookup.add(new Vector2i(-4, 1));
        indexLookup.add(new Vector2i(-5, 1));

        indexLookup.add(new Vector2i(-5, 0));

        //red house
        indexLookup.add(new Vector2i(-4, 0));
        indexLookup.add(new Vector2i(-3, 0));
        indexLookup.add(new Vector2i(-2, 0));
        indexLookup.add(new Vector2i(-1, 0));

        //blue house
        indexLookup.add(new Vector2i(0, -4));
        indexLookup.add(new Vector2i(0, -3));
        indexLookup.add(new Vector2i(0, -2));
        indexLookup.add(new Vector2i(0, -1));

        //green house
        indexLookup.add(new Vector2i(1, 0));
        indexLookup.add(new Vector2i(2, 0));
        indexLookup.add(new Vector2i(3, 0));
        indexLookup.add(new Vector2i(4, 0));

        //yellow house
        indexLookup.add(new Vector2i(0, 4));
        indexLookup.add(new Vector2i(0, 3));
        indexLookup.add(new Vector2i(0, 2));
        indexLookup.add(new Vector2i(0, 1));

        //red spawn
        indexLookup.add(new Vector2i(-5, -5));
        indexLookup.add(new Vector2i(-4, -5));
        indexLookup.add(new Vector2i(-5, -4));
        indexLookup.add(new Vector2i(-4, -4));

        //blue spawn
        indexLookup.add(new Vector2i(4, -5));
        indexLookup.add(new Vector2i(5, -5));
        indexLookup.add(new Vector2i(4, -4));
        indexLookup.add(new Vector2i(5, -4));

        //green spawn
        indexLookup.add(new Vector2i(4, 4));
        indexLookup.add(new Vector2i(5, 4));
        indexLookup.add(new Vector2i(4, 5));
        indexLookup.add(new Vector2i(5, 5));

        //yellow spawn
        indexLookup.add(new Vector2i(-5, 4));
        indexLookup.add(new Vector2i(-4, 4));
        indexLookup.add(new Vector2i(-5, 5));
        indexLookup.add(new Vector2i(-4, 5));

    }

    public static Vector2i boardIndexToPosition(int index) {
        return new Vector2i(indexLookup.get(index));
    }
}
