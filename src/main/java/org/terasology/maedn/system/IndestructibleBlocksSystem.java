package org.terasology.maedn.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.BeforeDestroyEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class IndestructibleBlocksSystem extends BaseComponentSystem {

    @ReceiveEvent
    public void onBeforeDestroy(BeforeDestroyEvent event, EntityRef sender) {
        event.consume();
    }
}
