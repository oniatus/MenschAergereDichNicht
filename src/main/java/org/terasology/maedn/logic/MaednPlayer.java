
package org.terasology.maedn.logic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class MaednPlayer implements DiceThrower, GameActionSelector {
    private Set<GameActionSelectionCallback> actionCallbacks = new HashSet<>();
    private Set<DiceThrowerCallback> diceThrowCallbacks = new HashSet<>();

    @Override
    public void register(GameActionSelectionCallback callback) {
        actionCallbacks.add(callback);
    }

    @Override
    public void remove(GameActionSelectionCallback callback) {
        actionCallbacks.remove(callback);
    }

    @Override
    public void register(DiceThrowerCallback callback) {
        diceThrowCallbacks.add(callback);
    }

    @Override
    public void remove(DiceThrowerCallback callback) {
        diceThrowCallbacks.remove(callback);

    }

    @Override
    public void selectAction(List<GameAction> possibleActions) {
        GameAction selectedAction = doSelectAction(possibleActions);
        if (!possibleActions.contains(selectedAction)) {
            throw new IllegalStateException();
        }
        actionCallbacks.forEach(callback -> callback.onActionSelected(selectedAction, this));
    }

    protected void onDiceResult(int result) {
        diceThrowCallbacks.forEach(callback -> callback.onDiceResult(result, this));
    }

    protected abstract GameAction doSelectAction(List<GameAction> possibleActions);

}
