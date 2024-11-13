package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品信息：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult pageResult=dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 批量删除
     */
    @DeleteMapping
    @ApiOperation("批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}",ids);
        dishService.delete(ids);
        return Result.success();
    }
    /**
     * 根据id查找菜品
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id显示菜品信息")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询：{}",id);
        DishVO dishVO=dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }
    /**
     * 根据分类id显示菜品
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id显示菜品信息")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("根据分类id显示菜品{}",categoryId);
        List<Dish> dishes=dishService.list(categoryId);
        return Result.success(dishes);
    }
    /**
     * 修改菜品
     */
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }
    /**
     * 起售停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售停售菜品")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("修改状态：{}",status);
        dishService.startOrStop(status,id);
        return Result.success();
    }
}
