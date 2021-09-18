package me.Larffxx.test;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MusicBot {
    private static AudioPlayerManager playerManager;
    private final static String TOKEN = "ODg4NDg1NTIwNjAxMTIwODI4.YUTYnw.4OyMQ55qp2IOo3Rb4bxcFrdlUtQ";
    private static TrackScheduler scheduler;
    private static AudioPlayer player;

    public static void main(String[] args) {

        AudioProvider provider = createLavaPlayer();
        final DiscordClient client = DiscordClient.create(TOKEN);
        final GatewayDiscordClient gateway = client.login().block();

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();
            final List<String> command = Arrays.asList(message.getContent().split(" "));
            if ("!connect".equals(message.getContent())) {

                final Member member = event.getMember().orElse(null);
                if (member != null) {
                    final VoiceState voiceState = member.getVoiceState().block();
                    if (voiceState != null) {
                        final VoiceChannel channel = voiceState.getChannel().block();
                        if (channel != null) {
                            // join returns a VoiceConnection which would be required if we were
                            // adding disconnection features, but for now we are just ignoring it.
                            channel.join(spec -> spec.setProvider(provider)).block();
                        }
                    }
                }
            } else if ("!play".equals(command.get(0))) {
                playerManager.loadItem(command.get(1), scheduler);
            } else if ("!stop".equals(command.get(0))) {
                player.stopTrack();
            }

        });
        gateway.onDisconnect().block();
    }

    private static AudioProvider createLavaPlayer() {

        // Creates AudioPlayer instances and translates URLs to AudioTrack instances
        playerManager = new DefaultAudioPlayerManager();

// This is an optimization strategy that Discord4J can utilize.
// It is not important to understand
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

// Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);

// Create an AudioPlayer so Discord4J can receive audio data
        player = playerManager.createPlayer();
        scheduler = new TrackScheduler(player);
// We will be creating LavaPlayerAudioProvider in the next step

        return new LavaPlayerAudioProvider(player);
    }

}