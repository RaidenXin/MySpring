package core.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import core.annotation.MyRequestParam;
import core.interceptor.InterceptorMethod;
import core.utils.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数处理类
 */
public class ParamHandler {

    public static <T> T[] handler(HttpServletRequest req, InterceptorMethod method) throws IOException {
        String body = getBody(req);
        //获取参数的注解 得到参数名称
        Annotation[][] paramAnnotations = method.getAnnotations();
        if (StringUtils.isNotBlank(body)){
            return (T[]) bodyHandler(paramAnnotations, method, body);
        }else {
            return (T[]) parameterMapHandler(paramAnnotations, req, method);
        }
    }

    /**
     * 用的是一个一个的参数传递 如http://localhost:8080/test2/getUser2?name=zhangsan 则走该方法
     * @param paramAnnotations
     * @param req
     * @param method
     * @return
     */
    private static Object[] parameterMapHandler(Annotation[][] paramAnnotations,HttpServletRequest req, InterceptorMethod method){
        //获取方法的参数类型列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求的参数
        Map<String, String> parameterMap = getParameterMap(req);
        Object[] paramValues = new Object[parameterTypes.length];
        //方法的参数列表
        for (int i = 0; i < parameterTypes.length; i++){
            Class<?> paramClass = parameterTypes[i];
            //根据参数名称，做某些处理
            String requestParam = parameterTypes[i].getSimpleName();
            MyRequestParam myRequestParams = ArrayUtils.getElement(paramAnnotations[i], MyRequestParam.class);
            if (null != myRequestParams){
                String paramName = myRequestParams.value();
                String value = parameterMap.get(paramName);
                if (requestParam.equals("List")){
                    List params = JSON.parseArray(value, paramClass);
                    paramValues[i] = params;
                }else if (requestParam.equals("String")){
                    paramValues[i] = value;
                } else {
                    String data = JSON.toJSONString(parameterMap);
                    Object param = JSON.parseObject(data, paramClass);
                    paramValues[i] = param;
                }
            }
        }
        return paramValues;
    }

    private static Map<String,String> getParameterMap(HttpServletRequest req){
        //获取请求的参数
        Map<String, String[]> parameterMap = req.getParameterMap();
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            result.put(entry.getKey(), values[0]);
        }
        return result;
    }

    /**
     * 如果是通过 body传递参数 则走该方法
     * @param paramAnnotations
     * @param method
     * @param body
     * @return
     */
    private static Object[] bodyHandler(Annotation[][] paramAnnotations,InterceptorMethod method,String body){
        //获取方法的参数类型列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] paramValues = new Object[parameterTypes.length];
        //如果只是一个参数，传递过来的一定是 这个参数的json串
        if (parameterTypes.length == 1){
            paramValues[0] = JSON.parseObject(body, parameterTypes[0]);
        }else {
            //如果不止一个参数，传递过来的一定是类似于把多个DTO放在map中 json化的json串
            // 每个JSONObject代表一个DTO的json串
            Map<String, JSONObject> parameterMap = JSON.parseObject(body, Map.class);
            for (int i = 0; i < parameterTypes.length; i++){
                Class<?> paramClass = parameterTypes[i];
                //根据参数名称，做某些处理
                String requestParam = parameterTypes[i].getSimpleName();
                MyRequestParam myRequestParams = ArrayUtils.getElement(paramAnnotations[i], MyRequestParam.class);
                if (null != myRequestParams){
                    String paramName = myRequestParams.value();
                    JSONObject value = parameterMap.get(paramName);
                    if (requestParam.equals("List")){
                        List params = JSON.parseArray(value.toJSONString(), paramClass);
                        paramValues[i] = params;
                    }else{
                        Object param = JSON.parseObject(value.toJSONString(), paramClass);
                        paramValues[i] = param;
                    }
                }
            }
        }
        return paramValues;
    }


    /**
     * 获取 body
     * @param req
     * @return
     * @throws IOException
     */
    private static String getBody(HttpServletRequest req) throws IOException {
        BufferedReader bufferedReader = req.getReader();
        StringBuilder builder = new StringBuilder();
        char[] chars = new char[1024];
        while (bufferedReader.read(chars) != -1){
            builder.append(chars);
        }
        return builder.toString();
    }
}
