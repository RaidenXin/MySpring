package core.handler;

import aspect.Aspect;
import core.annotation.*;
import core.servlet.MyDispatcherServlet;
import core.utils.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 切面处理器
 */
public class AspectHandler {

    public static void handler(MyDispatcherServlet servlet,Map<String, Object> ioc,Map<String, Object> proxyMap,List<String> classNames) {
        for (String className : classNames) {
            try{
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyAspect.class)){
                    List<String> sourceClassName = new ArrayList<>();
                    List<Method> beforeMethods = new ArrayList<>();
                    List<Method> afterMethods = new ArrayList<>();
                    String beanName = StringUtils.toLowerFirstWord(clazz.getSimpleName());
                    Aspect aspect = (Aspect) ioc.get(beanName);
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods){
                        if(method.isAnnotationPresent(MyPointcut.class)){
                            MyPointcut myPointcut = method.getAnnotation(MyPointcut.class);
                            String[] urls = myPointcut.values();
                            for (String url : urls){
                                Scanner.doScanner(servlet, url, sourceClassName);
                            }
                        }else if(method.isAnnotationPresent(MyBefore.class)){
                            beforeMethods.add(method);
                        }else if(method.isAnnotationPresent(MyAfter.class)){
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

    private static void doInstance(Map<String, Object> ioc,Map<String, Object> proxyMap,List<String> classNames,List<Method> beforeMethods,List<Method> afterMethods,Aspect aspect) {
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyService.class) && isProxy(beforeMethods, afterMethods, clazz)){
                    String beanName = StringUtils.toLowerFirstWord(clazz.getSimpleName());
                    doInstance(ioc, proxyMap, beforeMethods, afterMethods, clazz, aspect, beanName);
                }else if (clazz.isAnnotationPresent(MyController.class) && isProxy(beforeMethods, afterMethods, clazz)){
                    MyController controller = clazz.getAnnotation(MyController.class);
                    String beanName = StringUtils.isBlank(controller.value())? StringUtils.toLowerFirstWord(clazz.getSimpleName()) : controller.value();
                    doInstance(ioc, proxyMap, beforeMethods, afterMethods, clazz, aspect, beanName);
                }else if (clazz.isAnnotationPresent(MyComponent.class) && isProxy(beforeMethods, afterMethods, clazz)){
                    MyComponent component = clazz.getAnnotation(MyComponent.class);
                    String beanName = StringUtils.isBlank(component.value())? StringUtils.toLowerFirstWord(clazz.getSimpleName()) : component.value();
                    doInstance(ioc, proxyMap, beforeMethods, afterMethods, clazz, aspect, beanName);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static boolean isProxy(List<Method> beforeMethods,List<Method> afterMethods,Class<?> clazz){
        Set<String> methodNames = new HashSet<>();
        for (Method m : beforeMethods) {
            MyBefore myBefore = m.getAnnotation(MyBefore.class);
            methodNames.addAll(Arrays.asList(myBefore.MethodNames()));
        }
        for (Method m : afterMethods) {
            MyAfter myAfter = m.getAnnotation(MyAfter.class);
            methodNames.addAll(Arrays.asList(myAfter.MethodNames()));
        }
        for (Method method : clazz.getDeclaredMethods()){
            if (methodNames.contains(method.getName())){
                return true;
            }
        }
        return false;
    }

    private static void doInstance(Map<String, Object> ioc,Map<String, Object> proxyMap,List<Method> beforeMethods,List<Method> afterMethods,
                                   Class<?> clazz,Aspect aspect,String beanName) throws Exception{
        Object instance = ProxyBeanFactory.getProxyBean(ioc.get(beanName), aspect, beforeMethods, afterMethods);
        proxyMap.put(beanName, instance);
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> i : interfaces){
            proxyMap.put(StringUtils.toLowerFirstWord(i.getSimpleName()),instance);
        }
    }

}
