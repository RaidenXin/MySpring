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

    public static final String toLowerFirstWord(String beanName) {
        if (null == beanName){
            return "";
        }
        char[] chars = beanName.toCharArray();
        if (chars.length > 1){
            char c = chars[0];
            if (64 < c  && c < 91){
                chars[0] = (char) (c + 32);
            }
        }
        return String.copyValueOf(chars);
    }
}
