package com.gym.service.controller;

import com.gym.service.entity.Team;
import com.gym.service.entity.TeamMember;
import com.gym.service.entity.User;
import com.gym.service.repository.TeamMemberRepository;
import com.gym.service.repository.TeamRepository;
import com.gym.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private UserRepository userRepository;

    private Optional<User> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return Optional.empty();
        }
        return userRepository.findByUsername(auth.getName());
    }

    private static int parseInt(Object raw, int fallback) {
        if (raw == null) return fallback;
        if (raw instanceof Number) return ((Number) raw).intValue();
        try {
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private Map<String, Object> toTeamMap(Team team, Long currentUserId) {
        long realMemberCount = teamMemberRepository.countByTeamId(team.getId());
        int currentMembers = (int) realMemberCount;
        if (team.getCurrentMembers() == null || team.getCurrentMembers() != currentMembers) {
            team.setCurrentMembers(currentMembers);
            teamRepository.save(team);
        }

        Map<String, Object> teamMap = new HashMap<>();
        teamMap.put("id", team.getId());
        teamMap.put("title", team.getTitle());
        teamMap.put("description", team.getDescription());
        teamMap.put("category", team.getCategory());
        teamMap.put("creatorId", team.getCreatorId());
        teamMap.put("creatorName", team.getCreatorName());
        teamMap.put("maxMembers", team.getMaxMembers());
        teamMap.put("currentMembers", currentMembers);
        teamMap.put("location", team.getLocation());
        teamMap.put("meetTime", team.getMeetTime());
        teamMap.put("createdAt", team.getCreatedAt());
        teamMap.put("active", team.getActive());

        boolean joined = false;
        boolean createdByCurrentUser = false;
        if (currentUserId != null) {
            joined = teamMemberRepository.findByTeamIdAndUserId(team.getId(), currentUserId).isPresent();
            createdByCurrentUser = currentUserId.equals(team.getCreatorId());
        }
        teamMap.put("joined", joined);
        teamMap.put("createdByCurrentUser", createdByCurrentUser);
        return teamMap;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTeams(
            @RequestParam(required = false) String category) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long currentUserId = currentUser().map(User::getId).orElse(null);

            List<Team> teams;
            if (category != null && !category.trim().isEmpty()) {
                teams = teamRepository.findByCategoryAndActiveTrueOrderByCreatedAtDesc(category.trim());
            } else {
                teams = teamRepository.findByActiveTrueOrderByCreatedAtDesc();
            }

            List<Map<String, Object>> teamData = new java.util.ArrayList<>();
            for (Team team : teams) {
                teamData.add(toTeamMap(team, currentUserId));
            }

            response.put("success", true);
            response.put("data", teamData);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting teams: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTeamById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Team team = teamRepository.findById(id).orElse(null);
            if (team != null && Boolean.TRUE.equals(team.getActive())) {
                Long currentUserId = currentUser().map(User::getId).orElse(null);
                response.put("success", true);
                response.put("data", toTeamMap(team, currentUserId));
                List<TeamMember> members = teamMemberRepository.findByTeamId(id);
                response.put("members", members);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "组队不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("Error getting team: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> createTeam(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = currentUser();
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "LOGIN_REQUIRED");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            User user = userOpt.get();

            String title = request.get("title") == null ? "" : String.valueOf(request.get("title")).trim();
            String description = request.get("description") == null ? "" : String.valueOf(request.get("description")).trim();
            if (title.isEmpty() || description.isEmpty()) {
                response.put("success", false);
                response.put("message", "TEAM_INFO_REQUIRED");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            int maxMembers = parseInt(request.get("maxMembers"), 10);
            if (maxMembers < 2) maxMembers = 2;
            if (maxMembers > 100) maxMembers = 100;

            Team team = new Team();
            team.setTitle(title);
            team.setDescription(description);
            team.setCategory(request.get("category") == null ? "other" : String.valueOf(request.get("category")).trim());
            team.setCreatorId(user.getId());
            team.setCreatorName(user.getUsername());
            team.setMaxMembers(maxMembers);
            team.setCurrentMembers(1);
            team.setLocation(request.get("location") == null ? "" : String.valueOf(request.get("location")).trim());
            team.setMeetTime(request.get("meetTime") == null ? "" : String.valueOf(request.get("meetTime")).trim());
            team.setCreatedAt(LocalDateTime.now());
            team.setActive(true);
            team = teamRepository.save(team);

            TeamMember member = new TeamMember();
            member.setTeamId(team.getId());
            member.setUserId(user.getId());
            member.setUsername(user.getUsername());
            member.setJoinedAt(LocalDateTime.now());
            teamMemberRepository.save(member);

            response.put("success", true);
            response.put("data", toTeamMap(team, user.getId()));
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error creating team: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/join")
    @Transactional
    public ResponseEntity<Map<String, Object>> joinTeam(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = currentUser();
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "LOGIN_REQUIRED");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            User user = userOpt.get();

            Team team = teamRepository.findById(id).orElse(null);
            if (team == null || !Boolean.TRUE.equals(team.getActive())) {
                response.put("success", false);
                response.put("message", "组队不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            if (teamMemberRepository.findByTeamIdAndUserId(id, user.getId()).isPresent()) {
                response.put("success", false);
                response.put("message", "已经加入该组队");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            int currentMembers = (int) teamMemberRepository.countByTeamId(id);
            if (currentMembers >= team.getMaxMembers()) {
                response.put("success", false);
                response.put("message", "组队人数已满");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            TeamMember member = new TeamMember();
            member.setTeamId(id);
            member.setUserId(user.getId());
            member.setUsername(user.getUsername());
            member.setJoinedAt(LocalDateTime.now());
            teamMemberRepository.save(member);

            team.setCurrentMembers((int) teamMemberRepository.countByTeamId(id));
            teamRepository.save(team);

            response.put("success", true);
            response.put("message", "成功加入组队");
            response.put("data", toTeamMap(team, user.getId()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error joining team: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/leave")
    @Transactional
    public ResponseEntity<Map<String, Object>> leaveTeam(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = currentUser();
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "LOGIN_REQUIRED");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            User user = userOpt.get();

            Team team = teamRepository.findById(id).orElse(null);
            if (team == null || !Boolean.TRUE.equals(team.getActive())) {
                response.put("success", false);
                response.put("message", "组队不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            TeamMember member = teamMemberRepository.findByTeamIdAndUserId(id, user.getId()).orElse(null);
            if (member == null) {
                response.put("success", false);
                response.put("message", "未加入该组队");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (team.getCreatorId() != null && team.getCreatorId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "创建者不能退出自己创建的组队");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            teamMemberRepository.delete(member);
            team.setCurrentMembers((int) teamMemberRepository.countByTeamId(id));
            teamRepository.save(team);

            response.put("success", true);
            response.put("message", "成功退出组队");
            response.put("data", toTeamMap(team, user.getId()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error leaving team: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
