package bogen.studio.Room.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PaginationResult <T> {

    List<T> itemsInPage;
    long totalSearchCount;
    int pagesCount;
    int itemsInPageCount;
    int page;
    int size;
    boolean emptyPage;
    boolean firstPage;
    boolean lastPage;
}
