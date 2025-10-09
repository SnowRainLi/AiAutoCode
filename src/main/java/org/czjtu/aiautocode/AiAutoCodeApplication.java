package org.czjtu.aiautocode;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude =  {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("org.czjtu.aiautocode.mapper")
public class AiAutoCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAutoCodeApplication.class, args);
    }

}
