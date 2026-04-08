package com.example.flashbid.product.service;

import com.example.flashbid.auction.entity.Auction;
import com.example.flashbid.auction.repo.AuctionRepo;
import com.example.flashbid.auction.service.AuctionManagementService;
import com.example.flashbid.bid.entity.Bid;
import com.example.flashbid.bid.repo.BidRepo;
import com.example.flashbid.common.exception.UserAccessDeniedException;
import com.example.flashbid.common.redis.AuctionLiveUpdateService;
import com.example.flashbid.common.redis.AuctionRedisCacheService;
import com.example.flashbid.common.redis.AuctionSummaryCache;
import com.example.flashbid.common.util.EntityFetcher;
import com.example.flashbid.product.dto.CreateProductDto;
import com.example.flashbid.product.dto.EditProductDto;
import com.example.flashbid.product.dto.ProductDto;
import com.example.flashbid.product.entity.Product;
import com.example.flashbid.product.entity.ProductStatus;
import com.example.flashbid.product.repo.ProductRepo;
import com.example.flashbid.user.dto.UserDto;
import com.example.flashbid.user.entity.Role;
import com.example.flashbid.user.entity.User;
import com.example.flashbid.user.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo productRepo;
    private final AuctionRepo auctionRepo;
    private final AuctionManagementService auctionManagementService;
    private final BidRepo bidRepo;
    private final AuctionRedisCacheService auctionRedisCacheService;
    private final AuctionLiveUpdateService auctionLiveUpdateService;
    private final EntityFetcher entityFetcher;
    private final UserRepo userRepo;

    public ProductDto addProduct(CreateProductDto createProductDto) {
        User currentUser = entityFetcher.getCurrentUser();
        if (!canManageAuctions(currentUser)) {
            throw new UserAccessDeniedException("Only approved sellers or admins can create auctions.");
        }

        Product product = new Product();
        product.setName(createProductDto.getName());
        product.setStartingPrice(createProductDto.getStartingPrice());
        product.setStartTime(createProductDto.getStartTime());
        product.setEndTime(createProductDto.getEndTime());
        product.setDescription(createProductDto.getDescription());
        product.setUser(currentUser);
        product.setProductStatus(ProductStatus.SCHEDULED);

        Product savedProduct = productRepo.save(product);

        Auction auction = new Auction();
        auction.setProduct(savedProduct);
        auction.setStartTime(createProductDto.getStartTime());
        auction.setEndTime(createProductDto.getEndTime());
        auction.setMinimumIncrement(createProductDto.getMinimumIncrement());
        auction.setStatus(ProductStatus.SCHEDULED);
        auctionRepo.save(auction);
        auctionLiveUpdateService.scheduleRefresh(savedProduct.getId(), "AUCTION_CREATED");

        return mapToDto(savedProduct, auction);
    }

    @Transactional
    public ProductDto getProductById(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        Auction auction = entityFetcher.getAuctionByProductId(productId);
        return mapToDto(product, auction);
    }

    @Transactional
    public Page<ProductDto> getAllProducts(Optional<Integer> page, Optional<String> sortBy, 
                                          Optional<String> sortDir,
                                          Optional<String> name, Optional<ProductStatus> productStatus) {
        Sort.Direction direction = sortDir.orElse("desc").equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        PageRequest pageRequest = PageRequest.of(
                page.orElse(0),
                12,
                direction,
                sortBy.orElse("createdAt")
        );

        Page<Product> products;
        if (name.isPresent() && productStatus.isPresent()) {
            products = productRepo.findByNameContainingIgnoreCaseAndProductStatus(name.get(), productStatus.get(), pageRequest);
        } else if (name.isPresent()) {
            products = productRepo.findByNameContainingIgnoreCase(name.get(), pageRequest);
        } else if (productStatus.isPresent()) {
            products = productRepo.findByProductStatus(productStatus.get(), pageRequest);
        } else {
            products = productRepo.findAll(pageRequest);
        }

        List<Product> content = products.getContent();
        List<Long> productIds = content.stream().map(Product::getId).toList();
        Map<Long, Auction> auctionsByProductId = auctionRepo.findByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(auction -> auction.getProduct().getId(), auction -> auction));
        Map<Long, BidRepo.ProductBidAggregate> bidAggregates = bidRepo.findAggregatesByProductIds(productIds).stream()
                .collect(Collectors.toMap(BidRepo.ProductBidAggregate::getProductId, aggregate -> aggregate));

        return products.map(product -> mapToDto(
                product,
                auctionsByProductId.get(product.getId()),
                auctionRedisCacheService.getSummary(product.getId()),
                bidAggregates.get(product.getId())
        ));
    }

    @Transactional
    public List<ProductDto> getProductsByUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return productRepo.findByUser(user).stream()
                .map(product -> mapToDto(product, null, null, null))
                .collect(Collectors.toList());
    }

    public ProductDto editProduct(Long id, EditProductDto editProductDto) {
        User currentUser = entityFetcher.getCurrentUser();
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (!currentUser.getRole().equals(Role.ADMIN) && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UserAccessDeniedException("You do not have permission to edit this product.");
        }

        if (editProductDto.getName() != null) product.setName(editProductDto.getName());
        if (editProductDto.getDescription() != null) product.setDescription(editProductDto.getDescription());

        return mapToDto(productRepo.save(product), null, null, null);
    }

    public String deleteProduct(Long productId) {
        User currentUser = entityFetcher.getCurrentUser();
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (!currentUser.getRole().equals(Role.ADMIN) && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UserAccessDeniedException("You do not have permission to delete this product.");
        }

        auctionRepo.deleteByProductId(productId);
        productRepo.delete(product);
        return "Product deleted successfully.";
    }

    private boolean canManageAuctions(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.SELLER;
    }

    private ProductDto mapToDto(Product product, Auction existingAuction) {
        return mapToDto(product, existingAuction, null, null);
    }

    private ProductDto mapToDto(
            Product product,
            Auction existingAuction,
            AuctionSummaryCache providedSummary,
            BidRepo.ProductBidAggregate bidAggregate
    ) {
        Auction auction = existingAuction != null ? existingAuction : auctionRepo.findByProductId(product.getId()).orElse(null);
        AuctionSummaryCache cachedSummary = providedSummary != null ? providedSummary : auctionRedisCacheService.getSummary(product.getId());
        long minimumIncrement = cachedSummary != null
                ? cachedSummary.getMinimumIncrement()
                : auction != null ? auction.getMinimumIncrement() : 1L;
        Long currentBid = cachedSummary != null ? cachedSummary.getCurrentBid() : null;
        Long nextMinimumBid = cachedSummary != null ? cachedSummary.getNextMinimumBid() : null;
        Long bidCount = cachedSummary != null ? cachedSummary.getBidCount() : null;
        ProductStatus effectiveStatus = auction != null
                ? auctionManagementService.resolveStatus(auction)
                : cachedSummary != null ? cachedSummary.getStatus() : product.getProductStatus();

        if (currentBid == null || nextMinimumBid == null || bidCount == null) {
            if (bidAggregate != null) {
                bidCount = bidAggregate.getBidCount();
                currentBid = bidAggregate.getHighestAmount();
            } else {
                Optional<Bid> highestBid = bidRepo.findTopByProductOrderByAmountDesc(product);
                bidCount = bidRepo.countByProductId(product.getId());
                currentBid = highestBid.map(Bid::getAmount).orElse(product.getStartingPrice());
            }
            nextMinimumBid = currentBid + minimumIncrement;
        }

        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .startingPrice(product.getStartingPrice())
                .minimumIncrement(minimumIncrement)
                .currentBid(currentBid)
                .nextMinimumBid(nextMinimumBid)
                .bidCount(bidCount)
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .description(product.getDescription())
                .createdAt(product.getCreatedAt())
                .productStatus(effectiveStatus)
                .user(UserDto.builder()
                        .id(product.getUser().getId())
                        .username(product.getUser().getUsername())
                        .firstName(product.getUser().getFirstName())
                        .lastName(product.getUser().getLastName())
                        .email(product.getUser().getEmail())
                        .role(product.getUser().getRole())
                        .registrationDate(product.getUser().getRegistrationDate())
                        .sellerRequested(product.getUser().isSellerRequested())
                        .banned(product.getUser().isBanned())
                        .deleted(product.getUser().isDeleted())
                        .build())
                .build();
    }
}
