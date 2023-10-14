package fun.scaradev.todolist.task;

import fun.scaradev.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        UUID idUser = (UUID) request.getAttribute("idUser");

        taskModel.setIdUser(idUser);

        this.taskRepository.save(taskModel);

        LocalDateTime currentData = LocalDateTime.now();

        if (currentData.isAfter(taskModel.getStartAt()) || currentData.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A tarefa não pode ser criada com uma data retroativa");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A tarefa não pode ter previsão de encerramento antes do seu início");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(taskModel);
    }

    @GetMapping("")
    public ResponseEntity list(HttpServletRequest request) {
        UUID idUser = (UUID) request.getAttribute("idUser");

        List<TaskModel> tasks = this.taskRepository.findByIdUser(idUser);

        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
        TaskModel task = this.taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Esta tarefa não existe");
        }

        Utils.copyNonNullProperties(taskModel, task);

        UUID idUser = (UUID) request.getAttribute("idUser");

        if(!idUser.equals(task.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Esta tarefa não pertence a você");
        }

        taskModel.setId(id);
        taskModel.setIdUser(idUser);

        this.taskRepository.save(taskModel);

        return ResponseEntity.status(200).body(task);
    }
}
