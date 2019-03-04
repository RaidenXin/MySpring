package core.handler;

import java.lang.reflect.Method;

public class ProceedingJoinPoint {

    private final Object source;
    private final Object[] args;
    private final Method method;

    public ProceedingJoinPoint(Object source,Method method, Object[] args){
        this.source = source;
        this.args = args;
        this.method = method;
    }

    public <T> T proceed(Object[] args){
        T t = null;
        try{
            t = (T) method.invoke(source, args);
        }catch (Exception e){
            e.printStackTrace();
        }
        return t;
    }

    public Object[] getArgs() {
        return args;
    }
}
