package com.code.rental.controller;

import com.code.rental.controller.dto.BlockDTO;
import com.code.rental.controller.dto.response.BlockResponseDTO;
import com.code.rental.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Validated
@RequiredArgsConstructor
@RequestMapping("blocks")
@RestController
public class BlockController {

    private final BlockService blockService;

    @Operation(summary = "Get blocks by property ID")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BlockResponseDTO> getBlocksByPropertyId(@RequestParam final Long propertyId) {
        return blockService.getBlocksByPropertyId(propertyId);
    }

    @Operation(summary = "Create a block")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BlockResponseDTO> createBlock(@RequestBody @Valid final BlockDTO blockDTO) {
        final BlockResponseDTO block = blockService.createBlock(blockDTO);

        final URI location = UriComponentsBuilder.fromPath("/bookings/{id}")
                .buildAndExpand(block.getId()).toUri();

        return ResponseEntity.created(location).body(block);
    }

    @Operation(summary = "Get a block")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BlockResponseDTO getBlock(@PathVariable final Long id) {
        return blockService.getBlockById(id);
    }

    @Operation(summary = "Update a block")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BlockResponseDTO updateBlock(@PathVariable final Long id, @RequestBody @Valid final BlockDTO blockDTO) {
        return blockService.updateBlock(id, blockDTO);
    }

    @Operation(summary = "Delete a block")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteBlock(@PathVariable final Long id) {
        blockService.deleteBlock(id);
        return ResponseEntity.noContent().build();
    }
}
