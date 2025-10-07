package org.czjtu.aiautocode.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * html代码结果
 */
@Description("生成的html代码结果")
@Data
public class HtmlCodeResult {

    /**
     * html代码
     */
    @Description("html代码")
    private String htmlCode;

    /**
     * 描述
     */
    @Description("生成的描述")
    private String description;
}
