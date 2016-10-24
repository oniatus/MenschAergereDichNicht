package org.terasology.maedn.system;

import static org.terasology.maedn.Constants.MODULE_ID;
import static org.terasology.maedn.system.GameCreatorSystem.Field.D;
import static org.terasology.maedn.system.GameCreatorSystem.Field.EW;
import static org.terasology.maedn.system.GameCreatorSystem.Field.HB;
import static org.terasology.maedn.system.GameCreatorSystem.Field.HG;
import static org.terasology.maedn.system.GameCreatorSystem.Field.HR;
import static org.terasology.maedn.system.GameCreatorSystem.Field.HY;
import static org.terasology.maedn.system.GameCreatorSystem.Field.NE;
import static org.terasology.maedn.system.GameCreatorSystem.Field.NS;
import static org.terasology.maedn.system.GameCreatorSystem.Field.NW;
import static org.terasology.maedn.system.GameCreatorSystem.Field.SB;
import static org.terasology.maedn.system.GameCreatorSystem.Field.SE;
import static org.terasology.maedn.system.GameCreatorSystem.Field.SG;
import static org.terasology.maedn.system.GameCreatorSystem.Field.SR;
import static org.terasology.maedn.system.GameCreatorSystem.Field.SW;
import static org.terasology.maedn.system.GameCreatorSystem.Field.SY;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.maedn.MaednMath;
import org.terasology.maedn.event.AfterBoardSpawnedEvent;
import org.terasology.maedn.event.CreateMaednGameEvent;
import org.terasology.maedn.event.MaednGameComponent;
import org.terasology.maedn.logic.MaednGame;
import org.terasology.maedn.logic.MaednGame.Color;
import org.terasology.maedn.logic.MaednPlayer;
import org.terasology.maedn.logic.RandomMaednPlayer;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

@RegisterSystem(RegisterMode.AUTHORITY)
public class GameCreatorSystem extends BaseComponentSystem {

    private static final String PREFAB_PIECE_RED = MODULE_ID + ":pieceRed";
    private static final String PREFAB_PIECE_BLUE = MODULE_ID + ":pieceBlue";
    private static final String PREFAB_PIECE_GREEN = MODULE_ID + ":pieceGreen";
    private static final String PREFAB_PIECE_YELLOW = MODULE_ID + ":pieceYellow";

    public static enum Field {
        //empty
        D,
        //home & spawn
        HR, HB, HG, HY,
        //start_fields
        SR, SB, SG, SY,
        //fields
        EW, NE, NS, NW, SE, SW
    }

    private static Field[][] BOARD = new Field[][] {
            // @formatter:off
            {D, D, D, D, D, D, D, D, D, D, D, D, D, D, D},
            {D, D, D, D, D, D, D, D, D, D, D, D, D, D, D},
            {D, D, HR, HR, D, D, SE, EW, SB, D, D, HB, HB, D, D},
            {D, D, HR, HR, D, D, NS, HB, NS, D, D, HB, HB, D, D},
            {D, D, D, D, D, D, NS, HB, NS, D, D, D, D, D, D},
            {D, D, D, D, D, D, NS, HB, NS, D, D, D, D, D, D},
            {D, D, SR, EW, EW, EW, NW, HB, NE, EW, EW, EW, SW, D, D},
            {D, D, NS, HR, HR, HR, HR, D, HG, HG, HG, HG, NS, D, D},
            {D, D, NE, EW, EW, EW, SE, HY, SE, EW, EW, EW, SG, D, D},
            {D, D, D, D, D, D, NS, HY, NS, D, D, D, D, D, D},
            {D, D, D, D, D, D, NS, HY, NS, D, D, D, D, D, D},
            {D, D, HY, HY, D, D, NS, HY, NS, D, D, HG, HG, D, D},
            {D, D, HY, HY, D, D, SY, EW, NW, D, D, HG, HG, D, D},
            {D, D, D, D, D, D, D, D, D, D, D, D, D, D, D},
            {D, D, D, D, D, D, D, D, D, D, D, D, D, D, D},
            // @formatter:on    
    };

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    @In
    private EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(GameCreatorSystem.class);

    private Block boardDefault;
    private Block boardEW;
    private Block boardNE;
    private Block boardNS;
    private Block boardNW;
    private Block boardSE;
    private Block boardSW;
    private Block homeBlue;
    private Block homeGreen;
    private Block homeRed;
    private Block homeYellow;
    private Block spawnBlue;
    private Block spawnGreen;
    private Block spawnRed;
    private Block spawnYellow;
    private Block simpleGlass;
    private Block air;

    @Override
    public void initialise() {
        boardDefault = blockManager.getBlock(MODULE_ID + ":boardDefault");
        boardEW = blockManager.getBlock(MODULE_ID + ":boardEW");
        boardNE = blockManager.getBlock(MODULE_ID + ":boardNE");
        boardNS = blockManager.getBlock(MODULE_ID + ":boardNS");
        boardNW = blockManager.getBlock(MODULE_ID + ":boardNW");
        boardSE = blockManager.getBlock(MODULE_ID + ":boardSE");
        boardSW = blockManager.getBlock(MODULE_ID + ":boardSW");
        homeBlue = blockManager.getBlock(MODULE_ID + ":homeBlue");
        homeGreen = blockManager.getBlock(MODULE_ID + ":homeGreen");
        homeRed = blockManager.getBlock(MODULE_ID + ":homeRed");
        homeYellow = blockManager.getBlock(MODULE_ID + ":homeYellow");
        spawnBlue = blockManager.getBlock(MODULE_ID + ":spawnBlue");
        spawnGreen = blockManager.getBlock(MODULE_ID + ":spawnGreen");
        spawnRed = blockManager.getBlock(MODULE_ID + ":spawnRed");
        spawnYellow = blockManager.getBlock(MODULE_ID + ":spawnYellow");
        simpleGlass = blockManager.getBlock(MODULE_ID + ":simpleGlass");
        air = blockManager.getBlock("engine:air");
    }

    @ReceiveEvent
    public void onCreateGameEvent(CreateMaednGameEvent event, EntityRef entity) {
        //TODO add MaednPlayer for humans
        List<MaednPlayer> maednPlayers = new ArrayList<>();
        for (EntityRef player : event.getPlayers()) {

        }
        //TODO add ai which throws a dice
        for (int i = 0; i < 4 - event.getPlayers().size(); i++) {
            maednPlayers.add(new RandomMaednPlayer());
        }
        Color[] colors = Color.values();
        if (maednPlayers.size() != colors.length) {
            throw new RuntimeException("There must be one player for each color");
        }
        Map<Color, MaednPlayer> playerColors = new EnumMap<>(Color.class);
        for (int i = 0; i < maednPlayers.size(); i++) {
            playerColors.put(colors[i], maednPlayers.get(i));
        }

        MaednGame game = new MaednGame(Color.RED, playerColors);

        Vector3i spawnPosition = findSpawnPosition();
        Map<Vector3i, Block> blocksToSpawn = createBoardBlocks(spawnPosition);
        MaednGameComponent boardComponent = new MaednGameComponent();
        boardComponent.setGame(game);
        boardComponent.setBlockPositions(new ArrayList<>(blocksToSpawn.keySet()));
        boardComponent.setCenter(spawnPosition);
        boardComponent.setHumanPlayers(event.getPlayers());
        worldProvider.setBlocks(blocksToSpawn);
        Map<Integer, EntityRef> spawnedEntities = spawnBoardPieces(spawnPosition, game);
        boardComponent.setPieces(spawnedEntities);
        EntityRef boardEntity = entityManager.create(boardComponent);
        boardEntity.setAlwaysRelevant(true);

        //temporary: as long as there are no ai entities, use the board entity as ai entity
        List<EntityRef> aiEntities = new ArrayList<>();
        for (int i = 0; i < 4 - event.getPlayers().size(); i++) {
            EntityRef aiEntity = entityManager.create(new LocationComponent(spawnPosition.toVector3f()));
            aiEntity.setOwner(boardEntity);
            aiEntities.add(aiEntity);
        }
        boardComponent.setAiEntities(aiEntities);
        boardEntity.saveComponent(boardComponent);

        logger.info("New Board spawned at {}", spawnPosition);
        boardEntity.send(new AfterBoardSpawnedEvent());
    }

    private Map<Integer, EntityRef> spawnBoardPieces(Vector3i spawnPosition, MaednGame game) {
        //TODO spawn pieces based on indexes and maedMath
        Map<Integer, EntityRef> all = new HashMap<>();

        for (Color color : MaednGame.Color.values()) {
            String prefab = getPiecePrefabForColor(color);
            for (int index : game.getPiecesIndexes(color)) {
                Vector2i position = MaednMath.boardIndexToPosition(index);
                EntityRef piece = spawnRelativeToSpawnpoint(prefab, spawnPosition, position);
                all.put(index, piece);
            }
        }
        return all;
    }

    private String getPiecePrefabForColor(Color color) {
        switch (color) {
            case BLUE:
                return PREFAB_PIECE_BLUE;
            case GREEN:
                return PREFAB_PIECE_GREEN;
            case RED:
                return PREFAB_PIECE_RED;
            case YELLOW:
                return PREFAB_PIECE_YELLOW;
            default:
                throw new IllegalArgumentException();

        }
    }

    private EntityRef spawnRelativeToSpawnpoint(String prefabName, Vector3i spawnPosition, Vector2i delta) {
        //shift the pieces by halve size of the board, so the center of the board is at the spawn position
        EntityRef entity = entityManager.create(prefabName);
        LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
        //shift entity to the spawn point (keep prefab delta) and add offsets
        Vector3f newWorldPosition = locationComponent.getWorldPosition().add(spawnPosition.toVector3f()).addX(delta.getX() + 0.3f).addZ(delta.getY() + 0.2f)
                .addY(0.3f);
        if (prefabName.equals(PREFAB_PIECE_RED)) {
            logger.info("Piece: {},{}", newWorldPosition.getX(), newWorldPosition.getZ());
        }
        locationComponent.setWorldPosition(newWorldPosition);
        entity.saveComponent(locationComponent);
        return entity;
    }

    private Map<Vector3i, Block> createBoardBlocks(Vector3i spawnPosition) {
        Map<Vector3i, Block> blocksToPlace = new HashMap<>();
        //shift the board by halve size, so the center of the board is at the spawn position
        int xOffset = spawnPosition.getX() - BOARD.length / 2;
        int y = spawnPosition.getY();
        int zOffset = spawnPosition.getZ() - BOARD.length / 2;
        //fill area with blank blocks
        for (int x = 0; x < BOARD.length; x++) {
            for (int z = 0; z < BOARD.length; z++) {
                //board is stored row-whise
                Field field = BOARD[z][x];
                Block block = getBlockForField(field);
                int xPos = x + xOffset;
                int zPos = z + zOffset;
                if (field == HR) {
                    logger.info("spawn: {},{}", xPos, zPos);
                }
                blocksToPlace.put(new Vector3i(xPos, y, zPos), block);
                if (isBorder(x, z)) {
                    for (int y2 = 1; y2 < 5; y2++) {
                        blocksToPlace.put(new Vector3i(xPos, y + y2, zPos), simpleGlass);
                    }
                }
            }
        }

        return blocksToPlace;

    }

    private boolean isBorder(int x, int z) {
        return x == 0 || z == 0 || x == BOARD.length - 1 || z == BOARD.length - 1;
    }

    private Block getBlockForField(Field field) {
        switch (field) {

            case D:
                return boardDefault;
            case HB:
                return homeBlue;
            case HG:
                return homeGreen;
            case HR:
                return homeRed;
            case HY:
                return homeYellow;
            case EW:
                return boardEW;
            case NE:
                return boardNE;
            case NS:
                return boardNS;
            case NW:
                return boardNW;
            case SE:
                return boardSE;
            case SW:
                return boardSW;
            case SR:
                return spawnRed;
            case SG:
                return spawnGreen;
            case SB:
                return spawnBlue;
            case SY:
                return spawnYellow;
            default:
                throw new IllegalArgumentException("Unknown field: " + field);
        }
    }

    @SuppressWarnings("unchecked")
    @Command(requiredPermission = PermissionManager.NO_PERMISSION)
    public void destroyAllBoards(@Sender EntityRef sender) {
        for (EntityRef boardEntity : entityManager.getEntitiesWith(MaednGameComponent.class)) {
            MaednGameComponent boardComponent = boardEntity.getComponent(MaednGameComponent.class);
            //destroy pieces
            boardComponent.getPieces().values().stream().forEach(x -> x.destroy());
            //remove blocks
            Map<Vector3i, Block> blocksToRemove = boardComponent.getBlockPositions().stream().collect(Collectors.toMap(position -> position, position -> air));
            worldProvider.setBlocks(blocksToRemove);
            //destroy the board holding entity itself
            boardEntity.destroy();
        }

    }

    private Vector3i findSpawnPosition() {
        //TODO find positions for multiple boards
        return new Vector3i(0, -10, 0);
    }
}
