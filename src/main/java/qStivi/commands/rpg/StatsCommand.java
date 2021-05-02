package qStivi.commands.rpg;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import qStivi.ICommand;
import qStivi.db.DB;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import static org.slf4j.LoggerFactory.getLogger;

public class StatsCommand implements ICommand {
    private static final Logger logger = getLogger(StatsCommand.class);

    Timer timer = new Timer();

    @Override
    public void handle(GuildMessageReceivedEvent event, String[] args) throws SQLException, ClassNotFoundException {
        if (event.isWebhookMessage()) return;
        var hook = event.getChannel();
        var db = new DB();
        var commandUser = event.getMessage().getMentionedMembers().size() > 0 ? event.getMessage().getMentionedMembers().get(0) : null;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                event.getMessage().delete().queue();
            }
        }, 3000);

        var user = commandUser == null ? event.getMember() : commandUser;
        if (user == null) {
            logger.error("userId is null!");
            return;
        }
        long userID = user.getIdLong();

        if (db.userDoesNotExists(userID)) {
            db.insert("users", "id", user);
        }

        var lvl = db.getLevel(userID);
        var money = db.selectLong("users", "money", "id", userID);
        var userName = user.getEffectiveName();
        var ranking = db.getRanking();
        long position = 1337;
        var blackJackWins = db.selectLong("users", "blackjack_wins", "id", userID);
        var blackJackLoses = db.selectLong("users", "blackjack_loses", "id", userID);
        if (blackJackLoses == null || blackJackLoses == 0) blackJackLoses = 1L;
        if (blackJackWins == null) {
            logger.error("userId is null!");
            return;
        }
        var winLoseRatio = (double) blackJackWins / blackJackLoses;

        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i) == user.getIdLong()) {
                position = i;
            }
        }

        var embed = new EmbedBuilder();
        embed.setColor(user.getColor());
        embed.setAuthor(userName, "https://youtu.be/dQw4w9WgXcQ", user.getUser().getEffectiveAvatarUrl());
        if (position != 1337) embed.addField("Rank", "#" + position, false);
        embed.addField("Level", String.valueOf(lvl), true);
        embed.addField("Money", money + " :gem:", true);
        var xp = db.selectLong("users", "xp", "id", userID);
        embed.addField("XP", String.valueOf(xp), true);
        embed.setFooter("BlackJack win/lose ratio: " + winLoseRatio);

        hook.sendMessage(embed.build()).delay(DURATION).flatMap(Message::delete).queue();
    }

    @NotNull
    @Override
    public String getName() {
        return "stats";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "See cool stuff.";
    }

    @Override
    public long getXp() {
        return 3;
    }
}
