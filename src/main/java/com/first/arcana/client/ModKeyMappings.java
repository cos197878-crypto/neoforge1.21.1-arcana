package com.first.arcana.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

/** 키바인드 정의. 실제 등록은 ClientModEvents 에서 한다. */
public class ModKeyMappings {
    public static final String CATEGORY = "key.categories.arcana";

    public static final Lazy<KeyMapping> CAST_SPELL = Lazy.of(() -> new KeyMapping(
            "key.arcana.cast_spell",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY));

    public static final Lazy<KeyMapping> NEXT_SPELL = Lazy.of(() -> new KeyMapping(
            "key.arcana.next_spell",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY));
}
