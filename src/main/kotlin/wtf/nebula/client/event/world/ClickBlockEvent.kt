package wtf.nebula.client.event.world

import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import wtf.nebula.client.event.EventCancelable

class ClickBlockEvent(val blockPos: BlockPos, val facing: EnumFacing) : EventCancelable()