package com.first.arcana.client;

import com.first.arcana.Arcana;
import com.first.arcana.component.ModDataComponents;
import com.first.arcana.component.SpellContainer;
import com.first.arcana.item.custom.SpellBookItem;
import com.first.arcana.network.CastSpellPayload;
import com.first.arcana.attachment.ModAttachments;
import com.first.arcana.network.CycleSpellPayload;
import com.first.arcana.spell.SpellSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** 게임 버스 + 클라이언트 전용. 키 입력을 읽어 서버로 시전 요청을 보낸다. */
@EventBusSubscriber(modid = Arcana.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        // 쿨다운 HUD 예측: 서버는 값이 바뀐 틱에만 동기화 패킷을 보내므로, 클라이언트가
        // 로컬로도 1씩 줄여야 표시가 1초 단위로 튀지 않는다. 패킷이 오면 copyFrom 이 덮는다.
        player.getData(ModAttachments.MAGIC_DATA).tickCooldowns();

        // consumeClick 은 "눌린 횟수"를 하나씩 꺼내므로 while 로 비워야 입력이 밀리지 않는다.
        while (ModKeyMappings.CAST_SPELL.get().consumeClick()) {
            requestCast(player);
        }
        while (ModKeyMappings.NEXT_SPELL.get().consumeClick()) {
            cycleLocalSelection(player);
        }
    }

    private static void requestCast(LocalPlayer player) {
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof SpellBookItem)) {
            return;
        }
        SpellSlot slot = SpellBookItem.getSelectedSlot(stack);
        if (slot == null) {
            return;
        }
        // 판정은 서버가 다시 한다. 여기서는 요청만 보낸다.
        PacketDistributor.sendToServer(new CastSpellPayload(slot.spellId(), slot.level()));
    }

    /**
     * 선택 칸 전환은 표시용이라 클라이언트에서 바꿔도 되지만,
     * 서버가 가진 스택과 어긋나므로 실제 시전 시엔 서버가 들고 있는 값을 다시 확인한다.
     */
    private static void cycleLocalSelection(LocalPlayer player) {
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof SpellBookItem)) {
            return;
        }
        SpellContainer container = SpellBookItem.getContainer(stack);
        if (container.spells().isEmpty()) {
            return;
        }
        // 원본은 서버 스택이다. 로컬 갱신은 HUD 즉시 반응용이고, 서버가 곧 같은 값으로 덮는다.
        int next = (SpellBookItem.getSelectedIndex(stack) + 1) % container.spells().size();
        stack.set(ModDataComponents.SELECTED_SLOT.get(), next);
        PacketDistributor.sendToServer(new CycleSpellPayload());
    }
}
