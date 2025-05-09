package quantumv.blockcarts;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.MinecartDispenserBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Optional;

public class EquipBlockMinecartDispenserBehavior  extends ItemDispenserBehavior{
    public static final EquipBlockMinecartDispenserBehavior INSTANCE = new EquipBlockMinecartDispenserBehavior();

    public EquipBlockMinecartDispenserBehavior() {
    }

    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        return _dispense(pointer, stack) ? stack : super.dispenseSilently(pointer, stack);
    }
    static void registerDefaults() {
        DispenserBlock.registerBehavior(BlockCarts.BLOCKCART_ITEM, new MinecartDispenserBehavior(EntityType.MINECART));
    }
    public static boolean _dispense(BlockPointer pointer, ItemStack stack) {
        BlockPos blockPos = pointer.pos().offset((Direction)pointer.state().get(DispenserBlock.FACING));
        List<MinecartEntity> list = pointer.world().getEntitiesByClass(MinecartEntity.class, new Box(blockPos), (entity) -> {
            return true;
        });
        if (list.isEmpty()) {
            return false;
        } else {
            MinecartEntity cart = (MinecartEntity)list.getFirst();
            NbtCompound n = new NbtCompound();
            cart.writeNbt(n);

            if (!(stack.getItem() instanceof BlockItem)){
                return false;
            }
            if (n.getBoolean("HasBlock", false)){
                cart.dropStack((ServerWorld) cart.getWorld(),
                        Registries.BLOCK.get(Identifier.tryParse(n.getString("ContainedBlock", "minecraft:grass_block"))).asItem().getDefaultStack()
                );
            }
            String blockName = Registries.BLOCK.getId(((BlockItem) stack.getItem()).getBlock()).toString();




            n.putBoolean("HasBlock", true);
            n.putString("ContainedBlock", blockName);
            cart.readNbt(n);
            cart.setCustomBlockState(
                    Optional.of(Registries.BLOCK.get(Identifier.tryParse(blockName)).getDefaultState())
            );
            stack.decrement(1);


            return true;
        }
    }
}
