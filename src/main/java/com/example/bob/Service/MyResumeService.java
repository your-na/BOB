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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;



import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Period;
import java.time.YearMonth;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.UUID;
import org.springframework.web.bind.annotation.RequestParam;



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
        log.info("🔄 이력서 저장 시작 - title: {}, memberId(userIdLogin): {}",
                dto.getTitle(), dto.getMemberId());

        // 🔹 memberId로 실제 UserEntity 조회
        UserEntity loginUser = userRepository.findByUserIdLogin(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. memberId=" + dto.getMemberId()));

        // 이력서 엔티티 생성
        MyResume resume = MyResume.builder()
                .title(dto.getTitle())
                .memberId(dto.getMemberId())   // 로그인 아이디
                .userId(loginUser.getUserId()) // 실제 유저 고유 ID
                .build();

        // 섹션 변환 후 추가
        List<MyResumeSection> sections = dto.getSections().stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        sections.forEach(resume::addSection);

        // DB 저장
        MyResume saved = myResumeRepository.save(resume);
        log.info("✅ 이력서 저장 완료 - ID: {}, userId: {}", saved.getId(), saved.getUserId());
        // 🟢 이 부분 추가
        log.info("📊 저장된 섹션 개수: {}", saved.getSections().size());
        saved.getSections().forEach(sec ->
                log.info("➡️ 섹션 '{}' 안에 드래그아이템 {}개", sec.getTitle(), sec.getDragItems().size())
        );

        return saved.getId();
    }





    /**
     * 섹션 DTO → Entity 변환 (dragItems 포함)
     */
    private MyResumeSection convertToEntity(MyResumeSectionDto dto) {
        log.info("📦 섹션 변환 시작 - title: {}, type: {}", dto.getTitle(), dto.getType());
        // 🟢 이 부분 추가
        log.info("📝 섹션 설명(comment): {}", dto.getComment());
        log.info("📝 섹션 본문(content): {}", dto.getContent());
        log.info("🏷️ 태그(tags): {}", dto.getTags());
        log.info("✅ 조건(conditions): {}", dto.getConditions());
        MyResumeSection section = MyResumeSection.builder()
                .type(dto.getType())
                .title(dto.getTitle())
                .comment(dto.getComment())
                .content(dto.getContent())
                .multiSelect(dto.isMultiSelect())
                .tags(dto.getTags())
                .conditions(dto.getConditions())
                .fileNames(dto.getFileNames())
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

        // 🟢 로그 추가
        log.info("📂 드래그아이템 변환 - text: {}, filePath: {}, startDate: {}, endDate: {}",
                dto.getDisplayText(), dto.getFilePath(), dto.getStartDate(), dto.getEndDate());

        return MyResumeDragItem.builder()
                .displayText(dto.getDisplayText())
                .filePath(dto.getFilePath())
                .startDate(parseDate(dto.getStartDate(), formatter))
                .endDate(parseDate(dto.getEndDate(), formatter))
                .workplace(dto.getWorkplace())
                .jobTitle(dto.getJobTitle())
                .status(dto.getStatus())
                .build();
    }

    /**
     * 문자열 → LocalDate 파싱
     */
    private LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        if (dateStr == null || dateStr.isBlank()) return null;

        try {
            // "yyyy-MM-dd" 형식이면 정상 파싱
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            try {
                // "yyyy-MM" 형식이면 YearMonth로 파싱 후 첫날짜로 변환
                return YearMonth.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM"))
                        .atDay(1); // → 2025-09-01
            } catch (Exception ex) {
                log.error("❌ 날짜 파싱 실패: {}", dateStr, ex);
                return null; // 그래도 안 되면 null 처리
            }
        }
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
            resume.setNameHanja(user.getNameHanja()); // ✅ 한문 이름 세팅
            resume.setNameEng(user.getNameEng());     // ✅ 영문 이름 세팅
            log.info("👉 DB에서 불러온 한문이름={}, 영문이름={}", user.getNameHanja(), user.getNameEng());



        } else {
            log.warn("⚠ 사용자 조회 실패 - userIdLogin: {}", resume.getMemberId());
        }

        return resume;
    }

    /**
     * ✅ 로그인 사용자의 이력서를 삭제 (본인 것만 가능)
     */
    public void deleteResume(Long id, String memberId) {
        log.info("🗑️ 이력서 삭제 요청 - resumeId: {}, memberId: {}", id, memberId);

        MyResume resume = myResumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서 없음"));

        log.info("📌 이력서 작성자: {}, 로그인 사용자: {}", resume.getMemberId(), memberId);

        if (!resume.getMemberId().equals(memberId)) {
            throw new SecurityException("본인의 이력서만 삭제 가능합니다.");
        }

        myResumeRepository.deleteById(id);
        log.info("✅ 삭제 완료");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadResumeFile(@RequestParam("file") MultipartFile file) {
        try {
            // 저장 경로 (프로젝트 루트 기준)
            String uploadDir = System.getProperty("user.dir") + "/uploads/resumeFiles/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // 원래 이름과 확장자
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String uniqueName = UUID.randomUUID() + extension;

            // 실제 저장
            File dest = new File(dir, uniqueName);
            file.transferTo(dest);

            // 저장된 파일명 반환 (DB에는 이걸 저장해야 함)
            return ResponseEntity.ok(uniqueName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("파일 업로드 실패: " + e.getMessage());
        }
    }









}
