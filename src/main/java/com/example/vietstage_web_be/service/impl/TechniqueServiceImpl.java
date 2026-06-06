package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.TechniqueRequest;
import com.example.vietstage_web_be.dto.response.TechniqueResponse;
import com.example.vietstage_web_be.entity.Instruments;
import com.example.vietstage_web_be.entity.Techniques;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.InstrumentsRepository;
import com.example.vietstage_web_be.repository.TechniquesRepository;
import com.example.vietstage_web_be.service.ITechniqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechniqueServiceImpl implements ITechniqueService {

    private final TechniquesRepository techniquesRepository;
    private final InstrumentsRepository instrumentsRepository;

    @Override
    @Transactional
    public TechniqueResponse createTechnique(TechniqueRequest request) {
        Instruments instrument = instrumentsRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));

        Techniques technique = Techniques.builder()
                .name(request.getName())
                .description(request.getDescription())
                .instrument(instrument)
                .build();

        Techniques saved = techniquesRepository.save(technique);
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
        Techniques technique = techniquesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TECHNIQUE_NOT_FOUND));
        return mapToResponse(technique);
    }

    @Override
    @Transactional
    public TechniqueResponse updateTechnique(Long id, TechniqueRequest request) {
        Techniques technique = techniquesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TECHNIQUE_NOT_FOUND));

        Instruments instrument = instrumentsRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));

        technique.setName(request.getName());
        technique.setDescription(request.getDescription());
        technique.setInstrument(instrument);

        Techniques updated = techniquesRepository.save(technique);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTechnique(Long id) {
        Techniques technique = techniquesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TECHNIQUE_NOT_FOUND));
        techniquesRepository.delete(technique);
    }

    private TechniqueResponse mapToResponse(Techniques technique) {
        return TechniqueResponse.builder()
                .id(technique.getId())
                .name(technique.getName())
                .description(technique.getDescription())
                .instrumentId(technique.getInstrument() != null ? technique.getInstrument().getId() : null)
                .build();
    }
}
