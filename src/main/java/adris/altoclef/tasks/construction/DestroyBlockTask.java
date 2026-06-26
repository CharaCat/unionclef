package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.versionedfields.Blocks;
import adris.altoclef.tasks.movement.RunAwayFromPositionTask;
import adris.altoclef.tasks.movement.SafeRandomShimmyTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.Slot;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Destroy a block at a position.
 */
public class DestroyBlockTask extends Task implements ITaskRequiresGrounded {
    private final MovementProgressChecker stuckCheck = new MovementProgressChecker();
    private final MovementProgressChecker _moveChecker = new MovementProgressChecker();
    private final BlockPos pos;
    Block[] annoyingBlocks = new Block[]{
            Blocks.VINE,
            Blocks.NETHER_SPROUTS,
            Blocks.CAVE_VINES,
            Blocks.CAVE_VINES_PLANT,
            Blocks.TWISTING_VINES,
            Blocks.TWISTING_VINES_PLANT,
            Blocks.WEEPING_VINES_PLANT,
            Blocks.LADDER,
            Blocks.BIG_DRIPLEAF,
            Blocks.BIG_DRIPLEAF_STEM,
            Blocks.SMALL_DRIPLEAF,
            Blocks.TALL_GRASS,
            Blocks.SHORT_GRASS,
            Blocks.SWEET_BERRY_BUSH
    };
    private Task unstuckTask = null;
    private boolean isMining;

    public DestroyBlockTask(BlockPos pos) {
        this.pos = pos;
    }

    /**
     * Generates an array of BlockPos objects representing the sides of a given BlockPos.
     */
    private static BlockPos[] generateSides(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        return new BlockPos[]{
                new BlockPos(x + 1, y, z),
                new BlockPos(x - 1, y, z),
                new BlockPos(x, y, z + 1),
                new BlockPos(x, y, z - 1),
                new BlockPos(x + 1, y, z - 1),
                new BlockPos(x + 1, y, z + 1),
                new BlockPos(x - 1, y, z - 1),
                new BlockPos(x - 1, y, z + 1)
        };
    }

    /**
     * Checks if a block is annoying.
     */
    private boolean isAnnoying(AltoClef mod, BlockPos pos) {
        for (Block annoyingBlock : annoyingBlocks) {
            boolean isAnnoying = mod.getWorld().getBlockState(pos).getBlock() == annoyingBlock
                    || mod.getWorld().getBlockState(pos).getBlock() instanceof DoorBlock
                    || mod.getWorld().getBlockState(pos).getBlock() instanceof FenceBlock
                    || mod.getWorld().getBlockState(pos).getBlock() instanceof FenceGateBlock
                    || mod.getWorld().getBlockState(pos).getBlock() instanceof FlowerBlock;
            if (isAnnoying) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the position of the block where the player is stuck.
     */
    private BlockPos stuckInBlock(AltoClef mod) {
        BlockPos playerPos = mod.getPlayer().blockPosition();
        BlockPos[] toCheck = generateSides(playerPos);
        BlockPos[] toCheckHigh = generateSides(playerPos.above());

        if (isAnnoying(mod, playerPos)) return playerPos;
        if (isAnnoying(mod, playerPos.above())) return playerPos.above();

        for (BlockPos check : toCheck) {
            if (isAnnoying(mod, check)) return check;
        }
        for (BlockPos check : toCheckHigh) {
            if (isAnnoying(mod, check)) return check;
        }
        return null;
    }

    private Task getFenceUnstuckTask() {
        return new SafeRandomShimmyTask();
    }

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        mod.getClientBaritone().getPathingBehavior().forceCancel();
        _moveChecker.reset();
        stuckCheck.reset();

        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, ContainerInput.PICKUP));

            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, ContainerInput.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, ContainerInput.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, ContainerInput.PICKUP);
        } else {
            StorageHelper.closeScreen();
        }
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // Check for pillager wool
        if (mod.getWorld().getBlockState(pos).getBlock() == net.minecraft.world.level.block.Blocks.WOOL.white()) {
            for (Entity entity : mod.getWorld().getEntities(mod.getPlayer(), new net.minecraft.world.phys.AABB(pos).inflate(144), e -> e instanceof Pillager)) {
                Debug.logMessage("Blacklisting pillager wool.");
                mod.getBlockScanner().requestBlockUnreachable(pos, 0);
            }
        }

        // Reset move checker if baritone is pathing
        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            _moveChecker.reset();
        }

        // Handle nether portal
        if (WorldHelper.isInNetherPortal()) {
            if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                setDebugState("Getting out from nether portal");
                mod.getInputControls().hold(Input.SNEAK);
                mod.getInputControls().hold(Input.MOVE_FORWARD);
                return null;
            } else {
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        } else if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.MOVE_BACK);
            mod.getInputControls().release(Input.MOVE_FORWARD);
        }

        // Handle being stuck
        if (unstuckTask != null && unstuckTask.isActive() && !unstuckTask.isFinished() && stuckInBlock(mod) != null) {
            setDebugState("Getting unstuck from block.");
            stuckCheck.reset();
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getExploreProcess().onLostControl();
            return unstuckTask;
        }

        if (!_moveChecker.check(mod) || !stuckCheck.check(mod)) {
            BlockPos blockStuck = stuckInBlock(mod);
            if (blockStuck != null) {
                unstuckTask = getFenceUnstuckTask();
                return unstuckTask;
            }
            stuckCheck.reset();
        }

        if (!_moveChecker.check(mod)) {
            _moveChecker.reset();
            mod.getBlockScanner().requestBlockUnreachable(pos);
        }

        // Check if above the block
        Vec3 playerPos = mod.getPlayer().position();
        boolean aboveBlock = !WorldHelper.isSolidBlock(pos.above()) 
                && playerPos.y > pos.getY() 
                && (mod.getPlayer().onGround() ? playerPos : playerPos.add(0, -1, 0)).distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 0.8;
        if (aboveBlock) {
            if (WorldHelper.dangerousToBreakIfRightAbove(pos)) {
                setDebugState("It's dangerous to break as we're right above it, moving away and trying again.");
                return new RunAwayFromPositionTask(3, pos.getY(), pos);
            }
        }

        Optional<Rotation> reach = LookHelper.getReach(pos);
        if (reach.isPresent() && (mod.getPlayer().isInWater() || mod.getPlayer().onGround()) 
                && !mod.getFoodChain().needsToEat() && !WorldHelper.isInNetherPortal() 
                && mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
            setDebugState("Block in range, mining...");
            stuckCheck.reset();
            isMining = true;
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.MOVE_BACK);
            mod.getInputControls().release(Input.MOVE_FORWARD);
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getBuilderProcess().onLostControl();
            if (!LookHelper.isLookingAt(mod, reach.get())) {
                LookHelper.lookAt(reach.get());
            }
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
        } else {
            setDebugState("Getting to block...");
            if (isMining && mod.getPlayer().isInWater()) {
                setDebugState("We are in water... holding break button");
                isMining = false;
                mod.getBlockScanner().requestBlockUnreachable(pos);
                mod.getInputControls().hold(Input.CLICK_LEFT);
            } else {
                isMining = false;
            }
            boolean isCloseToMoveBack = pos.closerToCenterThan(mod.getPlayer().position(), 2);
            if (isCloseToMoveBack) {
                if (!mod.getClientBaritone().getPathingBehavior().isPathing() 
                        && !mod.getPlayer().isInWater() && !mod.getFoodChain().needsToEat()) {
                    mod.getInputControls().hold(Input.MOVE_BACK);
                    mod.getInputControls().hold(Input.SNEAK);
                } else {
                    mod.getInputControls().release(Input.MOVE_BACK);
                    mod.getInputControls().release(Input.SNEAK);
                }
            }
            if (!mod.getClientBaritone().getCustomGoalProcess().isActive()) {
                mod.getClientBaritone().getBuilderProcess().onLostControl();
                boolean isSnow = mod.getWorld().getBlockState(pos.above()).getBlock() == net.minecraft.world.level.block.Blocks.SNOW;
                mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(
                        isSnow ? new GoalBlock(pos) : new GoalNear(pos, 1));
            }
        }
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef mod = AltoClef.getInstance();
        mod.getClientBaritone().getPathingBehavior().forceCancel();
        if (!AltoClef.inGame()) return;
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, false);
        mod.getInputControls().release(Input.SNEAK);
        mod.getInputControls().release(Input.MOVE_BACK);
        mod.getInputControls().release(Input.MOVE_FORWARD);
    }

    @Override
    public boolean isFinished() {
        BlockState blockState = AltoClef.getInstance().getWorld().getBlockState(pos);
        return blockState.isAir();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof DestroyBlockTask task) {
            return task.pos.equals(pos);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Destroy block at " + pos.toShortString();
    }
}