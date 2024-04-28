package com.test.services;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;
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
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.test.controllers.config.ApiDownloadInput;
import com.test.dto.FileDTO;
import com.test.dto.QRCodeInput;
import com.test.exception.config.ApiException;
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

  private static final Integer HEIGHT = 500;

  private static final Integer WIDTH = 500;

  public InputStream loadResource(String path) {
    return Try.of(() -> path)
        .map(ClassPathResource::new)
        .mapTry(ClassPathResource::getInputStream)
        .getOrElseThrow(() -> new ApiException("error loading the resource " + path));

  }

  public ByteArrayOutputStream simpleWrite(BitMatrix bitMatrix) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    MatrixToImageWriter.writeToStream(bitMatrix, EXT, baos);
    return baos;
  }

  public ApiDownloadInput encode(QRCodeInput qrCodeInput) {

    byte[] bytes = Try.of(() -> qrCodeInput)
        .mapTry(e -> multiFormatWriter.encode(toUTF8(qrCodeInput.getInput()), BarcodeFormat.QR_CODE, WIDTH, HEIGHT))
        .mapTry(this::simpleWrite)
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

  public ByteArrayOutputStream logoWrite(BitMatrix bitMatrix) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
    BufferedImage overly = ImageIO.read(loadResource("static/logo.png"));

    int deltaHeight = (qrImage.getHeight() - overly.getHeight()) / 2;
    int deltaWidth = (qrImage.getWidth() - overly.getWidth()) / 2;

    BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics2d = Graphics2D.class.cast(combined.getGraphics());
    graphics2d.drawImage(qrImage, 0, 0, null);
    graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    graphics2d.drawImage(overly, deltaWidth, deltaHeight, null);
    ImageIO.write(combined, EXT, baos);

    return baos;

  }

  public ApiDownloadInput encodeLogo(QRCodeInput qrCodeInput) {

    byte[] bytes = Try.of(() -> qrCodeInput)
        .mapTry(e -> multiFormatWriter.encode(toUTF8(qrCodeInput.getInput()), BarcodeFormat.QR_CODE, WIDTH, HEIGHT))
        .mapTry(this::logoWrite)
        .map(ByteArrayOutputStream::toByteArray)
        .onFailure(ServerSideException::reThrow)
        .get();

    return ApiDownloadInput.builder()
        .bytes(bytes)
        .fileName("QR_" + DateUtility.nowDateTimeFormatted())
        .ext(EXT)
        .build();

  }

}
