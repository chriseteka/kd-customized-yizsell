package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.DiscountModel;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.DiscountModelRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Constants.DEFAULT_CREATOR;

@Service
public class DiscountModelServicesImpl implements DiscountModelServices  {

    private final DiscountModelRepository discountModelRepository;

    private final SellerRepository sellerRepository;

    @Autowired
    public DiscountModelServicesImpl(DiscountModelRepository discountModelRepository, SellerRepository sellerRepository) {
        this.discountModelRepository = discountModelRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public DiscountModel createEntity(DiscountModel discountModel) {

        preAuthorize();

        DiscountModel discountModelFound = discountModelRepository.findDistinctByDiscountNameAndCreatedBy
                (discountModel.getDiscountName(), AuthenticatedUserDetails.getUserFullName());

        if (discountModelFound != null) throw new InventoryAPIDuplicateEntryException("Duplicate entry",
                "A discount model with same name already exist in your business", null);

        discountModel.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
        return discountModelRepository.save(discountModel);
    }

    @Override
    public DiscountModel updateEntity(Long entityId, DiscountModel discountModel) {

        preAuthorize();

        DiscountModel discountModelFound = discountModelRepository.findDistinctByDiscountNameAndCreatedBy
                (discountModel.getDiscountName(), AuthenticatedUserDetails.getUserFullName());

        if (discountModelFound != null
                && !discountModelFound.getCreatedBy().equals(AuthenticatedUserDetails.getUserFullName()))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Cannot update a discount model that was not created by you.", null);

        if (discountModelFound != null && !discountModelFound.getDiscountModelId().equals(entityId))
            throw new InventoryAPIDuplicateEntryException("Duplicate entry",
                "A discount model with same name already exist in your business", null);

        return discountModelRepository.findById(entityId).map(d -> {

            d.setUpdateDate(new Date());
            d.setDiscountName(discountModel.getDiscountName());
            d.setDiscountPercentage(discountModel.getDiscountPercentage());

            return discountModelRepository.save(d);
        }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Discount model not found",
                "Cannot find discount model with id: " + entityId, null));
    }

    @Override
    public DiscountModel getSingleEntity(Long entityId) {

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException("Unknown user",
                "Cannot determine the account type of the logged in user", null);

        return discountModelRepository.findById(entityId)
                .map(discountModel -> {

                    if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

                        if (!discountModel.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                            throw new InventoryAPIOperationException("Not your module", "Discount module with id: "
                                    + entityId + " was not found in your list of discount modules", null);

                        return discountModel;
                    }else{
                        Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());

                        if (!discountModel.getCreatedBy().equalsIgnoreCase(seller.getCreatedBy()))
                            throw new InventoryAPIOperationException("Not your module", "Discount module with id: "
                                    + entityId + " was not found in your list of discount modules", null);

                        return discountModel;
                    }
                })
                .orElseThrow(() -> new InventoryAPIResourceNotFoundException("Discount model not found",
                        "Discount model with id: " + entityId + " was not found", null));
    }

    @Override
    public List<DiscountModel> getEntityList() {

        List<DiscountModel> discountModelList = discountModelRepository.findAllByCreatedBy(DEFAULT_CREATOR);

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException("Unknown user",
                "Cannot determine the account type of the logged in user", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            discountModelList.addAll(discountModelRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName()));
        else {

            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());

            discountModelList.addAll(discountModelRepository.findAllByCreatedBy(seller.getCreatedBy()));
        }

        return discountModelList;
    }

    @Override
    public DiscountModel deleteEntity(Long entityId) {

        preAuthorize();

        return discountModelRepository.findById(entityId).map(discountModel -> {

            if (!discountModel.getCreatedBy().equals(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Operation not allowed",
                        "Cannot delete a discount model that was not created by you.", null);

            discountModelRepository.delete(discountModel);

            return discountModel;
        }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Discount model not found",
                "Cannot find discount model with id: " + entityId, null));
    }

    private void preAuthorize(){

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);
    }
}
