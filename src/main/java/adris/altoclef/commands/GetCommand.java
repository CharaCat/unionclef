package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.*;
import adris.altoclef.commandsystem.args.ItemTargetArg;
import adris.altoclef.commandsystem.args.ListArg;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GetCommand extends Command {

    public GetCommand() throws CommandException {
        super("get", "Get an item/resource",
                new ListArg<>(new ItemTargetArg("stack"), "items")
        );
    }


    private void getItems(AltoClef mod, List<ItemTarget> items) {
        Task targetTask;
        if (items == null || items.isEmpty()) {
            mod.log("You must specify at least one item!");
            finish();
            return;
        }
        // Adjust each item target count: command "get cobblestone 14" means
        // "gather 14 MORE cobblestone" — we add the current inventory count so
        // the task completes when the player has (existing + 14) total.
        List<ItemTarget> adjusted = new ArrayList<>(items.size());
        for (ItemTarget target : items) {
            int existing = 0;
            Item[] matches = target.getMatches();
            if (matches != null && matches.length > 0) {
                for (Item match : matches) {
                    existing += adris.altoclef.util.helpers.StorageHelper.getItemCountInInventory(match);
                }
            }
            adjusted.add(new ItemTarget(target, target.getTargetCount() + existing));
        }
        if (adjusted.size() == 1) {
            targetTask = TaskCatalogue.getItemTask(adjusted.get(0));
        } else {
            targetTask = TaskCatalogue.getSquashedItemTask(adjusted.toArray(new ItemTarget[0]));
        }
        if (targetTask != null) {
            mod.runUserTask(targetTask, this::finish);
        } else {
            finish();
        }
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        List<ItemTarget> items = parser.get(List.class);

        getItems(mod, items);
    }
}