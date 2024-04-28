package com.test.services;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.HybridBinarizer;
import com.test.controllers.config.ApiDownloadInput;
import com.test.dto.FileDTO;
import com.test.dto.QRCodeInput;
import com.test.exception.config.ServerSideException;
import com.test.utility.DateUtility;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;

@Validated
@Service
@RequiredArgsConstructor
public class QRCodeService {

  public final MultiFormatReader multiFormatReader;

  public final MultiFormatWriter multiFormatWriter;

  private static final String EXT = "png";

  public ApiDownloadInput encode(QRCodeInput qrCodeInput) {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    byte[] bytes = Try.of(() -> qrCodeInput)
        .mapTry(e -> multiFormatWriter.encode(toUTF8(qrCodeInput.getInput()), BarcodeFormat.QR_CODE, e.getWidth(),
            e.getHeight()))
        .mapTry(e -> {
          MatrixToImageWriter.writeToStream(e, EXT, baos);
          return baos;
        })
        .map(ByteArrayOutputStream::toByteArray)
        .onFailure(ServerSideException::reThrow)
        .get();

    return ApiDownloadInput.builder()
        .bytes(bytes)
        .fileName("QR_" + DateUtility.nowDateTimeFormatted())
        .ext(EXT)
        .build();
  }

  public String toUTF8(String input) {
    return Try.of(() -> input)
        .mapTry(e -> new String(input.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
        .onFailure(ServerSideException::reThrow)
        .get();
  }

  public String decode(FileDTO fileDTO) {

    return Try.of(() -> fileDTO)
        .map(FileDTO::getFile)
        .mapTry(MultipartFile::getInputStream)
        .mapTry(ImageIO::read)
        .map(BufferedImageLuminanceSource::new)
        .map(HybridBinarizer::new)
        .map(BinaryBitmap::new)
        .mapTry(multiFormatReader::decode)
        .map(Result::getText)
        .onFailure(ServerSideException::reThrow)
        .get();

  }

}
