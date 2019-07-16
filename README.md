### IOC的基本原理
通过反射来解析类的各种信息，包括构造器，参数以及方法，然后将其封装为bean，定义信息类，将他的构造器，参数以及方法等放入map中，也就是容器，其实容器就是个map。

### 基本场景
写一个猴子吃苹果的例子，不用手动new对象，通过IOC来实现，基于xml的实现
### 基本步骤（通过定义xml来实现IOC）
1. 当写好配置文件启动项目时，框架会按照配置文件找到你要扫描的包下面的所以文件。
2. 找到解析包中所有的类进行反射解析，找到@service，@bean的注解类，将他们放入map中。
3. 每当你需要一个bean的时候他会先去容器中也就是map中找是否存在这个类，如果存在通过构造器new出来，这就是控制反转，框架帮你new。
4. 再找这个类是不是有要注入的属性或者方法，有就到容器中找到对应解析类，new出对象，并通过之前解析出的类找到setter 方法然后注入该对象。这就是依赖注入。
5. 如果类在容器中没有找到，则抛出异常，spring无法找到该类的定义。
6. 嵌套bean用递归，container会放到servletContext里面，每次请求都会从 servletContext 里面找到对应container，不用多次进行解析类定义。
7. 如果bean的scope是singleton,则这个bean不会重新创建，将这个bean放入map中，每次都从map中找。
8. 如果bean的scope是session,则该bean会放到session中。
### 实现类
#### 动物类:Animal
```
package ioc;

public abstract class Animal {
    public String name;
}

```
#### 猴子类：Monkey
```
package ioc;

public class Monkey extends Animal {
}

```
#### 苹果接口：Apple
```
package ioc;

/**
 * 苹果
 */
public interface Apple {
    /**
     * 吃苹果
     */
    public void eatApple(Animal p);
}

```
#### Eat类来实现Apple接口
```
package ioc;

public class Eat implements Apple {

    public void eatApple(Animal p){
        System.out.println(p.name + "吃苹果");
    }
}

```
#### MonkeyService
```
package ioc;

public class MonkeyService {
    private Apple apple;

    public Apple getApple() {
        return apple;
    }

    public void setApple(Apple apple) {
        this.apple = apple;
    }
    /**
     * 吃
     */
    public void eatService(Animal p){
        this.apple.eatApple(p);
    }
}

```
#### 工厂接口：BeanFactory
```
package ioc;

/**
 * bean 工厂类
 */
public interface BeanFactory {
    public Object getBean(String name);
}

```
#### 容器类实现
```
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

```
#### 测试类
```
package ioc;

import org.junit.Test;

public class MonkeyServiceTest {
    @Test
    public void helloService() throws Exception{
        var factory = new ClassPathXmlApplicationContext();
        var service = (MonkeyService)factory.getBean("service");
        var monkey = (Monkey)factory.getBean("monkey");
        monkey.name = "猴子";
        service.eatService(monkey);
    }
}

```
#### iocbeans.xml
```
<?xml version="1.0" encoding="UTF-8"?>

<beans>
    <bean id = "eat" class = "ioc.Eat"/>
    <bean id = "monkey" class="ioc.Monkey"/>
    <bean id = "service" class="ioc.MonkeyService">
        <property name = "apple" bean = "eat"/>
    </bean>
</beans>

```
#### pom.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.mjy</groupId>
    <artifactId>mjy</artifactId>
    <version>1.0-SNAPSHOT</version>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>10</source>
                    <target>10</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
    <dependencies>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>5.0.6.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-aspects</artifactId>
            <version>5.0.6.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
            <version>5.0.6.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.0.6.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>5.0.6.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-aop</artifactId>
            <version>5.0.6.RELEASE</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.jdom/jdom2 -->
        <dependency>
            <groupId>org.jdom</groupId>
            <artifactId>jdom2</artifactId>
            <version>2.0.6</version>
        </dependency>


        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```