package com.github.terminatornl.laggoggles;

import com.github.terminatornl.laggoggles.client.MessagePacketHandler;
import com.github.terminatornl.laggoggles.client.ProfileStatusHandler;
import com.github.terminatornl.laggoggles.client.ScanResultHandler;
import com.github.terminatornl.laggoggles.client.ServerDataPacketHandler;
import com.github.terminatornl.laggoggles.command.LagGogglesCommand;
import com.github.terminatornl.laggoggles.packet.*;
import com.github.terminatornl.laggoggles.profiler.ProfileResult;
import com.github.terminatornl.laggoggles.profiler.TickCounter;
import com.github.terminatornl.laggoggles.server.*;
import com.github.terminatornl.laggoggles.util.Perms;
import com.github.terminatornl.laggoggles.util.RunInServerThread;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.types.PermissionNode;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

public class CommonProxy {

    public static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(Main.MODID);

    private byte PACKET_ID = 0;

    public void preinit(FMLPreInitializationEvent e){
        LuckPermsProvider.get();
        PermissionNode.builder(ServerConfig.TELEPORT_PERMISSION).build();
    }

    public void init(FMLInitializationEvent e){}

    public void postinit(FMLPostInitializationEvent e){
        NETWORK_WRAPPER.registerMessage(
                ScanResultHandler.class,
                SPacketScanResult.class, PACKET_ID++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(
                ProfileStatusHandler.class,
                SPacketProfileStatus.class, PACKET_ID++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(
                ServerDataPacketHandler.class,
                SPacketServerData.class, PACKET_ID++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(
                RequestDataHandler.class,
                CPacketRequestServerData.class, PACKET_ID++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(
                MessagePacketHandler.class,
                SPacketMessage.class, PACKET_ID++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(
                ScanRequestHandler.class,
                CPacketRequestScan.class, PACKET_ID++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(
                TeleportRequestHandler.class,
                CPacketRequestEntityTeleport.class, PACKET_ID++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(
                TeleportToTileEntityRequestHandler.class,
                CPacketRequestTileEntityTeleport.class, PACKET_ID++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(
                RequestResultHandler.class,
                CPacketRequestResult.class, PACKET_ID++, Side.SERVER);
    }

    public static void sendTo(IMessage msg, EntityPlayerMP player){
        NETWORK_WRAPPER.sendTo(msg, player);
    }

    public static void sendTo(ProfileResult result, EntityPlayerMP player){
        List<SPacketScanResult> packets = Perms.getResultFor(player, result).createPackets(player);
        new RunInServerThread(new Runnable() {
            @Override
            public void run() {
                for (SPacketScanResult result : packets){
                    sendTo(result, player);
                }
            }
        });
    }

    public void serverStartingEvent(FMLServerStartingEvent e){
        e.registerServerCommand(new LagGogglesCommand());
        MinecraftForge.EVENT_BUS.register(new TickCounter());
        MinecraftForge.EVENT_BUS.register(new RequestDataHandler());
    }
}