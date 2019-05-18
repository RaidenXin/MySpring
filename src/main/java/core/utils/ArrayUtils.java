package core.utils;

public class ArrayUtils {

    public static <T> T getElement(Object[] array,Class<?> clazz){
        T result = null;
        for (Object t : array) {
            if (clazz.isInstance(t)){
                result = (T) t;
            }
        }
        return result;
    }
}
