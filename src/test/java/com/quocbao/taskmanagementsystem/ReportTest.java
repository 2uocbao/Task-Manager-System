package com.quocbao.taskmanagementsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.StorageProperties;
import com.quocbao.taskmanagementsystem.common.TypeEnum;
import com.quocbao.taskmanagementsystem.entity.Report;
import com.quocbao.taskmanagementsystem.entity.Task;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Assign.HaveReportEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.ReportRequest;
import com.quocbao.taskmanagementsystem.repository.ReportRepository;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskAssignmentHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;
import com.quocbao.taskmanagementsystem.serviceimpl.ReportServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ReportTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private TaskHelperService taskHelperService;

    @Mock
    private TaskAssignmentHelperService taskAssignmentHelperService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private IdEncoder idEncoder;

    @Mock
    private StorageProperties storageProperties;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ReportServiceImpl reportServiceImpl;

    Long userId = 1L;
    String taskIdS = "taskId";
    Long taskId = 1L;

    @BeforeEach
    void defaultData() {
        when(authenticationService.getUserIdInContext()).thenReturn(userId);
    }

    @Test
    void testAddFile_Success() throws IOException {
        User user = User.builder().id(userId).build();
        Task task = Task.builder().id(taskId).build();
        storageProperties.setLocation("app/storeFile");

        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(storageProperties.getLocation()).thenReturn("app/storeFile");
        MockMultipartFile file = new MockMultipartFile("file", "text.txt", "text/plain", "Hello world".getBytes());

        Path filePath = Path.of(storageProperties.getLocation(), taskIdS + file.getOriginalFilename());

        Files.createDirectories(filePath.getParent());
        file.transferTo(filePath.toFile());
        Report report = Report.builder().id(1L).user(user).task(task).fileName(file.getOriginalFilename())
                .filePath(filePath.toString()).type(TypeEnum.FILE).createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        when(taskAssignmentHelperService.isUserInTask(userId, taskId)).thenReturn(true);
        when(taskHelperService.isTaskExist(taskId)).thenReturn(true);

        when(reportRepository.save(any(Report.class))).thenReturn(report);
        reportServiceImpl.createReportForFile(file, taskIdS);
        ArgumentCaptor<HaveReportEvent> captor = ArgumentCaptor.forClass(HaveReportEvent.class);
        verify(reportRepository, times(1)).save(any(Report.class));
        verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());
        HaveReportEvent event = captor.getValue();
        assertEquals(taskId, event.getTaskId());
        assertEquals(userId, event.getUserId());
    }

    @Test
    void testAccessDenied() {
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        storageProperties.setLocation("app/storeFile");
        MockMultipartFile file = new MockMultipartFile("file", "text.txt", "text/plain", "Hello world".getBytes());

        when(taskAssignmentHelperService.isUserInTask(userId, taskId)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            reportServiceImpl.createReportForFile(file, taskIdS);
        });

        verify(reportRepository, never()).save(any(Report.class));
        verify(applicationEventPublisher, never()).publishEvent(any(HaveReportEvent.class));
    }

    @Test
    void testAddURL_Success() {
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setExternalUrl("taskIdS");
        reportRequest.setTaskId(taskIdS);
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignmentHelperService.isUserInTask(userId, taskId)).thenReturn(true);
        when(taskHelperService.isTaskExist(taskId)).thenReturn(true);
        Report report = Report.builder().id(1L).user(User.builder().id(userId).build())
                .task(Task.builder().id(taskId).build())
                .externalUrl(reportRequest.getExternalUrl()).type(TypeEnum.URL)
                .createdAt(Timestamp.valueOf(LocalDateTime.now())).build();
        when(reportRepository.save(any(Report.class))).thenReturn(report);
        reportServiceImpl.createReportForLink(reportRequest);
        verify(reportRepository, times(1)).save(any(Report.class));
        ArgumentCaptor<HaveReportEvent> captor = ArgumentCaptor.forClass(HaveReportEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());
        HaveReportEvent event = captor.getValue();
        assertEquals(taskId, event.getTaskId());
        assertEquals(userId, event.getUserId());
    }

    @Test
    void testAddURL_AccessDenied() {
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setExternalUrl("taskIdS");
        reportRequest.setTaskId(taskIdS);
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignmentHelperService.isUserInTask(userId, taskId)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            reportServiceImpl.createReportForLink(reportRequest);
        });
        verify(taskHelperService, never()).isTaskExist(taskId);
        verify(reportRepository, never()).save(any(Report.class));
        verify(applicationEventPublisher, never()).publishEvent(any(HaveReportEvent.class));
    }

    @Test
    void testAddURL_ResourceNotFound() {
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setExternalUrl("taskIdS");
        reportRequest.setTaskId(taskIdS);
        when(idEncoder.decode(taskIdS)).thenReturn(taskId);
        when(taskAssignmentHelperService.isUserInTask(userId, taskId)).thenReturn(true);
        when(taskHelperService.isTaskExist(taskId)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> {
            reportServiceImpl.createReportForLink(reportRequest);
        });
        verify(reportRepository, never()).save(any(Report.class));
        verify(applicationEventPublisher, never()).publishEvent(any(HaveReportEvent.class));
    }

    @Test
    void testDelete_Success() throws IOException {
        String reportIdS = "reportId";
        Long reportIdL = 1L;
        storageProperties.setLocation("app/storeFile");

        when(storageProperties.getLocation()).thenReturn("app/storeFile");
        MockMultipartFile file = new MockMultipartFile("file", "text.txt", "text/plain", "Hello world".getBytes());

        Path filePath = Path.of(storageProperties.getLocation(), taskIdS + file.getOriginalFilename());

        Files.createDirectories(filePath.getParent());
        file.transferTo(filePath.toFile());
        Report report = Report.builder().id(reportIdL).user(User.builder().id(userId).build())
                .task(Task.builder().id(taskId).build()).type(TypeEnum.URL).fileName(file.getOriginalFilename())
                .filePath(filePath.toString())
                .createdAt(Timestamp.valueOf(LocalDateTime.now())).build();
        when(idEncoder.decode(reportIdS)).thenReturn(reportIdL);
        when(taskHelperService.isCreatorTask(userId, taskId)).thenReturn(true);
        when(reportRepository.findById(reportIdL)).thenReturn(Optional.of(report));
        reportServiceImpl.deleteReport(reportIdS);
        verify(reportRepository, times(1)).findById(reportIdL);
        verify(reportRepository, times(1)).delete(report);
    }

    @Test
    void testDelete_AccessDenied() throws IOException {
        String reportIdS = "reportId";
        Long reportIdL = 1L;
        storageProperties.setLocation("app/storeFile");

        when(storageProperties.getLocation()).thenReturn("app/storeFile");
        MockMultipartFile file = new MockMultipartFile("file", "text.txt", "text/plain", "Hello world".getBytes());

        Path filePath = Path.of(storageProperties.getLocation(), taskIdS + file.getOriginalFilename());

        Files.createDirectories(filePath.getParent());
        file.transferTo(filePath.toFile());
        Report report = Report.builder().id(reportIdL).user(User.builder().id(userId).build())
                .task(Task.builder().id(taskId).build()).type(TypeEnum.URL).fileName(file.getOriginalFilename())
                .filePath(filePath.toString())
                .createdAt(Timestamp.valueOf(LocalDateTime.now())).build();
        when(idEncoder.decode(reportIdS)).thenReturn(reportIdL);
        when(taskHelperService.isCreatorTask(userId, taskId)).thenReturn(false);
        when(reportRepository.findById(reportIdL)).thenReturn(Optional.of(report));
        assertThrows(AccessDeniedException.class, () -> {
            reportServiceImpl.deleteReport(reportIdS);
        });
        verify(reportRepository, times(1)).findById(reportIdL);
        verify(reportRepository, never()).delete(report);
    }

    @Test
    void testDelete_ResourceNotFound() throws IOException {
        String reportIdS = "reportId";
        Long reportIdL = 1L;
        storageProperties.setLocation("app/storeFile");

        when(storageProperties.getLocation()).thenReturn("app/storeFile");
        MockMultipartFile file = new MockMultipartFile("file", "text.txt", "text/plain", "Hello world".getBytes());

        Path filePath = Path.of(storageProperties.getLocation(), taskIdS + file.getOriginalFilename());

        Files.createDirectories(filePath.getParent());
        file.transferTo(filePath.toFile());
        Report report = Report.builder().id(reportIdL).user(User.builder().id(userId).build())
                .task(Task.builder().id(taskId).build()).type(TypeEnum.URL).fileName(file.getOriginalFilename())
                .filePath(filePath.toString())
                .createdAt(Timestamp.valueOf(LocalDateTime.now())).build();
        when(idEncoder.decode(reportIdS)).thenReturn(reportIdL);
        when(reportRepository.findById(reportIdL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            reportServiceImpl.deleteReport(reportIdS);
        });
        verify(reportRepository, times(1)).findById(reportIdL);
        verify(reportRepository, never()).delete(report);
    }

    @Test
    void testRetrieve_AccessDenied() {
        String reportIdS = "reportId";
        Long reportId = 1L;
        when(idEncoder.decode(reportIdS)).thenReturn(reportId);
        when(taskAssignmentHelperService.isUserInTask(taskId, userId)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> {
            reportServiceImpl.getFile(reportIdS, reportIdS);
        });
        verify(reportRepository, never()).findById(reportId);
    }

    @Test
    void testRetrieve_ResourceNotFound() {
        String reportIdS = "reportId";
        Long reportId = 1L;
        when(idEncoder.decode(reportIdS)).thenReturn(reportId);
        when(taskAssignmentHelperService.isUserInTask(taskId, userId)).thenReturn(true);
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            reportServiceImpl.getFile(reportIdS, reportIdS);
        });
        verify(taskAssignmentHelperService, times(1)).isUserInTask(taskId, userId);
        verify(reportRepository, times(1)).findById(reportId);
    }
}
