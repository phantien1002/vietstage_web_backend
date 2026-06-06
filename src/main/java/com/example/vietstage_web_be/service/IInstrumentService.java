package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.InstrumentRequest;
import com.example.vietstage_web_be.dto.response.InstrumentResponse;

import java.util.List;

public interface IInstrumentService {
    InstrumentResponse createInstrument(InstrumentRequest request);
    List<InstrumentResponse> getAllInstruments();
    InstrumentResponse getInstrumentById(Long id);
    InstrumentResponse updateInstrument(Long id, InstrumentRequest request);
    void deleteInstrument(Long id);
}
