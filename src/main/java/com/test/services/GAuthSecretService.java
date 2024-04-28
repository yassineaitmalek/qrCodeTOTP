package com.test.services;

import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.test.controllers.config.ApiDownloadInput;
import com.test.dto.GAuthDTO;
import com.test.dto.GAuthInput;
import com.test.dto.QRCodeInput;
import com.test.exception.config.ApiException;
import com.test.models.local.GAuthSecret;
import com.test.repositories.local.GAuthSecretRepository;
import com.test.utility.StringTemplate;

import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GAuthSecretService {

  private final QRCodeService qrCodeService;

  private final GAuthSecretRepository gAuthSecretRepository;

  private final Random random;

  private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  public String generateRandomSecretKey() {

    StringBuilder sb = new StringBuilder(32);
    IntStream.range(0, 32).forEach(i -> sb.append(CHARS.charAt(random.nextInt(CHARS.length()))));
    return sb.toString();
  }

  public Optional<GAuthSecret> createGAuth(GAuthDTO gAuthDTO) {

    GAuthSecret gAuthSecret = GAuthSecret.builder()
        .secretKey(generateRandomSecretKey())
        .userName(gAuthDTO.getUserName())
        .build();

    return Optional.ofNullable(gAuthSecretRepository.save(gAuthSecret));

  }

  public boolean gAuthValidation(GAuthInput gAuthInput) {

    return gAuthSecretRepository.findTopByUserName(gAuthInput.getUserName())
        .map(GAuthSecret::getSecretKey)
        .map(GoogleAuthenticator::new)
        .map(e -> e.generate(new Date(System.currentTimeMillis())))
        .filter(gAuthInput.getTotp()::equals)
        .isPresent();

  }

  public ApiDownloadInput getGAuthQRCodeByUser(String userName) {
    return gAuthSecretRepository.findTopByUserName(userName)
        .map(e -> getOTPAuthData(e.getUserName(), e.getSecretKey(), "testIssuer"))
        .map(e -> QRCodeInput.builder().input(e).build())
        .map(qrCodeService::encode)
        .orElseThrow(() -> new ApiException("error while getting the GQRCode"));

  }

  public String getOTPAuthData(String userName, String secret, String issuer) {

    return StringTemplate.template("otpauth://totp/${issuer}:${userName}?secret=${secret}&issuer=${issuer}")
        .addParameter("issuer", issuer)
        .addParameter("secret", secret)
        .addParameter("userName", userName)
        .build();

  }

}
