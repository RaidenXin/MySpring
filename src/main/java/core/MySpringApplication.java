package core;

import core.annotation.MySpringBootApplication;
import core.config.EnvConfig;
import core.servlet.MyDispatcherServlet;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.util.logging.Logger;

public class MySpringApplication {

    private static Logger logger = Logger.getLogger("init");

    public static void run(Class<?> clazz, String... args) {
        if (clazz.isAnnotationPresent(MySpringBootApplication.class)) {
            try {
                if (!EnvConfig.init()) {
                    System.exit(0);
                }
                Tomcat tomcat = new Tomcat();
                final Integer webPort = Integer.parseInt(EnvConfig.port);
                tomcat.setPort(webPort);
                tomcat.setBaseDir("myspring");
                String webappDirLocation = "myspring/webapps";
                File dir = new File(webappDirLocation);
                if(!dir.exists()&&!dir.isDirectory()) {
                    dir.mkdirs();
                }
                Context context = tomcat.addContext("", "/");
                tomcat.addServlet("", "myspring", new MyDispatcherServlet());
                context.addServletMappingDecoded("/*", "myspring");
                tomcat.start();
                tomcat.getServer().await();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
