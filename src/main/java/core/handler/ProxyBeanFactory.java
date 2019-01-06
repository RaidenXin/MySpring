package core.handler;

import aspect.Aspect;
import aspect.AspectInvacationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class ProxyBeanFactory {

    public static Object getProxyBean(Object instance, Aspect aspect, List<Method> beforeMethods, List<Method> afterMethods)
            throws Exception{
        InvocationHandler aspectInvacationHandler = new AspectInvacationHandler(instance, aspect, beforeMethods, afterMethods);
        return Proxy.newProxyInstance(instance.getClass().getClassLoader(), instance.getClass().getInterfaces(), aspectInvacationHandler);
    }
}
