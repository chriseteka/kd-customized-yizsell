package com.chrisworks.personal.inventorysystem.Backend.Entities;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 1/16/2020
 * @email chriseteka@gmail.com
 */
@Data
public class ListWrapper {

    private int numberOfPages;

    private int totalNumberOfData;

    private List<?> content;

    public static ListWrapper prepareResponse(List<?> data, int page, int size){

        ListWrapper response = new ListWrapper();

        int dataSize = data.size();

        int numberOfPages = 0;

        if (page == 0 || size == 0){

            if (dataSize > 1) numberOfPages = 1;
            response.setNumberOfPages(numberOfPages);
            response.setContent(data);
            response.setTotalNumberOfData(dataSize);
        }
        else {
            List<?> paginatedList = data
                    .stream()
                    .skip((size * (page - 1)))
                    .limit(size)
                    .collect(Collectors.toList());
            int paginatedListSize = paginatedList.size();

            if (dataSize != 0 && paginatedListSize != 0)
                numberOfPages = dataSize % paginatedListSize != 0
                        ? (dataSize / paginatedListSize) + 1
                        : (dataSize / paginatedListSize);

            response.setContent(paginatedList);
            response.setNumberOfPages(numberOfPages);
            response.setTotalNumberOfData(dataSize);
        }

        return response;
    }
}
