package quantumv.blockcarts.mixin.client;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.EquippableDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import quantumv.blockcarts.EquipBlockMinecartDispenserBehavior;

@Mixin(DispenserBlock.class)
public class DispenserMixin {
    private static final ItemDispenserBehavior DEFAULT_BEHAVIOR = new EquipBlockMinecartDispenserBehavior();
    private static DispenserBehavior getBehaviorForItem(ItemStack stack) {
        return (DispenserBehavior)(stack.contains(DataComponentTypes.EQUIPPABLE) ? EquippableDispenserBehavior.INSTANCE : DEFAULT_BEHAVIOR);
    }
}
