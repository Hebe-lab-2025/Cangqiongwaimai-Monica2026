package com.sky.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * PageResult类
 * 用于分页查询结果的数据封装类
 * 包含分页信息（如当前页码、每页大小、总记录数等）和当前页的数据列表
 */
public class PageResult implements Serializable {
    private Long total;
    private List records;
}
