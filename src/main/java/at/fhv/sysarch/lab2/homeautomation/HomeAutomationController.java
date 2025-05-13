package at.fhv.sysarch.lab2.homeautomation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.sensors.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.Blinds;
import at.fhv.sysarch.lab2.homeautomation.environment.MqttWeatherActor;
import at.fhv.sysarch.lab2.homeautomation.devices.MediaStation;
import at.fhv.sysarch.lab2.homeautomation.sensors.WeatherSensor;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherCommand;
import at.fhv.sysarch.lab2.homeautomation.environment.WeatherEnvironmentActor;
import at.fhv.sysarch.lab2.homeautomation.environment.WeatherEnvironmentActor.WeatherEnvironmentCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.TemperatureCommand;
import at.fhv.sysarch.lab2.homeautomation.environment.TemperatureEnvironmentActor;
import at.fhv.sysarch.lab2.homeautomation.environment.TemperatureEnvironmentActor.TemperatureEnvironmentCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.MediaCommand;
import at.fhv.sysarch.lab2.homeautomation.ui.UI;


public class HomeAutomationController extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(HomeAutomationController::new);
    }

    private HomeAutomationController(ActorContext<Void> context) {
        super(context);

        ActorRef<AirCondition.AirConditionCommand> airCondition =
                getContext().spawn(AirCondition.create(), "AirCondition");

        ActorRef<Blinds.BlindsCommand> blinds =
                getContext().spawn(Blinds.create(), "Blinds");

        ActorRef<MediaCommand> mediaStation =
                getContext().spawn(MediaStation.create(blinds), "MediaStation");

        ActorRef<WeatherCommand> weatherSensor =
                getContext().spawn(WeatherSensor.create(blinds), "WeatherSensor");

        ActorRef<WeatherEnvironmentCommand> weatherEnv =
                getContext().spawn(WeatherEnvironmentActor.create(weatherSensor), "WeatherEnvironment");

        ActorRef<TemperatureCommand> tempSensor =
                getContext().spawn(TemperatureSensor.create(airCondition), "TemperatureSensor");

        ActorRef<TemperatureEnvironmentCommand> temperatureEnv =
                getContext().spawn(TemperatureEnvironmentActor.create(tempSensor), "TemperatureEnvironment");

        ActorRef<Void> ui = getContext().spawn(UI.create(airCondition, weatherEnv, temperatureEnv, mediaStation), "UI");

        ActorRef<MqttWeatherActor.MqttCommand> mqtt = getContext().spawn(MqttWeatherActor.create(weatherEnv, temperatureEnv), "MqttWeatherActor");

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
