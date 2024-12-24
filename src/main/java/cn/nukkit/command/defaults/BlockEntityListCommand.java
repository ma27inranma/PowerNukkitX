package cn.nukkit.command.defaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;

public class BlockEntityListCommand extends Command implements CoreCommand {
    public BlockEntityListCommand(String name) {
        super(name, "list block entities");
        this.setPermission("nukkit.blockentity.list");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
            CommandParameter.newType("levelname", CommandParamType.STRING)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        Level level = Server.getInstance().getLevelByName(args[0]);
        if(level == null){
            sender.sendMessage(TextFormat.RED + "Level not found");
            return false;
        }

        sender.sendMessage(TextFormat.GREEN + "Block entities: " + level.getBlockEntities().size());

        HashMap<String, Integer> blockEntityCount = new HashMap<>();
        for(BlockEntity blockEntity : level.getBlockEntities().values()){
            String blockEntityName = blockEntity.getClass().getSimpleName();

            blockEntityCount.putIfAbsent(blockEntityName, 0);
            blockEntityCount.put(blockEntityName, blockEntityCount.get(blockEntityName) + 1);
        }

        List<Entry<String, Integer>> sorted = new ArrayList<>(blockEntityCount.entrySet());
        Collections.sort(sorted, (a, b) -> b.getValue().compareTo(a.getValue()));

        for(Entry<String, Integer> entry : sorted){
            sender.sendMessage(TextFormat.GREEN + entry.getKey() + ": " + entry.getValue());
        }

        return true;
    }
}
