package com.macedo.auth.authsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "PagedResponse",
        description = "Resposta paginada de listagens"
)
public class PagedResponse<T> {

    @Schema(description = "Itens da página atual")
    private List<T> items;

    @Schema(description = "Informações de paginação")
    private PageInfo page;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Metadados de paginação")
    public static class PageInfo {

        @Schema(description = "Número da página atual (0-indexed)", example = "0")
        private int number;

        @Schema(description = "Tamanho da página", example = "10")
        private int size;

        @Schema(description = "Total de itens", example = "100")
        private long totalItems;

        @Schema(description = "Total de páginas", example = "10")
        private int totalPages;
    }

    public static <T> PagedResponse<T> of(List<T> items, int pageNumber, int pageSize, long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return PagedResponse.<T>builder()
                .items(items)
                .page(PageInfo.builder()
                        .number(pageNumber)
                        .size(pageSize)
                        .totalItems(totalItems)
                        .totalPages(totalPages)
                        .build())
                .build();
    }
}
