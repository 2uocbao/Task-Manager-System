package com.quocbao.taskmanagementsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.common.PriorityEnum;
import com.quocbao.taskmanagementsystem.common.RoleEnum;
import com.quocbao.taskmanagementsystem.common.StatusEnum;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Assign.AssignEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.ForbiddenException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.TaskRequest;
import com.quocbao.taskmanagementsystem.payload.response.TaskResponse;
import com.quocbao.taskmanagementsystem.repository.TaskRepository;
import com.quocbao.taskmanagementsystem.repository.TaskRepository.TaskProjection;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskAssignmentHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TeamHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TeamMemberHelperService;
import com.quocbao.taskmanagementsystem.serviceimpl.TaskServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TaskTest {

    @Mock
    private ApplicationEventPublisher appEventPublisher;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TeamHelperService teamHelperService;

    @Mock
    private TeamMemberHelperService teamMemberHelperService;

    @Mock
    private TaskAssignmentHelperService taskAssignHelperService;

    @Mock
    private AuthenticationService authService;

    @Mock
    private MethodGeneral methodGeneral;

    @Mock
    private IdEncoder idEncoder;

    @InjectMocks
    private TaskServiceImpl taskServiceImpl;

    Long userId = 1L;
    Long teamId = 1L;
    String teamIdS = "teamId";
    Long taskId = 1L;
    String taskIdS = "taskId";

    @BeforeEach
    void defaultValue() {
        when(authService.getUserIdInContext()).thenReturn(userId);
    }

    @Test
    void testCreate_Success() {
        String time = LocalDateTime.now().toString();
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("new task");
        taskRequest.setDescription("description");
        taskRequest.setStartDate(time);
        taskRequest.setDueAt("2025-09-14 22:00:00");
        taskRequest.setPriority("HIGH");
        Task task = new Task(taskRequest);
        task.setId(taskId);
        task.setTeam(Team.builder().id(teamId).build());
        task.setUser(User.builder().id(userId).build());
        task.setStatus(StatusEnum.PENDING);
        task.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        task.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        when(idEncoder.decode(teamIdS)).thenReturn(teamId);
        when(teamHelperService.isLeaderOfTeam(userId, teamId)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        taskServiceImpl.createTask(teamIdS, taskRequest);
        verify(taskRepository, times(1)).save(any(Task.class));
        ArgumentCaptor<AssignEvent> captor = ArgumentCaptor.forClass(AssignEvent.class);
        verify(appEventPublisher).publishEvent(captor.capture());
        AssignEvent event = captor.getValue();
        assertEquals(userId, event.getUserId());
        assertEquals(taskId, event.getTaskId());
    }

    @Test
    void testCreate_Forbidden() {
        when(idEncoder.decode(teamIdS)).thenReturn(teamId);
        when(teamHelperService.isLeaderOfTeam(userId, teamId)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> {
            taskServiceImpl.createTask(teamIdS, new TaskRequest());
        });
        verify(taskRepository, never()).save(any(Task.class));
        verify(appEventPublisher, never()).publishEvent(any(AssignEvent.class));
    }

    @Test
    void testRetrieve_Success() {
        Task task = Task.builder().id(taskId).user(User.builder().id(userId).build())
                .dueAt(Timestamp.valueOf(LocalDateTime.now())).startDate(Timestamp.valueOf(LocalDateTime.now()))
                .createdAt(Timestamp.valueOf(LocalDateTime.now())).updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .status(StatusEnum.PENDING).priority(PriorityEnum.HIGH).build();

        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignHelperService.isUserInTask(userId, taskId)).thenReturn(true);
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));
        taskServiceImpl.getTask(taskIdS);
        verify(taskRepository, times(1)).findById(anyLong());
    }

    @Test
    void testRetrieve_AccessDenied() {
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignHelperService.isUserInTask(userId, taskId)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            taskServiceImpl.getTask(taskIdS);
        });
        verify(taskRepository, never()).findById(taskId);
    }

    @Test
    void testRetrieve_ResourceNotFound() {
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignHelperService.isUserInTask(userId, taskId)).thenReturn(true);
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            taskServiceImpl.getTask(taskIdS);
        });
        verify(taskRepository).findById(anyLong());
    }

    @Test
    void testUpdate_Success() {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("new task");
        taskRequest.setDescription("description");
        taskRequest.setStartDate("2025-09-14 22:00:00");
        taskRequest.setDueAt("2025-09-14 22:00:00");
        taskRequest.setPriority("HIGH");
        taskRequest.setStatus(StatusEnum.CANCELLED.toString());
        Task task = Task.builder().id(taskId).user(User.builder().id(userId).build()).title("new task")
                .dueAt(Timestamp.valueOf(LocalDateTime.now())).startDate(Timestamp.valueOf(LocalDateTime.now()))
                .createdAt(Timestamp.valueOf(LocalDateTime.now())).updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .status(StatusEnum.PENDING).priority(PriorityEnum.HIGH).build();
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignHelperService.isRoleUserInTask(userId, taskId, RoleEnum.ADMIN)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        taskServiceImpl.updateTask(taskIdS, taskRequest);
        verify(taskRepository, times(1)).findById(anyLong());
    }

    @Test
    void testUpdate_Forbidden() {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("new task");
        taskRequest.setDescription("description");
        taskRequest.setStartDate("2025-09-14 22:00:00");
        taskRequest.setDueAt("2025-09-14 22:00:00");
        taskRequest.setPriority("HIGH");
        taskRequest.setStatus(StatusEnum.CANCELLED.toString());
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignHelperService.isRoleUserInTask(userId, taskId, RoleEnum.ADMIN)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> {
            taskServiceImpl.updateTask(taskIdS, taskRequest);
        });
        verify(taskRepository, never()).findById(taskId);
    }

    @Test
    void testUpdate_ResourceNotFound() {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("new task");
        taskRequest.setDescription("description");
        taskRequest.setStartDate("2025-09-14 22:00:00");
        taskRequest.setDueAt("2025-09-14 22:00:00");
        taskRequest.setPriority("HIGH");
        taskRequest.setStatus(StatusEnum.CANCELLED.toString());
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignHelperService.isRoleUserInTask(userId, taskId, RoleEnum.ADMIN)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            taskServiceImpl.updateTask(taskIdS, taskRequest);
        });
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void testDelete_Success() {
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        Task task = Task.builder().id(taskId).build();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskAssignHelperService.isRoleUserInTask(userId, taskId, RoleEnum.ADMIN)).thenReturn(true);
        taskServiceImpl.deleteTask(taskIdS);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void testDelete_Forbidden() {
        Task task = Task.builder().id(taskId).build();
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskAssignHelperService.isRoleUserInTask(userId, taskId, RoleEnum.ADMIN)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> {
            taskServiceImpl.deleteTask(taskIdS);
        });
        verify(taskRepository, never()).delete(task);
    }

    @Test
    void testDelete_ResourceNotFound() {
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            taskServiceImpl.deleteTask(taskIdS);
        });
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskAssignHelperService, never()).isRoleUserInTask(userId, taskId, RoleEnum.ADMIN);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void testRetrieveList_Success() {
        TaskProjection taskProjection = new TaskProjection() {
            @Override
            public Long getId() {
                return taskId;
            }

            @Override
            public String getTitle() {
                return "task";
            }

            @Override
            public String getPriority() {
                return PriorityEnum.HIGH.toString();
            }

            @Override
            public Timestamp getDueAt() {
                return Timestamp.valueOf("2025-06-12 00:00:00");
            }

            @Override
            public Long getCommentCount() {
                return 1L;
            }

            @Override
            public Long getReportCount() {
                return 1L;
            }
        };
        when(idEncoder.decode(teamIdS)).thenReturn(teamId);
        when(teamMemberHelperService.isMemberTeam(userId, teamId)).thenReturn(true);
        Pageable pageable = PageRequest.of(0, 10);
        when(taskRepository.getTask(userId, teamId, StatusEnum.PENDING, PriorityEnum.HIGH,
                Timestamp.valueOf("2025-06-12 00:00:00"), Timestamp.valueOf("2025-06-12 00:00:00"), pageable))
                .thenReturn(new PageImpl<>(List.of(taskProjection)));
        Page<TaskResponse> result = taskServiceImpl.getTasks(teamIdS, StatusEnum.PENDING.toString(),
                PriorityEnum.HIGH.toString(),
                "2025-06-12 00:00:00", "2025-06-12 00:00:00", pageable);
        verify(taskRepository, times(1)).getTask(userId, teamId, StatusEnum.PENDING, PriorityEnum.HIGH,
                Timestamp.valueOf("2025-06-12 00:00:00"), Timestamp.valueOf("2025-06-12 00:00:00"), pageable);
        assertEquals(result.getSize(), 1);
    }

    @Test
    void testRetrieveList_AccessDenied() {
        when(idEncoder.decode(teamIdS)).thenReturn(teamId);
        when(teamMemberHelperService.isMemberTeam(userId, teamId)).thenReturn(false);
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(AccessDeniedException.class, () -> {
            taskServiceImpl.getTasks(teamIdS, taskIdS, taskIdS, teamIdS, taskIdS, pageable);
        });
        verify(taskRepository, never()).getTask(userId, teamId, null, null, null, null, null);
    }

    @Test
    void testUpdateStatus_Success() {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setStatus(StatusEnum.COMPLETED.toString());
        Task task = Task.builder().id(taskId).user(User.builder().id(userId).build())
                .dueAt(Timestamp.valueOf(LocalDateTime.now())).startDate(Timestamp.valueOf(LocalDateTime.now()))
                .createdAt(Timestamp.valueOf(LocalDateTime.now())).updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .status(StatusEnum.PENDING).priority(PriorityEnum.HIGH).build();
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskAssignHelperService.isUserInTask(userId, taskId)).thenReturn(true);
        when(taskRepository.save(task)).thenReturn(task);
        taskServiceImpl.updateStatus(taskIdS, taskRequest);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testUpdateStatus_AccessDenied() {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setStatus(StatusEnum.COMPLETED.toString());
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignHelperService.isUserInTask(userId, taskId)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            taskServiceImpl.updateStatus(taskIdS, null);
        });
        verify(taskRepository, never()).findById(anyLong());
    }

    @Test
    void testUpdateStatus_ResourceNotFound() {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setStatus(StatusEnum.COMPLETED.toString());
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignHelperService.isUserInTask(userId, taskId)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            taskServiceImpl.updateStatus(taskIdS, null);
        });
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testUpdatePriority_Success() {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setPriority(PriorityEnum.HIGH.toString());
        Task task = Task.builder().id(taskId).user(User.builder().id(userId).build())
                .dueAt(Timestamp.valueOf(LocalDateTime.now())).startDate(Timestamp.valueOf(LocalDateTime.now()))
                .createdAt(Timestamp.valueOf(LocalDateTime.now())).updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .status(StatusEnum.PENDING).priority(PriorityEnum.HIGH).build();
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignHelperService.isRoleUserInTask(userId, taskId, RoleEnum.ADMIN)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        taskServiceImpl.updatePriority(taskIdS, taskRequest);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testUpdatePriority_AccessDenied() {
        TaskRequest taskRequest = new TaskRequest();
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignHelperService.isRoleUserInTask(userId, taskId, RoleEnum.ADMIN)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            taskServiceImpl.updatePriority(taskIdS, taskRequest);
        });
        verify(taskRepository, never()).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testUpdatePriority_ResourceNotFound() {
        TaskRequest taskRequest = new TaskRequest();
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignHelperService.isRoleUserInTask(userId, taskId, RoleEnum.ADMIN)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            taskServiceImpl.updatePriority(taskIdS, taskRequest);
        });
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }
}
