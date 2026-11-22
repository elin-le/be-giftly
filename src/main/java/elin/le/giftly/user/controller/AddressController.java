package elin.le.giftly.user.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.user.dto.AddressResponse;
import elin.le.giftly.user.dto.CreateAddressRequest;
import elin.le.giftly.user.dto.UpdateAddressRequest;
import elin.le.giftly.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Address management APIs for users")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @Operation(summary = "Create address", description = "Create a new shipping address for the current user")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @Parameter(description = "Address creation data", required = true)
            @Valid @RequestBody CreateAddressRequest request) {
        AddressResponse address = addressService.createAddress(request);
        return ResponseEntity.ok(ApiResponse.success("Address created successfully", address));
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Update address", description = "Update an existing address belonging to the current user")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @Parameter(description = "Address ID", required = true)
            @PathVariable UUID addressId,
            @Parameter(description = "Updated address data", required = true)
            @Valid @RequestBody UpdateAddressRequest request) {
        AddressResponse address = addressService.updateAddress(addressId, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    @PutMapping("/{addressId}/set-default")
    @Operation(summary = "Set default address", description = "Set an address as the default shipping address")
    public ResponseEntity<ApiResponse<AddressResponse>> setAddressAsDefault(
            @Parameter(description = "Address ID", required = true)
            @PathVariable UUID addressId) {
        AddressResponse address = addressService.setAddressAsDefault(addressId);
        return ResponseEntity.ok(ApiResponse.success("Default address set successfully", address));
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Delete address", description = "Delete an address belonging to the current user")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @Parameter(description = "Address ID", required = true)
            @PathVariable UUID addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get my addresses", description = "Retrieve all addresses belonging to the current user")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses() {
        List<AddressResponse> addresses = addressService.getMyAddresses();
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }

    @GetMapping("/{addressId}")
    @Operation(summary = "Get address by ID", description = "Retrieve details of a specific address belonging to the current user")
    public ResponseEntity<ApiResponse<AddressResponse>> getMyAddressById(
            @Parameter(description = "Address ID", required = true)
            @PathVariable UUID addressId) {
        AddressResponse address = addressService.getMyAddressById(addressId);
        return ResponseEntity.ok(ApiResponse.success("Address retrieved successfully", address));
    }
}
