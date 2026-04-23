package com.example.demo.service;

import com.example.demo.domain.dto.user.UserPageResponse;
import com.example.demo.domain.dto.user.UserSummaryResponse;
import com.example.demo.domain.entity.User;
import com.example.demo.mapper.UserMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserSummaryResponse getByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        return toSummary(user);
    }

    public UserPageResponse listUsers(int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;

        List<UserSummaryResponse> records = userMapper.listUsers(safePageSize, offset).stream()
                .map(this::toSummary)
                .toList();
        long total = userMapper.countUsers();
        return new UserPageResponse(records, total);
    }

    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        int updated = userMapper.updateStatus(id, status);
        if (updated == 0) {
            throw new IllegalArgumentException("update failed");
        }
    }

    private UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt());
    }
}
