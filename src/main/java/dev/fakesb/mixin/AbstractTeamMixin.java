package dev.fakesb.mixin;

import dev.fakesb.FakeText;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Pokrywa linie scoreboardu bez teamu (statyczny helper). */
@Mixin(AbstractTeam.class)
public abstract class AbstractTeamMixin {
    @Inject(method = "decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;",
            at = @At("RETURN"), cancellable = true, require = 0)
    private static void fakesb$decorate(AbstractTeam team, Text name, CallbackInfoReturnable<MutableText> cir) {
        cir.setReturnValue(FakeText.transform(cir.getReturnValue()));
    }
}
