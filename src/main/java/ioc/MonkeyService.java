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
