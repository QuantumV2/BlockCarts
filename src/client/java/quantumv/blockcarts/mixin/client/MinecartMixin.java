package quantumv.blockcarts.mixin.client;

import com.mojang.logging.LogUtils;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quantumv.blockcarts.BlockCartItem;
import quantumv.blockcarts.BlockCarts;

import java.util.Map;
import java.util.Optional;

@Mixin(MinecartEntity.class)
public abstract class MinecartMixin extends AbstractMinecartEntity {
    private String containedBlock = Registries.BLOCK.getId(Blocks.AIR).toString();

    //private static final TrackedData<Optional<BlockState>> CUSTOM_BLOCK_STATE = null;
    private boolean hasBlock = false;
    protected MinecartMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }
    private void resetContainedBlock(){
        hasBlock = false;
        containedBlock = Registries.BLOCK.getId(Blocks.AIR).toString();
        setCustomBlockState(Optional.of(Blocks.AIR.getDefaultState()));
    }
    public boolean isRideable() {
        return !hasBlock;
    }
    @Inject(at = @At("HEAD"), method="interact", cancellable = true)
    public void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        /*
        if (player.shouldCancelInteraction() && (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof AirBlockItem) && hasBlock) {

            ItemStack stack = Registries.BLOCK.get(Identifier.tryParse((containedBlock))).asItem().getDefaultStack();
            resetContainedBlock();
            player.giveItemStack(stack);
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
        }*/
        if (player.shouldCancelInteraction() && ((player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof BlockItem) || (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof AirBlockItem) ){
            if (hasBlock){
                ItemStack stack = Registries.BLOCK.get(Identifier.tryParse((containedBlock))).asItem().getDefaultStack();
                resetContainedBlock();
                playSound(SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1, 1);
                player.giveItemStack(stack);
            }
            setCustomBlockState(Optional.of(((BlockItem) player.getStackInHand(Hand.MAIN_HAND).getItem()).getBlock().getDefaultState()));
            playSound(SoundEvents.BLOCK_STONE_PLACE, 1, 1);
            hasBlock = true;
            containedBlock = Registries.BLOCK.getId(((BlockItem) player.getStackInHand(Hand.MAIN_HAND).getItem()).getBlock()).toString();
            player.getStackInHand(Hand.MAIN_HAND).decrement(1);
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
        }
        if (hasBlock) {
            cir.setReturnValue(ActionResult.PASS);
            cir.cancel();
            //return ActionResult.PASS;
        }
    }
    /*
    private static void writeBlockStateToNbt(NbtCompound nbt, BlockState state, String key) {
        NbtCompound stateTag = new NbtCompound();
        stateTag.putString("Name", Registries.BLOCK.getId(state.getBlock()).toString());

        if (!state.getEntries().isEmpty()) {
            NbtCompound propertiesTag = new NbtCompound();
            for (Map.Entry<Property<?>, Comparable<?>> entry : state.getEntries().entrySet()) {
                Property<?> property = entry.getKey();
                propertiesTag.putString(property.getName(), getPropertyValueString(property, entry.getValue()));
            }
            stateTag.put("Properties", propertiesTag);
        }

        nbt.put(key, stateTag);
    }

    private static <T extends Comparable<T>> String getPropertyValueString(Property<T> property, Comparable<?> value) {
        return property.name((T) value);
    }

    private static BlockState readBlockStateFromNbt(NbtCompound nbt, String key) {
        //LogUtils.getLogger().info("Reading blockstate for minecart");
        if (!nbt.contains(key)) {
            //LogUtils.getLogger().info("Reading blockstate for minecart failed");
            return Blocks.AIR.getDefaultState();
        }
        //LogUtils.getLogger().info("Reading blockstate keys for minecart");
        NbtCompound stateTag =nbt.getCompound(key).get();
        //LogUtils.getLogger().info("Found state tag for minecart: " + stateTag);
        Block block = Registries.BLOCK.get(Identifier.tryParse((stateTag.getString("Name", null))));
        //LogUtils.getLogger().info("Found block for minecart: " + block.toString() + " (" + (stateTag.getString("Name", null)) +
        //        ") (" + Identifier.tryParse((stateTag.getString("Name",null))) + ") (" + Registries.BLOCK.get(Identifier.tryParse((stateTag.getString("Name", null)))) + ")");
        BlockState state = block.getDefaultState();


        if (stateTag.contains("Properties")) {
            NbtCompound propertiesTag = stateTag.getCompound("Properties").get();
            for (String propertyName : propertiesTag.getKeys()) {
                Property<?> property = block.getStateManager().getProperty(propertyName);
                if (property != null) {
                    state = setPropertyValue(state, property, propertiesTag.getString(propertyName).get());
                }
            }
        }
        //LogUtils.getLogger().info(String.valueOf(state));
        return state;
    }

    private static <T extends Comparable<T>> BlockState setPropertyValue(BlockState state, Property<T> property, String value) {
        return property.parse(value).map(v -> state.with(property, v)).orElse(state);
    }
*/
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("HasBlock", hasBlock);
        nbt.putString("ContainedBlock", containedBlock);
    }
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        hasBlock = nbt.contains("HasBlock") ? nbt.getBoolean("HasBlock").get() : false;
        containedBlock = nbt.contains("ContainedBlock") ? nbt.getString("ContainedBlock").get() : "minecraft:air";
        //containedBlock = readBlockStateFromNbt(nbt, "ContainedBlock");
        //LogUtils.getLogger().info(String.valueOf(containedBlock));
    }



}
