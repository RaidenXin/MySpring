package core.handler;

import aspect.Aspect;
import core.annotation.*;
import core.servlet.MyDispatcherServlet;
import core.utils.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 切面处理器
 */
public class AspectHandler {

    public static void handler(MyDispatcherServlet servlet, Map<String, Object> aspectMap,Map<String, Object> ioc,List<String> classNames){
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
                    doInstance(ioc, sourceClassName, beforeMethods, afterMethods, aspect);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private static void doInstance(Map<String, Object> ioc,List<String> classNames,List<Method> beforeMethods,List<Method> afterMethods,Aspect aspect) {
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyService.class)){
                    String beanName = StringUtils.toLowerFirstWord(clazz.getSimpleName());
                    doInstance(ioc, beforeMethods, afterMethods, clazz, aspect, beanName);
                }else if (clazz.isAnnotationPresent(MyController.class)){
                    MyController controller = clazz.getAnnotation(MyController.class);
                    String beanName = StringUtils.EMPTY.equals(controller.value())? StringUtils.toLowerFirstWord(clazz.getSimpleName()) : controller.value();
                    doInstance(ioc, beforeMethods, afterMethods, clazz, aspect, beanName);
                }else if (clazz.isAnnotationPresent(MyComponent.class)){
                    MyComponent component = clazz.getAnnotation(MyComponent.class);
                    String beanName = StringUtils.EMPTY.equals(component.value())? StringUtils.toLowerFirstWord(clazz.getSimpleName()) : component.value();
                    doInstance(ioc, beforeMethods, afterMethods, clazz, aspect, beanName);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void doInstance(Map<String, Object> ioc,List<Method> beforeMethods,List<Method> afterMethods,
                                   Class<?> clazz,Aspect aspect,String beanName) throws Exception{
        Object instance = ProxyBeanFactory.getProxyBean(ioc.get(beanName), aspect, beforeMethods, afterMethods);
        ioc.putIfAbsent(beanName, instance);
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> i : interfaces){
            if (null != ioc.putIfAbsent(StringUtils.toLowerFirstWord(i.getSimpleName()),instance)){
                throw new RuntimeException("There can't be the same bean![" + beanName + "]");
            }
        }
    }
}
