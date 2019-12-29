package core.handler;

import core.annotation.MySpringBootApplication;
import core.servlet.MyDispatcherServlet;
import core.utils.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.List;

public class Scanner {

    private static final String CLASS_SUFFIX = ".class";
    /**
     *  扫描得到指定包下面的类名
     * @param packageName
     */
    public static void doScanner(MyDispatcherServlet servlet, String packageName, List<String> classNames) {
        Class startClass = servlet.getStartClass();
        packageName = StringUtils.isBlank(packageName)? StringUtils.EMPTY : packageName;
        String path;
        if (startClass.isAnnotationPresent(MySpringBootApplication.class)){
            path = startClass.getResource("/" + packageName.replaceAll("\\.","/")).getPath();
        }else {
            ClassLoader classLoader = servlet.getClass().getClassLoader();
            URL url = classLoader.getResource("/" + packageName.replaceAll("\\.","/"));
            path = url.getPath();
        }
        File root = new File(path);
        doScanner(root, packageName, classNames);
    }

    private static void doScanner(File dir,String packageName, List<String> classNames) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()){
                String path = getPath(file, packageName);
                doScanner(file, path, classNames);
            }else{
                String fileName = file.getName();
                if (fileName.endsWith(CLASS_SUFFIX)){
                    String path = getPath(file, packageName);
                    String className = path.replace(CLASS_SUFFIX,"");
                    classNames.add(className);
                }
            }
        }
    }

    private static String getPath(File file,String packageName){
        if (StringUtils.isBlank(packageName)){
            return file.getName();
        }
        return packageName + "." + file.getName();
    }
}
