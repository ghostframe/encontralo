package com.jonybuzz.encontralo.service;

import com.jonybuzz.encontralo.dto.EspecieDto;
import com.jonybuzz.encontralo.dto.SeleccionablesDto;
import com.jonybuzz.encontralo.repository.ColorRepository;
import com.jonybuzz.encontralo.repository.EspecieRepository;
import com.jonybuzz.encontralo.repository.FranjaEtariaRepository;
import com.jonybuzz.encontralo.repository.PelajeRepository;
import com.jonybuzz.encontralo.repository.RazaRepository;
import com.jonybuzz.encontralo.repository.SexoRepository;
import com.jonybuzz.encontralo.repository.TamanioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class SeleccionablesService {

    @Autowired
    private RazaRepository razaRepository;
    @Autowired
    private SexoRepository sexoRepository;
    @Autowired
    private ColorRepository colorRepository;
    @Autowired
    private EspecieRepository especieRepository;
    @Autowired
    private TamanioRepository tamanioRepository;
    @Autowired
    private FranjaEtariaRepository franjaEtariaRepository;
    @Autowired
    private PelajeRepository pelajeRepository;


    public SeleccionablesDto obtenerSeleccionables() {
        SeleccionablesDto dto = new SeleccionablesDto();
        dto.setSexos(new HashSet<>(sexoRepository.findAll()));
        dto.setColores(new HashSet<>(colorRepository.findAll()));
        dto.setEspecies(especieRepository.findAll().stream()
                .map(especie -> new EspecieDto(
                        razaRepository.findByEspecieId(especie.getId()),
                        tamanioRepository.findByEspecieId(especie.getId())))
                .collect(Collectors.toList()));
        dto.setFranjasEtarias(franjaEtariaRepository.findAll());
        dto.setPelajes(pelajeRepository.findAll());
        return dto;
    }
}
