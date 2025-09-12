package com.quocbao.taskmanagementsystem.serviceimpl;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quocbao.taskmanagementsystem.common.ConvertData;
import com.quocbao.taskmanagementsystem.common.IdEncoder;
import com.quocbao.taskmanagementsystem.common.MethodGeneral;
import com.quocbao.taskmanagementsystem.common.StatusEnum;
import com.quocbao.taskmanagementsystem.entity.Team;
import com.quocbao.taskmanagementsystem.entity.User;
import com.quocbao.taskmanagementsystem.exception.DuplicateException;
import com.quocbao.taskmanagementsystem.exception.ResourceNotFoundException;
import com.quocbao.taskmanagementsystem.payload.request.TeamRequest;
import com.quocbao.taskmanagementsystem.payload.response.TeamResponse;
import com.quocbao.taskmanagementsystem.repository.TeamRepository;
import com.quocbao.taskmanagementsystem.service.TeamService;
import com.quocbao.taskmanagementsystem.service.utils.AuthenticationService;
import com.quocbao.taskmanagementsystem.service.utils.TaskHelperService;
import com.quocbao.taskmanagementsystem.service.utils.UserHelperService;
import com.quocbao.taskmanagementsystem.specifications.TeamSpecification;

@Service
public class TeamServiceImpl implements TeamService {

	private final TeamRepository teamRepository;

	private final UserHelperService userHelperService;

	private final TaskHelperService taskHelperService;

	private final AuthenticationService authService;

	private final MethodGeneral methodGeneral;

	private final IdEncoder idEncoder;

	public TeamServiceImpl(TeamRepository teamRepository, UserHelperService userHelperService,
			TaskHelperService taskHelperService, AuthenticationService authService,
			MethodGeneral methodGeneral, IdEncoder idEncoder) {
		this.teamRepository = teamRepository;
		this.userHelperService = userHelperService;
		this.taskHelperService = taskHelperService;
		this.authService = authService;
		this.methodGeneral = methodGeneral;
		this.idEncoder = idEncoder;
	}

	@Override
	public TeamResponse createTeam(TeamRequest teamRequest) {
		Long currentUserId = authService.getUserIdInContext();
		if (!userHelperService.isUserExist(currentUserId)) { // Check user exist
			throw new ResourceNotFoundException("User can not found");
		}
		isNameAlreadyExist(currentUserId, teamRequest.getName());
		User user = User.builder().id(currentUserId).build();
		Team team = teamRepository.save(Team.builder().leaderId(user).name(teamRequest.getName()).build());
		return new TeamResponse(idEncoder.encode(team.getId()), team.getName());
	}

	@Override
	public TeamResponse updateTeam(String teamId, TeamRequest teamRequest) {
		Long currentUserId = authService.getUserIdInContext();
		return teamRepository.findById(idEncoder.decode(teamId)).map(team -> {
			if (!team.getName().equals(teamRequest.getName())) {
				isNameAlreadyExist(currentUserId, teamRequest.getName());
			}
			// Check user is creator
			methodGeneral.validatePermission(currentUserId, team.getLeaderId().getId());
			team.setName(teamRequest.getName());
			teamRepository.save(team);
			return new TeamResponse(teamId, teamRequest.getName());
		}).orElseThrow(() -> new ResourceNotFoundException("Team can not found"));

	}

	@Override
	public void deleteTeam(String teamId) {
		Long currentUserId = authService.getUserIdInContext();
		Long teamIdLong = idEncoder.decode(teamId);
		teamRepository.findById(teamIdLong).ifPresentOrElse(team -> {
			if (taskHelperService.isTaskActive(teamIdLong, StatusEnum.COMPLETED.toString())) {
				throw new DuplicateException("This team has not completed some tasks. Can not delete.");
			}
			methodGeneral.validatePermission(team.getLeaderId().getId(), currentUserId);
			teamRepository.delete(team);
		}, () -> {
			throw new ResourceNotFoundException("This team not found");
		});
	}

	@Override
	public List<TeamResponse> getTeams() {
		Long currentUserId = authService.getUserIdInContext();
		return teamRepository.getTeams(currentUserId).stream().map(team -> {
			return new TeamResponse(idEncoder.encode(team.getId()), team.getName(),
					idEncoder.encode(team.getLeaderId()), team.getFirstName() + " " + team.getLastName(),
					team.getImage(), ConvertData.timeStampToString(team.getCreatedAt()));
		}).toList();
	}

	@Override
	public List<TeamResponse> getCustomTeams() {
		Long currentUserId = authService.getUserIdInContext();
		return teamRepository.getCustomTeams(currentUserId).stream().map(team -> {
			return new TeamResponse(idEncoder.encode(team.getId()), team.getName());
		}).toList();
	}

	@Override
	public Page<TeamResponse> searchTeams(String keyword, Pageable pageable) {
		Long currentUserId = authService.getUserIdInContext();
		return teamRepository.searchTeams(currentUserId, keyword, pageable).map(team -> {
			return new TeamResponse(idEncoder.encode(team.getId()), team.getName(),
					idEncoder.encode(team.getLeaderId()), team.getFirstName() + " " + team.getLastName(),
					team.getImage(), ConvertData.timeStampToString(team.getCreatedAt()));
		});
	}

	public void isNameAlreadyExist(Long userId, String name) {
		if (teamRepository.exists(Specification
				.where(TeamSpecification.getTeamByLeaderId(userId).and(TeamSpecification.getTeamByName(name))))) {
			throw new DuplicateException(name + " already exist");
		}
	}
}
