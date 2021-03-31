package thatguy.mod.miningspeed2;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.swing.text.StyleConstants;

public class PacketModeToggle implements IMessage
{
    @Override
    public void fromBytes(ByteBuf buf)
    {

    }

    @Override
    public void toBytes(ByteBuf buf)
    {

    }

    public static class Handler implements IMessageHandler<PacketModeToggle, IMessage>
    {
        private final static ITextComponent ENABLED_TEXT_COMPONENT = new TextComponentString("Enabled");
        private final static ITextComponent DISABLED_TEXT_COMPONENT = new TextComponentString("Disabled");

        static
        {
            ENABLED_TEXT_COMPONENT.getStyle().setColor(TextFormatting.GREEN);
            DISABLED_TEXT_COMPONENT.getStyle().setColor(TextFormatting.RED);
        }


        @Override
        public IMessage onMessage(PacketModeToggle message, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack item = player.getHeldItemMainhand();

            if(!item.hasTagCompound())
            {
                item.setTagCompound(new NBTTagCompound());
            }

            NBTTagCompound tag = item.getTagCompound();

            if(!tag.hasKey(Reference.MINING_CONTROL_ENABLED_TAG))
            {
                tag.setBoolean(Reference.MINING_CONTROL_ENABLED_TAG, sendPlayerStatusMessage(player, true));
                return null;
            }

            tag.setBoolean(Reference.MINING_CONTROL_ENABLED_TAG, sendPlayerStatusMessage(player, !tag.getBoolean(Reference.MINING_CONTROL_ENABLED_TAG)));

            return null;
        }

        private boolean sendPlayerStatusMessage(EntityPlayerMP player, boolean enabled)
        {
            player.sendMessage(new TextComponentString("Mining control is now ").appendSibling(
                    (enabled ? ENABLED_TEXT_COMPONENT : DISABLED_TEXT_COMPONENT)).appendSibling(new TextComponentString(" for this tool")));

            player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.4F, 1.0F - 0.1F * (enabled ? 1 : 2));

            return enabled;
        }
    }
}
