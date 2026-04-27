package com.armilp.ifreq.client.keys;

import com.armilp.ifreq.MainEZ;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    // Create a custom category for your mod
    public static final String CATEGORY = "key.category." + MainEZ.MODID;

    public static KeyBinding OPEN_WALKIE_GUI = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key." + MainEZ.MODID + ".open_walkie_gui",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_R,
                    CATEGORY
            )
    );

    public static void register() {
        // Registration happens automatically when KeyBindingHelper.registerKeyBinding is called
        // This method is just for initialization if needed
    }
}