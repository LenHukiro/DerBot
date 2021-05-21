package qStivi.commands.rpg;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import qStivi.ICommand;
import qStivi.db.DB;

import java.sql.SQLException;

public class moneyCommand implements ICommand {

    @Override
    public void handle(GuildMessageReceivedEvent event, String[] args) throws SQLException, ClassNotFoundException {
        var hook = event.getChannel();
        if (!(event.getAuthor().getIdLong() == 219108246143631364L)) {
            hook.sendMessage("You don't have the permission to do that").queue();
            return;
        }

        var subcommand = args[1];
        var userID = event.getMessage().getMentionedUsers().get(0).getIdLong();
        var amount = Long.parseLong(args[3]);

        var db = new DB();
        if (subcommand.equals("give")) {
//            db.increment("users", "money", "id", userID, amount);
            db.incrementMoney(amount, userID);
        }
        if (subcommand.equals("remove")) {
//            db.decrement("users", "money", "id", userID, amount);
            db.decrementMoney(amount, userID);
        }
        hook.sendMessage("Done!").queue();
    }

    @NotNull
    @Override
    public String getName() {
        return "money";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Manages money.";
    }

    @Override
    public long getXp() {
        return 0;
    }
}