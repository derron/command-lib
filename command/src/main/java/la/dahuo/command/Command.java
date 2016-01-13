package la.dahuo.command;

import java.util.Map;

/**
 * Created by dhu on 15/12/16.
 */
public abstract class Command {
    public abstract void execute();

    public static Command parse(String action, Map<String, Object> params) {
        CommandFactory factory = CommandRepository.getInstance().getFactory(action);
        if (factory != null) {
            return factory.newCommand(params);
        }
        return EmptyCommand.INSTANCE;
    }

    public static class EmptyCommand extends Command {

        public static final EmptyCommand INSTANCE = new EmptyCommand();
        private EmptyCommand() {
        }

        @Override
        public void execute() {
        }
    }

}
