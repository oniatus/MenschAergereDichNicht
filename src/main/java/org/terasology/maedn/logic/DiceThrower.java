
package org.terasology.maedn.logic;

public interface DiceThrower {

    /**
     * The dice thrower should throw a dice (somehow) and return the result to all registered callbacks.
     */
    void requestDiceResult();

    void register(DiceThrowerCallback observer);

    void remove(DiceThrowerCallback observer);
}
