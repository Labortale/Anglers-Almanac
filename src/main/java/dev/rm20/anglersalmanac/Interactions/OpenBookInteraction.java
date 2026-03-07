package dev.rm20.anglersalmanac.Interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ItemBase;
import com.hypixel.hytale.protocol.ItemTranslationProperties;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateTranslations;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AlmanacBook.BookPageManager;
import dev.rm20.anglersalmanac.Metadata.BookData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;


public class OpenBookInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<OpenBookInteraction> CODEC = BuilderCodec.builder(
            OpenBookInteraction.class, OpenBookInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        Ref<EntityStore> playerRef = context.getOwningEntity();
        ItemStack heldItem = context.getHeldItem();
        if (commandBuffer == null || playerRef == null || heldItem == null) return;
        Player player = commandBuffer.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;
        BookData data = heldItem.getFromMetadataOrNull(BookData.KEY, BookData.CODEC);
        if (data == null || data.getPlayerUUID().isEmpty()) {
            ItemStack newBook;
            UUIDComponent uuid = playerRef.getStore().getComponent(playerRef, UUIDComponent.getComponentType());
            if(uuid == null) return;
            BookData newData = new BookData();
            newData.setPlayerUUID(uuid.getUuid().toString());
            newData.setPlayerName(player.getDisplayName());
            newData.setPageNumber(0);
            newBook = heldItem.withMetadata(BookData.KEYED_CODEC, newData);
            byte slot = player.getInventory().getActiveHotbarSlot();
            player.getInventory().getHotbar().replaceItemStackInSlot(slot, heldItem, newBook);
            PlayerRef playerRef1 = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
            syncCustomBookDisplay(
                    playerRef1,
                    uuid.getUuid().toString(),
                    player.getDisplayName()
            );
            BookPageManager.OpenPage(player,0,newData.getPlayerUUID(),player.getDisplayName());
            //PageManager pageManager = player.getPageManager();
            //StatUiPage statUiPage = new StatUiPage(playerRef1,newData.getPlayerUUID(),player.getDisplayName());
            //.openCustomPage(playerRef, playerRef.getStore(), statUiPage);
        }
        else
        {
            PlayerRef playerRef1 = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
            //PageManager pageManager = player.getPageManager();
            //StatUiPage statUiPage = new StatUiPage(playerRef1,data.getPlayerUUID(),player.getDisplayName());
            //pageManager.openCustomPage(playerRef, playerRef.getStore(), statUiPage);
            BookPageManager.OpenPage(player,0,data.getPlayerUUID(),player.getDisplayName());

            syncCustomBookDisplay(
                    playerRef1,
                    data.getPlayerUUID(),
                    data.getPlayerName()
            );
        }

    }


    //todo
    public static void syncCustomBookDisplay(PlayerRef playerRef, String playerUuid, String playerName) {
        Item baseItem = Item.getAssetMap().getAsset("Almanac_Book");
        String customId = "Almanac_Book_" + playerUuid;
        //registerItemOnServer(customId, baseItem);

        ItemBase definition = baseItem.toPacket().clone();

        definition.id = customId;
        definition.translationProperties = new ItemTranslationProperties(
                "almanac.book."+playerName+".name",
                "almanac.book."+playerName+".description"
        );

        Map<String, String> translations = new HashMap<>();
        translations.put("almanac.book."+playerName+".name", playerName + "'s Angler's Almanac");
        translations.put("almanac.book."+playerName+".description", "<color is=\"#AAAAAA\">Bound to ID:</color>\n<i>" + playerUuid + "</i>");

        UpdateTranslations packet = new UpdateTranslations();
        packet.type = UpdateType.AddOrUpdate;
        packet.translations = translations;
        playerRef.getPacketHandler().writeNoCache(packet);

//        UpdateItems itemPacket = new UpdateItems();
//        itemPacket.items = new HashMap<>();
//        itemPacket.items.put(customId, definition);
//        playerRef.getPacketHandler().writeNoCache(itemPacket);
    }
}
