package ioc;

public class Eat implements Apple {

    public void eatApple(Animal p){
        System.out.println(p.name + "吃苹果");
    }
}
