package com.devcrew1os.repository.admin;

import com.devcrew1os.entity.admin.AdminTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AdminTodoRepository extends JpaRepository<AdminTodo, Integer> {
    List<AdminTodo> findAllByChallenge_IdIn(List<Integer> challengeIds);
    List<AdminTodo> findAllByIdIn(List<Integer> ids);
}
