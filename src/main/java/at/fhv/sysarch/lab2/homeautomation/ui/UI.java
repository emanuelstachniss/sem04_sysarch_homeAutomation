package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.environment.WeatherEnvironmentActor;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherTypes;

import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private final ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private final ActorRef<AirCondition.AirConditionCommand> airCondition;
    private final ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnvironment;

    public static Behavior<Void> create(
            ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
            ActorRef<AirCondition.AirConditionCommand> airCondition,
            ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnvironment) {
        return Behaviors.setup(context -> new UI(context, tempSensor, airCondition, weatherEnvironment));
    }

    private UI(
            ActorContext<Void> context,
            ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
            ActorRef<AirCondition.AirConditionCommand> airCondition,
            ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnvironment) {
        super(context);
        this.tempSensor = tempSensor;
        this.airCondition = airCondition;
        this.weatherEnvironment = weatherEnvironment;
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
            String[] command = reader.split(" ");

            switch (command[0]) {
                case "t":
                    if (command.length > 1) {
                        try {
                            double temperature = Double.parseDouble(command[1]);
                            tempSensor.tell(new TemperatureSensor.ReadTemperature(temperature));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid temperature value.");
                        }
                    }
                    break;

                case "weather":
                    if (command.length > 1) {
                        try {
                            WeatherTypes weather = WeatherTypes.valueOf(command[1].toLowerCase());
                            weatherEnvironment.tell(new WeatherEnvironmentActor.SetWeather(weather));
                        } catch (IllegalArgumentException e) {
                            System.out.println("Unknown weather type. Use: sunny, cloudy");
                        }
                    }
                    break;

                case "startsim":
                    weatherEnvironment.tell(WeatherEnvironmentActor.SimpleCommand.START_SIMULATION);
                    break;

                case "stopsim":
                    weatherEnvironment.tell(WeatherEnvironmentActor.SimpleCommand.STOP_SIMULATION);
                    break;

                case "help":
                    System.out.println("Commands:");
                    System.out.println("  t <value>         - Set temperature (e.g., t 23.5)");
                    System.out.println("  weather <type>    - Set weather manually (sunny/cloudy)");
                    System.out.println("  startsim          - Start weather simulation");
                    System.out.println("  stopsim           - Stop weather simulation");
                    System.out.println("  quit              - Exit");
                    break;

                default:
                    System.out.println("Unknown command. Type 'help' for options.");
            }
        }

        getContext().getLog().info("UI done");
    }
}
