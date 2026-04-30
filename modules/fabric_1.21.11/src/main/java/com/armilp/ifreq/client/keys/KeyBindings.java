package com.armilp.ifreq.client.keys;

import com.armilp.ifreq.MainEZ;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static com.armilp.ifreq.MainEZ.MODID;

public class KeyBindings {

    // Create a custom category for your mod
    public static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(MODID ,"key.category." + MODID));

    public static KeyBinding OPEN_WALKIE_GUI = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key." + MODID + ".open_walkie_gui",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_R,
                    CATEGORY
            )
    );

    public static void register() {

    }
}