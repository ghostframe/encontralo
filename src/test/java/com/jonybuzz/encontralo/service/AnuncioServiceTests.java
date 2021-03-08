package com.jonybuzz.encontralo.service;

import com.jonybuzz.encontralo.ApplicationTests;
import com.jonybuzz.encontralo.dto.AnuncioDto;
import com.jonybuzz.encontralo.dto.ImagenDownloadDto;
import com.jonybuzz.encontralo.dto.ImagenUploadDto;
import com.jonybuzz.encontralo.dto.NuevoAnuncioDto;
import com.jonybuzz.encontralo.model.*;
import com.jonybuzz.encontralo.repository.AnuncioRepository;
import com.jonybuzz.encontralo.testutils.builder.AnuncioBuilder;
import com.jonybuzz.encontralo.testutils.builder.ImagenBuilder;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AnuncioServiceTests extends ApplicationTests {

    @Autowired
    AnuncioService anuncioService;

    @Autowired
    AnuncioRepository anuncioRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EntityManager em;

    @Test
    void crearAnuncio_recibeAnuncioEstandar_persisteOkYDevuelveId() {

        NuevoAnuncioDto nuevoAnuncioDto = nuevoAnuncioPerroPerdido();
        //Ejecucion
        Long id = anuncioService.crearAnuncio(nuevoAnuncioDto);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "anuncio")).isEqualTo(1);
        assertThat(id).isNotNull();
        var anuncioPersistido = anuncioRepository.getOne(id);
        assertThat(anuncioPersistido.getColores()).hasSize(2);
        assertThat(anuncioPersistido.getNombreMascotaNormalizado()).isEqualTo("SENOR ITATI");
        assertThat(anuncioPersistido.getFechaCreacion()).isCloseTo(LocalDateTime.now(), within(5, SECONDS));
        assertThat(anuncioPersistido.getComentario()).isEqualTo("Tiene una chapita con mi teléfono! Revisar");
    }

    @Test
    void obtenerAnuncios_recibeFiltroConTipoPerdidoYEspeciePerro_deveuelveSoloEsos() {
        var filtro = new FiltroAnuncios();
        filtro.setTipoAnuncio(TipoAnuncio.PERDIDO);
        filtro.setEspecieId(1);
        filtro.setPagina(1);
        Imagen fotoPerro = ImagenBuilder.vacia().build();
        AnuncioBuilder.perro().perdido().conFotos(fotoPerro).build(em); //OK
        AnuncioBuilder.gato().perdido().build(em);
        AnuncioBuilder.perro().encontrado().build(em);
        AnuncioBuilder.gato().encontrado().build(em);
        AnuncioBuilder.datosMinimos().encontrado().build(em);
        AnuncioBuilder.datosMinimos().razaPerro().perdido().build(em); //OK
        //Ejecucion
        var paginaAnuncios = anuncioService.buscarAnuncios(filtro);
        assertThat(paginaAnuncios).isNotEmpty();
        assertThat(paginaAnuncios.getPageable().getPageSize()).isEqualTo(15);
        assertThat(paginaAnuncios.getTotalPages()).isEqualTo(1);
        assertThat(paginaAnuncios.getTotalElements()).isEqualTo(2);
        Especie especiePerro = new Especie();
        especiePerro.setId(1);
        assertThat(paginaAnuncios.getContent())
                .extracting(AnuncioDto::getTipo, AnuncioDto::getEspecie)
                .containsExactly(Tuple.tuple(TipoAnuncio.PERDIDO, especiePerro), Tuple.tuple(TipoAnuncio.PERDIDO, especiePerro));
        assertThat(paginaAnuncios.getContent().get(0).getFotos())
                .extracting(ImagenDownloadDto::getPosicion, ImagenDownloadDto::getUrl)
                .containsExactly(Tuple.tuple(fotoPerro.getPosicion(), "/imagenes/" + fotoPerro.getId()));
    }

    private NuevoAnuncioDto nuevoAnuncioPerroPerdido() {
        return NuevoAnuncioDto.builder()
                .tipo(TipoAnuncio.PERDIDO)
                .nombreMascota("Señor Itatí,, ")
                .especieId(1)
                .razaId(3)
                .sexoId(1)
                .tamanioId(3)
                .franjaEtariaId(3)
                .coloresIds(Set.of(2, 3))
                .tieneCollar(true)
                .pelajeId(1)
                .fotos(Set.of(ImagenUploadDto.builder()
                                .posicion(1)
                                .datosBase64("R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==")
                                .build(),
                        ImagenUploadDto.builder()
                                .posicion(2)
                                .datosBase64("R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==")
                                .build()))
                .localidadId(2)
                .comentario(" Tiene una chapita con mi teléfono! \n\r Revisar")
                .telefonoContacto("11-5678-0987")
                .build();
    }

}
