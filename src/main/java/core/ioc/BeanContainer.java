package core.ioc;

import java.util.*;

public class BeanContainer {

    private Map<String,Object> ioc;

    public BeanContainer(){
        ioc = new HashMap<>();
    }

    public void put(String key, Object bean){
        if (ioc.containsKey(key)){
            Object oldBean = ioc.get(key);
            if (oldBean instanceof List){
                ((List<Object>) oldBean).add(oldBean);
            }else {
                List<Object> beans = new ArrayList<>();
                beans.add(oldBean);
                beans.add(bean);
                ioc.put(key, beans);
            }
        }else {
            ioc.put(key, bean);
        }
    }

    public Object get(String key){
        return ioc.get(key);
    }

    public boolean containsKey(String key){
        return ioc.containsKey(key);
    }

    public boolean isEmpty(){
        return ioc.isEmpty();
    }

    public Set<Map.Entry<String, Object>> entrySet(){
        return ioc.entrySet();
    }
}
