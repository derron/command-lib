package la.dahuo.command;

import java.util.Map;

/**
 * Created by dhu on 15/12/16.
 */
public interface CommandFactory {
    Command newCommand(Map<String, Object> params);
}
