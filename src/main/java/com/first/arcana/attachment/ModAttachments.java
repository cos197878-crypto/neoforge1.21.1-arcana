package com.first.arcana.attachment;

import com.first.arcana.Arcana;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Arcana.MOD_ID);

    /** player.getData(ModAttachments.MAGIC_DATA) 로 꺼낸다. */
    public static final Supplier<AttachmentType<MagicData>> MAGIC_DATA = ATTACHMENT_TYPES.register(
            "magic_data",
            () -> AttachmentType.builder(MagicData::new)
                    .serialize(MagicData.CODEC)
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
