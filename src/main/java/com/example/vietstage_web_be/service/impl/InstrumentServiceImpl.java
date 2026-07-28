package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.InstrumentRequest;
import com.example.vietstage_web_be.dto.response.InstrumentResponse;
import com.example.vietstage_web_be.entity.Instrument;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.InstrumentRepository;
import com.example.vietstage_web_be.service.IInstrumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstrumentServiceImpl implements IInstrumentService {

    private final InstrumentRepository instrumentsRepository;

    @Override
    @Transactional
    public InstrumentResponse createInstrument(InstrumentRequest request) {
        if (instrumentsRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.INSTRUMENT_ALREADY_EXIST);
        }

        Long nextId = instrumentsRepository.findTopByOrderByIdDesc().map(com.example.vietstage_web_be.entity.Instrument::getId).orElse(0L) + 1;
        String insCode = "INS-" + request.getName().substring(0, Math.min(2, request.getName().length())).toUpperCase() + "-" + String.format("%03d", nextId);

        Instrument instrument = Instrument.builder()
                .instrumentCode(insCode)
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .isActive(true)
                .build();

        Instrument saved = instrumentsRepository.save(instrument);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstrumentResponse> getAllInstruments() {
        return instrumentsRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InstrumentResponse getInstrumentById(Long id) {
        Instrument instrument = instrumentsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));
        return mapToResponse(instrument);
    }

    @Override
    @Transactional
    public InstrumentResponse updateInstrument(Long id, InstrumentRequest request) {
        Instrument instrument = instrumentsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));

        if (!instrument.getName().equalsIgnoreCase(request.getName()) &&
                instrumentsRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.INSTRUMENT_ALREADY_EXIST);
        }

        instrument.setName(request.getName());
        instrument.setDescription(request.getDescription());
        instrument.setIconUrl(request.getIconUrl());

        Instrument updated = instrumentsRepository.save(instrument);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteInstrument(Long id) {
        Instrument instrument = instrumentsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));
        instrumentsRepository.delete(instrument);
    }

    private InstrumentResponse mapToResponse(Instrument instrument) {
        return InstrumentResponse.builder()
                .id(instrument.getId())
                .instrumentCode(instrument.getInstrumentCode())
                .name(instrument.getName())
                .description(instrument.getDescription())
                .iconUrl(instrument.getIconUrl())
                .build();
    }
}
