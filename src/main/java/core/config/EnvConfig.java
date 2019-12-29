package core.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;


/**
 *   
 *  *    
 *  * 环境配置加载类
 *  * @author huangyan 
 *  * @date 2018/6/5 11:38  
 *  
 */
public class EnvConfig {

    private String port;

    public EnvConfig(){
        init();
    }
    /**
     *     *    
     *     * 初始化加载配置
     *     * @author
     *     * @date 2018/6/5 11:25  
     *     * @param []  
     *     * @return boolean  
     *    
     */
    public boolean init() {

        Configuration config;
        try {
            String env = System.getProperty("env");
            if (env == null) {
                System.out.println("没有配置环境，使用本地配置local");
                env = "local";
            }
            System.out.println("当前的环境是: " + env);
            String resource = this.getClass().getResource("/").getPath();
            String fileName = resource + "/application" + "-" + env + ".properties";
            if (!new File(fileName).exists()){
                fileName = resource + "myapplication.properties";
            }
            config = new PropertiesConfiguration(fileName);
            port = config.getString("tomcat.port");
            if (port == null || port.isEmpty()) {
                port = "8080";
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getPort() {
        return port;
    }
}
