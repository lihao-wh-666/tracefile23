package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.BusinessException;
import com.exam.common.ErrorCode;
import com.exam.dto.UserPreferenceDTO;
import com.exam.entity.UserPreference;
import com.exam.mapper.UserPreferenceMapper;
import com.exam.service.UserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserPreferenceServiceImpl implements UserPreferenceService {

    @Autowired
    private UserPreferenceMapper userPreferenceMapper;

    @Override
    public UserPreference getByUserId(Long userId) {
        LambdaQueryWrapper<UserPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPreference::getUserId, userId);
        return userPreferenceMapper.selectOne(wrapper);
    }

    @Override
    public UserPreference getCurrentUserPreference() {
        Long userId = getCurrentUserId();
        UserPreference preference = getByUserId(userId);
        if (preference == null) {
            preference = initDefaultPreference(userId);
        }
        return preference;
    }

    @Override
    public boolean savePreference(UserPreferenceDTO dto) {
        Long userId = getCurrentUserId();
        if (dto.getTheme() == null || dto.getTheme().isEmpty()) {
            dto.setTheme("light");
        }
        if (dto.getLanguage() == null || dto.getLanguage().isEmpty()) {
            dto.setLanguage("zh-CN");
        }
        if (dto.getSidebarCollapsed() == null) {
            dto.setSidebarCollapsed(0);
        }

        UserPreference exist = getByUserId(userId);
        if (exist == null) {
            UserPreference preference = new UserPreference();
            preference.setUserId(userId);
            preference.setTheme(dto.getTheme());
            preference.setLanguage(dto.getLanguage());
            preference.setSidebarCollapsed(dto.getSidebarCollapsed());
            preference.setExtraConfig(dto.getExtraConfig());
            return userPreferenceMapper.insert(preference) > 0;
        } else {
            UserPreference update = new UserPreference();
            update.setId(exist.getId());
            update.setTheme(dto.getTheme());
            update.setLanguage(dto.getLanguage());
            update.setSidebarCollapsed(dto.getSidebarCollapsed());
            update.setExtraConfig(dto.getExtraConfig());
            return userPreferenceMapper.updateById(update) > 0;
        }
    }

    @Override
    public UserPreference initDefaultPreference(Long userId) {
        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setTheme("light");
        preference.setLanguage("zh-CN");
        preference.setSidebarCollapsed(0);
        preference.setExtraConfig(null);
        userPreferenceMapper.insert(preference);
        return preference;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
