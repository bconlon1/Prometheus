package earth.terrarium.prometheus.mixin.common;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.DataResult;
import earth.terrarium.prometheus.common.handlers.CustomPlayerDataHandler;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(PlayerDataStorage.class)
public class PlayerDataStorageMixin implements CustomPlayerDataHandler {

    @Shadow @Final private File playerDir;

    @Shadow @Final protected DataFixer fixerUpper;

    @Override
    public DataResult<CompoundTag> prometheus$edit(UUID uuid, Consumer<CompoundTag> editor) {
        CompoundTag tag;

        try {
            File file = new File(this.playerDir, uuid.toString() + ".dat");
            if (file.exists() && file.isFile()) {
                tag = NbtIo.readCompressed(file);
            } else {
                return DataResult.error(() -> "Player data file for " + uuid + " does not exist");
            }
        } catch (Exception var4) {
            return DataResult.error(() -> "Failed to load player data for " + uuid);
        }

        int i = NbtUtils.getDataVersion(tag, -1);
        tag = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, tag, i);

        editor.accept(tag);
        try {
            File file = File.createTempFile(uuid + "-", ".dat", this.playerDir);
            NbtIo.writeCompressed(tag, file);
            File file2 = new File(this.playerDir, uuid + ".dat");
            File file3 = new File(this.playerDir, uuid + ".dat_old");
            Util.safeReplaceFile(file2, file, file3);
        } catch (Exception var6) {
            return DataResult.error(() -> "Failed to save player data for " + uuid);
        }
        return DataResult.success(tag);
    }
}
