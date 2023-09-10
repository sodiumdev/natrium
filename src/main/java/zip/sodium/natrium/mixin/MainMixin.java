package zip.sodium.natrium.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Main.class})
public class MainMixin {
  @Inject(method = "main", at = @At("HEAD"), remap = false)
  private static void natrium$fastMath(String[] args, CallbackInfo info) {
    System.setProperty("joml.fastmath", "true");
    System.setProperty("joml.sinLookup", "true");
  }
}