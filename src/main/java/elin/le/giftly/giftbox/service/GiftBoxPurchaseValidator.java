package elin.le.giftly.giftbox.service;

import elin.le.giftly.common.enums.CustomComponentType;
import elin.le.giftly.common.enums.DimensionUnit;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.VariantStatus;
import elin.le.giftly.common.enums.WeightUnit;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.giftbox.entity.GiftBoxConfig;
import elin.le.giftly.giftbox.entity.GiftBoxConfigItem;
import elin.le.giftly.giftbox.repository.GiftBoxConfigItemRepository;
import elin.le.giftly.giftbox.repository.GiftBoxConfigRepository;
import elin.le.giftly.product.entity.GiftBoxVariantDetail;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GiftBoxPurchaseValidator {

    private static final int MIN_CONTENT_QUANTITY = 3;

    private final GiftBoxConfigRepository giftBoxConfigRepository;
    private final GiftBoxConfigItemRepository giftBoxConfigItemRepository;
    private final ProductVariantRepository productVariantRepository;

    public ValidatedGiftBox validate(UUID configId, UUID userId, int boxQuantity) {
        if (boxQuantity < 1) {
            throw new BadRequestException("Gift box quantity must be at least 1");
        }

        GiftBoxConfig config = giftBoxConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBox", "id", configId));
        if (!config.getUserId().equals(userId)) {
            throw new BadRequestException("You do not have access to this gift box");
        }

        List<GiftBoxConfigItem> configItems = giftBoxConfigItemRepository.findByConfigId(configId);
        List<ValidatedItem> items = new ArrayList<>();
        EnumSet<CustomComponentType> componentTypes = EnumSet.noneOf(CustomComponentType.class);
        Map<UUID, Long> stockRequirements = new HashMap<>();
        int boxCount = 0;
        long contentQuantity = 0;
        BigDecimal unitPrice = BigDecimal.ZERO;
        ProductVariant boxVariant = null;

        for (GiftBoxConfigItem item : configItems) {
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new BadRequestException("Gift box item quantity must be at least 1");
            }
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product Variant", "id", item.getProductVariantId()));
            validateAvailable(variant);

            if (item.getComponentType() == CustomComponentType.BOX) {
                boxCount++;
                boxVariant = variant;
                if (item.getQuantity() != 1
                        || variant.getProduct().getProductType() != ProductType.READY_MADE_GIFT_BOX
                        || variant.getProduct().getCustomComponentType() != CustomComponentType.BOX) {
                    throw new BadRequestException("A custom gift box must contain exactly one available box variant");
                }
            } else if (item.getComponentType() != null) {
                CustomComponentType productComponentType = variant.getProduct().getCustomComponentType();
                if (item.getComponentType() == CustomComponentType.BOX
                        || item.getComponentType() != productComponentType
                        || !componentTypes.add(item.getComponentType())) {
                    throw new BadRequestException("Gift box component selection is invalid or duplicated");
                }
                if (item.getQuantity() != 1) {
                    throw new BadRequestException("Only one component can be selected for type: " + item.getComponentType());
                }
            } else {
                validateContentVariant(variant);
                if (!isAccessory(variant)) {
                    contentQuantity += item.getQuantity();
                }
            }

            long requiredStock = (long) item.getQuantity() * boxQuantity;
            stockRequirements.merge(variant.getId(), requiredStock, Long::sum);
            BigDecimal itemPrice = item.getComponentType() == null && isPackagingAccessory(variant)
                    ? BigDecimal.ZERO : variant.getPrice();
            unitPrice = unitPrice.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            items.add(new ValidatedItem(item, variant, itemPrice));
        }

        if (boxCount != 1) {
            throw new BadRequestException("A custom gift box must contain exactly one box");
        }
        if (contentQuantity < MIN_CONTENT_QUANTITY) {
            throw new BadRequestException("A custom gift box requires at least 3 non-accessory items");
        }
        validateStock(stockRequirements);
        validateBoxCapacity(boxVariant, items);

        return new ValidatedGiftBox(config, List.copyOf(items), unitPrice, Map.copyOf(stockRequirements));
    }

    private void validateAvailable(ProductVariant variant) {
        if (variant.getStatus() != VariantStatus.AVAILABLE
                || variant.getProduct().getStatus() != ProductStatus.AVAILABLE) {
            throw new BadRequestException("Product variant is not available: " + variant.getSku());
        }
    }

    private void validateContentVariant(ProductVariant variant) {
        if (variant.getProduct().getCustomComponentType() != null) {
            throw new BadRequestException("Custom components must be selected through the components field");
        }
        if (variant.getProduct().getProductType() == ProductType.READY_MADE_GIFT_SET
                || variant.getProduct().getProductType() == ProductType.READY_MADE_GIFT_BOX) {
            throw new BadRequestException("Ready-made sets and boxes cannot be added as gift box contents");
        }
        if (variant.getProduct().getProductType() == ProductType.REGULAR
                && Boolean.FALSE.equals(variant.getProduct().getIsCustomBoxEligible())) {
            throw new BadRequestException("Product is not eligible for custom box: " + variant.getProduct().getName());
        }
    }

    private void validateStock(Map<UUID, Long> stockRequirements) {
        for (Map.Entry<UUID, Long> requirement : stockRequirements.entrySet()) {
            ProductVariant variant = productVariantRepository.findById(requirement.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", requirement.getKey()));
            if (variant.getStockQuantity() < requirement.getValue()) {
                throw new BadRequestException("Insufficient stock for: " + variant.getProduct().getName()
                        + ". Available: " + variant.getStockQuantity());
            }
        }
    }

    private void validateBoxCapacity(ProductVariant boxVariant, List<ValidatedItem> items) {
        GiftBoxVariantDetail detail = boxVariant.getGiftBoxVariantDetail();
        if (detail == null) {
            return;
        }

        boolean hasAnyInnerDimension = detail.getInnerLength() != null
                || detail.getInnerWidth() != null || detail.getInnerHeight() != null;
        boolean hasAllInnerDimensions = detail.getInnerLength() != null
                && detail.getInnerWidth() != null && detail.getInnerHeight() != null;
        if (hasAnyInnerDimension && !hasAllInnerDimensions) {
            throw new BadRequestException("Gift box inner dimensions are incomplete");
        }

        BigDecimal totalWeightKg = BigDecimal.ZERO;
        for (ValidatedItem item : items) {
            if (item.configItem().getComponentType() == CustomComponentType.BOX) {
                continue;
            }
            ProductVariant variant = item.variant();
            if (detail.getMaxWeight() != null) {
                if (variant.getWeight() == null || variant.getWeightUnit() == null) {
                    throw new BadRequestException("Weight is required to validate gift box capacity for: "
                            + variant.getProduct().getName());
                }
                totalWeightKg = totalWeightKg.add(toKilograms(variant.getWeight(), variant.getWeightUnit())
                        .multiply(BigDecimal.valueOf(item.configItem().getQuantity())));
            }
            if (hasAllInnerDimensions && !isAccessory(variant)) {
                validateItemFitsBox(variant, detail);
            }
        }

        if (detail.getMaxWeight() != null) {
            if (detail.getWeightUnit() == null
                    || totalWeightKg.compareTo(toKilograms(detail.getMaxWeight().doubleValue(), detail.getWeightUnit())) > 0) {
                throw new BadRequestException("Gift box contents exceed the maximum weight");
            }
        }
    }

    private void validateItemFitsBox(ProductVariant variant, GiftBoxVariantDetail detail) {
        if (variant.getLength() == null || variant.getWidth() == null || variant.getHeight() == null
                || variant.getDimensionUnit() == null || detail.getDimensionUnit() == null) {
            throw new BadRequestException("Dimensions are required to validate gift box capacity for: "
                    + variant.getProduct().getName());
        }

        double[] itemDimensions = {
                toMeters(variant.getLength(), variant.getDimensionUnit()),
                toMeters(variant.getWidth(), variant.getDimensionUnit()),
                toMeters(variant.getHeight(), variant.getDimensionUnit())
        };
        double[] boxDimensions = {
                toMeters(detail.getInnerLength(), detail.getDimensionUnit()),
                toMeters(detail.getInnerWidth(), detail.getDimensionUnit()),
                toMeters(detail.getInnerHeight(), detail.getDimensionUnit())
        };

        if (!fits(itemDimensions, boxDimensions)) {
            throw new BadRequestException("Product does not fit inside the selected gift box: "
                    + variant.getProduct().getName());
        }
    }

    private boolean fits(double[] item, double[] box) {
        return fits(item[0], item[1], item[2], box[0], box[1], box[2])
                || fits(item[0], item[2], item[1], box[0], box[1], box[2])
                || fits(item[1], item[0], item[2], box[0], box[1], box[2])
                || fits(item[1], item[2], item[0], box[0], box[1], box[2])
                || fits(item[2], item[0], item[1], box[0], box[1], box[2])
                || fits(item[2], item[1], item[0], box[0], box[1], box[2]);
    }

    private boolean fits(double length, double width, double height, double boxLength, double boxWidth, double boxHeight) {
        return length <= boxLength && width <= boxWidth && height <= boxHeight;
    }

    private double toMeters(double value, String unit) {
        return value * switch (DimensionUnit.valueOf(unit.toUpperCase())) {
            case MM -> 0.001;
            case CM -> 0.01;
            case M -> 1.0;
        };
    }

    private BigDecimal toKilograms(double value, String unit) {
        return toKilograms(BigDecimal.valueOf(value), unit);
    }

    private BigDecimal toKilograms(BigDecimal value, String unit) {
        return switch (WeightUnit.valueOf(unit.toUpperCase())) {
            case G -> value.divide(BigDecimal.valueOf(1000));
            case KG -> value;
        };
    }

    private boolean isAccessory(ProductVariant variant) {
        return isPackagingAccessory(variant)
                || variant.getProduct().getProductType() == ProductType.GREETING_CARD;
    }

    private boolean isPackagingAccessory(ProductVariant variant) {
        return variant.getProduct().getProductType() == ProductType.PACKAGING_ACCESSORY;
    }

    public record ValidatedGiftBox(
            GiftBoxConfig config,
            List<ValidatedItem> items,
            BigDecimal unitPrice,
            Map<UUID, Long> stockRequirements
    ) {}

    public record ValidatedItem(GiftBoxConfigItem configItem, ProductVariant variant, BigDecimal unitPrice) {}
}