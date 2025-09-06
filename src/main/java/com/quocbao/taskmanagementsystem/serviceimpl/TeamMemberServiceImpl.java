package com.quocbao.taskmanagementsystem.serviceimpl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.NotificationType;
import com.quocbao.taskmanagementsystem.entity.TeamMember;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.events.DeleteEvent.NotifiLeavedEvent;
import com.quocbao.taskmanagementsystem.events.NotifiEvent.TeamMemberEvent;
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
import com.quocbao.taskmanagementsystem.service.utils.TeamHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.specifications.TeamMemberSpecification;

import jakarta.transaction.Transactional;

@Service
public class TeamMemberServiceImpl implements TeamMemberService {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final TeamMemberRepository teamMemberRepository;

	private final ContactHelperService contactHelperService;

	private final UserHelperService userHelperService;

	private final TeamHelperService teamHelperService;

	private final TaskAssignmentHelperService taskAssignmentHelperService;

	private final AuthenticationService authService;

	private final IdEncoder idEncoder;

	public TeamMemberServiceImpl(ApplicationEventPublisher applicationEventPublisher,
			TeamMemberRepository teamMemberRepository, ContactHelperService contactHelperService,
			UserHelperService userHelperService, TeamHelperService teamHelperService,
			TaskAssignmentHelperService taskAssignmentHelperService, AuthenticationService authService,
			IdEncoder idEncoder) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.teamMemberRepository = teamMemberRepository;
		this.contactHelperService = contactHelperService;
		this.userHelperService = userHelperService;
		this.teamHelperService = teamHelperService;
		this.taskAssignmentHelperService = taskAssignmentHelperService;
		this.authService = authService;
		this.idEncoder = idEncoder;
	}

	@Override
	public TeamMemberResponse createTeamMember(String teamId, TeamMemberRequest teamMemberRequest) {

		Long currentUserId = authService.getUserIdInContext();

		if (!isLeaderOfTeam(currentUserId, teamId)) {
			throw new ForbiddenException("User do not have permission");
		}

		if (!contactHelperService.isConnected(currentUserId, teamMemberRequest.getMemberId())) {
			throw new ResourceNotFoundException("Can not add user to team");
		}

		if (alreadyExistUserInTeam(teamId, idEncoder.decode(teamMemberRequest.getMemberId()))) {
			throw new DuplicateException("User already exist in team");
		}

		// Retrieve info user is new member
		Long memberId = idEncoder.decode(teamMemberRequest.getMemberId());
		User user = userHelperService.getUser(memberId).get();
		Team team = Team.builder().id(idEncoder.decode(teamId)).build();

		TeamMember teamMember = teamMemberRepository.save(TeamMember.builder().user(user).team(team).build());

		String userName = user.getFirstName() + " " + user.getLastName();
		String image = teamMember.getUser().getImage();

		sendNotification(currentUserId, user.getId(), teamId, teamMemberRequest.getTeamName(),
				teamMemberRequest.getLeaderName(), NotificationType.ADD_MEMBER.toString(),
				user.getToken() == null ? null : user.getToken(), user.getLanguage());

		return new TeamMemberResponse(idEncoder.encode(teamMember.getId()), teamMemberRequest.getMemberId(), userName,
				image, ConvertData.timeStampToString(teamMember.getJoinedAt()));
	}

	@Override
	@Transactional
	public void deleteTeamMember(String teamId, String teamMemberId, TeamMemberRequest teamMemberRequest) {
		Long currentUserId = authService.getUserIdInContext();
		if (!isLeaderOfTeam(currentUserId, teamId)) {
			throw new ForbiddenException("User do not have permission");
		}

		teamMemberRepository.findById(idEncoder.decode(teamMemberId)).ifPresentOrElse(teamMember -> {
			if (teamMember.getUser().getId() == currentUserId) {
				throw new ResourceNotFoundException("Could not delete");
			}
			// Push the event delete notification related to this user.
			applicationEventPublisher.publishEvent(
					new NotifiLeavedEvent(teamMember.getUser().getId(), NotificationType.TEAM.toString()));
			sendNotification(currentUserId, teamMember.getUser().getId(), teamId, teamMemberRequest.getTeamName(),
					teamMemberRequest.getLeaderName(), NotificationType.REMOVE_MEMBER.toString(),
					teamMember.getUser().getToken() == null ? null : teamMember.getUser().getToken(),
					teamMember.getUser().getLanguage());
			teamMemberRepository.deleteById(idEncoder.decode(teamMemberId));
		}, () -> new ResourceNotFoundException("Team member not found"));
	}

	@Override
	public Page<TeamMemberResponse> getTeamMembers(String teamId, Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		if (!isLeaderOfTeam(currentUserId, teamId) && !alreadyExistUserInTeam(teamId, currentUserId)) {
			throw new ForbiddenException("User do not have permission");
		}
		return teamMemberRepository.getTeamMembers(idEncoder.decode(teamId), pageable).map(teamMember -> {
			String userName = teamMember.getFirstName() + " " + teamMember.getLastName();
			String image = teamMember.getImage();
			return new TeamMemberResponse(idEncoder.encode(teamMember.getId()),
					idEncoder.encode(teamMember.getUserId()), userName, image,
					ConvertData.timeStampToString(teamMember.getJoinedAt()));
		});
	}

	@Override
	public Page<TeamMemberResponse> searchTeamMembers(String teamId, String keyword, Pageable pageable) {
		return teamMemberRepository.searchTeamMembers(idEncoder.decode(teamId), keyword, pageable).map(teamMember -> {
			String userName = teamMember.getFirstName() + " " + teamMember.getLastName();
			String image = teamMember.getImage();
			return new TeamMemberResponse(teamId, idEncoder.encode(teamMember.getUserId()), userName, image,
					ConvertData.timeStampToString(teamMember.getJoinedAt()));
		});
	}

	@Override
	@Transactional
	public void leaveTeam(String teamId, TeamMemberRequest teamMemberRequest) {
		Long currentUserId = authService.getUserIdInContext();
		Long teamIdLong = idEncoder.decode(teamId);
		Boolean isMemberInTeam = teamMemberRepository.exists(Specification.where(TeamMemberSpecification
				.getTeamMemberByUserId(currentUserId).and(TeamMemberSpecification.getTeamMemberByTeamId(teamIdLong))));
		if (!isMemberInTeam) {
			// Is the user in the team
			throw new ResourceNotFoundException("You was leaved this team");
		}
		if (taskAssignmentHelperService.isInAnyTask(currentUserId)) {
			// Has the task not been completed
			throw new DuplicateException("Can not leave this team. You need to leave the task where you joined.");
		}
		// Fetch the leader of this team.
		User leader = teamHelperService.getTeamById(teamId).getLeaderId();

		// Push the event delete notification related to this user.
		applicationEventPublisher.publishEvent(new NotifiLeavedEvent(currentUserId, NotificationType.TEAM.toString()));
		// Send the notification to the leader of this team.
		sendNotification(currentUserId, leader.getId(), teamId, teamMemberRequest.getTeamName(),
				teamMemberRequest.getLeaderName(), NotificationType.LEAVE_MEMBER.toString(),
				leader.getToken() == null ? null : leader.getToken(), leader.getLanguage());
		teamMemberRepository.delete(Specification.where(TeamMemberSpecification.getTeamMemberByUserId(currentUserId)
				.and(TeamMemberSpecification.getTeamMemberByTeamId(teamIdLong))));
	}

	protected Boolean isLeaderOfTeam(Long userId, String teamId) {
		if (teamHelperService.isLeaderOfTeam(userId, teamId)) {
			return true;
		}
		return false;
	}

	protected Boolean alreadyExistUserInTeam(String teamId, Long userId) {
		if (teamMemberRepository
				.exists(Specification.where(TeamMemberSpecification.getTeamMemberByTeamId(idEncoder.decode(teamId))
						.and(TeamMemberSpecification.getTeamMemberByUserId(userId))))) {
			return true;
		}
		return false;
	}

	protected void sendNotification(Long userId, Long receiverId, String teamId, String teamName, String leaderName,
			String contentType, String token, String language) {
		applicationEventPublisher.publishEvent(new TeamMemberEvent(userId, receiverId, idEncoder.decode(teamId),
				leaderName, teamName, contentType, token, language));
	}
}
