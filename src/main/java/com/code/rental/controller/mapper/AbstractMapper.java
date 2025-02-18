package com.code.rental.controller.mapper;

import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

interface AbstractMapper<CLASS, DTO> {

    default List<DTO> convertToDTOs(Collection<CLASS> entities) {
        return entities == null ? null :
            entities.stream()
                .filter(Objects::nonNull)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    default List<CLASS> convertToEntities(Collection<DTO> dtos) {
        return dtos == null ? null :
            dtos.stream()
                .filter(Objects::nonNull)
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    DTO convertToDTO(CLASS entity);

    default CLASS convertToEntity(DTO dto) {
        return null;
    }

    default Page<DTO> convertToPageDTO(Page<CLASS> entity) {
        return entity.map(this::convertToDTO);
    }
}
