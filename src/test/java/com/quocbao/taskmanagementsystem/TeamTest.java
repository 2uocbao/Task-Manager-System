package com.quocbao.taskmanagementsystem;

import static org.mockito.Mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.serviceimpl.TeamServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TeamTest {

	@Mock
	private TeamRepository teamRepository;

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

	@Test
	void testCreateTeam_Success() {
		Long userId = 1L;
		User user = User.builder().id(userId).build();

		when(authService.getUserIdInContext()).thenReturn(userId);

		when(userHelperService.isUserExist(userId)).thenReturn(true);

		String teamName = "abc";
		Long teamId = 1L;
		Team team = Team.builder().id(teamId).leaderId(user).name(teamName).build();

		TeamRequest teamRequest = new TeamRequest();
		teamRequest.setName(teamName);

		when(teamRepository.save(any(Team.class))).thenReturn(team);

		TeamResponse result = teamService.createTeam(teamRequest);

		assertNotNull(result);

		assertEquals(teamName, result.getName());

		verify(teamRepository).save(any(Team.class));
	}

	@Test
	void testCreateTeam_UserNotFound() {
		Long userId = 1L;

		when(authService.getUserIdInContext()).thenReturn(userId);

		when(userHelperService.isUserExist(userId)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> {
			teamService.createTeam(null);
		});
		verify(teamRepository, never()).save(any());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testCreateTeam_TeamNameAlreadyExist() {
		String teamName = "abc";
		Long userId = 1L;
		when(authService.getUserIdInContext()).thenReturn(userId);
		when(userHelperService.isUserExist(userId)).thenReturn(true);
		when(teamRepository.exists(any(Specification.class))).thenReturn(true);
		assertThrows(DuplicateException.class, () -> {
			TeamRequest teamRequest = new TeamRequest();
			teamRequest.setName(teamName);
			teamService.createTeam(teamRequest);
		});
		verify(teamRepository, never()).save(any());
	}

	@Test
	void testUpdateTeam_Success() {
		Long userId = 1L;
		User user = User.builder().id(userId).build();
		Team existingTeam = Team.builder().id(1L).leaderId(user).name("abc").build();
		TeamRequest teamRequest = new TeamRequest();
		teamRequest.setName("abc2");
		when(teamRepository.findById(any(Long.class))).thenReturn(Optional.of(existingTeam));
		when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

		TeamResponse teamResponse = teamService.updateTeam(idEncoder.encode(existingTeam.getId()), teamRequest);

		assertNotNull(teamResponse);

		assertEquals(teamResponse.getName(), "abc2");

		verify(teamRepository).save(existingTeam);
	}

	@Test
	void testUpdateTeam_WhenUserIsNotLeader_ThrowsForbiddenException() {
		Long teamId = 1L;
		String encodedId = "abc123";
		Long currentUserId = 99L;
		Long leaderId = 1L;

		TeamRequest request = new TeamRequest();
		request.setName("New Name");

		Team team = Team.builder()
				.id(teamId)
				.name("Old Name")
				.leaderId(User.builder().id(leaderId).build())
				.build();

		when(authService.getUserIdInContext()).thenReturn(currentUserId);
		when(idEncoder.decode(encodedId)).thenReturn(teamId);
		when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
		doThrow(new ForbiddenException("No permission"))
				.when(methodGeneral).validatePermission(currentUserId, leaderId);

		assertThrows(ForbiddenException.class, () -> {
			teamService.updateTeam(encodedId, request);
		});

		verify(teamRepository, never()).save(any());
	}
}
