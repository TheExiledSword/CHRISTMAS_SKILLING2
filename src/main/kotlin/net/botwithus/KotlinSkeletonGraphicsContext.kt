package net.botwithus

import net.botwithus.rs3.game.Client
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer
import net.botwithus.rs3.game.scene.entities.characters.player.Player
import net.botwithus.rs3.imgui.ImGui
import net.botwithus.rs3.imgui.ImGuiWindowFlag
import net.botwithus.rs3.script.ScriptConsole
import net.botwithus.rs3.script.ScriptGraphicsContext
import javax.swing.OverlayLayout

class KotlinSkeletonGraphicsContext(
    private val script: KotlinSkeleton,
    console: ScriptConsole
) : ScriptGraphicsContext (console) {

    override fun drawSettings() {
        super.drawSettings()
        if (ImGui.Begin("Christmas Skiller: Cooking", ImGuiWindowFlag.None.value)) {
            if (ImGui.BeginTabBar("My bar", ImGuiWindowFlag.None.value)) {
                if (ImGui.BeginTabItem("Settings", ImGuiWindowFlag.None.value)) {
                    ImGui.Text("My scripts state is: " + script.botState)
                    ImGui.Text("Player Animation: " + (Client.getLocalPlayer()?.animationId ?: 0))
                    ImGui.Text("Player Coordinates: " + (Client.getLocalPlayer()?.coordinate ?: ""))
                    drawConsole()
                    ImGui.EndTabItem()
                }
                ImGui.EndTabBar()
            }
            ImGui.End()
        }
    }

    private fun drawConsole() {
        ImGui.Separator()
        ImGui.Text("Console:")
        ImGui.Separator()

        if (ImGui.Button("Clear Console")) script.console.clear()
        ImGui.SameLine()
        script.console.isScrollToBottom = ImGui.Checkbox("Scroll to bottom", script.console.isScrollToBottom)

        if (ImGui.BeginChild("##console_lines" + this.hashCode(), -1.0f, -1.0f, true, 0)) {
            for (var1 in 0..199) {
                val var2 = (script.console.lineIndex + var1) % 200
                script.console.consoleLines[var2]?.let {
                    val var3 = Regex("\\[(.*?)\\]").find(it)?.value ?: ""
                    when {
                        var3.contains("ERROR") -> ImGui.PushStyleColor(0, 1.0f, 0.0f, 0.0f, 1.0f)
                        var3.contains("WARNING") -> ImGui.PushStyleColor(0, 1.0f, 1.0f, 0.0f, 1.0f)
                        var3.contains("INFO") -> ImGui.PushStyleColor(0, 0.0f, 1.0f, 0.0f, 1.0f)
                        var3.contains("DEBUG") -> ImGui.PushStyleColor(0, 0.6f, 0.6f, 1.0f, 1.0f)
                        var3.contains("TRACE") -> ImGui.PushStyleColor(0, 0.0f, 1.0f, 1.0f, 1.0f)
                        var3.contains("SUCCESS") -> ImGui.PushStyleColor(0, 0.0f, 1.0f, 0.0f, 1.0f)
                        var3.contains("FAILURE") -> ImGui.PushStyleColor(0, 1.0f, 0.0f, 0.0f, 1.0f)
                        else -> ImGui.PushStyleColor(0, 1.0f, 1.0f, 1.0f, 1.0f)
                    }
                    drawConsoleLine(it)
                    ImGui.PopStyleColor()
                }
            }
            if (script.console.isScrollToBottom) ImGui.SetScrollHereY(1.0f)
            ImGui.EndChild()
        }
    }

    override fun drawOverlay() {
        // This is how you can overlay your script on top of the game

        super.drawOverlay()
    }

}