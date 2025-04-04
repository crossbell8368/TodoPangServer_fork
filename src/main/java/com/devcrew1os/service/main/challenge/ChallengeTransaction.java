package com.devcrew1os.service.main.challenge;

import com.devcrew1os.entity.main.challenge.ChallengeCategory;
import com.devcrew1os.entity.main.challenge.ChallengeInfo;
import com.devcrew1os.repository.main.challenge.ChallengeCategoryRepository;
import com.devcrew1os.repository.main.challenge.ChallengeInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeTransaction {

    private final ChallengeCategoryRepository categoryRepo;
    private final ChallengeInfoRepository infoRepo;

    public List<ChallengeCategory> getCategories() {
        List<ChallengeCategory> categoryList = categoryRepo.findAllByOrderByIdDesc();
        if(categoryList.isEmpty()){
            throw new RuntimeException("Category list is empty");
        } else {
            return categoryList;
        }
    }

    public List<ChallengeInfo> getInfos() {
        List<ChallengeInfo> infoList = infoRepo.findAllByOrderByIdDesc();
        if(infoList.isEmpty()){
            throw new RuntimeException("Infos list is empty");
        } else {
            return infoList;
        }
    }
}
