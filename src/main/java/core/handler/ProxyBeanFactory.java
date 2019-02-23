package core.handler;

import aspect.Aspect;
import aspect.AspectInvacationHandler;

import java.lang.reflect.Method;
import java.util.List;

public class ProxyBeanFactory {

    public static Object getProxyBean(Object instance, Aspect aspect, List<Method> beforeMethods, List<Method> afterMethods)
            throws Exception{
        AspectInvacationHandler aspectInvacationHandler = new AspectInvacationHandler(instance, aspect, beforeMethods, afterMethods);
        return ((AspectInvacationHandler) aspectInvacationHandler).getInstance();
    }
}
