package core.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;


/**
 *   
 *  *    
 *  * 环境配置加载类
 *  * @author huangyan 
 *  * @date 2018/6/5 11:38  
 *  
 */
public class EnvConfig {

    public static String port = null;

    public static String basedir = null;
    public static String filepath = null;


    /**
     *     *    
     *     * 初始化加载配置
     *     * @author
     *     * @date 2018/6/5 11:25  
     *     * @param []  
     *     * @return boolean  
     *    
     */
    public static boolean init() {


        Configuration config;
        try {
            String env = System.getProperty("env");
            if (env == null) {
                System.out.println("没有配置环境，使用本地配置local");
                env = "local";
            }
            System.out.println("当前的环境是: " + env);
            String fileName = "application" + "-" + env + ".properties";


            config = new PropertiesConfiguration(fileName);
            port = config.getString("tomcat.port");
            if (port == null || port.isEmpty()) {
                port = "8080";
            }
            basedir = config.getString("tomcat.basedir");
            filepath = config.getString("filepath");


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
