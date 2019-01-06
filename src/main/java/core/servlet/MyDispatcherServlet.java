package core.servlet;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import core.annotation.*;
import core.utils.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;


public class MyDispatcherServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("init");
    private Properties properties = new Properties();
    private List<String> classNames = new ArrayList<>();
    private Map<String, Object> ioc = new HashMap<>();
    private Map<String, Method> handlerMapping = new  HashMap<>();
    private Map<String, Object> controllerMap  =new HashMap<>();

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
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        // 去掉url前面的斜杠"/"，所有的@MyRequestMapping可以不用写斜杠"/"
        if(url.lastIndexOf('/')!=0){
            url = url.substring(1);
        }
        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 NOT FOUND!");
            logger.info("404 NOT FOUND!" + url);
            return;
        }
        Method method = this.handlerMapping.get(url);
        //获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求的参数
        Map<String, String[]> parameterMap = req.getParameterMap();
        //保存参数值
        Object [] paramValues = new Object[parameterTypes.length];
        //方法的参数列表
        for (int i = 0; i < parameterTypes.length; i++){
            //根据参数名称，做某些处理
            String requestParam = parameterTypes[i].getSimpleName();
            if (requestParam.equals("HttpServletRequest")){
                //参数类型已明确，这边强转类型
                paramValues[i] = req;
                continue;
            }
            if (requestParam.equals("HttpServletResponse")){
                paramValues[i] = resp;
                continue;
            }
            if(requestParam.equals("String")){
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                    paramValues[i] = value;
                }
            }
        }
        //利用反射机制来调用
        try {
            //第一个参数是method所对应的实例 在ioc容器中
            //method.invoke(this.controllerMap.get(url), paramValues);
            method.invoke(this.controllerMap.get(url), paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init();
        logger.info("初始化MyDispatcherServlet");
        //1.加载配置文件，填充properties字段；
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
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
    private void doAutowired(){
        if (ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String,Object> entry : ioc.entrySet()) {
            Object bean = entry.getValue();
            Class<?> clazz = bean.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if(field.isAnnotationPresent(MyAutowired.class)){
                    String beanName = "";
                    if (field.isAnnotationPresent(MyQualifier.class)){
                        MyQualifier myQualifier = field.getAnnotation(MyQualifier.class);
                        beanName = myQualifier.value().trim();
                    }else {
                        beanName = toLowerFirstWord(field.getType().getSimpleName());
                    }
                    try {
                        field.set(bean, ioc.get(beanName));
                    }catch (Exception e){
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }
    }
    private void initHandlerMapping() {
        if (ioc.isEmpty()){
            return;
        }
        try{
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Class<?> clazz = entry.getValue().getClass();
                if (clazz.isAnnotationPresent(MyController.class)){
                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(MyRequestMapping.class)){
                        MyRequestMapping myRequestMapping = clazz.getAnnotation(MyRequestMapping.class);
                        baseUrl = myRequestMapping.value();
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(MyRequestMapping.class)){
                            MyRequestMapping myRequestMapping = method.getAnnotation(MyRequestMapping.class);
                            String url =(baseUrl+"/"+myRequestMapping.value()).replaceAll("/+", "/");
                            handlerMapping.put(url, method);
                            MyController myController = clazz.getAnnotation(MyController.class);
                            String beanName = StringUtils.isBlank(myController.value())? clazz.getSimpleName() : myController.value();
                            beanName = toLowerFirstWord(beanName);
                            controllerMap.put(url, ioc.get(beanName));
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        logger.info("11");
    }

    /**
     * 实例化
     */
    private void doInstance() {
        if (classNames.isEmpty()){
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)){
                    MyController myController = clazz.getAnnotation(MyController.class);
                    String beanName = StringUtils.isBlank(myController.value())? clazz.getSimpleName() : myController.value();
                    ioc.put(toLowerFirstWord(beanName),clazz.newInstance());
                }else if (clazz.isAnnotationPresent(MyService.class) || clazz.isAnnotationPresent(MyController.class)){
                    String beanName = toLowerFirstWord(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    Class[] interfaces=clazz.getInterfaces();
                    for (Class<?> i : interfaces){
                        ioc.put(toLowerFirstWord(i.getSimpleName()),instance);
                    }
                }else{
                    continue;
                }
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }
    }

    private String toLowerFirstWord(String beanName) {
        if (null == beanName){
            return "";
        }
        char[] chars = beanName.toCharArray();
        if (chars.length > 1){
            char c = chars[0];
            if (64 < c  && c < 91){
                chars[0] = (char) (c + 32);
            }
        }
        return String.copyValueOf(chars);
    }

    /**
     *  将指定包下扫描得到的类，添加到classNames字段中
     * @param packageName
     */
    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.","/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()){
                doScanner(packageName+"."+file.getName());
            }else{
                String className = packageName + "." + file.getName().replace(".class","");
                classNames.add(className);
            }
        }
    }

    /**
     * 加载配置文件
     * @param location 配置文件的位置
     */
    private void doLoadConfig(String location) {
        //把web.xml中的contextConfigLocation对应value值的文件加载到流里面
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);) {
            //用Properties文件加载文件里的内容
            logger.info("读取"+location+"里面的文件");
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
            logger.info(e.getMessage());
        }
    }
}
