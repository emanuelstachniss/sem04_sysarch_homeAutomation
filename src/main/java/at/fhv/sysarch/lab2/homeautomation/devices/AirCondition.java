package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class AirCondition extends AbstractBehavior<AirCondition.AirConditionCommand> {

    public interface AirConditionCommand {}

    public static class ReadTemperature implements AirConditionCommand {
        public Double temperature;
        public String unit;
        public ReadTemperature(Double temperature, String unit) {
            this.temperature = temperature;
            this.unit = unit;
        }
    }

    private Boolean airConditionOn = false;

    public AirCondition(ActorContext<AirConditionCommand> context) {
        super(context);
    }

    public static Behavior<AirConditionCommand> create() {
        return Behaviors.setup(AirCondition::new);
    }

    @Override
    public Receive<AirConditionCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadTemperature.class, this::onReadTemperature)
                .build();
    }

    private Behavior<AirConditionCommand> onReadTemperature(ReadTemperature cmd) {
        if (cmd.temperature > 20.0 && !airConditionOn) {
            getContext().getLog().info("Temperature is {} {} -> AC starts cooling", cmd.temperature, cmd.unit);
            airConditionOn = true;
        } else if (cmd.temperature < 20.0 && airConditionOn) {
            getContext().getLog().info("Temperature is {} {} -> AC turned off", cmd.temperature, cmd.unit);
            airConditionOn = false;
        }
        return this;
    }
}
