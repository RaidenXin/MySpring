package aspect;

import core.annotation.MyAfter;
import core.annotation.MyBefore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class AspectInvacationHandler implements InvocationHandler {

    private final Object source;
    private final Aspect aspect;
    private final List<Method> beforeMethods;
    private final List<Method> afterMethods;

    public AspectInvacationHandler(Object source,Aspect aspect,List<Method> beforeMethods,List<Method> afterMethods){
        super();
        this.source = source;
        this.aspect = aspect;
        this.beforeMethods = beforeMethods;
        this.afterMethods = afterMethods;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        for (Method m : beforeMethods) {
            if (null != m){
                MyBefore before = m.getAnnotation(MyBefore.class);
                if (methodName.startsWith(before.value())){
                    m.invoke(aspect);
                }
            }
        }
        Object result = method.invoke(source, args);
        for (Method m : afterMethods) {
            if (null != m){
                MyAfter after = m.getAnnotation(MyAfter.class);
                if (methodName.startsWith(after.value())){
                    m.invoke(aspect);
                }
            }
        }
        return result;
    }
}
