package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据多个菜品id查套餐id
     */
    List<Long> getSetmealsIdsByDishIds(List<Long> dishIds);
}
