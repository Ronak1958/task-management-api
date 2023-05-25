package com.paytm.restapi.restfulapi.jpa;

import com.paytm.restapi.restfulapi.user.Task;
import com.paytm.restapi.restfulapi.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

}
