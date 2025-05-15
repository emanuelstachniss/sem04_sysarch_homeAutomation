package at.fhv.sysarch.lab2.homeautomation.order;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.grpc.GrpcClientSettings;
import at.fhv.sysarch.lab2.homeautomation.grpc.*;
import org.slf4j.Logger;

import java.util.concurrent.CompletionStage;

public class OrderExecutor extends AbstractBehavior<PlaceOrder> {

    public static Behavior<PlaceOrder> create() {
        return Behaviors.setup(context -> {
            GrpcClientSettings settings = GrpcClientSettings.connectToServiceAt("localhost", 8080, context.getSystem()).withTls(false);
            OrderServiceClient client = OrderServiceClient.create(settings, context.getSystem());
            return new OrderExecutor(context, client);
        });
    }

    private final OrderService client;

    private OrderExecutor(ActorContext<PlaceOrder> context, OrderServiceClient client) {
        super(context);
        this.client = client;
    }

    @Override
    public Receive<PlaceOrder> createReceive() {
        return newReceiveBuilder()
                .onMessage(PlaceOrder.class, this::onPlaceOrder)
                .build();
    }

    private Behavior<PlaceOrder> onPlaceOrder(PlaceOrder order) {
         getContext().getLog().info("Order received {} ", order.productName);
         CompletionStage< OrderReply> request = this.client.order(OrderRequest.newBuilder().setProduct(order.productName).build());

        Logger logger = getContext().getLog();
        request.thenAccept(reply -> logger.info("Received order reply: {}", reply));
        return Behaviors.same();
    }

}

