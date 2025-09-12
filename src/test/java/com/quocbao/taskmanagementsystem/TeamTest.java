package com.quocbao.taskmanagementsystem;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.exception.DuplicateException;
import com.quocbao.taskmanagementsystem.exception.ForbiddenException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.TeamRequest;
import com.quocbao.taskmanagementsystem.payload.response.TeamResponse;
import com.quocbao.taskmanagementsystem.repository.TeamRepository;
import com.quocbao.taskmanagementsystem.repository.TeamRepository.TeamProjection;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.serviceimpl.TeamServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TeamTest {

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private TaskHelperService taskHelperService;

	@Mock
	private UserHelperService userHelperService;

	@Mock
	private AuthenticationService authService;

	@Mock
	private IdEncoder idEncoder;

	@Mock
	private MethodGeneral methodGeneral;

	@InjectMocks
	private TeamServiceImpl teamService;

	@BeforeEach
	void defaultData() {
		Long userId = 1L;
		when(authService.getUserIdInContext()).thenReturn(userId);
	}

	@Test
	void testCreateTeam_Success() {
		when(userHelperService.isUserExist(anyLong())).thenReturn(true);
		String teamName = "abc";
		Long teamId = 1L;
		Team team = Team.builder().id(teamId).leaderId(User.builder().id(1L).build()).name(teamName).build();
		TeamRequest teamRequest = new TeamRequest();
		teamRequest.setName(teamName);
		when(teamRepository.save(any(Team.class))).thenReturn(team);
		TeamResponse result = teamService.createTeam(teamRequest);
		assertEquals(teamName, result.getName());
		verify(teamRepository).save(any(Team.class));
	}

	@Test
	void testCreateTeam_UserNotFound() {
		when(userHelperService.isUserExist(1L)).thenReturn(false);
		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			teamService.createTeam(null);
		});
		assertEquals("User can not found", ex.getMessage());
		verify(teamRepository, never()).save(any());
	}

	@Test
	void testCreateTeam_TeamNameAlreadyExist() {
		String teamName = "abc";
		Long userId = 1L;
		when(userHelperService.isUserExist(userId)).thenReturn(true);
		when(teamRepository.exists(any(Specification.class))).thenReturn(true);
		DuplicateException ex = assertThrows(DuplicateException.class, () -> {
			TeamRequest teamRequest = new TeamRequest();
			teamRequest.setName(teamName);
			teamService.createTeam(teamRequest);
		});
		assertEquals("abc already exist", ex.getMessage());
		verify(teamRepository, never()).save(any());
	}

	@Test
	void testUpdateTeam_Success() {
		Team existingTeam = Team.builder().id(1L).leaderId(User.builder().id(1L).build()).name("abc").build();
		TeamRequest teamRequest = new TeamRequest();
		teamRequest.setName("abc2");
		when(teamRepository.findById(anyLong())).thenReturn(Optional.of(existingTeam));
		when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));
		TeamResponse teamResponse = teamService.updateTeam(idEncoder.encode(existingTeam.getId()), teamRequest);
		assertNotNull(teamResponse);
		assertEquals(teamResponse.getName(), "abc2");
		verify(teamRepository).findById(anyLong());
		verify(teamRepository).save(existingTeam);
	}

	@Test
	void testUpdateTeam_NotFound() {
		String teamId = "teamId";
		Long teamIdLong = 1L;
		when(idEncoder.decode(teamId)).thenReturn(teamIdLong);
		when(teamRepository.findById(teamIdLong)).thenReturn(Optional.empty());
		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			teamService.deleteTeam(teamId);
		});
		assertEquals("This team not found", ex.getMessage());
		verify(teamRepository).findById(teamIdLong);
	}

	@Test
	void testUpdateTeam_ThrowsForbiddenException() {
		Long teamId = 1L;
		String teamIdString = "2L";
		Long leaderId = 2L;
		Team team = Team.builder()
				.id(teamId)
				.name("Old Name")
				.leaderId(User.builder().id(leaderId).build())
				.build();
		when(idEncoder.decode(teamIdString)).thenReturn(1L);
		when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
		doThrow(new ForbiddenException("User does not have permission."))
				.when(methodGeneral).validatePermission(1L, leaderId);
		TeamRequest teamRequest = new TeamRequest();
		teamRequest.setName("abc2");
		ForbiddenException ex = assertThrows(ForbiddenException.class, () -> {
			teamService.updateTeam(teamIdString, teamRequest);
		});
		assertEquals("User does not have permission.", ex.getMessage());
		verify(teamRepository).findById(teamId);
		verify(teamRepository, never()).save(any());
	}

	@Test
	void testUpdateTeam_TeamNameAlreadyExist() {
		Long teamId = 1L;
		String teamIdString = "2L";
		Long leaderId = 2L;
		Team team = Team.builder()
				.id(teamId)
				.name("Old Name 1")
				.leaderId(User.builder().id(leaderId).build())
				.build();
		when(idEncoder.decode(teamIdString)).thenReturn(teamId);
		when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
		when(teamRepository.exists(any(Specification.class))).thenReturn(true);

		DuplicateException ex = assertThrows(DuplicateException.class, () -> {
			TeamRequest teamRequest = new TeamRequest();
			teamRequest.setName("Old Name");
			teamService.updateTeam(teamIdString, teamRequest);
		});
		assertEquals("Old Name already exist", ex.getMessage());
		verify(teamRepository, never()).save(any());
	}

	@Test
	void testDeleteTeam_Success() {
		String teamId = "teamId";
		Team team = Team.builder().id(1L).leaderId(User.builder().id(1L).build()).build();
		when(idEncoder.decode(teamId)).thenReturn(team.getId());
		when(taskHelperService.isTaskActive(anyLong(), anyString())).thenReturn(false);
		when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
		teamService.deleteTeam(teamId);
		verify(teamRepository).delete(team);
	}

	@Test
	void testDeleteTeam_ForbiddenException() {
		String teamId = "teamId";
		Team team = Team.builder().id(1L).leaderId(User.builder().id(2L).build()).build();
		when(idEncoder.decode(teamId)).thenReturn(team.getId());
		when(taskHelperService.isTaskActive(anyLong(), anyString())).thenReturn(false);
		when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
		doThrow(new ForbiddenException("User does not have permission."))
				.when(methodGeneral).validatePermission(team.getLeaderId().getId(), 1L);
		ForbiddenException ex = assertThrows(ForbiddenException.class, () -> {
			teamService.deleteTeam(teamId);
		});
		assertEquals("User does not have permission.", ex.getMessage());
		verify(teamRepository).findById(team.getId());
		verify(teamRepository, never()).deleteById(team.getId());
	}

	@Test
	void testDeleteTeam_AlreadyTaskActive() {
		String teamId = "teamId";
		Team team = Team.builder().id(1L).leaderId(User.builder().id(2L).build()).build();
		when(idEncoder.decode(teamId)).thenReturn(team.getId());
		when(taskHelperService.isTaskActive(anyLong(), anyString())).thenReturn(true);
		when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
		DuplicateException ex = assertThrows(DuplicateException.class, () -> {
			teamService.deleteTeam(teamId);
		});
		assertEquals("This team has not completed some tasks. Can not delete.", ex.getMessage());
		verify(teamRepository).findById(team.getId());
	}

	@Test
	void testRetriveTeams_NotNull() {
		Long userId = 1L;
		TeamProjection teamProjection = new TeamProjection() {

			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getName() {
				return "Team";
			}

			@Override
			public Timestamp getCreatedAt() {
				return Timestamp.valueOf(LocalDateTime.now());
			}

			@Override
			public Long getLeaderId() {
				return 1L;
			}

			@Override
			public String getFirstName() {
				return "John";
			}

			@Override
			public String getLastName() {
				return "Doe";
			}

			@Override
			public String getImage() {
				return null;
			}

		};
		when(teamRepository.getTeams(userId)).thenReturn(List.of(teamProjection));
		List<TeamResponse> teamResponses = teamService.getTeams();
		assertEquals(1, teamResponses.size());
	}
}
