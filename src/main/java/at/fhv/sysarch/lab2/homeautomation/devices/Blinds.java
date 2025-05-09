package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherTypes;

public class Blinds extends AbstractBehavior<Blinds.BlindsCommand> {

    public interface BlindsCommand {}

    public static class AdjustBlinds implements BlindsCommand {
        public final WeatherTypes weather;

        public AdjustBlinds(WeatherTypes weather) {
            this.weather = weather;
        }
    }

    public static Behavior<BlindsCommand> create() {
        return Behaviors.setup(Blinds::new);
    }

    private Blinds(ActorContext<BlindsCommand> context) {
        super(context);
    }

    @Override
    public Receive<BlindsCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AdjustBlinds.class, this::onAdjustBlinds)
                .build();
    }

    private Behavior<BlindsCommand> onAdjustBlinds(AdjustBlinds cmd) {
        if (cmd.weather == WeatherTypes.sunny) {
            getContext().getLog().info("Weather is sunny -> Lowering blinds.");
        } else if (cmd.weather == WeatherTypes.cloudy) {
            getContext().getLog().info("Weather is cloudy -> Raising blinds.");
        }
        return this;
    }
}
