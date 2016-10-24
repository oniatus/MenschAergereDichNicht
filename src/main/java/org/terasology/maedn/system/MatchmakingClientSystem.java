package org.terasology.maedn.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.maedn.event.StartMatchmakingEvent;

@RegisterSystem(RegisterMode.CLIENT)
public class MatchmakingClientSystem extends BaseComponentSystem {
    @Command(requiredPermission = PermissionManager.NO_PERMISSION)
    public void startMADN(@Sender EntityRef sender) {
        sender.send(new StartMatchmakingEvent());
    }
}
