package org.terasology.maedn.system;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.JumpEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.maedn.Constants;
import org.terasology.maedn.event.DiceThrowResultEvent;
import org.terasology.maedn.event.DiceThrowerComponent;
import org.terasology.maedn.event.ThrowDiceEvent;
import org.terasology.math.Direction;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.components.RigidBodyComponent;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RegisterSystem(RegisterMode.AUTHORITY)
public class DiceSystem extends BaseComponentSystem {

    private static final String DICE_PREFAB_NAME = Constants.MODULE_ID + ":dice";

    private static final String ACTION_POLL_DICE_STATUS = "ACTION_POLL_DICE";
    private static final long POLL_DICE_DELAY_MS = TimeUnit.MILLISECONDS.toMillis(500);

    private static final Logger LOG = LoggerFactory.getLogger(DiceSystem.class);

    private static final Map<Direction, Integer> DICE_VALUE_MAPPINGS = Maps.newEnumMap(Direction.class);

    static {
        DICE_VALUE_MAPPINGS.put(Direction.DOWN, 1);
        DICE_VALUE_MAPPINGS.put(Direction.RIGHT, 2);
        DICE_VALUE_MAPPINGS.put(Direction.BACKWARD, 3);
        DICE_VALUE_MAPPINGS.put(Direction.FORWARD, 4);
        DICE_VALUE_MAPPINGS.put(Direction.LEFT, 5);
        DICE_VALUE_MAPPINGS.put(Direction.UP, 6);
    }

    @In
    private EntityManager entityManager;

    @In
    private PrefabManager prefabManager;

    @In
    private DelayManager delayManager;

    private Random random = new FastRandom();

    private Prefab dicePrefab;

    @Override
    public void initialise() {
        dicePrefab = prefabManager.getPrefab(DICE_PREFAB_NAME);
        throwSound = Assets.getSound(Constants.MODULE_ID + ":throw_dice").get();
    }

    private Map<EntityRef, Vector3f> diceVelocities = new HashMap<>();

    private StaticSound throwSound;

    @ReceiveEvent(components = {LocationComponent.class, DiceThrowerComponent.class})
    public void onJumpThrowDice(JumpEvent event, EntityRef throwingPlayer) {
        throwingPlayer.send(new ThrowDiceEvent());
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onThrowDice(ThrowDiceEvent event, EntityRef throwingPlayer) {
        LocationComponent playerLocation = throwingPlayer.getComponent(LocationComponent.class);
        Quat4f randomRotation = randomRotation();
        EntityRef diceEntity = entityManager.create(dicePrefab, new Vector3f(playerLocation.getWorldPosition()).addY(1),
                randomRotation);
        diceEntity.setOwner(throwingPlayer);
        LOG.info("Spawning dice");
        Vector3f velocity = new Vector3f(0, 20, 10);
        velocity = playerLocation.getWorldRotation().rotate(velocity);
        diceEntity.send(new ImpulseEvent(velocity));
        throwingPlayer.send(new PlaySoundEvent(throwSound, 1));

        delayManager.addDelayedAction(diceEntity, ACTION_POLL_DICE_STATUS, POLL_DICE_DELAY_MS);
        diceVelocities.put(diceEntity, new Vector3f(diceEntity.getComponent(RigidBodyComponent.class).velocity));
    }

    @ReceiveEvent()
    public void onPollDiceVelocity(DelayedActionTriggeredEvent event, EntityRef diceEntity) {
        if (event.getActionId().equals(ACTION_POLL_DICE_STATUS)) {
            Vector3f velocityBefore = diceVelocities.get(diceEntity);
            Vector3f velocity = diceEntity.getComponent(RigidBodyComponent.class).velocity;
            if (velocity.distance(velocityBefore) > 0) {
                //dice still moving
                velocityBefore.set(velocity);
                delayManager.addDelayedAction(diceEntity, ACTION_POLL_DICE_STATUS, 100);
            } else {
                //dice stopped
                Direction direction = findDiceTopDirection(diceEntity.getComponent(LocationComponent.class).getWorldRotation());
                int value = DICE_VALUE_MAPPINGS.get(direction);
                LOG.info("Dice rolled to {}", value);
                diceEntity.send(new DiceThrowResultEvent(value));
            }
        }
    }

    private Direction findDiceTopDirection(Quat4f worldRotation) {
        Vector3f top = Direction.UP.getVector3f();
        double minDistance = Double.POSITIVE_INFINITY;
        Direction mostTopDirection = null;
        for (Direction direction : Direction.values()) {
            Vector3f rotated = worldRotation.rotate(direction.getVector3f());
            if (rotated.equals(top)) {
                //if one side rotates to the top vector, the side is pointing directly upwards
                return direction;
            }
            double distance = rotated.distance(top);
            if (distance < minDistance) {
                //in all other cases, collect the side which is closest to pointing upwards (dice may be stuck in some way)
                minDistance = distance;
                mostTopDirection = direction;
            }
        }
        return mostTopDirection;
    }

    private Quat4f randomRotation() {
        return new Quat4f(random.nextFloat() * 360 * TeraMath.DEG_TO_RAD, random.nextFloat() * 360 * TeraMath.DEG_TO_RAD, random.nextFloat() * 360 * TeraMath.DEG_TO_RAD);
    }
}
