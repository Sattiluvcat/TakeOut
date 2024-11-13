package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    public void insertWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();
        System.out.println(setmealId);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insert(setmealDishes);
    }

    @Override
    public PageResult getSetmealPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page=setmealMapper.getSetmealPage(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        Setmeal setmeal = new Setmeal();
        for (Long id : ids) {
            setmeal=setmealMapper.getById(id);
            if(setmeal!=null&& Objects.equals(setmeal.getStatus(), StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        ids.forEach(setmealId->{
            setmealMapper.delete(setmealId);
            setmealDishMapper.delete(setmealId);
        });
    }

    @Override
    public SetmealVO getSetmealById(Long setmealId) {
        Setmeal setmeal=setmealMapper.getById(setmealId);
        SetmealVO setmealVO=new SetmealVO();
        setmealVO.setSetmealDishes(setmealDishMapper.getSetmealDishesById(setmealId));
        BeanUtils.copyProperties(setmeal,setmealVO);
        return setmealVO;
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        Long setmealId = setmealDTO.getId();
        setmealDishMapper.delete(setmealId);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insert(setmealDishes);
    }

    @Override
    @Transactional
    public void startOrStop(Integer status,Long setmealId) {
        Setmeal setmeal= Setmeal.builder()
                .id(setmealId)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        if(Objects.equals(status, StatusConstant.ENABLE)){
            List<Long> dishIds=setmealDishMapper.getDishIdsBySetmealId(setmealId);
            for(Long dishId:dishIds){
                Dish dish = dishMapper.getById(dishId);
                if(Objects.equals(dish.getStatus(), StatusConstant.DISABLE)) throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        setmealMapper.update(setmeal);
    }


}
