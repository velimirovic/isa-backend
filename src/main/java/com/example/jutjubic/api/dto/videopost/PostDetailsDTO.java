package com.example.jutjubic.api.dto.videopost;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostDetailsDTO {
    private String title;
    private String description;
    private List<String> tags;
}
