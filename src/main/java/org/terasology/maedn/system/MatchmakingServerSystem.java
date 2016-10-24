package org.terasology.maedn.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.maedn.event.AfterBoardSpawnedEvent;
import org.terasology.maedn.event.MaednGameComponent;
import org.terasology.maedn.event.CreateMaednGameEvent;
import org.terasology.maedn.event.StartMatchmakingEvent;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;

import com.google.common.collect.Lists;

@RegisterSystem(RegisterMode.AUTHORITY)
public class MatchmakingServerSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(MatchmakingServerSystem.class);

    @ReceiveEvent()
    public void onStartMatchmaking(StartMatchmakingEvent event, EntityRef sender) {
        //TODO matchmaking logic
        CreateMaednGameEvent createGameEvent = new CreateMaednGameEvent();
        createGameEvent.setPlayers(Lists.newArrayList());
        sender.send(createGameEvent);
    }

    @ReceiveEvent(components = MaednGameComponent.class)
    public void onAfterBoardSpawned(AfterBoardSpawnedEvent event, EntityRef board) {
        MaednGameComponent boardComponent = board.getComponent(MaednGameComponent.class);
        Vector3i center = boardComponent.getCenter();
        for (EntityRef player : boardComponent.getHumanPlayers()) {
            logger.info("Teleporting {} to board", player);
            ClientComponent clientComp = player.getComponent(ClientComponent.class);
            clientComp.character.send(new CharacterTeleportEvent(center.toVector3f().addY(2)));
        }
    }
}
