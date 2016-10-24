
package org.terasology.maedn.logic;

import java.util.List;

import org.terasology.utilities.random.MersenneRandom;
import org.terasology.utilities.random.Random;

public class RandomMaednPlayer extends MaednPlayer {

    private Random random = new MersenneRandom();

    @Override
    public void requestDiceResult() {
        onDiceResult(random.nextInt(6) + 1);
    }

    @Override
    protected GameAction doSelectAction(List<GameAction> possibleActions) {
        return possibleActions.get(random.nextInt(possibleActions.size()));
    }

}
