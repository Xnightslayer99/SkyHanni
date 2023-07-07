package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.events.withAlpha
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.EntityUtils.hasMaxHealth
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityEndermite
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.monster.EntitySlime
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class HighlightMiningCommissionMobs {
    private val config get() = SkyHanniMod.feature.misc.mining
    private var active = listOf<MobType>()

    enum class MobType(val commissionName: String, val isMob: (EntityLivingBase) -> Boolean) {

        // Dwarven Mines
        DWARVEN_GOBLIN_SLAYER("Goblin Slayer", { it.name == "Goblin " }),
        STAR_PUNCHER("Star Sentry Puncher", { it.name == "Crystal Sentry" }),
        ICE_WALKER("Ice Walker Slayer", { it.name == "Ice Walker" }),
        GOLDEN_GOBLIN("Golden Goblin Slayer", { it.name.contains("Golden Goblin") }), // TODO test

        // Crystal Hollows
        AUTOMATON("Automaton Slayer", { it is EntityIronGolem }),
        TEAM_TREASURITE_MEMBER("Team Treasurite Member Slayer", { it.name == "Team Treasurite" }),
        YOG("Yog Slayer", { it is EntityMagmaCube }),
        THYST("Thyst Slayer", { it is EntityEndermite && it.hasMaxHealth(5_000) }),
        CORLEONE("Corleone Slayer", { it.hasMaxHealth(1_000_000) && it.name == "Team Treasurite" }),
        SLUDGE("Sludge Slayer", {
            it is EntitySlime && (it.hasMaxHealth(5_000) || it.hasMaxHealth(10_000) || it.hasMaxHealth(25_000))
        }),
        CH_GOBLIN_SLAYER("Goblin Slayer", { it.name == "Weakling " }),
        ;
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(40)) return

        val entities = Minecraft.getMinecraft().theWorld.loadedEntityList.filterIsInstance<EntityLivingBase>()
        for ((type, entity) in active.flatMap { type -> entities.map { type to it } }) {
            if (type.isMob(entity)) {
                RenderLivingEntityHelper.setEntityColor(entity, LorenzColor.YELLOW.toColor().withAlpha(127))
                { isEnabled() && type in active }
            }
        }
    }

    @SubscribeEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        if (!isEnabled()) return

        MobType.values().filter { type ->
            event.tabList.find { line -> line.contains(type.commissionName) }?.let { !it.endsWith("§aDONE") } ?: false
        }.let {
            if (it != active) {
                active = it
            }
        }
    }

    @SubscribeEvent
    fun onEntityHealthUpdate(event: EntityMaxHealthUpdateEvent) {
        if (!isEnabled()) return

        val entity = event.entity
        for (type in active) {
            if (type.isMob(entity)) {
                RenderLivingEntityHelper.setEntityColor(entity, LorenzColor.YELLOW.toColor().withAlpha(127))
                { isEnabled() && type in active }
            }
        }
    }

    fun isEnabled() = config.highlightCommissionMobs &&
            (IslandType.DWARVEN_MINES.isInIsland() || IslandType.CRYSTAL_HOLLOWS.isInIsland())
}