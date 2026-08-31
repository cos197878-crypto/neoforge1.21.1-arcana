package com.first.arcana.client;

import com.first.arcana.Arcana;
import com.first.arcana.client.hud.ManaHudLayer;
import com.first.arcana.client.model.FireballModel;
import com.first.arcana.client.renderer.FireballRenderer;
import com.first.arcana.client.screen.SpellBookScreen;
import com.first.arcana.entity.ModEntityTypes;
import com.first.arcana.item.ModItems;
import com.first.arcana.menu.ModMenuTypes;
import com.first.arcana.item.custom.ScrollItem;
import com.first.arcana.spell.AbstractSpell;
import com.first.arcana.spell.SpellSlot;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** 모드 버스 + 클라이언트 전용. NeoForge 21.1 에서는 bus 를 명시해야 안전하다. */
@EventBusSubscriber(modid = Arcana.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.CAST_SPELL.get());
        event.register(ModKeyMappings.NEXT_SPELL.get());
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, Arcana.id("mana_bar"), new ManaHudLayer());
    }

    /**
     * 모델의 레이어 정의만 등록한다. 이걸 해둬야 나중에 렌더러에서
     * context.bakeLayer(FireballModel.LAYER_LOCATION) 으로 꺼내 쓸 수 있다.
     * 엔티티와 렌더러는 아직 붙이지 않았다.
     */
    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FireballModel.LAYER_LOCATION, FireballModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SPELL_BOOK.get(), SpellBookScreen::new);
    }

    /** 렌더러를 등록하지 않으면 엔티티가 스폰되는 순간 게임이 튕긴다. */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.FIREBALL.get(), FireballRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 셋업 이벤트는 병렬로 돌기 때문에, 바닐라 전역 상태를 건드리는 등록은 enqueueWork 로 감싼다.
        event.enqueueWork(ClientModEvents::registerScrollModelProperty);
    }

    /**
     * 두루마리에 담긴 주문의 계열을 모델 predicate 값으로 노출한다.
     * scroll.json 의 overrides 가 이 값을 보고 계열별 텍스처를 고른다.
     */
    private static void registerScrollModelProperty() {
        ItemProperties.register(
                ModItems.SCROLL.get(),
                Arcana.id("school"),
                (stack, level, entity, seed) -> {
                    SpellSlot slot = ScrollItem.getSlot(stack);
                    if (slot == null) {
                        return 0.0F;
                    }
                    AbstractSpell spell = slot.spell();
                    return spell == null ? 0.0F : spell.getSchool().getModelIndex();
                });
    }
}
