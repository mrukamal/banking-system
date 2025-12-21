package com.bankingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private Boolean active;
    private List<AccountDTO> accounts = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}