package at.fhv.sysarch.lab2.orderSystem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import at.fhv.sysarch.lab2.homeautomation.devices.Fridge;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderServiceHandlerFactory;
import at.fhv.sysarch.lab2.orderSystem.internal.OrderCommand;

import java.util.concurrent.CompletionStage;

public class OrderSystemHandler extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(OrderSystemHandler::new);
    }

    private OrderSystemHandler(ActorContext<Void> context) {
        super(context);

        ActorRef<OrderCommand> orderProcessor = context.spawn(OrderProcessor.create(), "OrderProcessor");
        ActorRef<OrderServiceClientActor.OrderCommand> orderClient = context.spawn(OrderServiceClientActor.create(), "OrderServiceClient");
        ActorRef<Fridge.FridgeCommand> fridge = context.spawn(Fridge.create(orderClient), "Fridge");

        OrderServiceImpl orderService = new OrderServiceImpl(orderProcessor, context.getSystem());

        CompletionStage<ServerBinding> binding = Http.get(context.getSystem())
                .newServerAt("localhost", 8080)
                .bind(OrderServiceHandlerFactory.create(orderService, context.getSystem()));

        binding.thenAccept(serverBinding ->
                context.getLog().info("Server online at http://localhost:8080/")
        );
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().build();
    }
}
