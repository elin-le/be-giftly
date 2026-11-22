package elin.le.giftly.user.service;

import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.user.dto.AddressResponse;
import elin.le.giftly.user.dto.CreateAddressRequest;
import elin.le.giftly.user.dto.UpdateAddressRequest;
import elin.le.giftly.user.entity.Address;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.AddressRepository;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional
    public AddressResponse createAddress(CreateAddressRequest request) {
        UUID userId = getCurrentUserId();

        // If setting as default, unset other defaults
        if (Boolean.TRUE.equals(request.isDefault())) {
            Address defaultAddress = addressRepository.findByUserIdAndIsDefault(userId, true);
            if (defaultAddress != null) {
                defaultAddress.setIsDefault(false);
                addressRepository.save(defaultAddress);
            }
        }

        Address address = Address.builder()
                .userId(userId)
                .recipientName(request.recipientName())
                .phone(request.phone())
                .addressLine(request.addressLine())
                .ward(request.ward())
                .district(request.district())
                .city(request.city())
                .isDefault(request.isDefault() != null ? request.isDefault() : false)
                .build();

        address = addressRepository.save(address);
        return toAddressResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(UUID addressId, UpdateAddressRequest request) {
        UUID userId = getCurrentUserId();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getUserId().equals(userId)) {
            throw new BadRequestException("Address does not belong to you");
        }

        address.setRecipientName(request.recipientName());
        address.setPhone(request.phone());
        address.setAddressLine(request.addressLine());
        address.setWard(request.ward());
        address.setDistrict(request.district());
        address.setCity(request.city());

        address = addressRepository.save(address);
        return toAddressResponse(address);
    }

    @Transactional
    public AddressResponse setAddressAsDefault(UUID addressId) {
        UUID userId = getCurrentUserId();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getUserId().equals(userId)) {
            throw new BadRequestException("Address does not belong to you");
        }

        // Unset other defaults
        Address defaultAddress = addressRepository.findByUserIdAndIsDefault(userId, true);
        if (defaultAddress != null && !defaultAddress.getId().equals(addressId)) {
            defaultAddress.setIsDefault(false);
            addressRepository.save(defaultAddress);
        }

        address.setIsDefault(true);
        address = addressRepository.save(address);
        return toAddressResponse(address);
    }

    @Transactional
    public void deleteAddress(UUID addressId) {
        UUID userId = getCurrentUserId();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getUserId().equals(userId)) {
            throw new BadRequestException("Address does not belong to you");
        }

        addressRepository.delete(address);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses() {
        UUID userId = getCurrentUserId();
        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(this::toAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressResponse getMyAddressById(UUID addressId) {
        UUID userId = getCurrentUserId();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getUserId().equals(userId)) {
            throw new BadRequestException("Address does not belong to you");
        }

        return toAddressResponse(address);
    }

    private AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getRecipientName(),
                address.getPhone(),
                address.getAddressLine(),
                address.getWard(),
                address.getDistrict(),
                address.getCity(),
                address.getIsDefault()
        );
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }
}
