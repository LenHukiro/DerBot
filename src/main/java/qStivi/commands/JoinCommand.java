package qStivi.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import qStivi.Bot;
import qStivi.ICommand;
import qStivi.commands.rpg.SkillsCommand;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

public class JoinCommand implements ICommand {

    private long xp;

    public static boolean join(Guild guild, User author) {
        AtomicBoolean successful = new AtomicBoolean(false);
        guild.getVoiceChannels().forEach(
                (channel) -> channel.getMembers().forEach(
                        (member) -> {
                            if (member.getId().equals(author.getId())) {
                                //audioManager = guild.getAudioManager();
                                guild.getAudioManager().openAudioConnection(channel);
                                successful.set(true);
                            }
                        }
                )
        );
        return successful.get();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void handle(GuildMessageReceivedEvent event, String[] args) throws SQLException, ClassNotFoundException {
        var hook = event.getChannel();

        Guild guild = event.getGuild();
        User author = event.getMember().getUser();

        var success = join(guild, author);
        if (success) {
            hook.sendMessage("Hi").queue();
        } else {
            hook.sendMessage("Something went wrong :(").queue();
        }


        xp = 3 + (long) (3 * SkillsCommand.getSocialXPPMultiplier(event.getAuthor().getIdLong()));
    }

    @NotNull
    @Override
    public String getName() {
        return "join";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Connects or moves bot to your voice channel.";
    }

    @Override
    public long getXp() {
        return xp;
    }
}
