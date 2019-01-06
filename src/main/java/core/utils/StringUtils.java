package core.utils;

public class StringUtils {

    public static final String EMPTY = "";

    private StringUtils(){}

    public static final boolean isBlank(String value){
        if (null == value || EMPTY.equals(value.trim())){
            return true;
        }
        return false;
    }

    public static final boolean isNotBlank(String value){
        return !isBlank(value);
    }
}
