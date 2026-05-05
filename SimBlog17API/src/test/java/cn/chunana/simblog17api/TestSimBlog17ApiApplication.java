package cn.chunana.simblog17api;

import org.springframework.boot.SpringApplication;

public class TestSimBlog17ApiApplication {

    static void main(String[] args) {
        String[] testProfileArgs = new String[args.length + 1];
        testProfileArgs[0] = "--spring.profiles.active=test";
        System.arraycopy(args, 0, testProfileArgs, 1, args.length);

        SpringApplication.from(SimBlog17ApiApplication::main)
                         .with(TestcontainersConfiguration.class)
                         .run(testProfileArgs);
    }

}
