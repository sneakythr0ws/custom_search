package org.nick.util.customsearch.model;

import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Created by VNikolaenko on 29.06.2015.
 */
@Value
@RequiredArgsConstructor
public class Store {
    /*
    static final Logger log = LoggerFactory.getLogger(Store.class);
    public static final String COOKIE = "Cookie";
    public static final String AEP_USUC_F_SITE_GLO_REGION_RU_B_LOCALE_EN_US_C_TP_RUB = "aep_usuc_f=site=glo&region=RU&b_locale=en_US&c_tp=RUB";*/

    private final String link;
    private final String title;
}
