package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 是否存在
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list =shoppingCartMapper.list(shoppingCart);
        // 存在加一
        if(list!=null&& !list.isEmpty()){
            ShoppingCart shoppingCart1 = list.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber()+1);
            shoppingCartMapper.updateById(shoppingCart1);
        }else{
            // 不存在时插入购物车表
            Long dishId = shoppingCart.getDishId();
            if(dishId!=null){
                Dish dish=dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            }else{
                Long setmealId = shoppingCart.getSetmealId();
                Setmeal setmeal=setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> show() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        return shoppingCartMapper.list(shoppingCart);
    }

    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.clean(userId);
    }

    @Override
    public void delete1Thing(ShoppingCartDTO shoppingCartDTO) {
        // 搜索
        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list =shoppingCartMapper.list(shoppingCart);
        if(list==null){
            throw new DeletionNotAllowedException("该商品不在购物车中哦");
        }
        ShoppingCart shoppingCart1 = list.get(0);
        // 通过number判断情况
        Integer number=shoppingCart1.getNumber();
        if(number==1){
            shoppingCartMapper.delete1Thing(shoppingCart1.getId());
        }else{
            shoppingCart1.setNumber(number-1);
            shoppingCartMapper.updateById(shoppingCart1);
        }
    }
}
