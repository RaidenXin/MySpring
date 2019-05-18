package core.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InterceptorMethod {

    private Method method;
    private Annotation[][] annotations;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Annotation[][] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[][] annotations) {
        this.annotations = annotations;
    }

    public Object invoke(Object obj, Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(obj, args);
    }

    public Class<?>[] getParameterTypes(){
        return this.method.getParameterTypes();
    }
}
