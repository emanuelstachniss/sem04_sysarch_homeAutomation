package at.fhv.sysarch.lab2.orderSystem;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class OrderProcessor extends AbstractBehavior<String> {

    public static Behavior<String> create() {
        return Behaviors.setup(OrderProcessor::new);
    }

    private OrderProcessor(ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessage(String.class, this::onOrderReceived)
                .build();
    }

    private Behavior<String> onOrderReceived(String s) {
        getContext().getLog().info("Order received {}", s);
        // TODO: process order, generate return value
        return Behaviors.same();
    }
}
