package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.TechniqueRequest;
import com.example.vietstage_web_be.dto.request.UpdateTechniqueRequest;
import com.example.vietstage_web_be.dto.response.TechniqueResponse;
import com.example.vietstage_web_be.entity.Instrument;
import com.example.vietstage_web_be.entity.Technique;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.InstrumentRepository;
import com.example.vietstage_web_be.repository.TechniqueRepository;
import com.example.vietstage_web_be.service.ITechniqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechniqueServiceImpl implements ITechniqueService {

    private final TechniqueRepository techniquesRepository;
    private final InstrumentRepository instrumentsRepository;

    @Override
    @Transactional
    public TechniqueResponse createTechnique(TechniqueRequest request) {
        Instrument instrument = instrumentsRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));

        if (techniquesRepository.existsByNameIgnoreCaseAndInstrumentId(request.getName(), request.getInstrumentId())) {
            throw new AppException(ErrorCode.TECHNIQUE_ALREADY_EXIST);
        }

        Technique technique = Technique.builder()
                .name(request.getName())
                .description(request.getDescription())
                .guideUrl(request.getGuideUrl())
                .instrument(instrument)
                .build();

        Technique saved = techniquesRepository.save(technique);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechniqueResponse> getAllTechniques() {
        return techniquesRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechniqueResponse> getTechniquesByInstrumentId(Long instrumentId) {
        if (!instrumentsRepository.existsById(instrumentId)) {
            throw new AppException(ErrorCode.INSTRUMENT_NOT_FOUND);
        }
        return techniquesRepository.findByInstrumentId(instrumentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TechniqueResponse getTechniqueById(Long id) {
        Technique technique = techniquesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TECHNIQUE_NOT_FOUND));
        return mapToResponse(technique);
    }

    @Override
    @Transactional
    public TechniqueResponse updateTechnique(Long id, UpdateTechniqueRequest request) {
        Technique technique = techniquesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TECHNIQUE_NOT_FOUND));

        // Kiểm tra trùng tên trong cùng nhạc cụ (instrument giữ nguyên, không đổi)
        boolean isNameChanged = !technique.getName().equalsIgnoreCase(request.getName());
        if (isNameChanged) {
            Long instrumentId = technique.getInstrument() != null ? technique.getInstrument().getId() : null;
            if (instrumentId != null &&
                    techniquesRepository.existsByNameIgnoreCaseAndInstrumentId(request.getName(), instrumentId)) {
                throw new AppException(ErrorCode.TECHNIQUE_ALREADY_EXIST);
            }
        }

        // Chỉ cập nhật name, description, guideUrl — instrument KHÔNG thay đổi
        technique.setName(request.getName());
        technique.setDescription(request.getDescription());
        technique.setGuideUrl(request.getGuideUrl());

        Technique updated = techniquesRepository.save(technique);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTechnique(Long id) {
        Technique technique = techniquesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TECHNIQUE_NOT_FOUND));
        techniquesRepository.delete(technique);
    }

    private TechniqueResponse mapToResponse(Technique technique) {
        return TechniqueResponse.builder()
                .id(technique.getId())
                .name(technique.getName())
                .description(technique.getDescription())
                .guideUrl(technique.getGuideUrl())
                .instrumentId(technique.getInstrument() != null ? technique.getInstrument().getId() : null)
                .build();
    }
}
