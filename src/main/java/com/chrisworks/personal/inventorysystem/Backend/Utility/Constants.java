package com.chrisworks.personal.inventorysystem.Backend.Utility;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.DiscountModel;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.DiscountModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 1/19/2020
 * @email chriseteka@gmail.com
 */
@Component
public class Constants implements CommandLineRunner {

    //Default Discount Types
    public static final String LOYALTY_DISCOUNT = "LOYALTY DISCOUNT";

    public static final String GENERAL_DISCOUNT = "GENERAL DISCOUNT";

    public static final String OVER_SALE_DISCOUNT = "OVER SALE DISCOUNT";

    public static final String UNDER_SALE_DISCOUNT = "UNDER SALE DISCOUNT";

    public static final String DEFAULT_CREATOR = "SYSTEM@ADMIN.COM";

    private final DiscountModelRepository discountModelRepository;

    @Autowired
    public Constants(DiscountModelRepository discountModelRepository) {
        this.discountModelRepository = discountModelRepository;
    }

    public final static List<String> DEFAULT_DISCOUNTS = new ArrayList<>(Arrays.asList(LOYALTY_DISCOUNT,
            GENERAL_DISCOUNT, OVER_SALE_DISCOUNT, UNDER_SALE_DISCOUNT));

    //Persist default, all users of the app can access this in addition to the ones they choose to create
    //This will also be used during applications stock and income balancing.
    //Created by is system@admin.com
    @Override
    public void run(String... args) throws Exception {

        List<DiscountModel> defaultDiscountModels = discountModelRepository.findAllByCreatedBy(DEFAULT_CREATOR);

        if (defaultDiscountModels.isEmpty()) {
            persistDefaultDiscountModels();
            return;
        }

        if (defaultDiscountModels.size() < 4) {
            discountModelRepository.deleteAll();
            persistDefaultDiscountModels();
        }
    }

    private void persistDefaultDiscountModels(){
        DEFAULT_DISCOUNTS
            .forEach(discountName -> {

                DiscountModel discountModel = new DiscountModel();

                discountModel.setCreatedBy(DEFAULT_CREATOR);
                discountModel.setDiscountName(discountName);
                discountModelRepository.save(discountModel);
            });
    }
}
