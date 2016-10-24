
package org.terasology.maedn.logic;

import org.terasology.maedn.logic.MaednGame.Color;

public interface MaednGameCallback {

    /**
     * Called after one color finished its turn.
     */
    void onTurnFinished(Color color);

    /**
     * Called after a piece is captured. 
     * This is followed by a moved event for the captured piece back to the spawn, 
     * followed by a moved event from the moving piece to the target field.
     */
    void onPieceCaptured(Color capturedColor, Color fromColor, int onIndex);

    /**
     * Called after a piece moved from one field to another.
     * The sequence contains all indexes that were traveled over, 
     * including the target but excluding the start. 
     * If the movement was directly (spawning or capturing), 
     * the sequence will be {@link MaednGame#NO_SEQUENCE}
     */
    void onPieceMoved(Color color, int fromIndex, int toIndex, int[] sequence);

    /**
     * A color may perform an extra turn after the current turn.
     * @param color
     */
    void onHasExtraTurn(Color color);

    void onGameWon(Color color);
}
