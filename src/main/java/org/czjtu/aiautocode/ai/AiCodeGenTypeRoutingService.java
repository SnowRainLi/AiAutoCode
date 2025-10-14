package org.czjtu.aiautocode.ai;

import dev.langchain4j.service.SystemMessage;
import org.czjtu.aiautocode.model.enums.CodeGenTypeEnum;

/**
 * 描述：代码生成模式路由服务
 */
public interface AiCodeGenTypeRoutingService {

    /**
     * 路由代码生成模式
     * @param userPrompt 用户输入
     * @return 代码生成模式
     */
    @SystemMessage(fromResource = "prompt/code-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);

}
