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
        public WeatherTypes value;
        public SetWeather(WeatherTypes value) {
            this.value = value;
        }
    }

    //    swap between simulation modes (internal/external/manual/off)
    public static class SetSimulationMode implements WeatherEnvironmentCommand {
        public SimulationMode mode;
        public SetSimulationMode(SimulationMode mode) {
            this.mode = mode;
        }
    }

    public static class ExternalWeatherUpdate implements WeatherEnvironmentCommand {
        public WeatherTypes condition;
        public ExternalWeatherUpdate(WeatherTypes condition) {
            this.condition = condition;
        }
    }

    private SimulationMode mode = SimulationMode.EXTERNAL;
    private final ActorRef<WeatherCommand> weatherSensor;
    private final TimerScheduler<WeatherEnvironmentCommand> timers;
    private final Random random = new Random();
    private WeatherTypes currentWeather = WeatherTypes.SUNNY;


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
                .onMessage(ExternalWeatherUpdate.class, this::onExternalWeatherUpdate)
                .onMessage(SetSimulationMode.class, this::onSetSimulationMode)
                .onMessage(SetWeather.class, this::onSetWeather)
                .build();
    }

    private Behavior<WeatherEnvironmentCommand> onSetSimulationMode(SetSimulationMode cmd) {
        this.mode = cmd.mode;

        getContext().getLog().info("Simulation mode set to {}", mode);

        if (mode == SimulationMode.MANUAL || mode == SimulationMode.OFF) {
            timers.cancel(SimpleCommand.TICK); // Stop internal ticks
        } else if (mode == SimulationMode.INTERNAL) {
            timers.startTimerAtFixedRate(SimpleCommand.TICK, Duration.ofSeconds(5));
        }

        return this;
    }


    private Behavior<WeatherEnvironmentCommand> onTick() {
        if (mode == SimulationMode.INTERNAL) {
            currentWeather = getRandomWeather();
            getContext().getLog().info("Simulated weather: {}", currentWeather);
            weatherSensor.tell(new ReadWeather(currentWeather));
        }
        return this;
    }

    private Behavior<WeatherEnvironmentCommand> onStartSimulation() {
        mode = SimulationMode.EXTERNAL;
        getContext().getLog().info("Weather simulation started");
        return this;
    }

    private Behavior<WeatherEnvironmentCommand> onStopSimulation() {
        mode = SimulationMode.OFF;
        getContext().getLog().info("Weather simulation stopped");
        return this;
    }

    private Behavior<WeatherEnvironmentCommand> onSetWeather(SetWeather cmd) {
        mode = SimulationMode.MANUAL;
        currentWeather = cmd.value;
        getContext().getLog().info("Manually set weather to {}", currentWeather);
        weatherSensor.tell(new ReadWeather(currentWeather));
        return this;
    }

    private Behavior<WeatherEnvironmentCommand> onExternalWeatherUpdate(ExternalWeatherUpdate cmd) {
        if (mode == SimulationMode.EXTERNAL) {
            currentWeather = cmd.condition;
            getContext().getLog().info("External weather update received: {}", currentWeather);
            weatherSensor.tell(new ReadWeather(currentWeather));
        } else {
            getContext().getLog().debug("Ignored external weather update due to mode: {}", mode);
        }
        return this;
    }

    private WeatherTypes getRandomWeather() {
        WeatherTypes[] values = WeatherTypes.values();
        return values[random.nextInt(values.length)];
    }
}
