package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据多个菜品id查套餐id
     */
    List<Long> getSetmealsIdsByDishIds(List<Long> dishIds);

    void insert(List<SetmealDish> setmealDishes);

    @Delete("delete from setmeal_dish where setmeal_id=#{setmealId}")
    void delete(Long setmealId);

    @Select("select * from setmeal_dish where setmeal_id=#{setmealId}")
    List<SetmealDish> getSetmealDishesById(Long setmealId);

    @Select("select dish_id from setmeal_dish where setmeal_id=#{setmealId}")
    List<Long> getDishIdsBySetmealId(Long setmealId);
}
