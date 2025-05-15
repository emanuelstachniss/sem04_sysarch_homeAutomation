package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.commands.fridge.*;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.MediaCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.MediaPlayer;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.MediaType;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherTypes;
import at.fhv.sysarch.lab2.homeautomation.devices.*;
import at.fhv.sysarch.lab2.homeautomation.devices.Fridge;
import at.fhv.sysarch.lab2.homeautomation.environment.SimulationMode;
import at.fhv.sysarch.lab2.homeautomation.environment.WeatherEnvironmentActor;

import java.util.List;
import java.util.Scanner;

public class UI extends AbstractBehavior<Object> {

    private final ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private final ActorRef<AirCondition.AirConditionCommand> airCondition;
    private final ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnvironment;
    private final ActorRef<MediaCommand> mediaStation;
    private final ActorRef<FridgeCommand> fridge;

    public static Behavior<Object> create(
            ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
            ActorRef<AirCondition.AirConditionCommand> airCondition,
            ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnvironment,
            ActorRef<MediaCommand> mediaStation,
            ActorRef<FridgeCommand> fridge) {
        return Behaviors.setup(context -> new UI(context, tempSensor, airCondition, weatherEnvironment, mediaStation, fridge));
    }

    private UI(
            ActorContext<Object> context,
            ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
            ActorRef<AirCondition.AirConditionCommand> airCondition,
            ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnvironment,
            ActorRef<MediaCommand> mediaStation,
            ActorRef<FridgeCommand> fridge) {
        super(context);
        this.tempSensor = tempSensor;
        this.airCondition = airCondition;
        this.weatherEnvironment = weatherEnvironment;
        this.mediaStation = mediaStation;
        this.fridge = fridge;

        new Thread(this::runCommandLine).start();

        getContext().getLog().info("UI started");
    }

    @Override
    public Receive<Object> createReceive() {
        return newReceiveBuilder()
                .onSignal(PostStop.class, signal -> onPostStop())
                .onMessage(List.class, this::onOrderHistoryReceived)
                .build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }

    private Behavior<Object> onOrderHistoryReceived(List<Receipt> orderHistory) {
        getContext().getLog().info("Received order history: {}", orderHistory);
        if (orderHistory.isEmpty()) {
            System.out.println("No order history.");
        } else {
            for (Receipt receipt : orderHistory) {
                System.out.println("Receipt: " + receipt);
                for (Product product : receipt.products()) {
                    System.out.println("  - " + product.getName() + " x" + product.getQuantity());
                }
            }
        }
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
                            double temperature = Double.parseDouble(command[1]);
                            tempSensor.tell(new TemperatureSensor.ReadTemperature(temperature));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid temperature value.");
                        }
                    } else {
                        System.out.println("Usage: t <value>");
                    }
                    break;

                case "weather":
                    if (command.length > 1) {
                        try {
                            WeatherTypes weather = WeatherTypes.valueOf(command[1].toUpperCase());
                            weatherEnvironment.tell(new WeatherEnvironmentActor.SetWeather(weather));
                        } catch (IllegalArgumentException e) {
                            System.out.println("Unknown weather type. Use: SUNNY, CLOUDY, RAIN, SNOW, STORM");
                        }
                    } else {
                        System.out.println("Usage: weather <type>");
                    }
                    break;

                case "start":
                    if (command.length > 1) {
                        try {
                            SimulationMode mode = SimulationMode.valueOf(command[1].toUpperCase());
                            weatherEnvironment.tell(new WeatherEnvironmentActor.SetSimulationMode(mode));
                        } catch (IllegalArgumentException e) {
                            System.out.println("Unknown simulation mode. Use: EXTERNAL or INTERNAL");
                        }
                    } else {
                        System.out.println("Usage: start <mode>");
                    }
                    break;

                case "stopsim":
                    weatherEnvironment.tell(new WeatherEnvironmentActor.SetSimulationMode(SimulationMode.OFF));
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

                case "consume":
                    if (command.length > 1) {
                        fridge.tell(new Fridge.ConsumeProduct(command[1]));
                    } else {
                        System.out.println("Usage: consume <productName>");
                    }
                    break;

                case "order":
                    if (command.length > 1) {
                        String[] args = command[1].split(" ");
                        if (args.length == 2) {
                            try {
                                String product = args[0];
                                int quantity = Integer.parseInt(args[1]);
                                fridge.tell(new Fridge.OrderProduct(product, quantity, getContext().getSelf().narrow()));
                            } catch (NumberFormatException e) {
                                System.out.println("Quantity must be an integer.");
                            }
                        } else {
                            System.out.println("Usage: order <product> <qty>");
                        }
                    } else {
                        System.out.println("Usage: order <product> <qty>");
                    }
                    break;

                case "stock":
                    fridge.tell(new Fridge.FridgeState(getContext().getSelf().narrow()));
                    break;

                case "help":
                    System.out.println("Commands:");
                    System.out.println("  t <value>            - Set temperature");
                    System.out.println("  weather <type>       - Set weather manually (SUNNY, CLOUDY, RAIN, SNOW, STORM)");
                    System.out.println("  start <mode>         - Start weather simulation (EXTERNAL, INTERNAL)");
                    System.out.println("  stopsim              - Stop weather simulation");
                    System.out.println("  play <title>         - Play movie");
                    System.out.println("  stop <title>         - Stop movie");
                    System.out.println("  consume <product>    - Consume product");
                    System.out.println("  order <product> <qty>- Order product");
                    System.out.println("  stock                - Show fridge stock");
                    System.out.println("  history              - Show order history");
                    System.out.println("  quit                 - Exit");
                    break;

                default:
                    System.out.println("Unknown command. Type 'help' for available commands.");
            }
        }

        getContext().getLog().info("UI done");
    }
}
