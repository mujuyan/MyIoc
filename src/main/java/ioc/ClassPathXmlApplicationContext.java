package ioc;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ioc 容器类
 */
public class ClassPathXmlApplicationContext implements BeanFactory {
    // IOC 容器
    private Map<String,Object> beans = new HashMap<String, Object>();

    /**
     * 构造函数完成xml的解析
     */
    public ClassPathXmlApplicationContext() throws Exception{
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(this.getClass().getClassLoader().getResource("iocbeans.xml"));
        Element rootElement = document.getRootElement();
        // 获取bean 元素集合
        List<Element> list = rootElement.getChildren("bean");

        for(int i = 0; i < list.size(); i ++){
            Element e = list.get(i);
            String id = e.getAttributeValue("id");
            String clazz = e.getAttributeValue("class");

            System.out.println(id + ":" + clazz);

            // 通过反射构建对象
            Object o = Class.forName(clazz).getDeclaredConstructor().newInstance();

            // 将对象添加到map容器中
            beans.put(id,o);

            // 注入依赖
            for(Element property : e.getChildren("property")){
                String name = property.getAttributeValue("name");
                String bean = property.getAttributeValue("bean");
                Object propertyObj = beans.get(bean);

                // 拼接setter方法
                String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
                System.out.println("methodName = " + methodName);

                Method m = o.getClass().getMethod(methodName,propertyObj.getClass().getInterfaces());

                // 注入
                m.invoke(o,propertyObj);
            }
        }

    }

    @Override
    public Object getBean(String name){
        return beans.get(name);
    }
}
