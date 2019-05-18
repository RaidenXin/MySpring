package core.factory;

import core.aspect.Aspect;
import core.aspect.AspectInvacationHandler;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 代理工厂。底层是通过CGLIB来实现代理的
 */
public class ProxyBeanFactory {

    public static Object getProxyBean(Object instance, Aspect aspect, List<Method> beforeMethods, List<Method> afterMethods,List<Method> aroundMethods)
            throws Exception{
        AspectInvacationHandler aspectInvacationHandler = new AspectInvacationHandler(instance, aspect, beforeMethods, afterMethods, aroundMethods);
        return aspectInvacationHandler.getInstance();
    }
}
