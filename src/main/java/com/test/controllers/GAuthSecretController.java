package com.test.controllers;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.controllers.config.AbstractController;
import com.test.controllers.config.ApiDataResponse;
import com.test.dto.GAuthDTO;
import com.test.dto.GAuthInput;
import com.test.models.local.GAuthSecret;
import com.test.services.GAuthSecretService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gauth")
@RequiredArgsConstructor
public class GAuthSecretController implements AbstractController {

  private final GAuthSecretService gAuthSecretService;

  @PostMapping
  public ResponseEntity<ApiDataResponse<Optional<GAuthSecret>>> createGAuthSecret(@RequestBody GAuthDTO gAuthDTO) {
    return create(() -> gAuthSecretService.createGAuth(gAuthDTO));
  }

  @GetMapping("/{userName}")
  public ResponseEntity<byte[]> gQRCode(@PathVariable String userName) {
    return download(gAuthSecretService.getGAuthQRCodeByUser(userName));
  }

  @PostMapping("/checkValid")
  public ResponseEntity<ApiDataResponse<Boolean>> checkValid(@RequestBody GAuthInput gAuthInput) {
    return ok(() -> gAuthSecretService.gAuthValidation(gAuthInput));
  }

}
