package core.handler;

import core.servlet.MyDispatcherServlet;

import java.io.File;
import java.net.URL;
import java.util.List;

public class Scanner {

    /**
     *  将指定包下扫描得到的类，添加到classNames字段中
     * @param packageName
     */
    public static void doScanner(MyDispatcherServlet servlet, String packageName, List<String> classNames) {
        URL url = servlet.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.","/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()){
                doScanner(servlet, packageName + "." + file.getName(), classNames);
            }else{
                String className = packageName + "." + file.getName().replace(".class","");
                classNames.add(className);
            }
        }
    }
}
