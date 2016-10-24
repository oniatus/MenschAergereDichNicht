
package org.terasology.maedn.logic;

import java.util.List;

public interface GameActionSelector {

    /**
     * Selects one of the possible actions and notifies all registered callbacks.
     */
    void selectAction(List<GameAction> possibleActions);

    void register(GameActionSelectionCallback observer);

    void remove(GameActionSelectionCallback observer);
}
