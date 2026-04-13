package vn.edu.ute.controller.profile.command;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory Pattern: Khởi tạo Command phù hợp dựa trên URL và Method.
 */
public class ProfileCommandFactory {
    
    private static final Map<String, ProfileCommand> getCommands = new HashMap<>();
    private static final Map<String, ProfileCommand> postCommands = new HashMap<>();

    static {
        // Đăng ký các GET commands
        getCommands.put("/profile", new ShowProfileCommand());
        getCommands.put("/profile/addresses", new ShowAddressesCommand());
        getCommands.put("/profile/orders", new ShowOrdersCommand());

        // Đăng ký các POST commands
        postCommands.put("/profile/update", new UpdateProfileCommand());
        postCommands.put("/profile/avatar", new UploadAvatarCommand());
        postCommands.put("/profile/change-password", new ChangePasswordCommand());
        postCommands.put("/profile/addresses/add", new AddAddressCommand());
        postCommands.put("/profile/addresses/edit", new EditAddressCommand());
        postCommands.put("/profile/addresses/delete", new DeleteAddressCommand());
        postCommands.put("/profile/addresses/default", new SetDefaultAddressCommand());
        postCommands.put("/profile/orders/cancel", new CancelOrderCommand());
    }

    public static ProfileCommand getCommand(String method, String path) {
        if ("GET".equalsIgnoreCase(method)) {
            return getCommands.get(path);
        } else if ("POST".equalsIgnoreCase(method)) {
            return postCommands.get(path);
        }
        return null;
    }
}
