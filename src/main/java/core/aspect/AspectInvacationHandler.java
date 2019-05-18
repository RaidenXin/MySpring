package core.aspect;

import core.annotation.MyAfter;
import core.annotation.MyAround;
import core.annotation.MyBefore;
import core.handler.ProceedingJoinPoint;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 切面实际处理类
 */
public class AspectInvacationHandler implements MethodInterceptor {

    private Object source;
    private Aspect aspect;
    //前置方法映射
    private Map<String, List<Method>> beforeMethodMapping;
    //后置方法映射
    private Map<String, List<Method>> afterMethodMapping;
    //环绕方法映射
    private Map<String, List<Method>> aroundMethodMapping;

    public AspectInvacationHandler(Object source,Aspect aspect,List<Method> beforeMethods,List<Method> afterMethods,List<Method> aroundMethods){
        super();
        this.source = source;
        this.aspect = aspect;
        this.beforeMethodMapping = new HashMap<>();
        this.afterMethodMapping = new HashMap<>();
        this.aroundMethodMapping = new HashMap<>();
        init(beforeMethods, afterMethods, aroundMethods);
    }

    private void init(List<Method> beforeMethods,List<Method> afterMethods,List<Method> aroundMethods){
        Method[] methods = source.getClass().getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            this.beforeMethodMapping.put(methodName, methodFilter(beforeMethods, methodName));
            this.afterMethodMapping.put(methodName, methodFilter(afterMethods, methodName));
            this.aroundMethodMapping.put(methodName, methodFilter(aroundMethods, methodName));
        }

    }

    /**
     * 根据被拦截的方法名称，获取需要执行的前置方法或者后置方法
     * @param methods
     * @param methodName
     * @return
     */
    private List<Method> methodFilter(List<Method> methods,String methodName){
        List<Method> result = new ArrayList<>();
        for (Method method : methods) {
            if (null != method){
                if (isIntercept(methodName, getMethodNames(method))){
                    result.add(method);
                }
            }
        }
        return result;
    }

    /**
     * 获取注解中的需要拦截方法名称
     * @param methon
     * @return
     */
    private String[] getMethodNames(Method methon){
        if (methon.isAnnotationPresent(MyAfter.class)){
            return methon.getAnnotation(MyAfter.class).MethodNames();
        }else if (methon.isAnnotationPresent(MyBefore.class)){
            return methon.getAnnotation(MyBefore.class).MethodNames();
        }else if (methon.isAnnotationPresent(MyAround.class)){
            return methon.getAnnotation(MyAround.class).MethodNames();
        }
        return new String[0];
    }

    /**
     * 获取一个代理实例
     * @return
     */
    public Object getInstance() {
        // 操作字节码 生成虚拟子类
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(source.getClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    private void before(String methodName, Object[] args) throws InvocationTargetException, IllegalAccessException {
        for (Method m : beforeMethodMapping.get(methodName)) {
            m.invoke(aspect);
        }
    }

    private void after(String methodName, Object[] args) throws InvocationTargetException, IllegalAccessException {
        for (Method m : afterMethodMapping.get(methodName)) {
            m.invoke(aspect);
        }
    }

    private Object around(String methodName, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        for (Method m : aroundMethodMapping.get(methodName)) {
            Object[] objects = {new ProceedingJoinPoint(source, method, args)};
            return m.invoke(aspect, objects);
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
