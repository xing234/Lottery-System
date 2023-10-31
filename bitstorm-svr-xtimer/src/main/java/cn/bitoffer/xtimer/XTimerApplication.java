package cn.bitoffer.xtimer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"cn.bitoffer"})
public class XTimerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XTimerApplication.class, args);
    }

}
