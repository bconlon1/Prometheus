package earth.terrarium.prometheus.mixin.common;

import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.prometheus.common.handlers.permission.CommandPermissionHandler;
import earth.terrarium.prometheus.common.handlers.permission.PermissionHolder;
import earth.terrarium.prometheus.common.handlers.role.RoleHandler;
import earth.terrarium.prometheus.common.network.NetworkHandler;
import earth.terrarium.prometheus.common.network.messages.client.CommandPermissionsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(Player.class)
public abstract class PermissionPlayerMixin extends LivingEntity implements PermissionHolder {

    @Unique
    private Map<String, TriState> prometheus$permissions;

    protected PermissionPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void prometheus$updatePermissions() {
        prometheus$permissions = RoleHandler.getPermissions((Player) ((Object) this));
        //noinspection ConstantValue
        if (getServer() != null && ((Object) this) instanceof ServerPlayer serverPlayer && NetworkHandler.CHANNEL.canSendPlayerPackets(serverPlayer)) {
            getServer().getCommands().sendCommands(serverPlayer);
            NetworkHandler.CHANNEL.sendToPlayer(new CommandPermissionsPacket(CommandPermissionHandler.COMMAND_PERMS), serverPlayer);
        }
    }

    @Override
    public TriState prometheus$hasPermission(String permission) {
        return prometheus$permissions.getOrDefault(permission, TriState.UNDEFINED);
    }

    @Override
    public Map<String, TriState> prometheus$getPermissions() {
        return prometheus$permissions;
    }
}
