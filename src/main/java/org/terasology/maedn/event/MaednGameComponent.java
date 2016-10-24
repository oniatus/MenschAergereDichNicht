package org.terasology.maedn.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.maedn.logic.MaednGame;
import org.terasology.math.geom.Vector3i;

public class MaednGameComponent implements Component {

    private List<Vector3i> blockPositions = new ArrayList<>();
    private Map<Integer, EntityRef> pieces = new HashMap<>();
    private List<EntityRef> humanPlayers = new ArrayList<>();
    private List<EntityRef> aiEntities = new ArrayList<>();
    private Vector3i center;
    private transient MaednGame game;

    public List<Vector3i> getBlockPositions() {
        return blockPositions;
    }

    public void setBlockPositions(List<Vector3i> blockPosition) {
        this.blockPositions = blockPosition;
    }

    public Map<Integer, EntityRef> getPieces() {
        return pieces;
    }

    public void setPieces(Map<Integer, EntityRef> spawnedEntities) {
        this.pieces = spawnedEntities;
    }

    public Vector3i getCenter() {
        return center;
    }

    public void setCenter(Vector3i center) {
        this.center = center;
    }

    public List<EntityRef> getHumanPlayers() {
        return humanPlayers;
    }

    public void setHumanPlayers(List<EntityRef> humanPlayers) {
        this.humanPlayers = humanPlayers;
    }

    public List<EntityRef> getAiEntities() {
        return aiEntities;
    }

    public void setAiEntities(List<EntityRef> aiEntities) {
        this.aiEntities = aiEntities;
    }

    public MaednGame getGame() {
        return game;
    }

    public void setGame(MaednGame game) {
        this.game = game;
    }

}
