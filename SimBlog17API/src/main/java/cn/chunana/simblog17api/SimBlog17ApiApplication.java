package cn.chunana.simblog17api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SimBlog17ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimBlog17ApiApplication.class, args);
    }

}
