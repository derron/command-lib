package la.dahuo.command;

/**
 * Created by dhu on 15/12/16.
 */
public class TypeConverter {
    public static String toString(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof String) {
            return (String) value;
        } else {
            return value.toString();
        }
    }

    public static boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        } else if (value instanceof Number) {
            return ((Number)value).intValue() > 0;
        } else if (value instanceof String) {
            boolean result;
            try {
                result = Boolean.parseBoolean((String)value);
            } catch (NumberFormatException e) {
                result = false;
            }
            return result;
        } else {
            return false;
        }
    }

    public static int toInt(Object value) {
        if (value == null) {
            return 0;
        } else if (value instanceof Number) {
            return ((Number)value).intValue();
        } else if (value instanceof String) {
            int result;
            try {
                result = Integer.parseInt((String)value);
            } catch (NumberFormatException e) {
                result = 0;
            }
            return result;
        } else {
            return 0;
        }
    }

    public static long toLong(Object value) {
        if (value == null) {
            return 0;
        } else if (value instanceof Number) {
            return ((Number)value).longValue();
        } else if (value instanceof String) {
            long result;
            try {
                result = Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                result = 0;
            }
            return result;
        } else {
            return 0;
        }
    }

    public static float toFloat(Object value) {
        if (value == null) {
            return 0;
        } else if (value instanceof Number) {
            return ((Number)value).floatValue();
        } else if (value instanceof String) {
            float result;
            try {
                result = Float.parseFloat((String) value);
            } catch (NumberFormatException e) {
                result = 0;
            }
            return result;
        } else {
            return 0;
        }
    }

    public static double toDouble(Object value) {
        if (value == null) {
            return 0;
        } else if (value instanceof Number) {
            return ((Number)value).doubleValue();
        } else if (value instanceof String) {
            double result;
            try {
                result = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                result = 0;
            }
            return result;
        } else {
            return 0;
        }
    }

}
