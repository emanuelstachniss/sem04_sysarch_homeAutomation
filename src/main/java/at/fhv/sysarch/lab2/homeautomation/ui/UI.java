package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.MediaType;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.environment.SimulationMode;
import at.fhv.sysarch.lab2.homeautomation.environment.TemperatureEnvironmentActor;
import at.fhv.sysarch.lab2.homeautomation.environment.WeatherEnvironmentActor;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherTypes;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.MediaCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.MediaPlayer;

import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private final ActorRef<AirCondition.AirConditionCommand> airCondition;
    private final ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnvironment;
    private final ActorRef<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> temperatureEnvironment;
    private final ActorRef<MediaCommand> mediaStation;

    public static Behavior<Void> create(
            ActorRef<AirCondition.AirConditionCommand> airCondition,
            ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnvironment,
            ActorRef<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> temperatureEnvironment,
            ActorRef<MediaCommand> mediaStation) {
        return Behaviors.setup(context -> new UI(context, airCondition, weatherEnvironment, temperatureEnvironment, mediaStation));
    }

    private UI(
            ActorContext<Void> context,
            ActorRef<AirCondition.AirConditionCommand> airCondition,
            ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnvironment,
            ActorRef<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> temperatureEnvironment,
            ActorRef<MediaCommand> mediaStation) {
        super(context);
        this.airCondition = airCondition;
        this.weatherEnvironment = weatherEnvironment;
        this.temperatureEnvironment = temperatureEnvironment;
        this.mediaStation = mediaStation;
        new Thread(this::runCommandLine).start();

        getContext().getLog().info("UI started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }

    public void runCommandLine() {
        Scanner scanner = new Scanner(System.in);
        String reader = "";

        while (!reader.equalsIgnoreCase("quit") && scanner.hasNextLine()) {
            reader = scanner.nextLine();
            String[] command = reader.split(" ", 2);

            switch (command[0]) {
                case "t":
                    if (command.length > 1) {
                        try {
                            Double temperature = Double.parseDouble(command[1]);
                            temperatureEnvironment.tell(new TemperatureEnvironmentActor.SetTemperature(temperature));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid temperature value.");
                        }
                    }
                    break;

                case "weather":
                    if (command.length > 1) {
                        try {
                            WeatherTypes weather = WeatherTypes.valueOf(command[1].toUpperCase());
                            weatherEnvironment.tell(new WeatherEnvironmentActor.SetWeather(weather));
                        } catch (IllegalArgumentException e) {
                            System.out.println("Unknown weather type. Use: SUNNY, CLOUDY, RAIN, SNOW or STORM");
                        }
                    }
                    break;

                case "start":
                    if (command.length > 1) {
                        try {
                            SimulationMode mode = SimulationMode.valueOf(command[1].toUpperCase());
                            weatherEnvironment.tell(new WeatherEnvironmentActor.SetSimulationMode(mode));
                            temperatureEnvironment.tell(new TemperatureEnvironmentActor.SetSimulationMode(mode));
                        } catch (IllegalArgumentException e) {
                            System.out.println("Unknown simulation mode. Use: start <mode> - Start weather simulation (external/internal)");
                        }
                    } else {
                        System.out.println("Unknown simulation mode. Use: start <mode> - Start weather simulation (external/internal)");
                    }
                    break;

                case "stopsim":
                    weatherEnvironment.tell(new WeatherEnvironmentActor.SetSimulationMode(SimulationMode.OFF));
                    temperatureEnvironment.tell(new TemperatureEnvironmentActor.SetSimulationMode(SimulationMode.OFF));
                    break;

                case "play":
                    if (command.length > 1) {
                        mediaStation.tell(new MediaPlayer(command[1], MediaType.PLAY));
                    } else {
                        System.out.println("Usage: play <movieTitle>");
                    }
                    break;

                case "stop":
                    if (command.length > 1) {
                        mediaStation.tell(new MediaPlayer(command[1], MediaType.STOP));

                    } else {
                        System.out.println("Usage: stop <movieTitle>");
                    }
                    break;

                case "help":
                    System.out.println("Commands:");
                    System.out.println("  t <value>         - Set temperature (e.g., t 23.5)");
                    System.out.println("  weather <type>    - Set weather manually (sunny/cloudy)");
                    System.out.println("  start <mode>      - Start weather simulation (external/internal)");
                    System.out.println("  stopsim              - Stop weather simulation");
                    System.out.println("  play <title>      - Play movie (e.g., play Inception)");
                    System.out.println("  stop <title>      - Stop movie");
                    System.out.println("  quit              - Exit");
                    break;

                default:
                    System.out.println("Unknown command. Type 'help' for options.");
            }
        }

        getContext().getLog().info("UI done");
    }
}
