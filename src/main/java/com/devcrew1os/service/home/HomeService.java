package com.devcrew1os.service.home;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.home.HomeChallengeCard;
import com.devcrew1os.dto.home.HomeData;
import com.devcrew1os.dto.home.HomeReq;
import com.devcrew1os.dto.home.HomeRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HomeService {

    public HomeRes getDummyHomeData(HomeReq req) {
        HomeRes res = new HomeRes(true, true, "[Info] Get Dummy HomeData: " + req.getUserId(), ErrorCode.OK);
        HomeData data = new HomeData(
                "김하나",
                20,
                15,
                Map.of(
                        1, "생활",
                        2, "건강",
                        3, "운동",
                        4, "하루에5분",
                        5,"여가"
                ),
                List.of(
                        HomeChallengeCard.builder()
                                .title("쩝쩝박사")
                                .diff(2)
                                .popularity(98)
                                .build(),
                        HomeChallengeCard.builder()
                                .title("물 마셨나요?")
                                .diff(3)
                                .popularity(42)
                                .build(),
                        HomeChallengeCard.builder()
                                .title("운동 하지마세요")
                                .diff(1)
                                .popularity(78)
                                .build(),
                        HomeChallengeCard.builder()
                                .title("허리 절대지켜!")
                                .diff(3)
                                .popularity(56)
                                .build(),
                        HomeChallengeCard.builder()
                                .title("일일 퀘스트 끝냈나요?")
                                .diff(2)
                                .popularity(12)
                                .build()
                )
        );
        res.setData(data);
        return res;
    }

}
