package dev.fakesb.mixin;

import dev.fakesb.FakeText;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Team.class)
public abstract class TeamMixin {
    @Inject(method = "decorateName(Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;",
            at = @At("RETURN"), cancellable = true, require = 0)
    private void fakesb$decorate(Text name, CallbackInfoReturnable<MutableText> cir) {
        cir.setReturnValue(FakeText.transform(cir.getReturnValue()));
    }
}
