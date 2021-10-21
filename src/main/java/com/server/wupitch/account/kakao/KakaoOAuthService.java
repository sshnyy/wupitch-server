package com.server.wupitch.account.kakao;

import com.server.wupitch.account.AccountRepository;
import com.server.wupitch.account.AccountService;
import com.server.wupitch.account.dto.AccountAuthDto;
import com.server.wupitch.account.dto.SignInReq;
import com.server.wupitch.account.dto.SignInRes;
import com.server.wupitch.account.entity.Account;
import com.server.wupitch.account.entity.enumtypes.GenderType;
import com.server.wupitch.configure.entity.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Transactional
@RequiredArgsConstructor
@Service
public class KakaoOAuthService {

    private final KakaoOAuth2 kakaoOAuth2;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${OAuth.KAKAO_TOKEN}")
    private String KAKAO_TOKEN;

    public SignInRes kakaoLogin(String code) {
        KakaoUserInfo kakaoUserInfo = kakaoOAuth2.getUserInfo(code);
        String oAuthId = "kakao_" + kakaoUserInfo.getId();

        String nickname = kakaoUserInfo.getNickname();
        String email = kakaoUserInfo.getEmail();

        Account account = null;
        Optional<Account> byKakaoIdAndStatus = accountRepository.findByOAuthIdAndStatus(oAuthId, Status.VALID);
        if (byKakaoIdAndStatus.isPresent()) account = byKakaoIdAndStatus.get();
        else {
            AccountAuthDto accountReq = AccountAuthDto.builder()
                    .oAuthId(oAuthId)
                    .email(email)
                    .nickname(nickname)
                    .password(passwordEncoder.encode(oAuthId+KAKAO_TOKEN))
                    .build();

            Account newAccount = Account.createAccount(accountReq);
            Account save = accountRepository.save(newAccount);
            account = save;

        }

        SignInReq signInreq = SignInReq.builder()
                .oAuthId(oAuthId)
                .email(account.getEmail())
                .password(oAuthId+KAKAO_TOKEN)
                .build();


        return accountService.signIn(signInreq);

    }
}
