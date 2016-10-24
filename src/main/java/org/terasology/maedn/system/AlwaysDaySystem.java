package org.terasology.maedn.system;

import java.util.concurrent.TimeUnit;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

@RegisterSystem(RegisterMode.CLIENT)
public class AlwaysDaySystem extends BaseComponentSystem {

    private static final float NOON_DAY_TIME = 1.5f;
    private static final long INITIAL_DELAY = TimeUnit.SECONDS.toMillis(1);
    private static final long ACTION_SCHEDULE_RATE = TimeUnit.SECONDS.toMillis(3);

    private static final String SET_DAY_TIME_ACTION = "ACTION_SET_DAY_TIME";

    @In
    private WorldProvider worldProvider;

    @In
    private DelayManager delayManager;

    @In
    private EntityManager entityManager;

    @Override
    public void postBegin() {
        EntityRef entity = entityManager.create();
        entity.setAlwaysRelevant(true);
        delayManager.addPeriodicAction(entity, SET_DAY_TIME_ACTION, INITIAL_DELAY, ACTION_SCHEDULE_RATE);
    }

    @ReceiveEvent
    public void onPeriodicAction(PeriodicActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(SET_DAY_TIME_ACTION)) {
            worldProvider.getTime().setDays(NOON_DAY_TIME);
        }
    }

}
