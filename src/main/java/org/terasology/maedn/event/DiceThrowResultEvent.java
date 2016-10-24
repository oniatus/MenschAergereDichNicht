package org.terasology.maedn.event;

import org.terasology.entitySystem.event.Event;

/**
 * Holds the result of a dice throw.
 */
public class DiceThrowResultEvent implements Event {

    private int result;

    public DiceThrowResultEvent() {
    }

    public DiceThrowResultEvent(int result) {
        this.result = result;
    }
    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
