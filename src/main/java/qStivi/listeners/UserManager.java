package qStivi.listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import qStivi.Bot;
import qStivi.db.DB;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.slf4j.LoggerFactory.getLogger;

public class UserManager extends ListenerAdapter {
//    private static final Logger logger = getLogger(UserManager.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    Timer timer = new Timer();
    List<Task> tasks = new ArrayList<>();

    public UserManager() throws SQLException, ClassNotFoundException {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<Task> _tasks;

                Lock r = lock.readLock();
                r.lock();
                try {
                    _tasks = tasks;
                } finally {
                    r.unlock();
                }

                for (Task task : _tasks) {
                    task.run();
                }
            }
        }, 60 * 1000, 60 * 1000);
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if (Bot.DEV_MODE && event.getChannelJoined().getIdLong() != Bot.DEV_VOICE_CHANNEL_ID) return;
        if (event.getMember().getUser().isBot()) return;

        var id = Long.parseLong(event.getMember().getUser().getId());

        Task task = new Task(new TimerTask() {
            @Override
            public void run() {
                var amountOfUsers = event.getMember().getVoiceState().getChannel().getMembers().size();
                var xp = (3 * amountOfUsers) + 2;
                try {
//                    db.increment("users", "xp", "id", id, xp);
                    db.incrementXP(xp, id);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                try {
//                    db.increment("users", "xp_voice", "id", id, xp);
                    db.incrementXPVoice(xp, id);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
//                logger.info(String.valueOf(amountOfUsers));
            }
        }, id);

        Lock w = lock.writeLock();
        w.lock();
        try {
            tasks.add(task);
        } finally {
            w.unlock();
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (Bot.DEV_MODE && !event.getChannelLeft().getId().equals(Bot.DEV_VOICE_CHANNEL_ID)) return;
//        logger.info("leave");
        var id = Long.parseLong(event.getMember().getId());
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.id == id) {
                Lock w = lock.writeLock();
                w.lock();
                try {
                    tasks.remove(task);
                } finally {
                    w.unlock();
                }
                task.cancel();
            }
        }
    }

    public static class Task {
        TimerTask timerTask;
        Long id;

        public Task(TimerTask timerTask, Long id) {
            this.timerTask = timerTask;
            this.id = id;
        }

        private void run() {
            this.timerTask.run();
        }

        private void cancel() {
            this.timerTask.cancel();
        }
    }
}
