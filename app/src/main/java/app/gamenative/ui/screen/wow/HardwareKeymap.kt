package app.gamenative.ui.screen.wow

import android.util.SparseArray
import android.view.KeyEvent
import com.winlator.xserver.Keyboard
import com.winlator.xserver.XKeycode
import java.io.File
import timber.log.Timber

/**
 * The player's remaps for a built-in hardware keyboard, read from `keymap.txt` in the game folder.
 *
 * Phone keyboards such as the Unihertz Titan's have no Esc, Tab or function keys but do have keys a PC doesn't
 * (Sym, Fn), so which key does what has to be the player's choice. Keys nothing is mapped to are listed in
 * `keymap-unmapped.txt` by their Android name, which is the name the file expects.
 */
object HardwareKeymap {
    const val FILE_NAME = "keymap.txt"
    private const val UNMAPPED_FILE_NAME = "keymap-unmapped.txt"

    private val template = """
        # Remaps for a built-in hardware keyboard. One per line:
        #
        #     <phone key> = <PC key>
        #     ALT+<phone key> = <PC key>      (only while Alt is held; the game doesn't see Alt)
        #     SHIFT+<phone key> = <PC key>    (only while Shift is held; the game doesn't see Shift)
        #
        # A SHIFT+ line takes that capital letter away from chat. To keep capitals, bind Shift+letter inside
        # WoW's own Key Bindings menu instead.
        #
        # Phone keys are Android key names without KEYCODE_: A-Z, 0-9, SYM, FUNCTION, SPACE, ENTER, DEL (backspace),
        # SHIFT_LEFT, SHIFT_RIGHT, ALT_LEFT, CTRL_LEFT, VOLUME_UP ... A key the game ignored is named in
        # $UNMAPPED_FILE_NAME next to this file after you press it.
        #
        # PC keys: A-Z, 0-9, F1-F12, ESC, TAB, ENTER, SPACE, BKSP, DEL, INSERT, HOME, END, PRIOR (page up),
        # NEXT (page down), UP, DOWN, LEFT, RIGHT, SHIFT_L, SHIFT_R, CTRL_L, CTRL_R, ALT_L, ALT_R, CAPS_LOCK,
        # MINUS, EQUAL, BRACKET_LEFT, BRACKET_RIGHT, BACKSLASH, SEMICOLON, APOSTROPHE, COMMA, PERIOD, SLASH, GRAVE.
        #
        # Digits and symbols typed with Alt (Alt+W for 1, and so on) already reach the game as those keys.
        # Changes apply the next time you press Play. Delete this file to get the defaults back.

        SYM = TAB
        FUNCTION = ESC
    """.trimIndent() + "\n"

    fun apply(gameRoot: File) {
        val file = File(gameRoot, FILE_NAME)
        val lines = runCatching {
            if (!file.exists()) file.writeText(template)
            file.readLines()
        }.getOrElse {
            Timber.w(it, "Couldn't read $FILE_NAME, using the default key remaps")
            template.lines()
        }
        val keymap = parse(lines)
        Keyboard.setUserKeymap(keymap.keys, keymap.altKeys, keymap.shiftKeys)

        val unmapped = File(gameRoot, UNMAPPED_FILE_NAME)
        val seen = HashSet<String>()
        Keyboard.setUnmappedKeyListener { androidName ->
            // Only keys the game never saw are recorded, never what was typed.
            val name = androidName.removePrefix("KEYCODE_")
            if (synchronized(seen) { seen.add(name) }) runCatching { unmapped.appendText("$name\n") }
        }
    }

    internal class Keymap(
        val keys: SparseArray<XKeycode> = SparseArray(),
        val altKeys: SparseArray<XKeycode> = SparseArray(),
        val shiftKeys: SparseArray<XKeycode> = SparseArray(),
    )

    internal fun parse(lines: List<String>): Keymap {
        val keymap = Keymap()
        for (raw in lines) {
            val line = raw.substringBefore('#').trim()
            if (line.isEmpty()) continue
            val from = line.substringBefore('=').trim().uppercase()
            val to = line.substringAfter('=', "").trim().uppercase()
            val table = when {
                from.startsWith("ALT+") -> keymap.altKeys
                from.startsWith("SHIFT+") -> keymap.shiftKeys
                else -> keymap.keys
            }
            val keyCode = KeyEvent.keyCodeFromString("KEYCODE_" + from.substringAfter('+').trim())
            val target = runCatching { XKeycode.valueOf("KEY_$to") }.getOrNull()
            if (keyCode == KeyEvent.KEYCODE_UNKNOWN || target == null) {
                Timber.w("Ignoring $FILE_NAME line: $raw")
                continue
            }
            table.put(keyCode, target)
        }
        return keymap
    }
}
