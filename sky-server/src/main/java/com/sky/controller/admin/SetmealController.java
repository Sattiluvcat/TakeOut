package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags="套餐")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    /**
     * 新增套餐
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result insertWithDish(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.insertWithDish(setmealDTO);
        return Result.success();
    }
    /**
     * 分页查询
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> getSetmealPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        PageResult pageResult=setmealService.getSetmealPage(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 批量删除套餐
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("批量删除套餐：{}",ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }
    /**
     * 根据id查套餐
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查套餐")
    public Result<SetmealVO> getSetmealById(@PathVariable Long id) {
        log.info("通过id查询套餐：{}",id);
        SetmealVO setmealVO=setmealService.getSetmealById(id);
        return Result.success(setmealVO);
    }
    /**
     * 修改套餐
     */
    @PutMapping
    @ApiOperation("修改套餐信息")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        setmealService.update(setmealDTO);
        return Result.success();
    }
    /**
     * 起售停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售停售套餐")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("修改状态：{}",status);
        setmealService.startOrStop(status,id);
        return Result.success();
    }
}
