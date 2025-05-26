package com.example.bob.Service;

import com.example.bob.Entity.ContestTeamEntity;
import com.example.bob.Entity.GroupChatParticipant;
import com.example.bob.Entity.GroupChatRoom;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRoomRepository roomRepository;
    private final GroupChatParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final ContestTeamMemberRepository contestTeamMemberRepository;
    private final ContestTeamRepository contestTeamRepository;

    @Transactional
    public Long createRoom(String roomName, List<Long> userIds, Long creatorId) {
        if (!userIds.contains(creatorId)) {
            userIds.add(0, creatorId); // 맨 앞에 추가하여 방장으로 지정
        }

        GroupChatRoom room = GroupChatRoom.builder()
                .roomName(roomName)
                .createdAt(LocalDateTime.now())
                .build();
        roomRepository.save(room);

        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음: ID=" + userId));

            GroupChatParticipant participant = GroupChatParticipant.builder()
                    .groupChatRoom(room)
                    .user(user)
                    .isAdmin(i == 0) // 첫 번째 유저를 방장으로 설정
                    .build();

            participantRepository.save(participant);
        }

        return room.getId();
    }

    public Long getOrCreateTeamChatRoom(Long teamId, Long creatorId) {
        // 이미 존재하는지 확인
        Optional<GroupChatRoom> existing = roomRepository.findByTeamId(teamId);
        if (existing.isPresent()) return existing.get().getId();

        // 없으면 새로 생성
        List<Long> userIds = contestTeamMemberRepository.findAcceptedMemberIdsByTeamId(teamId);

        if (!userIds.contains(creatorId)) {
            userIds.add(creatorId); // 팀장이 빠졌을 가능성 대비
        }

        String teamName = contestTeamRepository.findById(teamId)
                .map(ContestTeamEntity::getTeamName)
                .orElse("팀 채팅");

        GroupChatRoom newRoom = GroupChatRoom.builder()
                .roomName(teamName)
                .teamId(teamId)
                .createdAt(LocalDateTime.now())
                .build();

        roomRepository.saveAndFlush(newRoom);

        List<GroupChatParticipant> participants = userIds.stream()
                .map(uid -> {
                    UserEntity user = userRepository.findById(uid)
                            .orElseThrow(() -> new RuntimeException("사용자 없음: ID=" + uid));
                    return GroupChatParticipant.builder()
                            .groupChatRoom(newRoom)
                            .user(user)
                            .isAdmin(false)
                            .build();
                })
                .toList();

        participantRepository.saveAll(participants);
        participantRepository.flush();

        return newRoom.getId();
    }

}
