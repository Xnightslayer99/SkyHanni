package at.hannibal2.skyhanni.features.nether.reputationhelper.dailykuudra

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.HyPixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.nether.reputationhelper.CrimsonIsleReputationHelper
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUItems
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import java.util.regex.Pattern

class DailyKuudraBossHelper(private val reputationHelper: CrimsonIsleReputationHelper) {
    val kuudraTiers = mutableListOf<KuudraTier>()
    private val pattern = Pattern.compile("  Kuudra's Hollow \\(T(.*)\\)")

    fun init() {
        val repoData = reputationHelper.repoData
        val jsonElement = repoData["KUUDRA"]
        var tier = 1
        for ((displayName, extraData) in jsonElement.asJsonObject.entrySet()) {
            val data = extraData.asJsonObject
            val displayItem = data["item"]?.asString
            kuudraTiers.add(KuudraTier(displayName, displayItem, tier))
            tier++
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!HyPixelData.skyBlock) return
        if (LorenzUtils.skyBlockIsland != IslandType.KUUDRA_ARENA) return
        if (!SkyHanniMod.feature.misc.crimsonIsleReputationHelper) return

        val message = event.message
        if (message != "                               §r§6§lKUUDRA DOWN!") return

        for (line in ScoreboardData.sidebarLines) {
            val matcher = pattern.matcher(line)
            if (matcher.matches()) {
                val tier = matcher.group(1).toInt()
                val kuudraTier = getByTier(tier)!!
                finished(kuudraTier)
                return
            }
        }
    }

    private fun finished(kuudraTier: KuudraTier) {
        LorenzUtils.debug("Detected kuudra tier done: $kuudraTier")
        reputationHelper.questHelper.finishKuudra(kuudraTier)
        kuudraTier.doneToday = true
        reputationHelper.update()
    }

    fun render(display: MutableList<List<Any>>) {
        val done = kuudraTiers.count { it.doneToday }
        display.add(Collections.singletonList(""))
        display.add(Collections.singletonList("Daily Kuudra ($done/2 killed)"))
        if (done != 2) {
            for (tier in kuudraTiers) {
                val result = if (tier.doneToday) "§7Done" else "§bTodo"
                val displayName = tier.getDisplayName()
                val displayItem = tier.displayItem
                if (displayItem == null) {
                    display.add(Collections.singletonList("  $displayName: $result"))
                } else {
                    val lineList = mutableListOf<Any>()
                    lineList.add(" ")
                    lineList.add(NEUItems.readItemFromRepo(displayItem))
                    lineList.add("$displayName: $result")
                    display.add(lineList)
                }
            }
        }
    }

    fun reset() {
        for (miniBoss in kuudraTiers) {
            miniBoss.doneToday = false
        }
    }

    fun saveConfig() {
        SkyHanniMod.feature.hidden.crimsonIsleKuudraTiersDone.clear()

        kuudraTiers.filter { it.doneToday }
            .forEach { SkyHanniMod.feature.hidden.crimsonIsleKuudraTiersDone.add(it.name) }
    }

    fun loadConfig() {
        for (name in SkyHanniMod.feature.hidden.crimsonIsleKuudraTiersDone) {
            getByDisplayName(name)!!.doneToday = true
        }
    }

    private fun getByDisplayName(name: String) = kuudraTiers.firstOrNull { it.name == name }

    private fun getByTier(number: Int) = kuudraTiers.firstOrNull { it.tierNumber == number }
}