package qStivi.commands.music;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import qStivi.Bot;
import qStivi.ICommand;
import qStivi.audioManagers.PlayerManager;
import qStivi.commands.rpg.SkillsCommand;

import javax.annotation.Nonnull;
import java.sql.SQLException;

public class StopCommand implements ICommand {

    private long xp;

    @Override
    public void handle(GuildMessageReceivedEvent event, String[] args) throws SQLException, ClassNotFoundException {
        var hook = event.getChannel();
        PlayerManager.getINSTANCE().clearQueue(event.getGuild());
        PlayerManager.getINSTANCE().skip(event.getGuild());
        hook.sendMessage("Playback stopped.").queue();
        xp = 0;

        xp = 3 + (long) (3 * SkillsCommand.getSocialXPPMultiplier(event.getAuthor().getIdLong()));
    }

    @Override
    public @Nonnull
    String getName() {
        return "stop";
    }

    @Override
    public @Nonnull
    String getDescription() {
        return "Stop music from playing and clears queue";
    }

    @Override
    public long getXp() {
        return xp;
    }
}
