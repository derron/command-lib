package la.dahuo.command;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dhu on 15/12/16.
 */
public class CommandRepository {
    private static CommandRepository sInstance = new CommandRepository();
    private CommandRepository() {
        mFactories = new HashMap<>();
        try {
            Class<?> clazz = Class.forName("la.dahuo.command.CommandRegisters");
            Method method = clazz.getMethod("register", Map.class);
            method.invoke(null, mFactories);
        } catch (Exception e) {
            throw new RuntimeException("There is something wrong with command codegen", e);
        }
    }

    public static CommandRepository getInstance() {
        return sInstance;
    }

    private Map<String, CommandFactory> mFactories;

    public CommandFactory getFactory(String action) {
        return mFactories.get(action);
    }

}
