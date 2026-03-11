package com.taskmanager.domain.repository;

import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Task> findByOwnerIdAndStatusAndPriority(Long ownerId, TaskStatus status, Priority priority, Pageable pageable);

    Page<Task> findByOwnerIdAndStatus(Long ownerId, TaskStatus status, Pageable pageable);

    Page<Task> findByOwnerIdAndPriority(Long ownerId, Priority priority, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByPriority(Priority priority, Pageable pageable);

    Page<Task> findByStatusAndPriority(TaskStatus status, Priority priority, Pageable pageable);
}
