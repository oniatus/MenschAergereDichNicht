
package org.terasology.maedn.logic;

public class MovePieceAction implements GameAction {

    private final int fromIndex;
    private final int toIndex;
    private final int[] sequence;

    public MovePieceAction(int fromIndex, int toIndex, int[] sequence) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.sequence = sequence;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }

    public int[] getSequence() {
        return sequence;
    }
}
