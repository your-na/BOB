package com.example.bob.Service;

import com.example.bob.DTO.MyResumeDto;
import com.example.bob.DTO.MyResumeSectionDto;
import com.example.bob.DTO.MyResumeDragItemDto;
import com.example.bob.Entity.MyResume;
import com.example.bob.Entity.MyResumeSection;
import com.example.bob.Entity.MyResumeDragItem;
import com.example.bob.Repository.MyResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.UserRepository;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Period;

/**
 * 나만의 이력서 저장 처리 서비스
 */
@Service
@RequiredArgsConstructor
public class MyResumeService {

    private final MyResumeRepository myResumeRepository;
    private static final Logger log = LoggerFactory.getLogger(MyResumeService.class);
    private final UserRepository userRepository;



    /**
     * 이력서 저장 처리 (프론트에서 전달한 DTO 기준)
     */
    public Long save(MyResumeDto dto) {
        log.info("🔄 이력서 저장 시작 - title: {}, memberId(userIdLogin): {}", dto.getTitle(), dto.getMemberId());

        // 이력서 엔티티 생성
        MyResume resume = MyResume.builder()
                .title(dto.getTitle())
                .memberId(dto.getMemberId())  // ⚠️ String 타입 (userIdLogin)
                .build();

        // 각 섹션 DTO를 엔티티로 변환 후 이력서에 추가
        List<MyResumeSection> sections = dto.getSections().stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        sections.forEach(resume::addSection); // 연관관계 설정 포함

        // DB 저장
        MyResume saved = myResumeRepository.save(resume);
        log.info("✅ 이력서 저장 완료 - ID: {}", saved.getId());

        return saved.getId(); // 저장된 ID 반환
    }



    /**
     * 섹션 DTO → Entity 변환 (dragItems 포함)
     */
    private MyResumeSection convertToEntity(MyResumeSectionDto dto) {
        log.info("📦 섹션 변환 시작 - title: {}, type: {}", dto.getTitle(), dto.getType());
        MyResumeSection section = MyResumeSection.builder()
                .type(dto.getType())
                .title(dto.getTitle())
                .comment(dto.getComment())
                .content(dto.getContent())
                .multiSelect(dto.isMultiSelect())
                .tags(dto.getTags())
                .conditions(dto.getConditions())
                .build();

        if (dto.getDragItems() != null && !dto.getDragItems().isEmpty()) {
            log.info("📎 섹션 '{}'에 드래그 항목 {}개 존재", dto.getTitle(), dto.getDragItems().size());

            List<MyResumeDragItem> dragItemEntities = dto.getDragItems().stream()
                    .map(this::convertDragItemDtoToEntity)
                    .collect(Collectors.toList());

            dragItemEntities.forEach(item -> {
                log.debug("🧷 드래그 아이템 - text: {}, file: {}", item.getDisplayText(), item.getFilePath());
                section.addDragItem(item);
            });
        } else {
            log.info("⚠️ 섹션 '{}'에 드래그 항목 없음", dto.getTitle());
        }

        return section;
    }

    /**
     * 드래그 항목 DTO → Entity 변환
     */
    private MyResumeDragItem convertDragItemDtoToEntity(MyResumeDragItemDto dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return MyResumeDragItem.builder()
                .displayText(dto.getDisplayText())
                .filePath(dto.getFilePath())
                .startDate(parseDate(dto.getStartDate(), formatter))
                .endDate(parseDate(dto.getEndDate(), formatter))
                .build();
    }

    /**
     * 문자열 → LocalDate 파싱
     */
    private LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr, formatter);
    }

    /**
     * 특정 사용자의 모든 이력서 목록 조회
     */
    public List<MyResume> findAllByMemberId(String memberId) {
        return myResumeRepository.findAllByMemberId(memberId);
    }


    /**
     *사용자 정보 주입
     */
    public MyResume findByIdWithSections(Long id) {
        log.info("🔍 이력서 상세 조회 시작 - resumeId: {}", id);

        MyResume resume = myResumeRepository.findByIdWithSections(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다. ID=" + id));

        log.info("✅ 이력서 조회 성공 - id: {}, title: {}", resume.getId(), resume.getTitle());
        log.info("📌 이력서의 memberId(userIdLogin으로 저장됨): {}", resume.getMemberId());

        // ✅ 사용자 정보 조회 (user_id_login 기준)
        UserEntity user = userRepository.findByUserIdLogin(resume.getMemberId()).orElse(null);

        if (user != null) {
            log.info("🙋 사용자 조회 성공 - userId: {}, userName: {}", user.getUserId(), user.getUserName());

            resume.setUserName(user.getUserName());
            resume.setProfileImageUrl(user.getProfileImageUrl());
            resume.setMainLanguage(user.getMainLanguage());
            resume.setSex(user.getSex());
            resume.setBirthday(user.getBirthday());

            // ✅ 나이 계산
            if (user.getBirthday() != null && !user.getBirthday().isEmpty()) {
                try {
                    int age = Period.between(LocalDate.parse(user.getBirthday()), LocalDate.now()).getYears();
                    resume.setAge(age);
                    log.info("📆 생년월일: {}, 계산된 나이: {}", user.getBirthday(), age);
                } catch (Exception e) {
                    resume.setAge(0); // 파싱 실패 시 기본값
                    log.error("❌ 생일 파싱 실패 - 생일 문자열: {}", user.getBirthday(), e);
                }
            }

            resume.setUserPhone(user.getUserPhone());
            resume.setUserEmail(user.getUserEmail());
            resume.setRegion(user.getRegion());
        } else {
            log.warn("⚠ 사용자 조회 실패 - userIdLogin: {}", resume.getMemberId());
        }

        return resume;
    }







}
