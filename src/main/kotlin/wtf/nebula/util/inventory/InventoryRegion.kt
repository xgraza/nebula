package wtf.nebula.util.inventory

enum class InventoryRegion(val range: IntRange) {
    HOTBAR(0..9),
    INVENTORY(9..36),
    ALL(0..36)
}