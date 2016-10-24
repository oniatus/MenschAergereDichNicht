
package org.terasology.maedn.logic;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.maedn.logic.MaednGame.Color;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import com.google.common.collect.Maps;

public class TestMaednGame {
    private static final Logger logger = LoggerFactory.getLogger(TestMaednGame.class);

    private static Random random = new FastRandom();

    private final class TestLoggingCallback implements MaednGameCallback {
        private MaednGame game;

        public TestLoggingCallback(MaednGame game) {
            this.game = game;
        }

        @Override
        public void onTurnFinished(Color color) {
            logger.info("### {} finished turn ###", color.name());
            //            printPieces();
        }

        @Override
        public void onPieceCaptured(Color capturedColor, Color fromColor, int onIndex) {
            logger.info("\t{} captured a piece from {} on field {}", capturedColor, fromColor, onIndex);

        }

        @Override
        public void onPieceMoved(Color color, int fromIndex, int toIndex, int[] sequence) {
            logger.info("\tA {} piece moved from field {} to field {} with sequence {}", color.name(), fromIndex, toIndex, sequence);
            //            printPieces();
        }

        @Override
        public void onHasExtraTurn(Color color) {
            logger.info("\t{} has an extra turn", color);

        }

        @Override
        public void onGameWon(Color color) {
            logger.info("!!! {} won the game", color);
        }

        private void printPieces() {
            for (Color color : Color.values()) {
                logger.info("\t\tPieces from {} - {}", color, game.getPiecesIndexes(color));
            }
        }
    }

    private static class TestRandomMaednPlayer extends MaednPlayer {

        @Override
        public void requestDiceResult() {
            onDiceResult(random.nextInt(6) + 1);
        }

        @Override
        protected GameAction doSelectAction(List<GameAction> possibleActions) {
            return possibleActions.get(random.nextInt(possibleActions.size()));
        }

    }

    @Test
    public void playRandomGameUntilEnd() {

        long start = System.currentTimeMillis();
        int totalTurns = 0;
        int maxTurns = Integer.MIN_VALUE;
        int minTurns = Integer.MAX_VALUE;
        Map<Color, Integer> colorWinRate = new EnumMap<>(Color.class);
        int noWinner = 0;
        for (Color color : Color.values()) {
            colorWinRate.put(color, 0);
        }
        int numGames = 1000;
        for (int i = 0; i < numGames; i++) {
            int turns = 0;
            Map<Color, MaednPlayer> players = Maps.newEnumMap(Color.class);
            for (Color color : Color.values()) {
                players.put(color, new TestRandomMaednPlayer());
            }
            MaednGame game = new MaednGame(Color.RED, players);
            //            game.register(new TestLoggingCallback(game));
            while (!game.isFinished() && turns++ < 1000000) {
                game.playOneTurn();
                totalTurns++;

            }
            Color winnerColor = game.getWinnerColor();
            if (winnerColor == null) {
                noWinner++;
            } else {
                colorWinRate.put(winnerColor, colorWinRate.get(winnerColor) + 1);
            }
            maxTurns = turns > maxTurns ? turns : maxTurns;
            minTurns = turns < minTurns ? turns : minTurns;
        }
        long delta = System.currentTimeMillis() - start;
        double avgTurns = (double) totalTurns / numGames;

        logger.info("Games took {}ms, with {} avg turns, {} max turns, {} min turns ", delta, avgTurns, maxTurns, minTurns);
        logger.info("Wins per color:");
        for (Color color : Color.values()) {
            logger.info("{} - {}", color, colorWinRate.get(color));
        }
        logger.info("{} times no winner", noWinner);
    }
}
