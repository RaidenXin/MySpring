package core;

import core.annotation.MySpringBootApplication;
import core.config.EnvConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class MySpringApplication {


    public static void run(Class<?> clazz, String... args) {
        if (clazz.isAnnotationPresent(MySpringBootApplication.class)) {
            try {
                if (!EnvConfig.init()) {
                    System.exit(0);
                }
                // 1.创建一个内嵌的Tomcat
                Tomcat tomcat = new Tomcat();


                // 2.设置Tomcat端口默认为8080
                final Integer webPort = Integer.parseInt(EnvConfig.port);
                tomcat.setPort(Integer.valueOf(webPort));


                // 3.设置工作目录,tomcat需要使用这个目录进行写一些东西
                final String baseDir = EnvConfig.basedir;
                tomcat.setBaseDir(baseDir);
                tomcat.getHost().setAutoDeploy(false);


                // 4. 设置webapp资源路径
                String webappDirLocation = "web/";
                StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());


                // 5. 设置上下文路每径
                String contextPath = "";
                ctx.setPath(contextPath);
                ctx.addLifecycleListener(new Tomcat.FixContextListener());
                ctx.setName("MyMVC");


                System.out.println("child Name:" + ctx.getName());
                tomcat.getHost().addChild(ctx);

                tomcat.start();
                tomcat.getServer().await();
            } catch (Exception exception) {
            }
        }
    }
}
