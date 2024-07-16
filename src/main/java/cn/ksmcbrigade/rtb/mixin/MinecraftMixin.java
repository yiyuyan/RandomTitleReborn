package cn.ksmcbrigade.rtb.mixin;

import cn.ksmcbrigade.rtb.RandomTitleReborn;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Unique
    private final String randomTitleReborn$randomTitle = RandomTitleReborn.getTitle();

    @Shadow
    public abstract boolean isConnectedToRealms();

    @Shadow @Nullable public abstract ClientPacketListener getConnection();

    @Shadow @Nullable public abstract ServerData getCurrentServer();

    @Shadow @Nullable private IntegratedServer singleplayerServer;

    @Shadow
    static Minecraft instance;

    @Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
    private void getWindowTitle(CallbackInfoReturnable<String> ci) {
        if(instance==null) return;
        try {
            String title = RandomTitleReborn.Get("format");

            StringBuilder stringBuilder = new StringBuilder(SharedConstants.getCurrentVersion().getName());
            stringBuilder.append(" ");

            ClientPacketListener clientPlayNetworkHandler = this.getConnection();

            if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getConnection().isConnected()) {
                stringBuilder.append(" - ");
                if (this.singleplayerServer != null && !this.singleplayerServer.isPublished()) {
                    stringBuilder.append(I18n.get("title.singleplayer"));
                } else if (this.isConnectedToRealms()) {
                    stringBuilder.append(I18n.get("title.multiplayer.realms"));
                } else if (this.singleplayerServer == null && (this.getCurrentServer() == null || !this.getCurrentServer().isLan())) {
                    stringBuilder.append(I18n.get("title.multiplayer.other"));
                } else {
                    stringBuilder.append(I18n.get("title.multiplayer.lan"));
                }

            }
            String date = new SimpleDateFormat(RandomTitleReborn.Get("dateformat")).format((System.currentTimeMillis()));

            title = title.replace("%date%", date);
            title = title.replace("%title%", randomTitleReborn$randomTitle);
            title = title.replace("%version%", stringBuilder.toString());
            title = title.replace("%mod%", String.valueOf(ModList.get().getMods().size()));

            ci.setReturnValue(title);
            ci.cancel();
        }
        catch (Exception e){
            RandomTitleReborn.LOGGER.error("Failed in set the window title {}",e.getMessage(),e);
        }
    }
}
