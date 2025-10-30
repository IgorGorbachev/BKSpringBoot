package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.client;

import com.igorgorbachev.SpringBootBK.exception.ArmtekException;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.config.ArmtekConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
public class ArmtekRequestBuilder {

    private final ArmtekConfig armtekConfig;

    public MultiValueMap<String, String> buildFormData(String article, String brand) {
        if (article == null || article.trim().isEmpty()) {
            throw new ArmtekException("Article (PIN) is required for search");
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("VKORG", armtekConfig.getVkorg());
        formData.add("KUNNR_RG", armtekConfig.getKunnrRg());
        formData.add("PIN", article.trim());
        formData.add("BRAND", brand != null ? brand.trim() : "");
        formData.add("QUERY_TYPE", "2"); // Без аналогов

        return formData;
    }
}
