package elin.le.giftly.giftbox.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.giftbox.dto.CreateGiftBoxRequest;
import elin.le.giftly.giftbox.dto.GiftBoxResponse;
import elin.le.giftly.giftbox.service.GiftBoxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gift-boxes")
@RequiredArgsConstructor
public class GiftBoxController {

    private final GiftBoxService giftBoxService;

    @PostMapping
    public ResponseEntity<ApiResponse<GiftBoxResponse>> createGiftBox(
            @Valid @RequestBody CreateGiftBoxRequest request) {
        GiftBoxResponse giftBox = giftBoxService.createGiftBox(request);
        return ResponseEntity.ok(ApiResponse.success("Gift box created successfully", giftBox));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GiftBoxResponse>>> getMyGiftBoxes() {
        List<GiftBoxResponse> giftBoxes = giftBoxService.getMyGiftBoxes();
        return ResponseEntity.ok(ApiResponse.success("Gift boxes retrieved successfully", giftBoxes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GiftBoxResponse>> getGiftBoxById(@PathVariable UUID id) {
        GiftBoxResponse giftBox = giftBoxService.getGiftBoxById(id);
        return ResponseEntity.ok(ApiResponse.success("Gift box retrieved successfully", giftBox));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGiftBox(@PathVariable UUID id) {
        giftBoxService.deleteGiftBox(id);
        return ResponseEntity.ok(ApiResponse.success("Gift box deleted successfully", null));
    }
}
