package com.example.bob.Repository;

import com.example.bob.Entity.PrivateChatRoom;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivateChatRoomRepository extends JpaRepository<PrivateChatRoom, Long> {
    Optional<PrivateChatRoom> findByUserAAndUserBOrUserBAndUserA(UserEntity a1, UserEntity b1, UserEntity a2, UserEntity b2);
}
