package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.dto.ChangePasswordDTO;
import com.exam.dto.UpdateProfileDTO;
import com.exam.dto.UserDTO;
import com.exam.entity.User;

public interface UserService {

    IPage<User> page(Integer current, Integer size, String keyword, Integer role, Integer status);

    User getById(Long id);

    boolean save(UserDTO dto);

    boolean update(Long id, UserDTO dto);

    boolean removeById(Long id);

    boolean updateStatus(Long id, Integer status);

    boolean updateProfile(Long userId, UpdateProfileDTO dto);

    boolean changePassword(Long userId, ChangePasswordDTO dto);

    String updateAvatar(Long userId, String avatarUrl);
}
