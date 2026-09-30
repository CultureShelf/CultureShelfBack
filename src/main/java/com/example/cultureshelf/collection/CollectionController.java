package com.example.cultureshelf.collection;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.cultureshelf.collection.CollectionService.CollectionEntryDto;
import com.example.cultureshelf.collection.CollectionService.CollectionItemRequest;

@RestController
@RequestMapping("/api/v1/collection")
public class CollectionController {

    private final CollectionService service;

    public CollectionController(CollectionService service) {
        this.service = service;
    }

    @GetMapping
    public List<CollectionEntryDto> list(@RequestParam(required = false) CollectionStatus status) {
        return service.list(status);
    }

    @PutMapping("/variants/{variantId}")
    public CollectionItemDto upsert(@PathVariable UUID variantId, @Validated @RequestBody CollectionItemRequest request) {
        return service.upsert(variantId, request);
    }

    @DeleteMapping("/variants/{variantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID variantId) {
        service.remove(variantId);
    }
}
