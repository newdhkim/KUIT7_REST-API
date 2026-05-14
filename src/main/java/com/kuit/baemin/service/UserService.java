package com.kuit.baemin.service;

import com.kuit.baemin.domain.User.User;
import com.kuit.baemin.domain.User.UserStatus;
import com.kuit.baemin.dto.request.LoginReq;
import com.kuit.baemin.dto.request.SignUpReq;
import com.kuit.baemin.dto.request.UserUpdateReq;
import com.kuit.baemin.dto.response.UserRes;
import com.kuit.baemin.exception.UserException;
import com.kuit.baemin.exception.errorcode.ErrorStatus;
import com.kuit.baemin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kuit.baemin.exception.errorcode.ErrorStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원 가입
     */
    @Transactional
    public Long signUp(SignUpReq req) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new UserException(DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .phoneNumber(req.getPhoneNumber())
                .name(req.getNickname())
                .status(UserStatus.ACTIVE)
                .build();


        User saved = userRepository.save(user);
        return saved.getId();
    }

    /**
     * 로그인
     */
    public Long login(LoginReq req) {
        User user = userRepository
                .findByEmailAndStatus(req.getEmail(), UserStatus.ACTIVE)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        if (!user.getPassword().equals(req.getPassword())) {
            throw new UserException(INVALID_PASSWORD);
        }

        return user.getId();
    }

    /**
     * 회원 단건 조회
     */
    public UserRes getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        return UserRes.from(user);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        user.updateStatus(UserStatus.DELETED);
    }

    /**
     * 회원 정보 수정
     */
    @Transactional
    public void updateUser(Long userId, UserUpdateReq req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        if (req.getName() != null)
            user.updateName(req.getName());

        if (req.getEmail() != null) {
            // 이메일 중복 확인
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new UserException(DUPLICATE_EMAIL);
            }
            user.updateEmail(req.getEmail());
        }

        if (req.getPhoneNumber() != null)
            user.updatePhoneNumber(req.getPhoneNumber());

        if (req.getPassword() != null)
            user.updatePassword(req.getPassword());
    }
}
