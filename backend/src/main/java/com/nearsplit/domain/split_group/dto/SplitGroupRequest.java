package com.nearsplit.domain.split_group.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter @Setter
//@JsonSetter(nulls = Nulls.SKIP)
public class SplitGroupRequest {
    @NotBlank
    private String title;
    @NotNull @Positive
    private BigDecimal totalPrice;
    @Min(2)
    private Integer maxParticipants;
    //    private String pickupAddress;
    private String pickupLocation;
    private String pickupLocationGeo;
    private LocalDate closedAt;

}
