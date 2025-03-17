package com.code.rental.service;

import com.code.rental.controller.dto.request.BlockRequestDTO;
import com.code.rental.controller.dto.request.BookingRequestDTO;
import com.code.rental.controller.dto.response.BlockResponseDTO;
import com.code.rental.domain.Block;
import com.code.rental.domain.Property;
import com.code.rental.domain.User;
import com.code.rental.exception.ConflictException;
import com.code.rental.exception.ResourceNotFoundException;
import com.code.rental.repository.BlockRepository;
import com.code.rental.repository.PropertyRepository;
import com.code.rental.repository.UserRepository;
import com.code.rental.security.jwt.JwtService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class BlockServiceTest {

    @Autowired
    private BlockService blockService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @MockBean
    private JwtService jwtService;

    private User owner;
    private User guest;
    private Property property;

    @BeforeEach
    @Transactional
    public void setUp() {
        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@gmail.com")
                .password("123456")
                .build());

        guest = userRepository.save(User.builder()
                .name("Guest")
                .email("guest@gmail.com")
                .password("123456")
                .build());

        property = propertyRepository.save(Property.builder()
                .name("Beach House")
                .description("3 bedroom beach house")
                .location("Miami Beach")
                .owner(owner)
                .build());
    }

    @Test
    void shouldGetBlocksByPropertyId() {
        BlockRequestDTO blockDTO1 = BlockRequestDTO.builder()
                .propertyId(property.getId())
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        BlockRequestDTO blockDTO2 = BlockRequestDTO.builder()
                .propertyId(property.getId())
                .startDate(LocalDate.parse("2025-07-01"))
                .endDate(LocalDate.parse("2025-07-10"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(owner);

        blockService.createBlock(blockDTO1);
        blockService.createBlock(blockDTO2);

        List<BlockResponseDTO> blocks = blockService.getBlocksByPropertyId(property.getId());
        assertThat(blocks).hasSize(2);
        assertThat(blocks).extracting(BlockResponseDTO::getStartDate)
                .containsExactlyInAnyOrder(LocalDate.parse("2025-06-01"), LocalDate.parse("2025-07-01"));
        assertThat(blocks).extracting(BlockResponseDTO::getEndDate)
                .containsExactlyInAnyOrder(LocalDate.parse("2025-06-10"), LocalDate.parse("2025-07-10"));
    }

    @Test
    void shouldGetBlockById() {
        BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(property.getId())
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(owner);

        BlockResponseDTO blockResponseDTO = blockService.createBlock(blockDTO);

        BlockResponseDTO block = blockService.getBlockById(blockResponseDTO.getId());
        assertThat(block).isNotNull();
        assertThat(block.getPropertyId()).isEqualTo(property.getId());
        assertThat(block.getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void shouldCreateBlock() {
        BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(property.getId())
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(owner);

        BlockResponseDTO blockResponseDTO = blockService.createBlock(blockDTO);
        assertThat(blockResponseDTO).isNotNull();
        assertThat(blockResponseDTO.getPropertyId()).isEqualTo(property.getId());
        assertThat(blockResponseDTO.getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void shouldThrowIfPropertyNotFound() {
        BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(999L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(owner);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            blockService.createBlock(blockDTO);
        });

        assertThat(ex.getMessage()).isEqualTo("Property not found with ID 999");
    }

    @Test
    void shouldThrowIfNotPropertyOwner() {
        BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(property.getId())
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(guest);

        ConflictException ex = assertThrows(ConflictException.class, () -> {
            blockService.createBlock(blockDTO);
        });

        assertThat(ex.getMessage()).isEqualTo("You can't block a property that you don't own");
    }

    @Test
    void shouldUpdateBlock() {
        Block block = blockRepository.save(Block.builder()
                .property(property)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build());

        BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .startDate(LocalDate.parse("2025-06-02"))
                .endDate(LocalDate.parse("2025-06-11"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(owner);

        BlockResponseDTO updatedBlock = blockService.updateBlock(block.getId(), blockDTO);
        assertThat(updatedBlock.getStartDate()).isEqualTo(LocalDate.parse("2025-06-02"));
        assertThat(updatedBlock.getEndDate()).isEqualTo(LocalDate.parse("2025-06-11"));
    }

    @Test
    void shouldThrowUpdateIfBlockNotFound() {
        BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .startDate(LocalDate.parse("2025-06-02"))
                .endDate(LocalDate.parse("2025-06-11"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(owner);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            blockService.updateBlock(999L, blockDTO);
        });

        assertThat(ex.getMessage()).isEqualTo("Block not found with ID 999");
    }

    @Test
    void shouldThrowUpdateIfNotPropertyOwner() {
        Block block = blockRepository.save(Block.builder()
                .property(property)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build());

        BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .startDate(LocalDate.parse("2025-06-02"))
                .endDate(LocalDate.parse("2025-06-11"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(guest);

        ConflictException ex = assertThrows(ConflictException.class, () -> {
            blockService.updateBlock(block.getId(), blockDTO);
        });

        assertThat(ex.getMessage()).isEqualTo("You can't update a block that you don't own");
    }

    @Test
    void shouldDeleteBlock() {
        Block block = blockRepository.save(Block.builder()
                .property(property)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build());

        when(jwtService.getLoggedUser()).thenReturn(owner);

        blockService.deleteBlock(block.getId());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            blockService.getBlockById(block.getId());
        });

        assertThat(ex.getMessage()).isEqualTo("Block not found with ID " + block.getId());
    }

    @Test
    void shouldThrowDeleteIfBlockNotFound() {
        when(jwtService.getLoggedUser()).thenReturn(owner);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            blockService.deleteBlock(999L);
        });

        assertThat(ex.getMessage()).isEqualTo("Block not found with ID 999");
    }

    @Test
    void shouldThrowDeleteIfNotPropertyOwner() {
        Block block = blockRepository.save(Block.builder()
                .property(property)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build());

        when(jwtService.getLoggedUser()).thenReturn(guest);

        ConflictException ex = assertThrows(ConflictException.class, () -> {
            blockService.deleteBlock(block.getId());
        });

        assertThat(ex.getMessage()).isEqualTo("You can't delete a block that you don't own");
    }

    @Test
    void shouldNotBlockIfThereIsAnExistingBooking() {
        // create booking
        when(jwtService.getLoggedUser()).thenReturn(guest);
        BookingRequestDTO bookingRequestDTO = BookingRequestDTO.builder()
                .propertyId(property.getId())
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingRequestDTO);

        // create block
        when(jwtService.getLoggedUser()).thenReturn(owner);
        final List<List<String>> dateRanges = List.of(
                List.of("2025-06-01", "2025-06-01"),
                List.of("2025-06-10", "2025-06-20"),
                List.of("2025-06-05", "2025-06-15"),
                List.of("2025-05-20", "2025-06-05"));

        dateRanges.forEach(dateRange -> {
            BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                    .propertyId(property.getId())
                    .startDate(LocalDate.parse(dateRange.get(0)))
                    .endDate(LocalDate.parse(dateRange.get(1)))
                    .build();
            ConflictException ex = assertThrows(ConflictException.class, () -> blockService.createBlock(blockDTO));
            assertThat(ex.getMessage()).isEqualTo("There is an active booking conflict with the block dates");
        });

        List<BlockResponseDTO> blocks = blockService.getBlocksByPropertyId(property.getId());
        assertThat(blocks).isEmpty();
    }

    @Test
    void shouldCreateBlockEvenWithExistingNonOverlappingBlock() {
        // create block
        when(jwtService.getLoggedUser()).thenReturn(owner);
        BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(property.getId())
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();
        blockService.createBlock(blockDTO);

        // create block
        blockDTO = BlockRequestDTO.builder()
                .propertyId(property.getId())
                .startDate(LocalDate.parse("2025-06-11"))
                .endDate(LocalDate.parse("2025-06-20"))
                .build();
        BlockResponseDTO blockResponseDTO = blockService.createBlock(blockDTO);

        assertThat(blockResponseDTO).isNotNull();
        assertThat(blockResponseDTO.getPropertyId()).isEqualTo(property.getId());
        assertThat(blockResponseDTO.getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void shouldCreateBlockEvenWithExistingNonOverlappingBooking() {
        // create booking
        when(jwtService.getLoggedUser()).thenReturn(guest);
        BookingRequestDTO bookingRequestDTO = BookingRequestDTO.builder()
                .propertyId(property.getId())
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();
        bookingService.createBooking(bookingRequestDTO);

        // create block
        when(jwtService.getLoggedUser()).thenReturn(owner);
        BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(property.getId())
                .startDate(LocalDate.parse("2025-06-11"))
                .endDate(LocalDate.parse("2025-06-20"))
                .build();
        BlockResponseDTO blockResponseDTO = blockService.createBlock(blockDTO);

        assertThat(blockResponseDTO).isNotNull();
        assertThat(blockResponseDTO.getPropertyId()).isEqualTo(property.getId());
        assertThat(blockResponseDTO.getOwnerId()).isEqualTo(owner.getId());
    }
}
