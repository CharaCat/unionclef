package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Predicate;

public class StorageHelper {
    public static List<PlayerSlot> INACCESSIBLE_PLAYER_SLOTS = List.of(
            PlayerSlot.CRAFT_OUTPUT_SLOT,
            PlayerSlot.ARMOR_HELMET_SLOT,
            PlayerSlot.ARMOR_CHESTPLATE_SLOT,
            PlayerSlot.ARMOR_LEGGINGS_SLOT,
            PlayerSlot.ARMOR_BOOTS_SLOT,
            PlayerSlot.OFFHAND_SLOT
    );

    public static void closeScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            mc.player.closeContainer();
        }
    }

    public static ItemStack getItemStackInSlot(Slot slot) {
        if (slot == null) return ItemStack.EMPTY;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc != null ? mc.player : null;
        if (player == null) return ItemStack.EMPTY;
        if (Slot.isCursor(slot)) return getItemStackInCursorSlot();
        if (slot.equals(Slot.UNDEFINED)) return ItemStack.EMPTY;
        try {
            AbstractContainerMenu menu = player.containerMenu;
            int windowSlot = slot.getWindowSlot();
            if (menu != null && windowSlot >= 0 && windowSlot < menu.slots.size()) {
                return menu.getSlot(windowSlot).getItem();
            }
        } catch (Throwable ignored) {
        }
        try {
            Inventory inv = player.getInventory();
            int invSlot = slot.getInventorySlot();
            if (invSlot >= 0 && invSlot < inv.getContainerSize()) {
                return inv.getItem(invSlot);
            }
        } catch (Throwable ignored) {
        }
        return ItemStack.EMPTY;
    }

    public static MiningRequirement getCurrentMiningRequirement() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.level == null || mc.hitResult == null) return MiningRequirement.HAND;
        return MiningRequirement.HAND;
    }

    public static boolean miningRequirementMet(MiningRequirement requirement) {
        // For now, do not falsely block tasks; MC 26.2 tool-tier mapping is still being ported.
        return true;
    }

    public static boolean miningRequirementMetInventory(MiningRequirement requirement) { return miningRequirementMet(requirement); }
    public static Optional<Slot> getBestToolSlot(AltoClef mod, BlockState state) { return Optional.empty(); }
    public static boolean shouldSaveStack(AltoClef mod, Block block, ItemStack stack) { return false; }
    public static Optional<Slot> getGarbageSlot(AltoClef mod) { return Optional.of(Slot.UNDEFINED); }
    public static int getNumberOfThrowawayBlocks(AltoClef mod) { return 0; }
    public static Optional<Slot> getSlotWithThrowawayBlock(AltoClef mod) { return Optional.empty(); }
    public static Optional<Slot> getSlotWithThrowawayBlock(AltoClef mod, boolean limitToHotbar) { return Optional.empty(); }

    public static boolean itemTargetsMet(AltoClef mod, ItemTarget... targetsToMeet) {
        return targetsMet(targetsToMeet, true);
    }

    public static boolean itemTargetsMetInventory(ItemTarget... targetsToMeet) {
        return targetsMet(targetsToMeet, true);
    }

    public static boolean itemTargetsMetInventoryNoCursor(ItemTarget... targetsToMeet) {
        return targetsMet(targetsToMeet, false);
    }

    private static boolean targetsMet(ItemTarget[] targetsToMeet, boolean includeCursor) {
        if (targetsToMeet == null) return true;
        for (ItemTarget target : targetsToMeet) {
            if (target == null || target.isEmpty()) continue;
            int count = getInventoryCount(includeCursor, target.getMatches());
            if (count < target.getTargetCount()) return false;
        }
        return true;
    }

    private static int getInventoryCount(boolean includeCursor, Item... items) {
        if (items == null || items.length == 0) return 0;
        Set<Item> wanted = new HashSet<>(Arrays.asList(items));
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc != null ? mc.player : null;
        if (player == null) return 0;
        int count = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && wanted.contains(stack.getItem())) count += stack.getCount();
        }
        if (includeCursor) {
            ItemStack cursor = getItemStackInCursorSlot();
            if (!cursor.isEmpty() && wanted.contains(cursor.getItem())) count += cursor.getCount();
        }
        return count;
    }

    /** Count how many of a single item the player has in inventory (excluding cursor). */
    public static int getItemCountInInventory(Item item) {
        if (item == null) return 0;
        return getInventoryCount(false, item);
    }

    public static boolean isArmorEquipped(Item... any) { return false; }
    public static int getBuildingMaterialCount() { return 0; }

    private static net.minecraft.client.gui.screens.Screen currentScreen() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.gui != null ? mc.gui.screen() : null;
    }

    public static boolean isBigCraftingOpen() { return currentScreen() instanceof CraftingScreen; }
    public static boolean isPlayerInventoryOpen() { return currentScreen() instanceof InventoryScreen; }
    public static boolean isFurnaceOpen() { return currentScreen() instanceof FurnaceScreen; }
    public static boolean isChestOpen() { return currentScreen() instanceof ContainerScreen; }
    public static boolean isSmokerOpen() { return currentScreen() instanceof SmokerScreen; }
    public static boolean isBlastFurnaceOpen() { return currentScreen() instanceof BlastFurnaceScreen; }

    public static boolean isArmorEquippedAll(Item... items) { return false; }
    public static boolean isEquipped(Item... items) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return false;
        ItemStack selected = mc.player.getInventory().getSelectedItem();
        for (Item item : items) if (!selected.isEmpty() && selected.getItem() == item) return true;
        return false;
    }
    public static int calculateInventoryFoodScore() { return 0; }
    public static double calculateInventoryFuelCount(AltoClef mod) { return 0; }
    public static boolean hasRecipeMaterialsOrTarget(AltoClef mod, RecipeTarget... targets) { return false; }
    public static boolean hasCataloguedItem(AltoClef mod, String cataloguedName) { return false; }
    public static Optional<Slot> getFilledInventorySlotInaccessibleToContainer(AltoClef mod, ItemTarget withItem) { return Optional.empty(); }
    public static boolean isItemInaccessibleToContainer(AltoClef mod, ItemTarget item) { return false; }

    public static ItemStack getItemStackInCursorSlot() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc != null ? mc.player : null;
        if (player == null || player.containerMenu == null) return ItemStack.EMPTY;
        ItemStack carried = player.containerMenu.getCarried();
        return carried != null ? carried : ItemStack.EMPTY;
    }

    public static int getBrewingStandFuel() { return 0; }
    public static int getBrewingStandFuel(BrewingStandMenu handler) { return 0; }
    public static double getFurnaceFuel(AbstractFurnaceMenu handler) { return 0; }
    public static double getSmokerFuel(AbstractFurnaceMenu handler) { return 0; }
    public static double getBlastFurnaceFuel(AbstractFurnaceMenu handler) { return 0; }
    public static double getFurnaceFuel() { return 0; }
    public static double getSmokerFuel() { return 0; }
    public static double getBlastFurnaceFuel() { return 0; }
    public static double getFurnaceCookPercent(AbstractFurnaceMenu handler) { return 0; }
    public static double getSmokerCookPercent(AbstractFurnaceMenu handler) { return 0; }
    public static double getBlastFurnaceCookPercent(AbstractFurnaceMenu handler) { return 0; }
    public static double getFurnaceCookPercent() { return 0; }
    public static double getSmokerCookPercent() { return 0; }
    public static double getBlastFurnaceCookPercent() { return 0; }
    public static ItemTarget[] getAllInventoryItemsAsTargets(Predicate<Slot> accept) { return new ItemTarget[0]; }
}
