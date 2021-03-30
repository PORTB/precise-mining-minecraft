package thatguy.mod.miningspeed2;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.UUID;
import java.util.function.Supplier;

import static thatguy.mod.miningspeed2.MiningSpeed.MINING_SPEED_CONTROL_ENABLED_TAG;

public class Networking
{
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MiningSpeed.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register()
    {
        //INSTANCE.registerMessage(0, NBTUpdateMessage.class, )
        INSTANCE.messageBuilder(ToggleMiningControlMessage.class, 0)
                .encoder((updateMessage, packetBuffer) -> {})
                .decoder(buf -> new ToggleMiningControlMessage())
                .consumer(ToggleMiningControlMessage::handle)
                .add();
    }

    public static void sendToServer(Object object)
    {
        INSTANCE.sendToServer(object);
    }

    static class ToggleMiningControlMessage
    {
        private final static TextComponent ENABLED_TEXT_COMPONENT = new StringTextComponent("Enabled");
        private final static TextComponent DISABLED_TEXT_COMPONENT = new StringTextComponent("Disabled");

        static
        {
            ENABLED_TEXT_COMPONENT.mergeStyle(TextFormatting.GREEN);
            DISABLED_TEXT_COMPONENT.mergeStyle(TextFormatting.RED);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() ->
            {
                ServerPlayerEntity player = ctx.get().getSender();

                if(player == null)
                    return;

                ItemStack stack = player.getHeldItemMainhand();

                if(stack.getTag() == null)
                    stack.setTag(new CompoundNBT());

                boolean isMiningControlEnabled = !stack.getTag().getBoolean(MINING_SPEED_CONTROL_ENABLED_TAG);
                stack.getTag().putBoolean(MINING_SPEED_CONTROL_ENABLED_TAG, isMiningControlEnabled);

                player.sendMessage(
                        new StringTextComponent("Mining control is now ")
                                .append((isMiningControlEnabled ? ENABLED_TEXT_COMPONENT : DISABLED_TEXT_COMPONENT)).mergeStyle(TextFormatting.RESET)
                                .append(new StringTextComponent(" for this tool")), Util.DUMMY_UUID);

                player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.4F, 1.0F - 0.1F * (isMiningControlEnabled ? 1 : 2));
            });
        }
    }
}
