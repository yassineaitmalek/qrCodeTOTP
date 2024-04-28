package com.test.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GAuthInput {

  @NotEmpty
  @NotNull
  private String userName;

  @NotEmpty
  @NotNull
  private String totp;
}
