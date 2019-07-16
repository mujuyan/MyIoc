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
