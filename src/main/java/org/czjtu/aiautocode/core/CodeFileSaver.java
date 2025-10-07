package org.czjtu.aiautocode.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.czjtu.aiautocode.ai.model.HtmlCodeResult;
import org.czjtu.aiautocode.ai.model.MultiFileCodeResult;
import org.czjtu.aiautocode.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 文件保存器
 */
public class CodeFileSaver {

    //文件保存根目录
    private static final String FILE_SAVE_ROOT_PATH = System.getProperty("user.dir")+"/tmp/code_output/";
    //保存HTML网页代码
    public static File saveHtmlCode(HtmlCodeResult htmlCodeResult){
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writerToFile(baseDirPath,"index.html",htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }
    //保存多文件代码
    public static File saveMultiFileCode(MultiFileCodeResult multiFileCodeResult){
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writerToFile(baseDirPath,"index.html",multiFileCodeResult.getHtmlCode());
        writerToFile(baseDirPath,"style.css",multiFileCodeResult.getCssCode());
        writerToFile(baseDirPath,"script.js",multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }
    //构建文件的唯一路径：tmp/code_output/bizType_雪花id
    private static String buildUniqueDir(String bizType){
        String uniqueDirName = StrUtil.format("{}_{}" , bizType,IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_PATH+File.separator+uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }
    //保存单个文件
    private static void writerToFile(String dirPath,String fileName,String content){
        String filePath = dirPath+ File.separator+fileName;
        FileUtil.writeString(content,filePath, StandardCharsets.UTF_8);

    }

}
