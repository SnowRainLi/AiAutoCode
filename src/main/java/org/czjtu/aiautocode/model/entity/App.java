package org.czjtu.aiautocode.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 应用
 * @TableName app
 */
@TableName(value ="app")
@Data
public class App implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 应用名称
     */
    @TableField(value = "appName")
    private String appName;

    /**
     * 应用封面
     */
    @TableField(value = "cover")
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    @TableField(value = "initPrompt")
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    @TableField(value = "codeGenType")
    private String codeGenType;

    /**
     * 部署标识
     */
    @TableField(value = "deployKey")
    private String deployKey;

    /**
     * 部署时间
     */
    @TableField(value = "deployedTime")
    private Date deployedTime;

    /**
     * 优先级
     */
    @TableField(value = "priority")
    private Integer priority;

    /**
     * 创建用户id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 编辑时间
     */
    @TableField(value = "editTime")
    private Date editTime;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "isDelete")
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1895836130820534673L;
}