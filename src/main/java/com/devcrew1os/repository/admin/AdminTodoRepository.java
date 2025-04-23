package com.devcrew1os.repository.admin;

import com.devcrew1os.entity.admin.AdminTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminTodoRepository extends JpaRepository<AdminTodo, Integer> {
}
