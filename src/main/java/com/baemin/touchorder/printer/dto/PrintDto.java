package com.baemin.touchorder.printer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PrintDto {
    private List<PrintItem> assetItems;

    public List<PrintItem> getSortedList() {
        return assetItems.stream()
                .sorted(Comparator.comparing(PrintItem::getUserName))
                .collect(Collectors.toList());
    }
}
