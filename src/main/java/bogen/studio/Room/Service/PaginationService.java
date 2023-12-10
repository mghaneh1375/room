package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.PaginationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;



@Service
public class PaginationService {

    public Pageable buildPageable(
            int page,
            int size,
            String sortBaseField,
            String inputSortDirection
    ) {

        Sort.Direction sortDirection = inputSortDirection.equalsIgnoreCase("ascending") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(sortDirection, sortBaseField));
    }

    public <T> PaginationResult<T> buildPaginationResult(Page<T> t) {

        return new PaginationResult<T> ()
                .setItemsInPage(t.getContent())
                .setTotalSearchCount(t.getTotalElements())
                .setPagesCount(t.getTotalPages())
                .setItemsInPageCount(t.getNumberOfElements())
                .setPage(t.getPageable().getPageNumber())
                .setSize(t.getPageable().getPageSize())
                .setEmptyPage(t.isEmpty())
                .setFirstPage(t.isFirst())
                .setLastPage(t.isLast());
    }


}
