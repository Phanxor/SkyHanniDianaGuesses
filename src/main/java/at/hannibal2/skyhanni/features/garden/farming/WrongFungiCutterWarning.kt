package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.events.CropClickEvent
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getFungiCutterMode
import at.hannibal2.skyhanni.utils.SoundUtils
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

class WrongFungiCutterWarning {
    private var mode = FungiMode.UNKNOWN
    private var lastPlaySoundTime = 0L

    @SubscribeEvent
    fun onChatMessage(event: LorenzChatEvent) {
        val message = event.message
        if (message == "§eFungi Cutter Mode: §r§cRed Mushrooms") {
            mode = FungiMode.RED
        }
        if (message == "§eFungi Cutter Mode: §r§6Brown Mushrooms") {
            mode = FungiMode.BROWN
        }
    }

    @SubscribeEvent
    fun onBlockClick(event: CropClickEvent) {
        if (event.clickType != ClickType.LEFT_CLICK) return
        if (event.crop != CropType.MUSHROOM) return

        val toString = event.blockState.toString()
        if (toString == "minecraft:red_mushroom" && mode == FungiMode.BROWN) {
            notifyWrong()
        }
        if (toString == "minecraft:brown_mushroom" && mode == FungiMode.RED) {
            notifyWrong()
        }
    }

    private fun notifyWrong() {
        if (!SkyHanniMod.feature.garden.fungiCutterWarn) return

        LorenzUtils.sendTitle("§cWrong Fungi Cutter Mode!", 2.seconds)
        if (System.currentTimeMillis() > lastPlaySoundTime + 3_00) {
            lastPlaySoundTime = System.currentTimeMillis()
            SoundUtils.playBeepSound()
        }
    }

    @SubscribeEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        if (event.crop == CropType.MUSHROOM) {
            readItem(event.toolItem ?: error("Tool item is null"))
        } else {
            mode = FungiMode.UNKNOWN
        }
    }

    private fun readItem(item: ItemStack) {
        val rawMode = item.getFungiCutterMode() ?: error("Tool without fungi cutter mode: '${item.name}'")
        mode = FungiMode.getOrNull(rawMode)
    }

    enum class FungiMode {
        RED,
        BROWN,
        UNKNOWN,
        ;

        companion object {
            fun getOrNull(mode: String) =
                entries.firstOrNull { it.name == mode } ?: error("Unknown fungi mode: '$mode'")
        }
    }
}
