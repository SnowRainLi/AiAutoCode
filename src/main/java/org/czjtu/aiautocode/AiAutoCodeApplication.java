package org.czjtu.aiautocode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.czjtu.aiautocode.mapper")
public class AiAutoCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAutoCodeApplication.class, args);
    }

}
