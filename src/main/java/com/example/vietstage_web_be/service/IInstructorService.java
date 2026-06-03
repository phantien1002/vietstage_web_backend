package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.CreateInstructorRequest;
import com.example.vietstage_web_be.dto.response.CreateInstructorResponse;

public interface IInstructorService {
    CreateInstructorResponse createInstructorAccount(CreateInstructorRequest request);
}
