package at.fhv.sysarch.lab2.homeautomation.sensors;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.*;
import at.fhv.sysarch.lab2.homeautomation.devices.Blinds;

public class WeatherSensor extends AbstractBehavior<WeatherCommand> {

    private final ActorRef<Blinds.BlindsCommand> blinds;

    public static Behavior<WeatherCommand> create(ActorRef<Blinds.BlindsCommand> blinds) {
        return Behaviors.setup(ctx -> new WeatherSensor(ctx, blinds));
    }

    private WeatherSensor(ActorContext<WeatherCommand> context, ActorRef<Blinds.BlindsCommand> blinds) {
        super(context);
        this.blinds = blinds;
    }

    @Override
    public Receive<WeatherCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadWeather.class, this::onReadWeather)
                .build();
    }

    private Behavior<WeatherCommand> onReadWeather(ReadWeather cmd) {
        getContext().getLog().info("WeatherSensor received weather: {}", cmd.getWeather());
        blinds.tell(new Blinds.AdjustBlinds(cmd.getWeather()));
        return this;
    }
}
