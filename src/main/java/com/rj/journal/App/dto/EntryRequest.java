package com.rj.journal.App.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EntryRequest(@NotBlank @Size(max = 140) String title,
                           @NotBlank @Size(max = 20000) String content) { }
