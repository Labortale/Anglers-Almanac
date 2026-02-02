package dev.rm20.anglersalmanac.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.registration.CommandInfo;

import javax.annotation.Nonnull;

@CommandInfo(
        name = "SetMinigame",
        description = "set the default minigame in the config"
)
public class SetMinigameCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> minigameArg;
    public SetMinigameCommand(@Nonnull String name, @Nonnull String description) {
        super(name, description);
        this.minigameArg = this.withRequiredArg("minigame", "The minigame to set", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String choice = this.minigameArg.get(commandContext);

        if (choice == null) {
            commandContext.sendMessage(Message.raw("Please specify a minigame: TensionBar or NoMinigame"));
            return;
        }

        if (choice.equalsIgnoreCase("TensionBar")) {
            AnglersAlmanac.MOD_CONFIG.get().setMinigameToUse("TensionBar");
        } else if (choice.equalsIgnoreCase("NoMinigame")) {
            AnglersAlmanac.MOD_CONFIG.get().setMinigameToUse("NoMinigame");
        } else {
            commandContext.sendMessage(Message.raw("Invalid choice! Use 'TensionBar' or 'NoMinigame'."));
            return;
        }

        commandContext.sendMessage(Message.raw("Fishing minigame has been set to: " + AnglersAlmanac.MOD_CONFIG.get().getMinigameToUse()));
        AnglersAlmanac.MOD_CONFIG.save();
    }
}
