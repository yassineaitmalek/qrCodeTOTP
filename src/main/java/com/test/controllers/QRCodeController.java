package com.test.controllers;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.controllers.config.AbstractController;
import com.test.controllers.config.ApiDataResponse;
import com.test.dto.FileDTO;
import com.test.dto.QRCodeInput;
import com.test.services.QRCodeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/qrCode")
@RequiredArgsConstructor
public class QRCodeController implements AbstractController {

  private final QRCodeService qrCodeService;

  @PostMapping("/encode")
  public ResponseEntity<byte[]> input(@RequestBody @Valid @NotNull QRCodeInput qrCodeInput) {

    return download(qrCodeService.encode(qrCodeInput));
  }

  @PutMapping(value = "/encode", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
  public ResponseEntity<ApiDataResponse<String>> decode(@ModelAttribute @Valid @NotNull FileDTO fileDTO) {

    return ok(() -> qrCodeService.decode(fileDTO));
  }

}
