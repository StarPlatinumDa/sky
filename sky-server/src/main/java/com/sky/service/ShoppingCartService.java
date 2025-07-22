package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.ArrayList;

public interface ShoppingCartService {
    void add(ShoppingCartDTO shoppingCartDTO);

    ArrayList<ShoppingCart> list(Long id);

    void clean(Long userId);

    void sub(ShoppingCartDTO shoppingCartDTO);


}
