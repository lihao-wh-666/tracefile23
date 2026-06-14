package com.exam.service;

import com.exam.dto.UserPreferenceDTO;
import com.exam.entity.UserPreference;

public interface UserPreferenceService {

    UserPreference getByUserId(Long userId);

    UserPreference getCurrentUserPreference();

    boolean savePreference(UserPreferenceDTO dto);

    UserPreference initDefaultPreference(Long userId);
}
