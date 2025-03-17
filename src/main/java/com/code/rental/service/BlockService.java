package com.code.rental.service;

import com.code.rental.controller.dto.request.BlockRequestDTO;
import com.code.rental.controller.dto.response.BlockResponseDTO;
import com.code.rental.domain.AvailabilityEntry;
import com.code.rental.domain.AvailabilityEntryFactory;
import com.code.rental.domain.Property;
import com.code.rental.exception.ConflictException;
import com.code.rental.exception.ResourceNotFoundException;
import com.code.rental.repository.AvailabilityRepository;
import com.code.rental.repository.PropertyRepository;
import com.code.rental.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BlockService {

    private final PropertyRepository propertyRepository;
    private final AvailabilityRepository availabilityRepository;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public List<BlockResponseDTO> getBlocksByPropertyId(final Long propertyId) {
        return availabilityRepository.findAllBlocksByPropertyId(propertyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BlockResponseDTO createBlock(final BlockRequestDTO blockDTO) {
        final Property property = propertyRepository.findById(blockDTO.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID " + blockDTO.getPropertyId()));

        if (!property.getOwner().getId().equals(jwtService.getLoggedUser().getId())) {
            throw new ConflictException("You can't block a property that you don't own");
        }

        final boolean hasConflict = availabilityRepository.hasConflict(property, blockDTO.getStartDate(), blockDTO.getEndDate());
        if (hasConflict) {
            throw new ConflictException("Cannot block property for the selected dates");
        }

        final AvailabilityEntry block = AvailabilityEntryFactory.createBlock(
                property,
                blockDTO.getStartDate(),
                blockDTO.getEndDate());
        final AvailabilityEntry savedBlock = availabilityRepository.save(block);
        return mapToDTO(savedBlock);
    }

    public BlockResponseDTO getBlockById(final Long id) {
        final AvailabilityEntry block = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block", id));
        return mapToDTO(block);
    }

    @Transactional
    public BlockResponseDTO updateBlock(final Long id, final BlockRequestDTO blockDTO) {
        final AvailabilityEntry block = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block", id));

        if (!block.getProperty().getOwner().equals(jwtService.getLoggedUser())) {
            throw new ConflictException("You can't update a block that you don't own");
        }

        block.setStartDate(blockDTO.getStartDate());
        block.setEndDate(blockDTO.getEndDate());
        final AvailabilityEntry savedBlock = availabilityRepository.save(block);
        return mapToDTO(savedBlock);
    }

    @Transactional
    public void deleteBlock(final Long id) {
        final AvailabilityEntry block = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block", id));

        if (!block.getProperty().getOwner().equals(jwtService.getLoggedUser())) {
            throw new ConflictException("You can't delete a block that you don't own");
        }

        availabilityRepository.delete(block);
    }

    private BlockResponseDTO mapToDTO(final AvailabilityEntry block) {
        return BlockResponseDTO.builder()
                .id(block.getId())
                .ownerId(block.getProperty().getOwner().getId())
                .propertyId(block.getProperty().getId())
                .startDate(block.getStartDate())
                .endDate(block.getEndDate())
                .build();
    }
}
