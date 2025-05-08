package quantumv.blockcarts;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MinecartItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class BlockCartItem extends MinecartItem {
    private static final String TRANSLATION_KEY = "item.blockcarts.blockcart";
    private final EntityType<? extends AbstractMinecartEntity> type = EntityType.MINECART;

    public BlockCartItem(Settings settings) {
        super(EntityType.MINECART, settings);
    }
    public Text getName(ItemStack stack) {
        String name = getOrCreateBlockData(stack, "minecraft:grass_block");
        String blockName = Registries.BLOCK.get(Identifier.tryParse(name)).getName().getString();

        // Return the translated text with the dynamic part inserted
        return Text.translatable(TRANSLATION_KEY, blockName);
    }
    public static String getOrCreateBlockData(ItemStack stack, String placeholder){
        ComponentMap components = stack.getComponents();
        NbtComponent customData = components.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = new NbtCompound();
        if (customData == null){

            //n.putString("block", "minecraft:grass_block");
            customData = NbtComponent.of(new NbtCompound());
        }
        nbt = customData.copyNbt();
        if (nbt.getString("block").isEmpty()){
            nbt.putString("block", placeholder);

        }
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return nbt.getString("block").get();
    }

    @Override
    public int getMaxCount() {
        return super.getMaxCount();
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (!blockState.isIn(BlockTags.RAILS)) {
            return ActionResult.FAIL;
        } else {
            ItemStack itemStack = context.getStack();
            RailShape railShape = blockState.getBlock() instanceof AbstractRailBlock ? (RailShape)blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d = 0.0;
            if (railShape.isAscending()) {
                d = 0.5;
            }

            Vec3d vec3d = new Vec3d((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.0625 + d, (double)blockPos.getZ() + 0.5);
            AbstractMinecartEntity abstractMinecartEntity = AbstractMinecartEntity.create(world, vec3d.x, vec3d.y, vec3d.z, this.type, SpawnReason.DISPENSER, itemStack, context.getPlayer());
            if (abstractMinecartEntity == null) {
                return ActionResult.FAIL;
            } else {
                String blockName = getOrCreateBlockData(itemStack, "minecraft:grass_block");
                abstractMinecartEntity.setCustomBlockState(
                        Optional.of(Registries.BLOCK.get(Identifier.tryParse(blockName)).getDefaultState())
                );
                NbtCompound n = new NbtCompound();

                abstractMinecartEntity.writeNbt(n);
                n.putBoolean("HasBlock", true);
                n.putString("ContainedBlock", blockName);
                abstractMinecartEntity.readNbt(n);

                if (AbstractMinecartEntity.areMinecartImprovementsEnabled(world)) {
                    List<Entity> list = world.getOtherEntities((Entity)null, abstractMinecartEntity.getBoundingBox());
                    Iterator var12 = list.iterator();

                    while(var12.hasNext()) {
                        Entity entity = (Entity)var12.next();
                        if (entity instanceof AbstractMinecartEntity) {
                            return ActionResult.FAIL;
                        }
                    }
                }

                if (world instanceof ServerWorld) {
                    ServerWorld serverWorld = (ServerWorld)world;
                    serverWorld.spawnEntity(abstractMinecartEntity);
                    serverWorld.emitGameEvent(GameEvent.ENTITY_PLACE, blockPos, GameEvent.Emitter.of(context.getPlayer(), serverWorld.getBlockState(blockPos.down())));
                }

                itemStack.decrement(1);
                return ActionResult.SUCCESS;
            }
        }
    }

}
