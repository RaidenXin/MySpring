package aspect;

import core.annotation.MyAfter;
import core.annotation.MyAround;
import core.annotation.MyBefore;
import core.handler.ProceedingJoinPoint;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 切面实际处理类
 */
public class AspectInvacationHandler implements MethodInterceptor {

    private Object source;
    private Aspect aspect;
    private List<Method> beforeMethods;
    private List<Method> afterMethods;
    private List<Method> aroundMethods;

    public AspectInvacationHandler(Object source,Aspect aspect,List<Method> beforeMethods,List<Method> afterMethods,List<Method> aroundMethods){
        super();
        this.source = source;
        this.aspect = aspect;
        this.beforeMethods = beforeMethods;
        this.afterMethods = afterMethods;
        this.aroundMethods = aroundMethods;
    }

    public Object getInstance() {
        // 操作字节码 生成虚拟子类
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(source.getClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    private void before(String methodName, Object[] args) throws InvocationTargetException, IllegalAccessException {
        for (Method m : beforeMethods) {
            if (null != m){
                MyBefore before = m.getAnnotation(MyBefore.class);
                if (isIntercept(methodName, before.MethodNames())){
                    m.invoke(aspect);
                }
            }
        }
    }

    private void after(String methodName, Object[] args) throws InvocationTargetException, IllegalAccessException {
        for (Method m : afterMethods) {
            if (null != m){
                MyAfter after = m.getAnnotation(MyAfter.class);
                if (isIntercept(methodName, after.MethodNames())){
                    m.invoke(aspect);
                }
            }
        }
    }

    private Object around(String methodName, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        for (Method m : aroundMethods) {
            if (null != m){
                MyAround around = m.getAnnotation(MyAround.class);
                if (isIntercept(methodName, around.MethodNames())){
                    Object[] objects = {new ProceedingJoinPoint(source, method, args)};
                    return m.invoke(aspect, objects);
                }
            }
        }
        return method.invoke(source, args);
    }

    private boolean isIntercept(String methodName,String[] methodNames){
        for (String name : methodNames) {
            if (methodName.startsWith(name)){
                return true;
            }
        }
        return false;
    }

    private Object handler(Method method,Object[] args) throws Throwable{
        String methodName = method.getName();
        before(methodName, args);
        Object result = around(methodName, method, args);
        after(methodName, args);
        return result;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return handler(method, args);
    }
}
