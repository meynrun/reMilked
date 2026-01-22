package dev.meynrun.remilked.neoforge;

import dev.meynrun.remilked.Remilked;
import net.neoforged.fml.common.Mod;

@Mod(Remilked.MOD_ID)
public final class RemilkedNeoForge {
    public RemilkedNeoForge() {
        // Run our common setup.
        Remilked.init();
    }
}
