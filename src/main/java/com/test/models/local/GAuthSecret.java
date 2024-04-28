package com.test.models.local;

import javax.persistence.Entity;

import com.test.models.config.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GAuthSecret extends BaseEntity {

  private String secretKey;

  private String userName;

}
