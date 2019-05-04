package com.example.demo.service;

import com.example.demo.dao.UserDao;
import com.example.demo.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {


    @Autowired
    UserDao userDao;


    public User getById(int id){
        return userDao.getById(id);
    }
}
