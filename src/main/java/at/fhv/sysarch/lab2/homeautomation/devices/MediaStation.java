package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.MediaCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.MediaPlayer;

public class MediaStation extends AbstractBehavior<MediaCommand> {

    private final ActorRef<Blinds.BlindsCommand> blinds;
    private boolean isPlaying = false;

    public static Behavior<MediaCommand> create(ActorRef<Blinds.BlindsCommand> blinds) {
        return Behaviors.setup(ctx -> new MediaStation(ctx, blinds));
    }

    private MediaStation(ActorContext<MediaCommand> context, ActorRef<Blinds.BlindsCommand> blinds) {
        super(context);
        this.blinds = blinds;
    }

    @Override
    public Receive<MediaCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(MediaPlayer.class, this::onMediaPlayer)
                .build();
    }

    private Behavior<MediaCommand> onMediaPlayer(MediaPlayer mediaPlayer) {
        switch (mediaPlayer.action()) {
            case PLAY -> {
                if (!isPlaying) {
                    getContext().getLog().info("Playing movie: {}", mediaPlayer.movieTitle());
                    isPlaying = true;
                    blinds.tell(new Blinds.CloseBlinds());
                } else {
                    getContext().getLog().info("Cannot play: Movie already playing.");
                }
            }
            case STOP -> {
                if (isPlaying) {
                    getContext().getLog().info("Stopping movie: {}", mediaPlayer.movieTitle());
                    isPlaying = false;
                    blinds.tell(new Blinds.ReevaluateBlinds());
                } else {
                    getContext().getLog().info("No movie is playing.");
                }
            }
        }
        return this;
    }
}
