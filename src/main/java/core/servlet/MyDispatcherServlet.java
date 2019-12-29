package core.servlet;

import com.alibaba.fastjson.JSON;
import core.annotation.*;
import core.dto.Response;
import core.exception.BeanDefinitionStoreException;
import core.handler.AspectHandler;
import core.handler.ParamHandler;
import core.handler.Scanner;
import core.interceptor.InterceptorMethod;
import core.ioc.BeanContainer;
import core.utils.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Spring 核心分配器
 */
public class MyDispatcherServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6011758066002109961L;

	private static String location = "myapplication.properties";
	private static boolean allowBeanDefinitionOverriding = true;


	private Logger logger = Logger.getLogger("init");
    private Properties properties = new Properties();
    private List<String> classNames = new ArrayList<>();
    private BeanContainer ioc = new BeanContainer();
    private Map<String, InterceptorMethod> handlerMapping = new ConcurrentHashMap<>();
    private Map<String, Object> controllerMap  = new ConcurrentHashMap<>();
    private Map<String, Object> proxyMap = new ConcurrentHashMap<>();

    private Class startClass;

    public MyDispatcherServlet(Class startClass){
        this.startClass = startClass;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 注释掉父类实现，不然会报错：405 HTTP method GET is not supported by this URL
        //super.doGet(req, resp);
        logger.info("执行MyDispatcherServlet的doGet()");
        try {
            //处理请求
            doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 注释掉父类实现，不然会报错：405 HTTP method GET is not supported by this URL
        //super.doPost(req, resp);
        logger.info("执行MyDispatcherServlet的doPost()");
        try {
            //处理请求
            doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if(handlerMapping.isEmpty()){
            return;
        }
        String url = req.getRequestURI().replaceAll("/+", "/");
        Response<Object> response = new Response<>();
        if(!this.handlerMapping.containsKey(url)){
            response.setStatusCode("404");
            response.setContent("404 NOT FOUND!" + url);
            resp.getWriter().write(response.toString());
            logger.info("404 NOT FOUND!" + url);
            return;
        }
        InterceptorMethod method = this.handlerMapping.get(url);
        Annotation[][] paramAnnotations = method.getAnnotations();
        //获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求的参数
        Map<String, String[]> parameterMap = req.getParameterMap();
        //保存参数值
        Object [] paramValues = ParamHandler.handler(req, method);
        //利用反射机制来调用
        try {
            //第一个参数是method所对应的实例 在ioc容器中
            Object result = method.invoke(this.controllerMap.get(url), paramValues);
            response.setStatusCode("200");
            response.setContent(result);
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode("200");
            resp.getWriter().write(response.toString());
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init();
        logger.info("初始化MyDispatcherServlet");
        //1.加载配置文件，填充properties字段；
        doLoadConfig(location);
        //2.根据properties，初始化所有相关联的类,扫描用户设定的包下面所有的类
        doScanner(properties.getProperty("scanPackage"));
        //3.拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写
        doInstance();
        //5.初始化HandlerMapping(将url和method对应上)
        initHandlerMapping();
        //6.自动装配
        doAutowired();
    }

    /**
     * 自动装配
     */
    private void doAutowired() throws ServletException {
        if (ioc.isEmpty()){
            return;
        }
        try {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Object bean = entry.getValue();
                Class<?> clazz = entry.getValue().getClass();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if(field.isAnnotationPresent(MyAutowired.class)){
                        String beanName = "";
                        if (field.isAnnotationPresent(MyQualifier.class)){
                            MyQualifier myQualifier = field.getAnnotation(MyQualifier.class);
                            beanName = myQualifier.value().trim();
                        }else {
                            beanName = StringUtils.toLowerFirstWord(field.getType().getSimpleName());
                        }
                        try {
                            //获取实例的时候，如果有代理就获取代理实例，没有就直接获取该实例
                            Object fieldInstance = getBean(clazz.getName(), field.getName(), beanName);
                            field.set(bean, fieldInstance);
                        }catch (Exception e){
                            e.printStackTrace();//如果出现报错 则跳过
                            continue;
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取bean
     * @param beanName
     * @return
     */
    private Object getBean(String className,String fieldName, String beanName){
        //获取实例的时候，如果有代理就获取代理实例，没有就直接获取该实例
        Object fieldInstance = null;
        if(proxyMap.containsKey(beanName)){
            fieldInstance = proxyMap.get(beanName);
        }else {
            fieldInstance = ioc.get(beanName);
        }
        if (fieldInstance instanceof List<?>){
            StringBuilder context = new StringBuilder("Field ");
            context.append(fieldName);
            context.append(" in ");
            context.append(className);
            context.append(" required a single bean, but 2 were found:\n");
            for (Object bean : (List<Object>) fieldInstance) {
                context.append("- testBean: defined in file [");
                context.append(bean.getClass().getName());
                context.append("]\n");
            }
            throw new BeanDefinitionStoreException(context.toString());
        }
        return fieldInstance;
    }

    /**
     * 初始化链接映射
     */
    private void initHandlerMapping() {
        if (classNames.isEmpty()){
            return;
        }
        try{
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)){
                    MyController myController = clazz.getAnnotation(MyController.class);
                    String baseUrl = StringUtils.EMPTY;
                    if (clazz.isAnnotationPresent(MyRequestMapping.class)){
                        MyRequestMapping myRequestMapping = clazz.getAnnotation(MyRequestMapping.class);
                        baseUrl = myRequestMapping.value();
                    }
                    String beanName = StringUtils.toLowerFirstWord(StringUtils.isBlank(myController.value())? clazz.getSimpleName() : myController.value());
                    Method[] methods = clazz.getMethods();
                    Object bean = null == proxyMap.get(beanName)? ioc.get(beanName) : proxyMap.get(beanName);
                    Class<?> beanClass = bean.getClass();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(MyRequestMapping.class)){
                            Method m = beanClass.getMethod(method.getName(), method.getParameterTypes());
                            MyRequestMapping myRequestMapping = method.getAnnotation(MyRequestMapping.class);
                            String url = ("/" + baseUrl+"/"+myRequestMapping.value()).replaceAll("/+", "/");
                            InterceptorMethod interceptorMethod = new InterceptorMethod();
                            interceptorMethod.setMethod(m);
                            interceptorMethod.setAnnotations(method.getParameterAnnotations());
                            handlerMapping.put(url, interceptorMethod);
                            controllerMap.put(url, bean);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 实例化
     */
    private void doInstance() {
        if (classNames.isEmpty()){//没有类需要实例化
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)){
                    MyController myController = clazz.getAnnotation(MyController.class);
                    String beanName = StringUtils.isBlank(myController.value())? clazz.getSimpleName() : myController.value();
                    if (ioc.containsKey(StringUtils.toLowerFirstWord(beanName))){
                        throw new BeanDefinitionStoreException("There can't be the same beans![" + beanName + "]");
                    }
                    ioc.put(StringUtils.toLowerFirstWord(beanName),clazz.newInstance());
                }else if (clazz.isAnnotationPresent(MyService.class)){
                    MyService myService = clazz.getAnnotation(MyService.class);
                    String beanName = myService.value();
                    Object instance = clazz.newInstance();
                    doInterFacesInstance(clazz, instance, beanName);
                }else if (clazz.isAnnotationPresent(MyComponent.class)){
                    MyComponent myComponent = clazz.getAnnotation(MyComponent.class);
                    String beanName = myComponent.value();
                    Object instance = clazz.newInstance();
                    doInterFacesInstance(clazz, instance, beanName);
                }else {
                    continue;
                }
            }catch (Exception e){
                e.printStackTrace();
                continue;//如果实例化报错 则跳过该类
            }
        }
        //通过代理实现AOP
        AspectHandler.handler( this, ioc, proxyMap, classNames);
    }

    /**
     * 获取接口实例，放入IOC容器中
     * @param clazz
     * @param instance
     * @param beanName
     */
    private void doInterFacesInstance(Class<?> clazz,Object instance,String beanName){
        //如果自定义名字为空 则默认按照类型名称首字母小写和接口名称首字母小写 作为beanname放入ioc容器中
        if (!StringUtils.isBlank(beanName)){
            ioc.put(beanName, instance);
        }
        String className = StringUtils.toLowerFirstWord(clazz.getSimpleName());
        ioc.put(className, instance);
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> i : interfaces){
            String interfacesName = StringUtils.toLowerFirstWord(i.getSimpleName());
            //如果一个接口存在2个实例，且另外一个没有自定义beanName 是否覆盖为否 则报错
            if (ioc.containsKey(interfacesName) && StringUtils.isBlank(beanName) && !allowBeanDefinitionOverriding){
                throw new BeanDefinitionStoreException("There can't be the same beans![" + beanName + "]");
            }else {
                ioc.put(interfacesName, instance);
            }
        }
    }

    /**
     *  将指定包下扫描得到的类，添加到classNames字段中
     * @param packageName
     */
    private void doScanner(String packageName) {
        Scanner.doScanner(this, packageName, classNames);
    }

    /**
     * 加载配置文件
     * @param location 配置文件的位置
     */
    private void doLoadConfig(String location) {
        //配置文件加载到流里面
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location)) {
            //用Properties文件加载文件里的内容
            logger.info("读取" + location + "里面的文件");
            properties.load(resourceAsStream);
            if ( null != properties.get("allowBeanDefinitionOverriding")){
                allowBeanDefinitionOverriding = Boolean.valueOf((String) properties.get("allowBeanDefinitionOverriding"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info(e.getMessage());
        }
    }

    public Class getStartClass() {
        return startClass;
    }
}
