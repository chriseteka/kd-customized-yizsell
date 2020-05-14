package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Promo;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ShopStocks;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.PromoRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopStocksRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 5/14/2020
 * @email chriseteka@gmail.com
 */
@Service
@RequiredArgsConstructor
public class PromoServicesImpl implements PromoServices {

    private final PromoRepository promoRepository;
    private final ShopStocksRepository shopStocksRepository;
    private final SellerRepository sellerRepository;

    @Override
    public Promo addStockToPromo(Long promoId, Long stockId) {

        preAuthorize();
        return shopStocksRepository.findById(stockId).map(stock -> {

                if (!stock.getShop().getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                    throw new InventoryAPIOperationException("Stock not found", "Stock with id: " + stockId
                    + " was not found in any of your shops, hence operation cannot proceed", null);

                Promo existingPromo = getSingleEntity(promoId);
                Set<ShopStocks> existingStock = new HashSet<>(existingPromo.getPromoOnStock());
                existingStock.add(stock);
                existingPromo.setPromoOnStock(existingStock);

                return promoRepository.save(existingPromo);
            }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Stock not found",
                    "Stock with id: " + stockId + " was not found.", null));
    }

    @Override
    public Promo removeStockFromPromo(Long promoId, Long stockId) {

        preAuthorize();
        Promo existingPromo = getSingleEntity(promoId);
        Set<ShopStocks> existingStock = new HashSet<>(existingPromo.getPromoOnStock());
        existingStock.removeIf(s -> s.getShopStockId().equals(stockId));
        existingPromo.setPromoOnStock(existingStock);

        return promoRepository.save(existingPromo);
    }

    @Override
    public Promo endOrStartPromo(Long promoId) {

        preAuthorize();
        Promo existingPromo = getSingleEntity(promoId);
        if(existingPromo.isActive()) existingPromo.setActive(false);
        else existingPromo.setActive(true);

        return promoRepository.save(existingPromo);
    }

    @Override
    public List<Promo> fetchAllActivePromo() {

        String email;
        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){
            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
            email = seller.getCreatedBy();
        }

        else email = AuthenticatedUserDetails.getUserFullName();

        return promoRepository.findAllByCreatedBy(email).stream().filter(Promo::isActive).collect(Collectors.toList());
    }

    @Override
    public List<ShopStocks> fetchAllStockWithPromo() {

        return processPromoList(getEntityList());
    }

    @Override
    public List<ShopStocks> fetchAllStockWithActivePromo() {

        return processPromoList(fetchAllActivePromo());
    }

    @Override
    public Promo createEntity(Promo promo) {

        preAuthorize();
        if (promoRepository.findDistinctByPromoNameAndCreatedBy(promo.getPromoName(),
                AuthenticatedUserDetails.getUserFullName()) != null)
            throw new InventoryAPIDuplicateEntryException("Promo Already exist", "Promo Already exist", null);

        return promoRepository.save(promo);
    }

    @Override
    public Promo updateEntity(Long entityId, Promo promo) {

        preAuthorize();
        Promo existingPromo = getSingleEntity(entityId);
        existingPromo.setUpdateDate(new Date());
        existingPromo.setMinimumPurchase(promo.getMinimumPurchase());
        existingPromo.setRewardPerMinimum(promo.getRewardPerMinimum());
        if (!promo.getPromoName().equalsIgnoreCase(existingPromo.getPromoName())){
            if(promoRepository.findDistinctByPromoNameAndCreatedBy(promo.getPromoName(),
                    AuthenticatedUserDetails.getUserFullName()) == null)
            existingPromo.setPromoName(promo.getPromoName());
            else
                throw new InventoryAPIOperationException("Cannot Update Promo",
                        "You are trying to update a promo with a name already existing in your list of promos", null);
        }

        return promoRepository.save(promo);
    }

    @Override
    public Promo getSingleEntity(Long entityId) {

        preAuthorize();
        return promoRepository.findById(entityId)
                .map(promo -> {
                    if (!promo.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIResourceNotFoundException("Promo not found", "Promo with id: "
                                + entityId + " was not found in your list of promos", null);
                    return promo;
                })
                .orElseThrow(() -> new InventoryAPIResourceNotFoundException("Promo not found",
                        "Promo with id: " + entityId + " was not found", null));
    }

    @Override
    public List<Promo> getEntityList() {

        preAuthorize();
        return promoRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
    }

    @Override
    public Promo deleteEntity(Long entityId) {

        preAuthorize();
        Promo promo = getSingleEntity(entityId);
        promo.setPromoOnStock(null);
        promoRepository.delete(promo);
        return promo;
    }

    private void preAuthorize(){

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);
    }

    private List<ShopStocks> processPromoList(List<Promo> promoList){
        return promoList
                .stream()
                .map(Promo::getPromoOnStock)
                .flatMap(Set::parallelStream)
                .peek(s -> s.setHasPromo(true))
                .collect(Collectors.toList());
    }
}
