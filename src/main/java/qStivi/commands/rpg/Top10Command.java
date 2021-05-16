package qStivi.commands.rpg;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import qStivi.Bot;
import qStivi.ICommand;
import qStivi.db.DB;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

public class Top10Command implements ICommand {

    @Override
    public void handle(GuildMessageReceivedEvent event, String[] args) throws SQLException, ClassNotFoundException {
        var hook = event.getChannel();
        var db = new DB();
        var embed = new EmbedBuilder();

        var list = db.getRanking();
        var size = Math.min(list.size(), 10);
        for (int i = 0; i < size; i++) {
            Long id = list.get(i);
            var money = db.getMoney(id);
            var lvl = db.getLevel(id);

            AtomicReference<String> name = new AtomicReference<>();

            event.getJDA().retrieveUserById(id)
                    .map(User::getName)
                    .queue(name::set);

            while (name.get() == null) {
                Thread.onSpinWait();
            }
            var xp = db.getXP(id);
            embed.addField("", "#" + i + " [" + name.get() + "](https://youtu.be/dQw4w9WgXcQ) " + money + " :gem: :white_small_square: " + xp + "xp LVL: " + lvl, false);
        }
        hook.sendMessage(embed.build()).queue();
    }

    @NotNull
    @Override
    public String getName() {
        return "top10";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Top 10 Players with the most money.";
    }

    @Override
    public long getXp() {
        return 3;
    }
}
