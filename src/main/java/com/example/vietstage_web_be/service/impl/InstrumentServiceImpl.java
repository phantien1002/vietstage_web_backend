package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.InstrumentRequest;
import com.example.vietstage_web_be.dto.response.InstrumentResponse;
import com.example.vietstage_web_be.entity.Instruments;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.InstrumentsRepository;
import com.example.vietstage_web_be.service.IInstrumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstrumentServiceImpl implements IInstrumentService {

    private final InstrumentsRepository instrumentsRepository;

    @Override
    @Transactional
    public InstrumentResponse createInstrument(InstrumentRequest request) {
        if (instrumentsRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.INSTRUMENT_ALREADY_EXIST);
        }

        Instruments instrument = Instruments.builder()
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .build();

        Instruments saved = instrumentsRepository.save(instrument);
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
        Instruments instrument = instrumentsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));
        return mapToResponse(instrument);
    }

    @Override
    @Transactional
    public InstrumentResponse updateInstrument(Long id, InstrumentRequest request) {
        Instruments instrument = instrumentsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));

        if (!instrument.getName().equalsIgnoreCase(request.getName()) &&
                instrumentsRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.INSTRUMENT_ALREADY_EXIST);
        }

        instrument.setName(request.getName());
        instrument.setDescription(request.getDescription());
        instrument.setIconUrl(request.getIconUrl());

        Instruments updated = instrumentsRepository.save(instrument);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteInstrument(Long id) {
        Instruments instrument = instrumentsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));
        instrumentsRepository.delete(instrument);
    }

    private InstrumentResponse mapToResponse(Instruments instrument) {
        return InstrumentResponse.builder()
                .id(instrument.getId())
                .name(instrument.getName())
                .description(instrument.getDescription())
                .iconUrl(instrument.getIconUrl())
                .build();
    }
}
