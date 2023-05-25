package com.paytm.restapi.restfulapi.user;

import com.paytm.restapi.restfulapi.jpa.TaskRepository;
import com.paytm.restapi.restfulapi.jpa.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class UserJpaResource {

    private UserRepository repository;
    private TaskRepository taskRepository;
    public UserJpaResource(UserRepository repository,TaskRepository taskRepository){

        this.taskRepository=taskRepository;
        this.repository=repository;
    }
    @GetMapping(path = "/users")
    public List<User> retrieveAllUsers(){

        return repository.findAll();
    }
    @GetMapping(path = "/users/{id}")
    public User retrieveUser(@PathVariable int id) {

        Optional<User> user = repository.findById(id);
        if (user.isEmpty())
            throw new UserNotFoundException("id:" + id);
        return user.get();
    }
    @GetMapping("/users/{id}/tasks")
    public List<Task> retrieveTasksForUser(@PathVariable int id){
        Optional<User> user = repository.findById(id);
        if (user.isEmpty())
            throw new UserNotFoundException("id:" + id);
        return user.get().getTasks();

    }
    @GetMapping("/users/{userId}/tasks/{taskId}")
    public Task retrieveTaskForUser(@PathVariable int userId, @PathVariable int taskId) {
        Optional<User> user = repository.findById(userId);
        if (user.isEmpty()) {
            throw new UserNotFoundException("id:" + userId);
        }

        User foundUser = user.get();
        Optional<Task> task = foundUser.getTasks().stream()
                .filter(t -> t.getId() == taskId)
                .findFirst();

        if (task.isEmpty()) {
            throw new TaskNotFoundException("Task not found for id:" + taskId);
        }

        return task.get();
    }

    @GetMapping("/users/{userId}/tasks/search")
    public List<Task> searchTasksForUser(
            @PathVariable int userId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description
    ) {
        Optional<User> user = repository.findById(userId);
        if (user.isEmpty()) {
            throw new UserNotFoundException("id:" + userId);
        }

        List<Task> tasks = user.get().getTasks();

        if (title != null) {
            tasks = tasks.stream()
                    .filter(task -> task.getTitle().equalsIgnoreCase(title))
                    .collect(Collectors.toList());
        }

        if (description != null) {
            tasks = tasks.stream()
                    .filter(task -> task.getDescription().equalsIgnoreCase(description))
                    .collect(Collectors.toList());
        }

        return tasks;
    }

    @GetMapping("/users/{userId}/tasks/date")
    public List<Task> filterTasksByDate(@PathVariable int userId,
                                        @RequestParam(required = false) String dueDateFrom,
                                        @RequestParam(required = false) String dueDateTo) {
        Optional<User> user = repository.findById(userId);
        if (user.isEmpty()) {
            throw new UserNotFoundException("id:" + userId);
        }

        List<Task> tasks = user.get().getTasks();

        if (dueDateFrom != null && dueDateTo != null) {
            LocalDate fromDate = LocalDate.parse(dueDateFrom);
            LocalDate toDate = LocalDate.parse(dueDateTo);

            tasks = tasks.stream()
                    .filter(task -> task.getTargetDate().isAfter(fromDate.minusDays(1)) && task.getTargetDate().isBefore(toDate.plusDays(1)))
                    .collect(Collectors.toList());
        }

        return tasks;
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable int id){
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            throw new UserNotFoundException("id:" + id);
        }

        repository.deleteById(id);

    }
    @DeleteMapping("/users/{id}/tasks/{taskid}")
    public void deleteTaskById(@PathVariable int id, @PathVariable int taskid) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            throw new UserNotFoundException("id:" + id);
        }

        User foundUser = user.get();
        Optional<Task> taskToDelete = foundUser.getTasks().stream()
                .filter(task -> task.getId() == taskid)
                .findFirst();

        if (taskToDelete.isEmpty()) {
            throw new TaskNotFoundException("Task not found for id:" + taskid);
        }

        Task task = taskToDelete.get();
        foundUser.getTasks().remove(task); // Remove the task from the user's task list

        repository.save(foundUser); // Save the updated user without the deleted task
    }
    @PutMapping("/users/{id}/tasks/{taskid}/complete")
    public Task markTaskAsComplete(@PathVariable int id, @PathVariable int taskid) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            throw new UserNotFoundException("id:" + id);
        }

        User foundUser = user.get();
        Optional<Task> taskToUpdate = foundUser.getTasks().stream()
                .filter(task -> task.getId() == taskid)
                .findFirst();

        if (taskToUpdate.isEmpty()) {
            throw new TaskNotFoundException("Task not found for id:" + taskid);
        }

        Task task = taskToUpdate.get();
        task.setStatus(true); // Mark the task as complete by setting its status to true

        taskRepository.save(task); // Saving the updated task

        return task;
    }


    @PutMapping("/users/{id}/tasks/{task_id}")
    public Task updateTaskById(@PathVariable int id, @PathVariable int task_id, @RequestBody Task updatedTask) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            throw new UserNotFoundException("id:" + id);
        }

        User foundUser = user.get();
        Optional<Task> taskToUpdate = foundUser.getTasks().stream()
                .filter(task -> task.getId() == task_id)
                .findFirst();

        if (taskToUpdate.isEmpty()) {
            throw new TaskNotFoundException("Task not found for id:" + task_id);
        }

        Task existingTask = taskToUpdate.get();
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.isStatus());
        existingTask.setTargetDate(updatedTask.getTargetDate());


        taskRepository.save(existingTask); // Save the updated task

        return existingTask;
    }





    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user){
       User savedUser= repository.save(user);
        URI location= ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").
                buildAndExpand(savedUser.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/users/{id}/tasks")
    public ResponseEntity<Object> createTaskForUser(@PathVariable int id, @Valid @RequestBody Task task){
        Optional<User> user = repository.findById(id);
        if (user.isEmpty())
            throw new UserNotFoundException("id:" + id);

        task.setUser(user.get());
       Task savedTask=  taskRepository.save(task);

        URI location= ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").
                buildAndExpand(savedTask.getId())
                .toUri();
        return ResponseEntity.created(location).build();

    }
}
