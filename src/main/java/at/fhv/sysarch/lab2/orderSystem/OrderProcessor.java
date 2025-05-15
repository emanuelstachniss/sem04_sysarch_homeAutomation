package at.fhv.sysarch.lab2.orderSystem;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.orderSystem.internal.OrderCommand;
import at.fhv.sysarch.lab2.orderSystem.internal.OrderReceived;
import at.fhv.sysarch.lab2.orderSystem.internal.OrderReplyInternal;

public class OrderProcessor extends AbstractBehavior<OrderCommand> {

    public static Behavior<OrderCommand> create() {
        return Behaviors.setup(OrderProcessor::new);
    }

    private OrderProcessor(ActorContext<OrderCommand> context) {
        super(context);
    }

    @Override
    public Receive<OrderCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(OrderReceived.class, this::onOrderReceived)
                .build();
    }

    private Behavior<OrderCommand> onOrderReceived(OrderReceived s) {
        getContext().getLog().info("Order received {}", s);
        s.replyTo().tell(new OrderReplyInternal(true));
        // TODO: process order, generate return value
        return Behaviors.same();
    }
}
