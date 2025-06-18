package com.sky.service;


import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.ArrayList;

public interface CategoryService {
    void addNew(CategoryDTO categoryDTO);

    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    void deleteById(Long id);

    void status(Integer status, Long id);

    void update(CategoryDTO categoryDTO);

    ArrayList<Category> selectByType(Integer type);
}
