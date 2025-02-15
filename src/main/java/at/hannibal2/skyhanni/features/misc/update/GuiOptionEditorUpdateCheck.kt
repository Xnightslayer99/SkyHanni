package at.hannibal2.skyhanni.features.misc.update

import io.github.moulberry.moulconfig.gui.GuiOptionEditor
import io.github.moulberry.moulconfig.internal.TextRenderUtils
import io.github.moulberry.moulconfig.processor.ProcessedOption
import io.github.moulberry.notenoughupdates.itemeditor.GuiElementButton
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumChatFormatting.GREEN
import net.minecraft.util.EnumChatFormatting.RED
import org.lwjgl.input.Mouse

class GuiOptionEditorUpdateCheck(option: ProcessedOption) : GuiOptionEditor(option) {
    val button = GuiElementButton("", -1) { }

    override fun render(x: Int, y: Int, width: Int) {
        val fr = Minecraft.getMinecraft().fontRendererObj

        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toFloat() + 10, y.toFloat(), 1F)
        val width = width - 20
        val nextVersion = UpdateManager.getNextVersion()

        button.text = when (UpdateManager.updateState) {
            UpdateManager.UpdateState.AVAILABLE -> "Download update"
            UpdateManager.UpdateState.QUEUED -> "Downloading..."
            UpdateManager.UpdateState.DOWNLOADED -> "Downloaded"
            UpdateManager.UpdateState.NONE -> if (nextVersion == null) "Check for Updates" else "Up to date"
        }
        button.render(getButtonPosition(width), 10)

        if (UpdateManager.updateState == UpdateManager.UpdateState.DOWNLOADED) {
            TextRenderUtils.drawStringCentered(
                "${GREEN}The update will be installed after your next restart.",
                fr,
                width / 2F,
                40F,
                true,
                -1
            )
        }

        val widthRemaining = width - button.width - 10

        GlStateManager.scale(2F, 2F, 1F)
        val currentVersion = UpdateManager.getCurrentVersion()
        val sameVersion = currentVersion.equals(nextVersion, true)
        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            "${if (UpdateManager.updateState == UpdateManager.UpdateState.NONE) GREEN else RED}$currentVersion" +
                    if (nextVersion != null && !sameVersion) "➜ ${GREEN}${nextVersion}" else "",
            fr,
            widthRemaining / 4F,
            10F,
            true,
            widthRemaining / 2,
            -1
        )

        GlStateManager.popMatrix()
    }

    fun getButtonPosition(width: Int) = width - button.width
    override fun getHeight(): Int {
        return 55
    }

    override fun mouseInput(x: Int, y: Int, width: Int, mouseX: Int, mouseY: Int): Boolean {
        val width = width - 20
        if (Mouse.getEventButtonState() && (mouseX - getButtonPosition(width) - x) in (0..button.width) && (mouseY - 10 - y) in (0..button.height)) {
            when (UpdateManager.updateState) {
                UpdateManager.UpdateState.AVAILABLE -> UpdateManager.queueUpdate()
                UpdateManager.UpdateState.QUEUED -> {}
                UpdateManager.UpdateState.DOWNLOADED -> {}
                UpdateManager.UpdateState.NONE -> UpdateManager.checkUpdate()
            }
            return true
        }
        return false
    }

    override fun keyboardInput(): Boolean {
        return false
    }

    override fun fulfillsSearch(word: String): Boolean {
        return super.fulfillsSearch(word) || word in "download" || word in "update"
    }
}