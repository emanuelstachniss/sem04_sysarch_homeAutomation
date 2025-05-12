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

    public static class CloseBlinds implements BlindsCommand {}

    public static class ReevaluateBlinds implements BlindsCommand {}

    private WeatherTypes lastKnownWeather = WeatherTypes.SUNNY; // Defaultwert
    private boolean manuallyClosed = false;

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
                .onMessage(CloseBlinds.class, this::onCloseBlinds)
                .onMessage(ReevaluateBlinds.class, this::onReevaluateBlinds)
                .build();
    }

    private Behavior<BlindsCommand> onAdjustBlinds(AdjustBlinds cmd) {
        lastKnownWeather = cmd.weather;

        if (!manuallyClosed) {
            if (cmd.weather == WeatherTypes.SUNNY) {
                getContext().getLog().info("Weather is sunny -> Lowering blinds.");
            } else if (cmd.weather == WeatherTypes.CLOUDY) {
                getContext().getLog().info("Weather is cloudy -> Raising blinds.");
            }
        } else {
            getContext().getLog().info("Blinds manually closed -> Ignoring weather update.");
        }

        return this;
    }

    private Behavior<BlindsCommand> onCloseBlinds(CloseBlinds cmd) {
        getContext().getLog().info("Blinds manually closed for movie.");
        manuallyClosed = true;
        return this;
    }

    private Behavior<BlindsCommand> onReevaluateBlinds(ReevaluateBlinds cmd) {
        manuallyClosed = false;
        return onAdjustBlinds(new AdjustBlinds(lastKnownWeather));
    }
}
