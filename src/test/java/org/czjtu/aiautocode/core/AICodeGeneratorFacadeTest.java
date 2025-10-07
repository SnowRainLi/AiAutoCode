package org.czjtu.aiautocode.core;

import jakarta.annotation.Resource;
import org.czjtu.aiautocode.model.enums.CodeGenTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@SpringBootTest
class AICodeGeneratorFacadeTest {

    @Resource
    private AICodeGeneratorFacade aiCodeGeneratorFacade;
    @Test
    void generateAndSaveCode() {
        File file =aiCodeGeneratorFacade.generateAndSaveCode("生成一个登陆页面,不超过20行代码", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull( file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个登陆页面,不超过20行代码", CodeGenTypeEnum.HTML);
        //阻塞 等待所有代码生成完成
        List<String> result = codeStream.collectList().block();
        Assertions.assertNotNull(result);
        String completeCode = String.join("", result);
        Assertions.assertNotNull(completeCode);
    }
}