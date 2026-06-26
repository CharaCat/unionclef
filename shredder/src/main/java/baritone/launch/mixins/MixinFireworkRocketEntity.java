/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.launch.mixins;

import baritone.utils.accessor.IFireworkRocketEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.OptionalInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.level.Level;

@Mixin(FireworkRocketEntity.class)
public abstract class MixinFireworkRocketEntity extends Entity implements IFireworkRocketEntity {

    @Shadow
    @Final
    private static EntityDataAccessor<OptionalInt> SHOOTER_ENTITY_ID;

    @Shadow
    private LivingEntity shooter;

    @Shadow
    public abstract boolean wasShotByEntity();

    private MixinFireworkRocketEntity(Level level) {
        super(null, level);
    }

    @Override
    public LivingEntity getBoostedEntity() {
        if (this.wasShotByEntity() && this.shooter == null) {
            final Entity entity = this.level().getEntity(this.entityData.get(SHOOTER_ENTITY_ID).getAsInt());
            if (entity instanceof LivingEntity) {
                this.shooter = (LivingEntity) entity;
            }
        }
        return this.shooter;
    }
}
