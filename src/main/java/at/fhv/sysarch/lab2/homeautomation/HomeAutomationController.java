package at.fhv.sysarch.lab2.homeautomation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.Blinds;
import at.fhv.sysarch.lab2.homeautomation.sensors.WeatherSensor;
import at.fhv.sysarch.lab2.homeautomation.environment.WeatherEnvironmentActor;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherCommand;
import at.fhv.sysarch.lab2.homeautomation.environment.WeatherEnvironmentActor.WeatherEnvironmentCommand;
import at.fhv.sysarch.lab2.homeautomation.ui.UI;

import java.util.UUID;

public class HomeAutomationController extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(HomeAutomationController::new);
    }

    private HomeAutomationController(ActorContext<Void> context) {
        super(context);

        ActorRef<AirCondition.AirConditionCommand> airCondition =
                getContext().spawn(AirCondition.create(UUID.randomUUID().toString()), "AirCondition");
        ActorRef<TemperatureSensor.TemperatureCommand> tempSensor =
                getContext().spawn(TemperatureSensor.create(airCondition), "TemperatureSensor");

        ActorRef<Blinds.BlindsCommand> blinds =
                getContext().spawn(Blinds.create(), "Blinds");

        ActorRef<WeatherCommand> weatherSensor =
                getContext().spawn(WeatherSensor.create(blinds), "WeatherSensor");

        ActorRef<WeatherEnvironmentCommand> weatherEnv =
                getContext().spawn(WeatherEnvironmentActor.create(weatherSensor), "WeatherEnvironment");

        ActorRef<Void> ui = getContext().spawn(UI.create(tempSensor, airCondition, weatherEnv), "UI");


        getContext().getLog().info("HomeAutomation Application started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder()
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private HomeAutomationController onPostStop() {
        getContext().getLog().info("HomeAutomation Application stopped");
        return this;
    }
}
