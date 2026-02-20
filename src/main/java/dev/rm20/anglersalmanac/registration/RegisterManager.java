package dev.rm20.anglersalmanac.registration;

import com.google.common.reflect.ClassPath;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;

import static com.hypixel.hytale.server.core.command.system.AbstractCommand.LOGGER;

public class RegisterManager {

    private static final String PACKAGE_NAME = "dev.rm20.anglersalmanac.commands";

    public static void registerCommands(AnglersAlmanac plugin) {
        try {
            ClassPath classPath = ClassPath.from(plugin.getClass().getClassLoader());
            int count = 0;

            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(PACKAGE_NAME)) {
                Class<?> loadedClass = classInfo.load();
                if (AbstractCommand.class.isAssignableFrom(loadedClass) && loadedClass.isAnnotationPresent(CommandInfo.class)) {
                    CommandInfo info = loadedClass.getAnnotation(CommandInfo.class);
                    try {
                        AbstractCommand command = (AbstractCommand) loadedClass
                                .getConstructor(String.class, String.class)
                                .newInstance(info.name(), info.description());

                        // Register
                        plugin.getCommandRegistry().registerCommand(command);

                        plugin.getLogger().atInfo().log("Successfully registered command: " + info.name());
                        count++;
                    } catch (Exception e) {
                        plugin.getLogger().atInfo().log("Could not register command class: " + loadedClass.getSimpleName());
                        e.printStackTrace();
                    }
                }
            }
            plugin.getLogger().atInfo().log("Registered " + count + " commands automatically.");
        } catch (Exception e) {
            plugin.getLogger().atInfo().log("Failed to load command classes.");
            e.printStackTrace();
        }
    }


}