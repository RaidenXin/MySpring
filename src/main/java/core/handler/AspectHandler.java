package core.handler;

import core.aspect.Aspect;
import core.annotation.*;
import core.factory.ProxyBeanFactory;
import core.servlet.MyDispatcherServlet;
import core.utils.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 切面处理器
 */
public class AspectHandler {
    /**
     * 主处理方法
     * @param servlet
     * @param ioc
     * @param proxyMap
     * @param classNames
     */
    public static void handler(MyDispatcherServlet servlet,Map<String, Object> ioc,Map<String, Object> proxyMap,List<String> classNames) {
        for (String className : classNames) {
            try{
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyAspect.class)){
                    List<String> sourceClassName = new ArrayList<>();
                    List<Method> beforeMethods = new ArrayList<>();
                    List<Method> afterMethods = new ArrayList<>();
                    List<Method> aroundMethods = new ArrayList<>();
                    String beanName = StringUtils.toLowerFirstWord(clazz.getSimpleName());
                    for (Method method : clazz.getMethods()){
                        if(method.isAnnotationPresent(MyPointcut.class)){
                            String[] urls = method.getAnnotation(MyPointcut.class).values();
                            for (String url : urls){
                                //扫描要进行AOP的类，加载类名
                                Scanner.doScanner(servlet, url, sourceClassName);
                            }
                        }else if(method.isAnnotationPresent(MyBefore.class)){
                            beforeMethods.add(method);
                        }else if(method.isAnnotationPresent(MyAfter.class)){
                            afterMethods.add(method);
                        }else if(method.isAnnotationPresent(MyAround.class)){
                            aroundMethods.add(method);
                        }
                    }
                    doProxyInstance(ioc, proxyMap, sourceClassName, beforeMethods, afterMethods, aroundMethods, (Aspect) ioc.get(beanName));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private static void doProxyInstance(Map<String, Object> ioc, Map<String, Object> proxyMap, List<String> classNames, List<Method> beforeMethods, List<Method> afterMethods,List<Method> aroundMethods, Aspect aspect) {
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyService.class) && isProxy(beforeMethods, afterMethods, clazz)){
                    String beanName = StringUtils.toLowerFirstWord(clazz.getSimpleName());
                    doProxyInstance(ioc, proxyMap, beforeMethods, afterMethods, aroundMethods, clazz, aspect, beanName);
                }else if (clazz.isAnnotationPresent(MyController.class) && isProxy(beforeMethods, afterMethods, clazz)){
                    MyController controller = clazz.getAnnotation(MyController.class);
                    String beanName = StringUtils.isBlank(controller.value())? StringUtils.toLowerFirstWord(clazz.getSimpleName()) : controller.value();
                    doProxyInstance(ioc, proxyMap, beforeMethods, afterMethods, aroundMethods, clazz, aspect, beanName);
                }else if (clazz.isAnnotationPresent(MyComponent.class) && isProxy(beforeMethods, afterMethods, clazz)){
                    MyComponent component = clazz.getAnnotation(MyComponent.class);
                    String beanName = StringUtils.isBlank(component.value())? StringUtils.toLowerFirstWord(clazz.getSimpleName()) : component.value();
                    doProxyInstance(ioc, proxyMap, beforeMethods, afterMethods, aroundMethods, clazz, aspect, beanName);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否需要创建代理类
     * @param beforeMethods
     * @param afterMethods
     * @param clazz
     * @return
     */
    private static boolean isProxy(List<Method> beforeMethods,List<Method> afterMethods,Class<?> clazz){
        Set<String> methodNames = new HashSet<>();
        //看看该类中是否含有要拦截的方法，如果有就需要创建代理
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

    /**
     * 创建代理实例
     * @param ioc
     * @param proxyMap
     * @param beforeMethods
     * @param afterMethods
     * @param clazz
     * @param aspect
     * @param beanName
     * @throws Exception
     */
    private static void doProxyInstance(Map<String, Object> ioc, Map<String, Object> proxyMap, List<Method> beforeMethods, List<Method> afterMethods,List<Method> aroundMethods,
                                        Class<?> clazz, Aspect aspect, String beanName) throws Exception{
        //通过代理工厂获得代理实例
        Object instance = ProxyBeanFactory.getProxyBean(ioc.get(beanName), aspect, beforeMethods, afterMethods, aroundMethods);
        proxyMap.put(beanName, instance);
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> i : interfaces){
            proxyMap.put(StringUtils.toLowerFirstWord(i.getSimpleName()),instance);
        }
    }

}
