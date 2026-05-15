package com.gym.service.entity;

public class UserRanking {
    private Long userId;
    private String username;
    private String name;
    private String avatar;
    private Integer totalClasses;
    private Integer checkinDays;
    private Integer availablePoints;
    private Integer rank;
    
    public UserRanking() {}
    
    public UserRanking(Long userId, String username, String name, Integer totalClasses, Integer checkinDays) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.totalClasses = totalClasses;
        this.checkinDays = checkinDays;
        this.availablePoints = 0;
        this.avatar = getRandomAvatar();
    }
    
    private String getRandomAvatar() {
        String[] avatars = {"💪", "🏋️", "🧘", "🏃", "💃", "🏆", "🌟", "⚽", "🎯", "🔥"};
        int index = (userId != null ? userId.intValue() : 0) % avatars.length;
        return avatars[index];
    }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public Integer getTotalClasses() { return totalClasses; }
    public void setTotalClasses(Integer totalClasses) { this.totalClasses = totalClasses; }
    
    public Integer getCheckinDays() { return checkinDays; }
    public void setCheckinDays(Integer checkinDays) { this.checkinDays = checkinDays; }

    public Integer getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(Integer availablePoints) { this.availablePoints = availablePoints; }
    
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
}
