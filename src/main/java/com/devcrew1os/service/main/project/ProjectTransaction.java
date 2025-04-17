package com.devcrew1os.service.main.project;

import com.devcrew1os.entity.main.challenge.ChallengeTodo;
import com.devcrew1os.entity.main.project.ProjectChallenge;
import com.devcrew1os.entity.main.project.ProjectInfo;
import com.devcrew1os.entity.main.project.ProjectTodo;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.repository.main.challenge.ChallengeInfoProjection;
import com.devcrew1os.repository.main.challenge.ChallengeInfoRepository;
import com.devcrew1os.repository.main.challenge.ChallengeTodoRepository;
import com.devcrew1os.repository.main.project.ProjectChallengeRepository;
import com.devcrew1os.repository.main.project.ProjectInfoRepository;
import com.devcrew1os.repository.main.project.ProjectTodoRepository;
import com.devcrew1os.repository.main.users.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectTransaction {

    private final UserInfoRepository userInfoRepo;

    private final ChallengeInfoRepository challengeInfoRepo;
    private final ChallengeTodoRepository challengeTodoRepo;

    private final ProjectInfoRepository projectInfoRepo;
    private final ProjectTodoRepository projectTodoRepo;
    private final ProjectChallengeRepository projectChallengeRepo;

    /*===========================
       전체 목표조회
    ===========================*/
    public UserInfo getUserInfo(String userId) {
        return userInfoRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User not found")
        );
    }

    public ProjectInfo getProjectInfo(String userId) {
        return projectInfoRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("Project not found")
        );
    }

    public List<ProjectChallenge> getProjectChallenges(int projectId) {
       return projectChallengeRepo.findAllByProjectId(projectId);
    }

    public List<ProjectTodo> getProjectTodos(int projectId) {
        return projectTodoRepo.findAllByProjectId(projectId);
    }

    public List<ChallengeInfoProjection> getChallengeList(List<Integer> challengeIds) {
        return challengeInfoRepo.findAllByIdIn(challengeIds);
    }

    public List<ChallengeTodo> getChallengeTodos(List<Integer> todoIds) {
        return challengeTodoRepo.findAllByIdIn(todoIds);
    }
}
