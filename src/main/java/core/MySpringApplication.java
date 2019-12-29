package core;

import core.annotation.MySpringBootApplication;
import core.config.EnvConfig;
import core.servlet.MyDispatcherServlet;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.lang.annotation.Annotation;

public class MySpringApplication {

    public static void run(Class<?> clazz, String... args) {
        if (clazz.isAnnotationPresent(MySpringBootApplication.class)) {
            Annotation[] annotations = clazz.getAnnotations();
            try {
                // 创建tomcat服务器
                Tomcat tomcatServer = new Tomcat();
                // 设定端口号
                EnvConfig envConfig = new EnvConfig();
                final Integer webPort = Integer.parseInt(envConfig.getPort());
                tomcatServer.setPort(webPort);
                // 设置上下文路径
                StandardContext ctx = (StandardContext) tomcatServer.addWebapp("/", new File("src/main").getAbsolutePath());
                // 禁止项目重入加载
                ctx.setReloadable(false);
                // 设置读取class文件地址
                File additionWebInfClasses = new File("target/classes");
                // 设置我们webRoot
                WebResourceRoot resources = new StandardRoot(ctx);
                resources.addPreResources(new DirResourceSet(resources, "/target/classes", additionWebInfClasses.getAbsolutePath(), "/"));
                Wrapper myspring = tomcatServer.addServlet("", "myspring", new MyDispatcherServlet(clazz));
                myspring.load();
                ctx.addServletMappingDecoded("/*", "myspring");
                // 开启我们的tomcat
                tomcatServer.start();
                // tomcat等待接受请求
                tomcatServer.getServer().await();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
