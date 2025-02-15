package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class PatcherSendCoordinates {

    private val patcherBeacon = mutableListOf<PatcherBeacon>()
    private val logger = LorenzLogger("misc/patchercoords")

    // TODO USE SH-REPO
    private val pattern = "(?<playerName>.*): x: (?<x>.*), y: (?<y>.*), z: (?<z>.*)".toPattern()

    @SubscribeEvent
    fun onPatcherCoordinates(event: LorenzChatEvent) {
        if (!SkyHanniMod.feature.misc.patcherSendCoordWaypoint) return

        val message = event.message.removeColor()
        pattern.matchMatcher(message) {
            var description = group("playerName").split(" ").last()
            val x = group("x").toInt()
            val y = group("y").toInt()

            val end = group("z")
            val z = if (end.contains(" ")) {
                val split = end.split(" ")
                val extra = split.drop(1).joinToString(" ")
                description += " " + extra

                split.first().toInt()
            } else end.toInt()
            patcherBeacon.add(PatcherBeacon(LorenzVec(x, y, z), description, System.currentTimeMillis() / 1000))
            logger.log("got patcher coords and username")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if (!SkyHanniMod.feature.misc.patcherSendCoordWaypoint) return

        for (beacon in patcherBeacon) {
            val location = beacon.location
            event.drawColor(location, LorenzColor.DARK_GREEN, alpha = 1f)
            event.drawWaypointFilled(location, LorenzColor.GREEN.toColor(), true, true)
            event.drawString(location.add(0.5, 0.5, 0.5), beacon.name, true, LorenzColor.DARK_BLUE.toColor())
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!event.isMod(10)) return

        val location = LocationUtils.playerLocation()
        // removed patcher beacon!
        patcherBeacon.removeIf { System.currentTimeMillis() / 1000 > it.time + 5 && location.distanceIgnoreY(it.location) < 5 }

        // removed patcher beacon after time!
        patcherBeacon.removeIf { System.currentTimeMillis() / 1000 > it.time + 60 }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        patcherBeacon.clear()
        logger.log("Reset everything (world change)")
    }

    data class PatcherBeacon(val location: LorenzVec, val name: String, val time: Long)
}
