package org.terasology.maedn.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.ServerEvent;

import java.util.ArrayList;
import java.util.List;

@ServerEvent
public class CreateMaednGameEvent implements Event {

    private List<EntityRef> players = new ArrayList<>();

    public List<EntityRef> getPlayers() {
        return players;
    }

    public void setPlayers(List<EntityRef> players) {
        this.players = players;
    }
}
