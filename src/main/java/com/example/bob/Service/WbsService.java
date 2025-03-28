package com.example.bob.Service;

import com.example.bob.Entity.WbsEntity;
import com.example.bob.Repository.WbsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WbsService {

    private final WbsRepository wbsRepository;


    // ✅ WBS 전체 저장 (새로 저장하거나 업데이트)
    public void saveWbsList(List<WbsEntity> wbsList) {
        wbsRepository.saveAll(wbsList);
    }

    // ✅ 특정 프로젝트/공모전의 WBS 전체 조회
    public List<WbsEntity> getWbsList(String type, Long targetId) {
        return wbsRepository.findByTypeAndTargetId(type, targetId);
    }

    @Transactional
    // ✅ WBS 전체 삭제 (초기화용)
    public void deleteWbsList(String type, Long targetId) {
        wbsRepository.deleteByTypeAndTargetId(type, targetId);
    }

}
