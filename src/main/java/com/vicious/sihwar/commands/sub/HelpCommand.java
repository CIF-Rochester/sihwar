package com.vicious.sihwar.commands.sub;

import com.vicious.sihwar.commands.CommandException;
import com.vicious.sihwar.commands.GameCommand;
import org.bukkit.command.CommandSender;

public class HelpCommand extends GameCommand {
    private final GameCommand command;

    public HelpCommand(GameCommand command) {
        super("help", "Shows helpful information", "/" + command.getName() + " help", "help");
        this.command = command;
    }

    @Override
    protected void process(CommandSender sender, String[] args) throws CommandException {
        sender.sendMessage(command.getUsage());
    }
}
