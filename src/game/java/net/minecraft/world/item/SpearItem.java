package net.minecraft.world.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

public class SpearItem extends SwordItem {
    public SpearItem(ToolMaterial material, float attackDamageModifier, float attackSpeedModifier, Item.Properties properties) {
        super(material, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int l = getLungeLevel(itemstack, level);
            if (l > 0) {
                player.swing(hand);
                doThrust(player, itemstack, l);
            }
        }
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide);
    }

    public static int getLungeLevel(ItemStack stack, Level level) {
        if (!stack.is(Items.SPEAR)) {
            return 0;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(
            level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LUNGE),
            stack
        );
    }

    private static float getSpeed(Player player) {
        Vec3 motion = player.getDeltaMovement();
        return (float)Math.sqrt(motion.x * motion.x + motion.y * motion.y + motion.z * motion.z);
    }

    private static void doThrust(Player player, ItemStack stack, int level) {
        float speed = getSpeed(player);
        float damage = 4.0F + speed * 6.0F + level * 1.5F;
        float yaw = player.getYRot() * ((float)Math.PI / 180.0F);
        double dx = -Mth.sin(yaw);
        double dz = Mth.cos(yaw);
        double reach = 4.0 + speed * 2.5;
        AABB box = player.getBoundingBox().inflate(reach, 1.5, reach);
        double sx = player.getX();
        double sz = player.getZ();

        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
            double ex = target.getX() - sx;
            double ez = target.getZ() - sz;
            double projection = ex * dx + ez * dz;
            if (projection > 0.0 && projection < reach) {
                double cx = dx * projection;
                double cz = dz * projection;
                double dx2 = ex - cx;
                double dz2 = ez - cz;
                if (dx2 * dx2 + dz2 * dz2 < 3.0) {
                    target.hurt(player.damageSources().playerAttack(player), damage);
                    target.push(dx * speed * 0.4, 0.1, dz * speed * 0.4);
                }
            }
        }
    }

    public static void spearHit(Player player, double dx, double dz, double range, float damage) {
        double sx = player.getX();
        double sz = player.getZ();
        AABB box = player.getBoundingBox().inflate(range, 1.5, range);

        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
            double ex = target.getX() - sx;
            double ez = target.getZ() - sz;
            double projection = ex * dx + ez * dz;
            if (projection > 0.0 && projection < range) {
                double cx = dx * projection;
                double cz = dz * projection;
                double dx2 = ex - cx;
                double dz2 = ez - cz;
                if (dx2 * dx2 + dz2 * dz2 < 2.5) {
                    target.hurt(player.damageSources().playerAttack(player), damage);
                }
            }
        }
    }
}
