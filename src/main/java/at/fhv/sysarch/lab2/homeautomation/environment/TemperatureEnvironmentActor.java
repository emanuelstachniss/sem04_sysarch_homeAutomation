package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.ReadTemperature;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.TemperatureCommand;

import java.time.Duration;
import java.util.Random;

public class TemperatureEnvironmentActor extends AbstractBehavior<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> {
    public interface TemperatureEnvironmentCommand {}

    public enum SimpleCommand implements TemperatureEnvironmentCommand {
        TICK
    }

    public static class SetTemperature implements TemperatureEnvironmentCommand {
        public Double temperature;
        public SetTemperature(Double value) {
            this.temperature = value;
        }
    }

    public static class ExternalTemperatureUpdate implements TemperatureEnvironmentCommand {
        public Double temperature;
        public ExternalTemperatureUpdate(Double temperature) {
            this.temperature = temperature;
        }
    }

    public static class SetSimulationMode implements TemperatureEnvironmentCommand {
        public SimulationMode mode;
        public SetSimulationMode(SimulationMode mode) {
            this.mode = mode;
        }
    }

    private final ActorRef<TemperatureCommand> temperatureSensor;
    private final TimerScheduler<TemperatureEnvironmentCommand> timers;
    private final Random random = new Random();
    private Double currentTemperature;
    private SimulationMode mode = SimulationMode.EXTERNAL;

    private TemperatureEnvironmentActor(ActorContext<TemperatureEnvironmentCommand> context,
                                        TimerScheduler<TemperatureEnvironmentCommand> timers,
                                        ActorRef<TemperatureCommand> temperatureSensor) {
        super(context);
        this.temperatureSensor = temperatureSensor;
        this.timers = timers;
        this.currentTemperature = 23.0;
        timers.startTimerAtFixedRate(SimpleCommand.TICK, Duration.ofSeconds(5));
        getContext().getLog().info("TemperatureEnvironmentActor started with initial temperature: {}", currentTemperature);
    }

    public static Behavior<TemperatureEnvironmentCommand> create(ActorRef<TemperatureCommand> temperatureSensor) {
        return Behaviors.withTimers(timers -> Behaviors.setup(ctx -> new TemperatureEnvironmentActor(ctx, timers, temperatureSensor)));
    }

    @Override
    public Receive<TemperatureEnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(SimpleCommand.TICK, this::onTick)
                .onMessage(ExternalTemperatureUpdate.class, this::onExternalTemperatureUpdate)
                .onMessage(SetSimulationMode.class, this::onSetSimulationMode)
                .onMessage(SetTemperature.class, this::onSetTemperature)
                .build();
    }

    private Behavior<TemperatureEnvironmentCommand> onTick() {
        if (mode == SimulationMode.INTERNAL) {
            currentTemperature = getRandomTemperature();
            getContext().getLog().info("Simulated temperature: {}", currentTemperature);
            temperatureSensor.tell(new ReadTemperature(currentTemperature));
        }
        return this;
    }

    private Double getRandomTemperature() {
        double delta = random.nextDouble(); // Always between 0.0 and 1.0
        boolean increase = random.nextBoolean(); // true = increase, false = decrease
        double newTemp = increase ? currentTemperature + delta : currentTemperature - delta;
        return Math.round(newTemp * 10.0) / 10.0; // Round to 1 decimal place
    }

    private Behavior<TemperatureEnvironmentCommand> onStartSimulation() {
        mode = SimulationMode.EXTERNAL;
        getContext().getLog().info("Temperature simulation started");
        return this;
    }

    private Behavior<TemperatureEnvironmentCommand> onStopSimulation() {
        mode = SimulationMode.OFF;
        getContext().getLog().info("Temperature simulation stopped");
        return this;
    }

    private Behavior<TemperatureEnvironmentCommand> onSetTemperature(SetTemperature cmd) {
        mode = SimulationMode.MANUAL;
        currentTemperature = cmd.temperature;
        getContext().getLog().info("Temperature manually set to {}", currentTemperature);
        temperatureSensor.tell(new ReadTemperature(currentTemperature));
        return this;
    }

    private Behavior<TemperatureEnvironmentCommand> onExternalTemperatureUpdate(ExternalTemperatureUpdate cmd) {
        if (mode == SimulationMode.EXTERNAL) {
            currentTemperature = cmd.temperature;
            getContext().getLog().info("External temperature update received: {}", currentTemperature);
            temperatureSensor.tell(new ReadTemperature(currentTemperature));
        } else {
            getContext().getLog().debug("Ignored external temperature update due to mode: {}", mode);
        }
        return this;
    }

    private Behavior<TemperatureEnvironmentCommand> onSetSimulationMode(SetSimulationMode cmd) {
        this.mode = cmd.mode;
        getContext().getLog().info("Simulation mode set to {}", mode);

        if (mode == SimulationMode.MANUAL || mode == SimulationMode.OFF) {
            timers.cancel(WeatherEnvironmentActor.SimpleCommand.TICK); // Stop internal ticks
        } else if (mode == SimulationMode.INTERNAL) {
            currentTemperature = 23.0; //start value
            timers.startTimerAtFixedRate(SimpleCommand.TICK, Duration.ofSeconds(5));
        }
        return this;
    }

}
