package core.handler;

import aspect.Aspect;
import core.annotation.*;
import core.servlet.MyDispatcherServlet;
import core.utils.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 切面处理器
 */
public class AspectHandler {

    public static void handler(MyDispatcherServlet servlet, Map<String, Object> proxyMap, Map<String, Object> aspectMap,
                               Map<String, Object> ioc,List<String> classNames){
        for (String className : classNames) {
            try{
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyAspect.class)){
                    List<String> sourceClassName = new ArrayList<>();
                    List<Method> beforeMethods = new ArrayList<>();
                    List<Method> afterMethods = new ArrayList<>();
                    String beanName = StringUtils.toLowerFirstWord(clazz.getSimpleName());
                    Aspect aspect = (Aspect) ioc.get(beanName);
                    aspectMap.putIfAbsent(beanName, aspect);
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods){
                        if (method.isAnnotationPresent(MyPointcut.class)){
                            MyPointcut myPointcut = method.getAnnotation(MyPointcut.class);
                            String[] urls = myPointcut.values();
                            for (String url : urls){
                                Scanner.doScanner(servlet, url, sourceClassName);
                            }
                        }if (method.isAnnotationPresent(MyBefore.class)){
                            beforeMethods.add(method);
                        }if (method.isAnnotationPresent(MyAfter.class)){
                            afterMethods.add(method);
                        }
                    }
                    doInstance(ioc, proxyMap, sourceClassName, beforeMethods, afterMethods, aspect);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private static void doInstance(Map<String, Object> ioc,Map<String, Object> proxyMap, List<String> classNames, List<Method> beforeMethods,
                                   List<Method> afterMethods, Aspect aspect) {
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyService.class) || clazz.isAnnotationPresent(MyComponent.class)){
                    String beanName = StringUtils.toLowerFirstWord(clazz.getSimpleName());
                    Object instance = ProxyBeanFactory.getProxyBean(ioc.get(beanName), aspect, beforeMethods, afterMethods);
                    proxyMap.putIfAbsent(beanName, instance);
                    Class[] interfaces=clazz.getInterfaces();
                    for (Class<?> i : interfaces){
                        proxyMap.putIfAbsent(StringUtils.toLowerFirstWord(i.getSimpleName()),instance);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
