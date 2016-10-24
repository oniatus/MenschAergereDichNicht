package org.terasology.maedn.logic;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Game logic for 'Mensch ärgere dich nicht'
 */
public class MaednGame implements GameActionSelectionCallback, DiceThrowerCallback {

    public static final int[] NO_SEQUENCE = new int[0];
    private static final int NO_INDEX = -1;
    private static final int SPAWN_INDEX_START = 56;
    private static final int LAST_BOARD_FIELD_INDEX = 39;
    private static final int FIRST_BOARD_FIELD_INDEX = 0;
    private Map<Color, Integer> spawnFieldsStartIndex;
    private Map<Color, Integer> startFieldIndex;
    private Map<Color, Integer> houseEntryIndex;
    private Map<Color, Integer> houseStartIndex;

    private Color[] occupiedFields;

    private static final Logger logger = LoggerFactory.getLogger(MaednGame.class);

    private Map<Color, MaednPlayer> players;
    private Set<MaednGameCallback> callbacks = new HashSet<>();

    public static enum Color {
        RED, BLUE, YELLOW, GREEN;

        public Color getNextPlayerColor() {
            switch (this) {
                case BLUE:
                    return Color.GREEN;
                case GREEN:
                    return YELLOW;
                case RED:
                    return Color.BLUE;
                case YELLOW:
                    return RED;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private Color playerOnTurnColor;
    private Color winnerColor;

    private boolean currentPlayerHasExtraTurn = false;

    public MaednGame(Color startingPlayerColor, Map<Color, MaednPlayer> players) {
        this.playerOnTurnColor = startingPlayerColor;
        this.players = players;
        players.values().stream().forEach(player -> player.register((GameActionSelectionCallback) this));
        players.values().stream().forEach(player -> player.register((DiceThrowerCallback) this));
        initializeBoard();
    }

    private void initializeBoard() {
        //starting indexes for the 4 spawn fields of each color
        spawnFieldsStartIndex = new EnumMap<>(Color.class);
        spawnFieldsStartIndex.put(Color.RED, 56);
        spawnFieldsStartIndex.put(Color.BLUE, 60);
        spawnFieldsStartIndex.put(Color.GREEN, 64);
        spawnFieldsStartIndex.put(Color.YELLOW, 68);
        //the first field on the board for each color
        startFieldIndex = new EnumMap<>(Color.class);
        startFieldIndex.put(Color.RED, 0);
        startFieldIndex.put(Color.BLUE, 10);
        startFieldIndex.put(Color.GREEN, 20);
        startFieldIndex.put(Color.YELLOW, 30);
        //the entry (first field on the board for all players) of the house
        houseEntryIndex = new EnumMap<>(Color.class);
        houseEntryIndex.put(Color.RED, 39);
        houseEntryIndex.put(Color.BLUE, 9);
        houseEntryIndex.put(Color.GREEN, 19);
        houseEntryIndex.put(Color.YELLOW, 29);
        //first field of the actual house
        houseStartIndex = new EnumMap<>(Color.class);
        houseStartIndex.put(Color.RED, 40);
        houseStartIndex.put(Color.BLUE, 44);
        houseStartIndex.put(Color.GREEN, 48);
        houseStartIndex.put(Color.YELLOW, 52);

        occupiedFields = new Color[72]; //index 0-71
        //all pieces are on the spawn
        for (Color color : Color.values()) {
            int colorSpawnIndexStart = spawnFieldsStartIndex.get(color);
            for (int i = 0; i < 4; i++) {
                occupiedFields[colorSpawnIndexStart + i] = color;
            }
        }
    }

    public void register(MaednGameCallback callback) {
        callbacks.add(callback);
    }

    public void remove(MaednGameCallback callback) {
        callbacks.remove(callback);
    }

    public Color getPlayerOnTurnColor() {
        return playerOnTurnColor;
    }

    public MaednPlayer getPlayerOnTurn() {
        return players.get(playerOnTurnColor);
    }

    public void playOneTurn() {
        if (isFinished()) {
            return;
        }
        nextPlayer();
        getPlayerOnTurn().requestDiceResult();
    }

    private void nextPlayer() {
        currentPlayerHasExtraTurn = false;
        playerOnTurnColor = playerOnTurnColor.getNextPlayerColor();
    }

    @Override
    public void onDiceResult(int result, DiceThrower thrower) {
        currentPlayerHasExtraTurn = false;
        if (thrower == getPlayerOnTurn()) {
            List<GameAction> possibleActions = findPossibleActions(getPlayerOnTurnColor(), result);
            //no possible actions: end turn (even for a 6)
            if (possibleActions.isEmpty()) {
                endTurn(getPlayerOnTurnColor());
            } else {
                //6 = another turn for the player
                if (result == 6) {
                    currentPlayerHasExtraTurn = true;
                }
                getPlayerOnTurn().selectAction(possibleActions);
            }
        } else {
            logger.debug("Discarding dice event, thrower is not the active player");
        }

    }

    private List<GameAction> findPossibleActions(Color color, int diceResult) {
        //if the result is a 6, a piece has to be moved from the start, if possible
        if (diceResult == 6 && hasPiecesOnSpawn(color) && isStartEnterable(color)) {
            return newArrayList(actionMovePieceFromSpawnToStart(color));
        }
        //next priority are pieces on the start, if the spawn is not empty
        if (hasPiecesOnSpawn(color) && hasPieceOnStart(color) && canMove(startFieldIndex.get(color), diceResult, color)) {
            return newArrayList(actionMovePieceAwayFromStart(color, diceResult));
        }

        //else: use all possible pieces on board and move them by the result
        return Arrays.stream(getOnBoardPiecesIndexes(color))
                .filter(index -> canMove(index, diceResult, color))
                .mapToObj(fromIndex -> {
                    int toIndex = getIndexInDistance(fromIndex, diceResult, color);
                    int[] sequence = getMoveSequence(fromIndex, diceResult, color);
                    return new MovePieceAction(fromIndex, toIndex, sequence);
                })
                .collect(Collectors.toList());
    }

    public int[] getPiecesIndexes(Color color) {
        int[] result = new int[4];
        int piece = 0;
        for (int i = 0; i < occupiedFields.length; i++) {
            if (occupiedFields[i] == color) {
                result[piece++] = i;
            }
        }
        return result;
    }

    private int[] getOnBoardPiecesIndexes(Color color) {
        return Arrays.stream(getPiecesIndexes(color)).filter(x -> x < SPAWN_INDEX_START).toArray();
    }

    private boolean canMove(int index, int distance, Color color) {
        int[] sequence = getMoveSequence(index, distance, color);
        //not end of board and target field not occupied by the same color
        return sequence != NO_SEQUENCE && occupiedFields[sequence[sequence.length - 1]] != color;
    }

    private boolean hasPieceOnStart(Color color) {
        return occupiedFields[startFieldIndex.get(color)] == color;
    }

    private int getNextIndex(int index, Color color) {
        //invalid
        if (index == NO_INDEX) {
            return NO_INDEX;
        }
        //a color has to move in the house if possible
        if (index == houseEntryIndex.get(color)) {
            return houseStartIndex.get(color);
        }
        //no possible move at the end of the house
        if (index == houseStartIndex.get(color) + 3) {
            return NO_INDEX;
        }
        //end of regular board:
        if (index == LAST_BOARD_FIELD_INDEX) {
            return FIRST_BOARD_FIELD_INDEX;
        }
        //spawn fields have no movement but are handled by a spawn action
        if (index > SPAWN_INDEX_START) {
            return NO_INDEX;
        }
        //all other cases: move one forward
        return index + 1;
    }

    private int getIndexInDistance(int fromIndex, int distance, Color color) {
        int[] sequence = getMoveSequence(fromIndex, distance, color);
        if (sequence == NO_SEQUENCE) {
            return NO_INDEX;
        }
        return sequence[sequence.length - 1];
    }

    private int[] getMoveSequence(int index, int distance, Color color) {
        int[] sequence = new int[distance];
        int currentIndex = index;
        for (int i = 0; i < distance; i++) {
            int nextIndex = getNextIndex(currentIndex, color);
            if (nextIndex == NO_INDEX) {
                return NO_SEQUENCE;
            }
            sequence[i] = nextIndex;
            currentIndex = nextIndex;
        }
        return sequence;
    }

    private GameAction actionMovePieceFromSpawnToStart(Color color) {
        int fromIndex = findFirstOccupiedSpawnIndex(color);
        int toIndex = startFieldIndex.get(color);
        GameAction action = new MovePieceAction(fromIndex, toIndex, NO_SEQUENCE);
        return action;
    }

    private GameAction actionMovePieceAwayFromStart(Color color, int distance) {
        int fromIndex = startFieldIndex.get(color);
        int toIndex = getIndexInDistance(fromIndex, distance, color);
        int[] sequence = getMoveSequence(fromIndex, distance, color);
        GameAction action = new MovePieceAction(fromIndex, toIndex, sequence);
        return action;
    }

    private boolean isStartEnterable(Color color) {
        return occupiedFields[startFieldIndex.get(color)] != color;
    }

    private boolean hasPiecesOnSpawn(Color color) {
        int spawnStartIndex = spawnFieldsStartIndex.get(color);
        for (int i = 0; i < 4; i++) {
            if (occupiedFields[spawnStartIndex + i] == color) {
                return true;
            }
        }
        return false;
    }

    private int findFirstOccupiedSpawnIndex(Color color) {
        int spawnStartIndex = spawnFieldsStartIndex.get(color);
        for (int i = 0; i < 4; i++) {
            int index = spawnStartIndex + i;
            if (occupiedFields[index] == color) {
                return index;
            }
        }
        return NO_INDEX;
    }

    private int findFirstFreeSpawnIndex(Color color) {
        int spawnStartIndex = spawnFieldsStartIndex.get(color);
        for (int i = 0; i < 4; i++) {
            int index = spawnStartIndex + i;
            if (occupiedFields[index] == null) {
                return index;
            }
        }
        return NO_INDEX;
    }

    public Color getWinnerColor() {
        return winnerColor;
    }

    public boolean isFinished() {
        return getWinnerColor() != null;
    }

    private void endTurn(Color color) {
        callbacks.forEach(callback -> callback.onTurnFinished(color));
    }

    @Override
    public void onActionSelected(GameAction action, GameActionSelector performer) {
        if (performer == getPlayerOnTurn()) {
            apply(action);
            if (currentPlayerHasExtraTurn) {
                getPlayerOnTurn().requestDiceResult();
            } else {
                endTurn(getPlayerOnTurnColor());
            }
        } else {
            logger.debug("Discarding action, player is not on turn");
        }
    }

    private void apply(GameAction action) {
        if (action instanceof MovePieceAction) {
            MovePieceAction movePieceAction = (MovePieceAction) action;
            movePiece(movePieceAction.getFromIndex(), movePieceAction.getToIndex(), movePieceAction.getSequence());
        }
    }

    private void movePiece(int fromIndex, int toIndex, int[] sequence) {
        Color movedColor = occupiedFields[fromIndex];
        if (movedColor == null) {
            throw new IllegalStateException("Starting field was empty");
        }
        Color capturedColor = occupiedFields[toIndex];
        if (movedColor == capturedColor) {
            throw new IllegalStateException("Target field occupied by same color");
        }
        if (capturedColor != null) {
            if (toIndex >= 40 && toIndex <= 55) {
                throw new IllegalStateException("Captured a safe piece");
            }
            //a piece has been captured on the target position
            callbacks.forEach(callback -> callback.onPieceCaptured(capturedColor, movedColor, toIndex));
            //move the captured piece back to spawn
            movePiece(toIndex, findFirstFreeSpawnIndex(capturedColor), NO_SEQUENCE);
        }
        //now both fields must be free, move the piece to the new index
        occupiedFields[fromIndex] = null;
        occupiedFields[toIndex] = movedColor;

        callbacks.forEach(callback -> callback.onPieceMoved(movedColor, fromIndex, toIndex, sequence));

        //the game will always end with a move to the last field of the house
        if (isWinner(movedColor)) {
            winnerColor = movedColor;
            callbacks.forEach(callback -> callback.onGameWon(winnerColor));
        }
    }

    private boolean isWinner(Color color) {
        int houseStart = houseStartIndex.get(color);
        for (int i = 0; i < 4; i++) {
            if (occupiedFields[houseStart + i] != color) {
                return false;
            }
        }
        return true;
    }
}
