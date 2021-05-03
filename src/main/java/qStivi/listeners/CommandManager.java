package qStivi.listeners;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import qStivi.Bot;
import qStivi.ICommand;
import qStivi.commands.*;
import qStivi.commands.music.*;
import qStivi.commands.rpg.*;
import qStivi.db.DB;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class CommandManager extends ListenerAdapter {
    private static final Logger logger = getLogger(CommandManager.class);

    public final List<ICommand> commandList = new ArrayList<>();
//    public final Queue<SlashCommandEvent> events = new LinkedList<>();

    public CommandManager() {
        commandList.add(new TestCommand());
        commandList.add(new RollCommand());
        commandList.add(new StopCommand());
        commandList.add(new ContinueCommand());
        commandList.add(new DndCommand());
        commandList.add(new PauseCommand());
        commandList.add(new RepeatCommand());
        commandList.add(new SkipCommand());
        commandList.add(new CleanCommand());
        commandList.add(new JoinCommand());
        commandList.add(new LeaveCommand());
//        commandList.add(new PingCommand());
        commandList.add(new RedditCommand());
        commandList.add(new PlayCommand());
        commandList.add(new ShutdownCommand());
        commandList.add(new StatsCommand());
        commandList.add(new Top10Command());
        commandList.add(new WorkCommand());
        commandList.add(new BlackjackCommand());
        commandList.add(new moneyCommand());
        commandList.add(new DonateCommand());
        commandList.add(new BegCommand());
        commandList.add(new SlotsCommand());
    }

    public static String cleanForCommand(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFKD);
        str = str.replaceAll("[^a-z0-9A-Z -]", ""); // Remove all non valid chars
        str = str.replaceAll(" {2}", " ").trim(); // convert multiple spaces into one space
        return str;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        var channelID = event.getChannel().getId();
        if (Bot.DEV_MODE && !channelID.equals(Bot.DEV_CHANNEL_ID)) return;
        //noinspection ConstantConditions
        if (!Bot.DEV_MODE && !event.getChannel().getParent().getId().equals("833734651070775338")) {
            return;
        }
        if (!Bot.DEV_MODE && channelID.equals(Bot.DEV_CHANNEL_ID)) return;
        var message = event.getMessage().getContentRaw();
        if (!message.startsWith("/")) return;
        String[] args;
        if (!message.startsWith("/play")) {
            message = cleanForCommand(message);
            args = message.split(" ");
        } else {
            args = message.split(" ");
            args[0] = cleanForCommand(args[0]);
        }

        for (ICommand command : commandList) {
            if (command.getName().equals(args[0])) {
                DB db = null;
                try {
                    db = new DB();
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
                logger.info(event.getAuthor().getName() + " issued /" + args[0]);

//                events.offer(event);
                try {
                    command.handle(event, args);
                    db.incrementCommandTimesRecognized(command.getName(), 1, event.getAuthor().getIdLong());
                } catch (SQLException | ClassNotFoundException throwables) {
                    throwables.printStackTrace();
                }


                var id = event.getAuthor().getIdLong();
                Long diff = null;
                try {
                    diff = db.getLastCommand(id);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                if (diff > 10) {
                    try {
//                        db.increment("users", "xp", "id", id, command.getXp());
                        db.incrementXP(command.getXp(), id);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    try {
//                        db.increment("users", "xp_command", "id", id, command.getXp());
                        db.incrementCommandXP(command.getName(), command.getXp(), id);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
//                    db.update("users", "last_command_xp", "id", id, new Date().getTime() / 1000);
                    try {
                        db.setLastCommand(new Date().getTime(), id);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }

//                db.update("users", "last_command", "id", event.getAuthor().getIdLong(), new Date().getTime() / 1000);
                try {
                    db.setLastCommand(new Date().getTime(), id);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                logger.info("Event offered.");
            }
        }

    }
}
