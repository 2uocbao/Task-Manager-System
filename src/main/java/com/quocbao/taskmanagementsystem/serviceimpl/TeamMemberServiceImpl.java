package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.common.RoleEnum;
import com.quocbao.taskmanagementsystem.entity.TeamMember;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.Notification.NotificationAddEvent;
import com.quocbao.taskmanagementsystem.exception.AccessDeniedException;
import com.quocbao.taskmanagementsystem.exception.DuplicateException;
import com.quocbao.taskmanagementsystem.exception.ForbiddenException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.TeamMemberRequest;
import com.quocbao.taskmanagementsystem.payload.response.TeamMemberResponse;
import com.quocbao.taskmanagementsystem.repository.TeamMemberRepository;
import com.quocbao.taskmanagementsystem.service.TeamMemberService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.ContactHelperService;
import com.quocbao.taskmanagementsystem.service.utils.TaskAssignmentHelperService;
import com.quocbao.taskmanagementsystem.specifications.TeamMemberSpecification;

@Service
public class TeamMemberServiceImpl implements TeamMemberService {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final TeamMemberRepository teamMemberRepository;

	private final ContactHelperService contactHelperService;

	private final TaskAssignmentHelperService taskAssignmentHelperService;

	private final AuthenticationService authService;

	private final IdEncoder idEncoder;

	public TeamMemberServiceImpl(ApplicationEventPublisher applicationEventPublisher,
			TeamMemberRepository teamMemberRepository, ContactHelperService contactHelperService,

			TaskAssignmentHelperService taskAssignmentHelperService, AuthenticationService authService,
			IdEncoder idEncoder) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.teamMemberRepository = teamMemberRepository;
		this.contactHelperService = contactHelperService;
		this.taskAssignmentHelperService = taskAssignmentHelperService;
		this.authService = authService;
		this.idEncoder = idEncoder;
	}

	@Override
	public TeamMemberResponse createTeamMember(String teamId, TeamMemberRequest teamMemberRequest) {

		Long currentUserId = authService.getUserIdInContext();
		Long teamIdLong = idEncoder.decode(teamId);
		Long userMemberIdLong = idEncoder.decode(teamMemberRequest.getMemberId());

		if (!isLeaderOfTeam(currentUserId, teamIdLong, RoleEnum.ADMIN)) {
			throw new AccessDeniedException("User do not have permission");
		}

		if (!contactHelperService.isConnected(currentUserId, userMemberIdLong)) {
			throw new ResourceNotFoundException("Can not add user to team");
		}

		if (alreadyExistUserInTeam(teamIdLong, userMemberIdLong)) {
			throw new DuplicateException("User already exist in team");
		}

		User user = User.builder().id(userMemberIdLong).build();
		Team team = Team.builder().id(teamIdLong).build();

		TeamMember teamMember = teamMemberRepository
				.save(TeamMember.builder().user(user).team(team).role(RoleEnum.MEMBER).build());

		sendNotification(currentUserId, userMemberIdLong, teamIdLong, NotificationType.ADD_MEMBER.toString());

		return new TeamMemberResponse(idEncoder.encode(teamMember.getId()), teamMemberRequest.getMemberId(), null,
				null, ConvertData.timeStampToString(teamMember.getJoinedAt()));
	}

	@Override
	public void deleteTeamMember(String teamId, String teamMemberId) {
		Long currentUserId = authService.getUserIdInContext();
		Long teamIdLong = idEncoder.decode(teamId);
		Long teamMemberIdLong = idEncoder.decode(teamMemberId);
		if (!isLeaderOfTeam(currentUserId, teamIdLong, RoleEnum.ADMIN)) {
			throw new AccessDeniedException("You can not do this");
		}
		teamMemberRepository.findById(teamMemberIdLong).ifPresentOrElse(teamMember -> {
			if (teamMember.getUser().getId().equals(currentUserId)) {
				throw new ForbiddenException("Could not delete");
			}
			teamMemberRepository.deleteById(teamMemberIdLong);
			// Push the event delete notification related to this user.
			sendNotification(currentUserId, teamMember.getUser().getId(), teamIdLong,
					NotificationType.REMOVE_MEMBER.toString());
		}, () -> {
			throw new ResourceNotFoundException("Team member not found");
		});
	}

	@Override
	public Page<TeamMemberResponse> getTeamMembers(String teamId, Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		Long teamIdLong = idEncoder.decode(teamId);
		if (!alreadyExistUserInTeam(teamIdLong, currentUserId)) {
			throw new AccessDeniedException("User do not have permission");
		}
		return teamMemberRepository.getTeamMembers(teamIdLong, pageable).map(teamMember -> {
			String userName = teamMember.getFirstName() + " " + teamMember.getLastName();
			String image = teamMember.getImage();
			return new TeamMemberResponse(idEncoder.encode(teamMember.getId()),
					idEncoder.encode(teamMember.getUserId()), userName, image,
					ConvertData.timeStampToString(teamMember.getJoinedAt()));
		});
	}

	@Override
	public Page<TeamMemberResponse> searchTeamMembers(String teamId, String keyword, Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		Long teamIdLong = idEncoder.decode(teamId);
		if (!alreadyExistUserInTeam(teamIdLong, currentUserId)) {
			throw new AccessDeniedException("User do not have permission");
		}
		List<TeamMemberResponse> teamMemberResponse = new ArrayList<>();

		teamMemberRepository.searchTeamMembers(teamIdLong, keyword, pageable).getContent().stream()
				.forEach(teamMember -> {
					if (!teamMember.getUserId().equals(currentUserId)) {
						String userName = teamMember.getFirstName() + " " + teamMember.getLastName();
						String image = teamMember.getImage();
						teamMemberResponse.add(
								new TeamMemberResponse(teamId, idEncoder.encode(teamMember.getUserId()), userName,
										image,
										ConvertData.timeStampToString(teamMember.getJoinedAt())));
					}
				});
		Page<TeamMemberResponse> teamMemberResponsePage = new PageImpl<>(teamMemberResponse, pageable,
				teamMemberResponse.size());
		return teamMemberResponsePage;
	}

	@Override
	public void leaveTeam(String teamId) {
		Long currentUserId = authService.getUserIdInContext();
		Long teamIdLong = idEncoder.decode(teamId);

		if (isLeaderOfTeam(currentUserId, teamIdLong, RoleEnum.ADMIN)) {
			throw new ForbiddenException("You can not do this");
		}

		if (!alreadyExistUserInTeam(teamIdLong, currentUserId)) {
			// Is the user in the team
			throw new ResourceNotFoundException("You was leaved this team");
		}
		if (taskAssignmentHelperService.isInAnyTask(currentUserId)) {
			// Has the task not been completed
			throw new AccessDeniedException("Can not leave this team. You need to leave the task where you joined.");
		}
		Long result = teamMemberRepository
				.delete(Specification.where(TeamMemberSpecification.getTeamMemberByUserId(currentUserId)
						.and(TeamMemberSpecification.getTeamMemberByTeamId(teamIdLong))));
		if (result == 1) {
			// Send the notification to the leader of this team.
			sendNotification(currentUserId, teamIdLong, teamIdLong, NotificationType.LEAVE_MEMBER.toString());
		}
	}

	@Override
	public void addLeaderTeam(Long userId, Long teamId) {
		TeamMember teamMember = TeamMember.builder().user(User.builder().id(userId).build())
				.team(Team.builder().id(teamId).build()).role(RoleEnum.ADMIN).build();
		teamMemberRepository.save(teamMember);
	}

	protected Boolean isLeaderOfTeam(Long userId, Long teamId, RoleEnum role) {
		return teamMemberRepository.exists(Specification.where(TeamMemberSpecification.getTeamMemberByTeamId(teamId)
				.and(TeamMemberSpecification.getTeamMemberByUserId(userId))
				.and(TeamMemberSpecification.getTeamMemberByRole(role))));
	}

	protected Boolean alreadyExistUserInTeam(Long teamId, Long userId) {
		return teamMemberRepository
				.exists(Specification.where(TeamMemberSpecification.getTeamMemberByTeamId(teamId)
						.and(TeamMemberSpecification.getTeamMemberByUserId(userId))));
	}

	protected void sendNotification(Long userId, Long receiverId, Long teamId,
			String contentType) {
		applicationEventPublisher
				.publishEvent(new NotificationAddEvent(userId, receiverId, teamId, NotificationType.TEAM.toString(),
						contentType));
	}
}
