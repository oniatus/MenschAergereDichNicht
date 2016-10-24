
package org.terasology.maedn.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.maedn.MaednMath;
import org.terasology.maedn.event.AfterBoardSpawnedEvent;
import org.terasology.maedn.event.MaednGameComponent;
import org.terasology.maedn.logic.MaednGame;
import org.terasology.maedn.logic.MaednGame.Color;
import org.terasology.maedn.logic.MaednGameCallback;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.AUTHORITY)
public class GamePresenterSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(GamePresenterSystem.class);
    private List<MaednGame> gamesToPlayOneTurn = new ArrayList<>();

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    private EntityRef updateEntity;

    private final class GameUpdaterCallback implements MaednGameCallback {

        private final MaednGame game;
        private final Map<Integer, EntityRef> pieces;

        public GameUpdaterCallback(MaednGame game, Map<Integer, EntityRef> pieces) {
            this.game = game;
            this.pieces = pieces;
        }

        @Override
        public void onTurnFinished(Color color) {
            if (!game.isFinished()) {
                //unlikely, that this needs to be thread safe but better be sure :)
                synchronized (gamesToPlayOneTurn) {
                    gamesToPlayOneTurn.add(game);
                }
            }
        }

        @Override
        public void onPieceMoved(Color color, int fromIndex, int toIndex, int[] sequence) {
            EntityRef piece = pieces.remove(fromIndex);
            LocationComponent component = piece.getComponent(LocationComponent.class);
            Vector2i shift = MaednMath.boardIndexToPosition(toIndex).sub(MaednMath.boardIndexToPosition(fromIndex));
            logger.info("Piece moved from {}->{} to {}->{}, position is {}, shift is {}", fromIndex, MaednMath.boardIndexToPosition(fromIndex), toIndex,
                    MaednMath.boardIndexToPosition(toIndex), component.getWorldPosition(), shift);
            component.setWorldPosition(component.getWorldPosition().add(shift.getX(), 0, shift.getY()));
            pieces.put(toIndex, piece);
        }

        @Override
        public void onPieceCaptured(Color capturedColor, Color fromColor, int onIndex) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onHasExtraTurn(Color color) {
            logger.info("{} has an extra turn", color);
        }

        @Override
        public void onGameWon(Color color) {
            logger.info("{} won", color);
        }
    }

    private String ACTION_UPDATE_GAMES = "ACTION_UPDATE_GAMES";

    @ReceiveEvent
    public void onUpdateGames(PeriodicActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(ACTION_UPDATE_GAMES)) {
            synchronized (gamesToPlayOneTurn) {
                List<MaednGame> toUpdate = gamesToPlayOneTurn;
                gamesToPlayOneTurn = new ArrayList<>();
                for (MaednGame game : toUpdate) {
                    game.playOneTurn();
                }
            }
        }
    }

    @ReceiveEvent(components = MaednGameComponent.class)
    public void onAfterBoardSpawned(AfterBoardSpawnedEvent event, EntityRef entity) {
        if (updateEntity == null) {
            updateEntity = entityManager.create();
            updateEntity.setAlwaysRelevant(true);
            delayManager.addPeriodicAction(updateEntity, ACTION_UPDATE_GAMES, 1000, 1000);
        }
        MaednGameComponent gameComponent = entity.getComponent(MaednGameComponent.class);
        gameComponent.getGame().register(new GameUpdaterCallback(gameComponent.getGame(), gameComponent.getPieces()));
        gamesToPlayOneTurn.add(gameComponent.getGame());
    }

}
