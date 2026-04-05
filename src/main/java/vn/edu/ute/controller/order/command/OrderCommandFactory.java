package vn.edu.ute.controller.order.command;

import java.util.HashMap;
import java.util.Map;

public class OrderCommandFactory {
    private static final Map<String, OrderCommand> commands = new HashMap<>();

    static {
        TrackOrderCommand trackOrderCommand = new TrackOrderCommand();
        // Cả GET và POST đều dùng chung TrackOrderCommand
        commands.put("GET /order/track", trackOrderCommand);
        commands.put("POST /order/track", trackOrderCommand);
    }

    public static OrderCommand getCommand(String method, String path) {
        return commands.get(method.toUpperCase() + " " + path);
    }
}
