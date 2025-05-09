package quantumv.blockcarts.mixin.client;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.EquippableDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.MinecartDispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MinecartItem;
import org.spongepowered.asm.mixin.Mixin;
import quantumv.blockcarts.BlockCartItem;
import quantumv.blockcarts.EquipBlockMinecartDispenserBehavior;

import java.util.Arrays;
import java.util.List;

@Mixin(DispenserBlock.class)
public class DispenserMixin {
    private static final ItemDispenserBehavior DEFAULT_BEHAVIOR = new EquipBlockMinecartDispenserBehavior();
    private static DispenserBehavior getBehaviorForItem(ItemStack stack) {
        return (DispenserBehavior)(stack.contains(DataComponentTypes.EQUIPPABLE) ? EquippableDispenserBehavior.INSTANCE : DEFAULT_BEHAVIOR);
    }
}
