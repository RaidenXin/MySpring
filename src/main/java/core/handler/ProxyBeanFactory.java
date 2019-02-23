package core.handler;

import aspect.Aspect;
import aspect.AspectInvacationHandler;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 代理工厂。底层是通过CGLIB来实现代理的
 */
public class ProxyBeanFactory {

    public static Object getProxyBean(Object instance, Aspect aspect, List<Method> beforeMethods, List<Method> afterMethods)
            throws Exception{
        AspectInvacationHandler aspectInvacationHandler = new AspectInvacationHandler(instance, aspect, beforeMethods, afterMethods);
        return aspectInvacationHandler.getInstance();
    }
}
