package com.fin.system.vo;

import lombok.Data;

import java.util.List;

/**
 * 检验返回给前端的vo
 *
 * @author 散装java
 * @version 1.0.0
 * @date 2022-11-19
 */
@Data
public class CheckResultVo {
    /**
     * 是否已上传
     */
    private Boolean uploaded;

    private List<Integer> uploadedChunks;
}

