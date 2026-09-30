package com.example.cultureshelf.catalog.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.cultureshelf.catalog.service.SeriesService;
import com.example.cultureshelf.catalog.web.CatalogDtos.ImportRequest;
import com.example.cultureshelf.catalog.web.CatalogDtos.SeriesDetailDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.SeriesRequest;
import com.example.cultureshelf.catalog.web.CatalogDtos.SeriesSummaryDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.VolumeDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.VolumeRequest;

@RestController
@RequestMapping("/api/v1")
public class SeriesController {

    private final SeriesService service;

    public SeriesController(SeriesService service) {
        this.service = service;
    }

    @GetMapping("/series")
    public List<SeriesSummaryDto> list() {
        return service.list();
    }

    @GetMapping("/series/{id}")
    public SeriesDetailDto get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping("/series")
    @ResponseStatus(HttpStatus.CREATED)
    public SeriesDetailDto create(@Validated @RequestBody SeriesRequest request) {
        return service.create(request);
    }

    @PostMapping("/series/import")
    public SeriesDetailDto importSeries(@Validated @RequestBody ImportRequest request) {
        return service.importSeries(request.source(), request.externalId());
    }

    @PostMapping("/series/{id}/refresh")
    public SeriesDetailDto refresh(@PathVariable UUID id) {
        return service.refresh(id);
    }

    @PutMapping("/series/{id}")
    public SeriesDetailDto update(@PathVariable UUID id, @Validated @RequestBody SeriesRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/series/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PostMapping("/series/{id}/volumes")
    @ResponseStatus(HttpStatus.CREATED)
    public VolumeDto addVolume(@PathVariable UUID id, @Validated @RequestBody VolumeRequest request) {
        return service.addVolume(id, request);
    }

    @PutMapping("/volumes/{id}")
    public VolumeDto updateVolume(@PathVariable UUID id, @Validated @RequestBody VolumeRequest request) {
        return service.updateVolume(id, request);
    }

    @DeleteMapping("/volumes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVolume(@PathVariable UUID id) {
        service.deleteVolume(id);
    }
}
