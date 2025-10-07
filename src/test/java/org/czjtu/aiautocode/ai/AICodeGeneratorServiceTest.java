package org.czjtu.aiautocode.ai;

import jakarta.annotation.Resource;
import org.czjtu.aiautocode.ai.model.HtmlCodeResult;
import org.czjtu.aiautocode.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AICodeGeneratorServiceTest {
    @Resource
    private AICodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个博客不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("做个留言板不超过20行");
        Assertions.assertNotNull(result);
    }
}