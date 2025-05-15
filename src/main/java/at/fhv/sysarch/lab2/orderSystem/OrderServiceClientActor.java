package at.fhv.sysarch.lab2.orderSystem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import at.fhv.sysarch.lab2.homeautomation.commands.fridge.*;

import java.util.ArrayList;
import java.util.List;

public class OrderServiceClientActor extends AbstractBehavior<OrderServiceClientActor.OrderCommand> {


    public interface OrderCommand {}

    public interface OrderResponseCommand {}


    public static final class PlaceOrder implements OrderCommand {
        public final String productName;
        public final int quantity;
        public final ActorRef<OrderResponse> replyTo;

        public PlaceOrder(String productName, int quantity, ActorRef<OrderResponse> replyTo) {
            this.productName = productName;
            this.quantity = quantity;
            this.replyTo = replyTo;
        }
    }

    public static final class OrderResponse implements OrderResponseCommand {
        private final Receipt receipt;

        public OrderResponse(Receipt receipt) {
            this.receipt = receipt;
        }

        public Receipt receipt() {
            return receipt;
        }
    }

    public static Behavior<OrderCommand> create() {
        return Behaviors.setup(OrderServiceClientActor::new);
    }

    private OrderServiceClientActor(ActorContext<OrderCommand> context) {
        super(context);
    }

    @Override
    public Receive<OrderCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(PlaceOrder.class, this::onPlaceOrder)
                .build();
    }

    private Behavior<OrderCommand> onPlaceOrder(PlaceOrder msg) {
        getContext().getLog().info("Placing order for {}x {}", msg.quantity, msg.productName);

        List<Product> products = new ArrayList<>();
        for (int i = 0; i < msg.quantity; i++) {
            products.add(new Product(msg.productName, msg.quantity,10));
        }

        Receipt receipt = new Receipt(products);

        msg.replyTo.tell(new OrderResponse(receipt));

        return this;
    }
}
