package dev.rm20.anglersalmanac.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.Database.StatUiPage;
import dev.rm20.anglersalmanac.MinigameManager.SkillCheck.LinearSkillPage;
import dev.rm20.anglersalmanac.MinigameManager.SkillCheck.SkillCheckPage;
import dev.rm20.anglersalmanac.registration.CommandInfo;

import javax.annotation.Nonnull;


@CommandInfo(
        name = "UITest",
        description = "Test the UI"
)
public class MinigameStartCommand extends AbstractPlayerCommand {
    public MinigameStartCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if(commandContext.sender() instanceof Player player)
        {
            PageManager pageManager = player.getPageManager();
            //SkillCheckPage minigamePage = new SkillCheckPage(playerRef);
            //LinearSkillPage minigamePage = new LinearSkillPage(playerRef);
            StatUiPage statUiPage = new StatUiPage(playerRef);
            pageManager.openCustomPage(ref, store, statUiPage);

        }
    }

}
