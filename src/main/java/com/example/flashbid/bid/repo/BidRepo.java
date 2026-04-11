package com.example.flashbid.bid.repo;

import com.example.flashbid.bid.entity.Bid;
import com.example.flashbid.product.entity.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepo extends JpaRepository<Bid, Long> {
    interface ProductBidAggregate {
        Long getProductId();
        Long getHighestAmount();
        Long getBidCount();
    }

    Optional<Bid> findTopByProductOrderByAmountDesc(Product product);
    Page<Bid> findByProductIdOrderByTimestampDesc(Long productId, Pageable pageable);
    Page<Bid> findByProductIdOrderByAmountDesc(Long productId, Pageable pageable);
    Page<Bid> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    long countByProductId(Long productId);
    void deleteByProductId(Long productId);

    @Query("""
            select b.product.id as productId,
                   max(b.amount) as highestAmount,
                   count(b.id) as bidCount
            from Bid b
            where b.product.id in :productIds
            group by b.product.id
            """)
    List<ProductBidAggregate> findAggregatesByProductIds(@Param("productIds") List<Long> productIds);
}
