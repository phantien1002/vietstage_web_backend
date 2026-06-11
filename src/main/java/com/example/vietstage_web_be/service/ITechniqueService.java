package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.TechniqueRequest;
import com.example.vietstage_web_be.dto.request.UpdateTechniqueRequest;
import com.example.vietstage_web_be.dto.response.TechniqueResponse;

import java.util.List;

public interface ITechniqueService {
    TechniqueResponse createTechnique(TechniqueRequest request);
    List<TechniqueResponse> getAllTechniques();
    List<TechniqueResponse> getTechniquesByInstrumentId(Long instrumentId);
    TechniqueResponse getTechniqueById(Long id);
    TechniqueResponse updateTechnique(Long id, UpdateTechniqueRequest request);
    void deleteTechnique(Long id);
}
