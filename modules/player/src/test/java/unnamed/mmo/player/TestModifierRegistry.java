package unnamed.mmo.player;

import org.junit.jupiter.api.Test;
import unnamed.mmo.modifiers.ModifierType;

import static org.junit.jupiter.api.Assertions.*;

public class TestModifierRegistry {

    @Test
    public void loadRegistry() {
        System.out.println(ModifierType.REGISTRY.values());
        assertTrue(ModifierType.doesModifierExist("luck"));
        assertEquals(ModifierType.getBaseValue("potatoes"), 10d);
    }

}
