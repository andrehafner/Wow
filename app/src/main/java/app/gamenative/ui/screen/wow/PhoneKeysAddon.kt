package app.gamenative.ui.screen.wow

import java.io.File
import timber.log.Timber

/**
 * A small WoW add-on the launcher installs for the keyboard-and-mouse interface on a phone keyboard, which has no
 * number row: Shift with a right-hand letter casts action buttons 1-10, and the chat window starts minimized so it
 * doesn't cover a small screen. Doing this inside the game, rather than by remapping keys in the launcher, keeps
 * those capital letters typeable in chat, because only the game knows when the player is typing.
 */
internal object PhoneKeysAddon {
    private const val NAME = "PhoneKeys"

    // The client's interface number isn't known here, so InterfaceStyle turns the game's add-on version check off.
    private val toc = """
        ## Interface: 110207
        ## Title: Phone Keyboard
        ## Notes: Shift + U I O P J K L B N M cast action buttons 1-10, and chat starts minimized. Installed by the launcher on every start.
        $NAME.lua
    """.trimIndent() + "\n"

    private val lua = """
        local KEYS = { "U", "I", "O", "P", "J", "K", "L", "B", "N", "M" }
        local frame = CreateFrame("Frame")
        local pending = true

        -- Override bindings leave the player's saved key bindings untouched and vanish with the add-on.
        local function bind()
            if InCombatLockdown and InCombatLockdown() then return end
            ClearOverrideBindings(frame)
            for slot, key in ipairs(KEYS) do
                SetOverrideBinding(frame, false, "SHIFT-" .. key, "ACTIONBUTTON" .. slot)
            end
            pending = false
        end

        local function minimizeChat()
            local chat = ChatFrame1
            if not chat or not FCF_MinimizeFrame then return end
            if chat.minFrame and chat.minFrame:IsShown() then return end
            pcall(FCF_MinimizeFrame, chat, "LEFT")
        end

        frame:RegisterEvent("PLAYER_LOGIN")
        frame:RegisterEvent("PLAYER_REGEN_ENABLED")
        frame:SetScript("OnEvent", function(_, event)
            if event == "PLAYER_LOGIN" then
                pcall(bind)
                if C_Timer and C_Timer.After then C_Timer.After(2, minimizeChat) else minimizeChat() end
            elseif pending then
                pcall(bind)
            end
        end)
    """.trimIndent() + "\n"

    /** Writes the add-on when [enabled], and removes it otherwise so the gamepad interface is left as it was. */
    fun sync(gameRoot: File, enabled: Boolean) {
        val dir = File(gameRoot, "${WowClientDownloader.FLAVOR_DIR}/Interface/AddOns/$NAME")
        runCatching {
            if (enabled) {
                dir.mkdirs()
                File(dir, "$NAME.toc").writeText(toc)
                File(dir, "$NAME.lua").writeText(lua)
            } else if (dir.exists()) {
                dir.deleteRecursively()
            }
        }.onFailure { Timber.w(it, "Couldn't update the $NAME add-on") }
    }
}
