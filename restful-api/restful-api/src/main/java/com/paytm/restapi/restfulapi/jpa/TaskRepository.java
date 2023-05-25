package com.paytm.restapi.restfulapi.jpa;

import com.paytm.restapi.restfulapi.user.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task,Integer> {
}
