package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.ReadWeather;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherTypes;

import java.time.Duration;
import java.util.Random;

public class WeatherEnvironmentActor extends AbstractBehavior<WeatherEnvironmentActor.WeatherEnvironmentCommand> {

    public interface WeatherEnvironmentCommand {}

    public enum SimpleCommand implements WeatherEnvironmentCommand {
        TICK, START_SIMULATION, STOP_SIMULATION
    }

    public static class SetWeather implements WeatherEnvironmentCommand {
        public final WeatherTypes value;

        public SetWeather(WeatherTypes value) {
            this.value = value;
        }
    }

    private final ActorRef<WeatherCommand> weatherSensor;
    private final TimerScheduler<WeatherEnvironmentCommand> timers;
    private final Random random = new Random();
    private boolean simulate = true;
    private WeatherTypes currentWeather = WeatherTypes.sunny;

    private WeatherEnvironmentActor(ActorContext<WeatherEnvironmentCommand> context,
                                    TimerScheduler<WeatherEnvironmentCommand> timers,
                                    ActorRef<WeatherCommand> weatherSensor) {
        super(context);
        this.weatherSensor = weatherSensor;
        this.timers = timers;

        timers.startTimerAtFixedRate(SimpleCommand.TICK, Duration.ofSeconds(5));
        getContext().getLog().info("WeatherEnvironmentActor started with initial weather: {}", currentWeather);
    }

    public static Behavior<WeatherEnvironmentCommand> create(ActorRef<WeatherCommand> weatherSensor) {
        return Behaviors.withTimers(timers -> Behaviors.setup(ctx -> new WeatherEnvironmentActor(ctx, timers, weatherSensor)));
    }

    @Override
    public Receive<WeatherEnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(SimpleCommand.TICK, this::onTick)
                .onMessageEquals(SimpleCommand.START_SIMULATION, this::onStartSimulation)
                .onMessageEquals(SimpleCommand.STOP_SIMULATION, this::onStopSimulation)
                .onMessage(SetWeather.class, this::onSetWeather)
                .build();
    }

    private Behavior<WeatherEnvironmentCommand> onTick() {
        if (simulate) {
            currentWeather = getRandomWeather();
            getContext().getLog().info("Simulated weather: {}", currentWeather);
            weatherSensor.tell(new ReadWeather(currentWeather));
        }
        return this;
    }

    private Behavior<WeatherEnvironmentCommand> onStartSimulation() {
        simulate = true;
        getContext().getLog().info("Weather simulation started");
        return this;
    }

    private Behavior<WeatherEnvironmentCommand> onStopSimulation() {
        simulate = false;
        getContext().getLog().info("Weather simulation stopped");
        return this;
    }

    private Behavior<WeatherEnvironmentCommand> onSetWeather(SetWeather cmd) {
        simulate = false;
        currentWeather = cmd.value;
        getContext().getLog().info("Manually set weather to {}", currentWeather);
        weatherSensor.tell(new ReadWeather(currentWeather));
        return this;
    }

    private WeatherTypes getRandomWeather() {
        WeatherTypes[] values = WeatherTypes.values();
        return values[random.nextInt(values.length)];
    }
}
