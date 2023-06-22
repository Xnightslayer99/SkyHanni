package at.hannibal2.skyhanni.features.rift

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.withAlpha
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils.toChromaColor
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class RiftLarva {
    private val config get() = SkyHanniMod.feature.rift.larvas
    private var hasLarvaHookInHand = false
    val larvaSkullTexture =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTgzYjMwZTlkMTM1YjA1MTkwZWVhMmMzYWM2MWUyYWI1NWEyZDgxZTFhNThkYmIyNjk4M2ExNDA4MjY2NCJ9fX0="

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        checkHand()
        if (!hasLarvaHookInHand) return

        if (event.isMod(20)) {
            findLarvas()
        }
    }

    private fun checkHand() {
        hasLarvaHookInHand = InventoryUtils.getItemInHand()?.getInternalName() == "LARVA_HOOK"
    }

    private fun findLarvas() {

        val list = Minecraft.getMinecraft().theWorld?.loadedEntityList ?: return
        for (stand in list.filterIsInstance<EntityArmorStand>()) {
            if (stand.hasSkullTexture(larvaSkullTexture)) {
                RenderLivingEntityHelper.setEntityColor(
                    stand,
                    config.highlightColor.toChromaColor().withAlpha(1)
                ) { isEnabled() && hasLarvaHookInHand }
            }
        }
    }

    fun isEnabled() = RiftAPI.inRift() && config.highlight
}