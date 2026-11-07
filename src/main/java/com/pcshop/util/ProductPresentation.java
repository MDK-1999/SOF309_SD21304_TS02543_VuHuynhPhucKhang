package com.pcshop.util;

import com.pcshop.model.Product;
import com.pcshop.model.ProductSpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ProductPresentation {
    private static final Map<String, BrandProfile> BRANDS = Map.ofEntries(
        brand("amd", "AMD", "amd.com", "https://www.amd.com/en/products.html"),
        brand("intel", "Intel", "intel.com", "https://www.intel.com/content/www/us/en/products/overview.html"),
        brand("nvidia", "NVIDIA", "nvidia.com", "https://www.nvidia.com/en-us/geforce/graphics-cards/"),
        brand("asus", "ASUS", "asus.com", "https://www.asus.com/"),
        brand("msi", "MSI", "msi.com", "https://www.msi.com/"),
        brand("gigabyte", "Gigabyte", "gigabyte.com", "https://www.gigabyte.com/"),
        brand("kingston", "Kingston", "kingston.com", "https://www.kingston.com/"),
        brand("corsair", "Corsair", "corsair.com", "https://www.corsair.com/"),
        brand("g.skill", "G.Skill", "gskill.com", "https://www.gskill.com/"),
        brand("teamgroup", "TeamGroup", "teamgroupinc.com", "https://www.teamgroupinc.com/"),
        brand("samsung", "Samsung", "samsung.com", "https://semiconductor.samsung.com/consumer-storage/"),
        brand("wd", "Western Digital", "westerndigital.com", "https://www.westerndigital.com/"),
        brand("lexar", "Lexar", "lexar.com", "https://www.lexar.com/"),
        brand("crucial", "Crucial", "crucial.com", "https://www.crucial.com/"),
        brand("cooler master", "Cooler Master", "coolermaster.com", "https://www.coolermaster.com/"),
        brand("seasonic", "Seasonic", "seasonic.com", "https://seasonic.com/"),
        brand("deepcool", "DeepCool", "deepcool.com", "https://www.deepcool.com/"),
        brand("thermalright", "Thermalright", "thermalright.com", "https://www.thermalright.com/"),
        brand("arctic", "ARCTIC", "arctic.de", "https://www.arctic.de/en/"),
        brand("noctua", "Noctua", "noctua.at", "https://www.noctua.at/en/products"),
        brand("montech", "Montech", "montechpc.com", "https://www.montechpc.com/"),
        brand("nzxt", "NZXT", "nzxt.com", "https://nzxt.com/"),
        brand("lian li", "Lian Li", "lian-li.com", "https://lian-li.com/"),
        brand("hyte", "HYTE", "hyte.com", "https://hyte.com/"),
        brand("aoc", "AOC", "aoc.com", "https://aoc.com/"),
        brand("lg", "LG", "lg.com", "https://www.lg.com/"),
        brand("dell", "Dell", "dell.com", "https://www.dell.com/"),
        brand("logitech", "Logitech", "logitechg.com", "https://www.logitechg.com/"),
        brand("steelseries", "SteelSeries", "steelseries.com", "https://steelseries.com/"),
        brand("keychron", "Keychron", "keychron.com", "https://www.keychron.com/"),
        brand("razer", "Razer", "razer.com", "https://www.razer.com/"),
        brand("elgato", "Elgato", "elgato.com", "https://www.elgato.com/")
    );

    private static final Map<String, OfficialProduct> OFFICIAL = Map.ofEntries(
        Map.entry(key("AMD Ryzen 7 9800X3D"), new OfficialProduct(
            "https://www.amd.com/en/products/processors/desktops/ryzen/9000-series/amd-ryzen-7-9800x3d.html",
            "https://www.amd.com/content/dam/amd/en/images/products/processors/ryzen/2900400-ryzen-7-9800x3d-product.jpg")),
        Map.entry(key("NVIDIA GeForce RTX 5090"), new OfficialProduct(
            "https://www.nvidia.com/en-us/geforce/graphics-cards/50-series/rtx-5090/",
            "https://www.nvidia.com/content/nvidiaGDC/us/en_US/geforce/graphics-cards/50-series/rtx-5090/_jcr_content/root/responsivegrid/nv_modalcontainer_609051001/nv_image.coreimg.jpeg/1739962972157/geforce-rtx-5090-gallery-shot-1-ari.jpeg")),
        Map.entry(key("Noctua NH-D15 G2"), new OfficialProduct(
            "https://www.noctua.at/en/products/nh-d15-g2/specifications",
            "https://cdn.noctua.at/media/nh_d15_g2_hbc_4.jpg")),
        Map.entry(key("Lian Li O11 Vision Compact"), new OfficialProduct(
            "https://lian-li.com/product/o11-vision-compact/", null)),
        Map.entry(key("Intel Core i5-11400F"), new OfficialProduct(
            "https://www.intel.com/content/www/us/en/products/sku/212271/intel-core-i511400f-processor-12m-cache-up-to-4-40-ghz/specifications.html", null)),
        Map.entry(key("NVIDIA GeForce RTX 5060 Ti"), new OfficialProduct(
            "https://us-store.msi.com/GeForce-RTX-5060-Ti-16G-VENTUS-2X-OC-BLACK-PLUS",
            "https://asset-us-store.msi.com/image/cache/catalog/Pd_page/VGA/2026/5060TI16GV2XOCBP/5060TI16GV2XOCBP-8-1024x1024.png")),
        Map.entry(key("G.Skill Trident Z5 Neo RGB 32GB DDR5 6000"), new OfficialProduct(
            "https://www.gskill.com/product/165/390/1661410135/F5-6000J3038F16GX2-TZ5NR",
            "https://www.gskill.com/_upload/images/2208251640157.png")),
        Map.entry(key("TeamGroup T-Create Expert 96GB DDR5 6000"), new OfficialProduct(
            "https://www.teamgroupinc.com/en/product-detail/memory/T-CREATE/expert-u-dimm-ddr5-black/expert-u-dimm-ddr5-black-CTCED596G6400HC32ADC01/",
            "https://images.teamgroupinc.com/products/memory/u-dimm/ddr5/expert/black/dual_01.jpg")),
        Map.entry(key("Kingston NV2 1TB"), new OfficialProduct(
            "https://www.kingston.com/en/company/press/article/67873",
            "https://media.kingston.com/kingston/press/ktc-press-fe-nv2-ssd.jpg")),
        Map.entry(key("ASUS ROG Thor 1200P2 Gaming"), new OfficialProduct(
            "https://rog.asus.com/us/power-supply-units/rog-thor/rog-thor-1200p2-gaming-model/",
            "https://dlcdnwebimgs.asus.com/files/media/64384862-AFB5-4AFE-8874-2F2A976055EF/v1/img/kv.jpg")),
        Map.entry(key("LG UltraGear 27GP850 27 inch"), new OfficialProduct(
            "https://www.lg.com/us/monitors/lg-27gp850-b-gaming-monitor",
            "https://media.us.lg.com/transform/ecomm-PDPGalleryThumbnail-350x350/0d1e62da-95ec-40e6-b2a3-0ef94b03aea3/md08000370-Z-01-jpg?io=transform%3Afill%2Cwidth%3A900")),
        Map.entry(key("LG UltraGear 27GS95QE OLED 27 inch"), new OfficialProduct(
            "https://www.lg.com/us/monitors/lg-27gs95qe-b-gaming-monitor",
            "https://media.us.lg.com/transform/ecomm-PDPGalleryThumbnail-350x350/ebb82051-a91a-4bf6-a95b-567fd58abc32/Monitor-27GS95QE-B-OLED-Gallery_3000x3000?io=transform%3Afill%2Cwidth%3A900")),
        Map.entry(key("Gigabyte M27Q X 27 inch"), new OfficialProduct(
            "https://www.gigabyte.com/us/Monitor/M27Q-X-rev-20",
            "https://static.gigabyte.com/StaticFile/Image/Global/1491123be306fc888d90facf8d05899a/ProductRemoveBg/36349")),
        Map.entry(key("MSI MPG 321URX QD-OLED 32 inch"), new OfficialProduct(
            "https://www.msi.com/Monitor/MPG-321URX-QD-OLED",
            "https://storage-asset.msi.com/global/picture/image/feature/monitor/MPG-321URX-QD-OLED/images/kv-xs.jpg"))
    );

    /*
     * Curated model-specific product photos. Search-result URLs are intentionally
     * avoided because they expire, redirect, and frequently render as blank cards.
     */
    private static final Map<String, String> PRODUCT_IMAGES = Map.ofEntries(
        Map.entry(key("AMD Ryzen 5 5600X"), "https://api.e-server.com.ua/storage/233242/rs/U0472356_big___rs_1200_1200.jpg"),
        Map.entry(key("AMD Ryzen 7 5800X"), "https://images1.kabum.com.br/produtos/fotos/sync_mirakl/169551/xlarge/Processador-AMD-Ryzen-7-5800X-3-8GHz-Cache-32MB-Octa-Core-16-Threads-AM4-100-100000063WOF_1777401522.jpg"),
        Map.entry(key("AMD Ryzen 9 5900X"), "https://i.ebayimg.com/images/g/XXQAAeSwhrZp-8X5/s-l1600.webp"),
        Map.entry(key("AMD Ryzen 7 5800X3D"), "https://ict.hr/8819-medium_default/amd-ryzen-7-5800x3d-box.jpg"),
        Map.entry(key("AMD Ryzen 5 7500F"), "https://www.proshop.nl/Images/915x900/3195178_efb44fcf5eaf.png"),
        Map.entry(key("AMD Ryzen 5 7600"), "https://patoloco.com.br/resize/imagecache/ba9ad19a5b1abbdf61cf1a5124c9fcd9.jpeg"),
        Map.entry(key("AMD Ryzen 7 7700"), "https://jo.performapc.com/wp-content/uploads/2024/08/AMD-Ryzen-7-7700-8-core-Processor-500x500.jpg"),
        Map.entry(key("AMD Ryzen 7 7800X3D"), "https://ants.kz/upload/resize_cache/iblock/d11/450_450_1/kiuupkzd14psp7a38klfrvkjsb663kgz.jpg"),
        Map.entry(key("AMD Ryzen 9 7900"), "https://www.avxperten.no/images/product/74226/800x800/0.jpg"),
        Map.entry(key("AMD Ryzen 5 9600X"), "https://hivetec.mk/wp-content/uploads/2024/09/9600x.jpg"),
        Map.entry(key("AMD Ryzen 7 9700X"), "https://www.ivory.co.il/files/catalog/org/1721034043i43Wm.webp"),
        Map.entry(key("AMD Ryzen 7 9800X3D"), "https://www.amd.com/content/dam/amd/en/images/products/processors/ryzen/2900400-ryzen-7-9800x3d-product.jpg"),
        Map.entry(key("AMD Ryzen 9 9950X"), "https://m.media-amazon.com/images/I/41cjH8HH4zL._SL1500_.jpg"),
        Map.entry(key("Intel Core i5-10400F"), "https://phongnet.com/wp-content/uploads/2022/09/cpu-intel-core-i5-10400f-1-600x407.jpg"),
        Map.entry(key("Intel Core i5-11400F"), "https://sp-one.vn/Content/uploads/2023/05/cpu-intel-core-i5-gen11-500x500-1.webp"),
        Map.entry(key("Intel Core i5-12400F"), "https://phuongthanhcomputer.com/wp-content/uploads/2022/10/CPU-Intel-Core-i5-12400F.jpg"),
        Map.entry(key("Intel Core i5-12600K"), "https://www.tradeinn.com/f/13845/138457076/intel-core-i5-12600k-3.7ghz-cpu.webp"),
        Map.entry(key("Intel Core i5-13600K"), "https://i.ebayimg.com/images/g/BdgAAeSwdjRpo93j/s-l1600.webp"),
        Map.entry(key("Intel Core i7-13700K"), "https://i.ebayimg.com/images/g/rawAAeSw77xqA6Es/s-l1600.webp"),
        Map.entry(key("Intel Core i5-14600K"), "https://cybercare.co.th/wp-content/uploads/2024/12/A0154873OK_BIG_1-600x600.jpg"),
        Map.entry(key("Intel Core i7-14700K"), "https://sigmatiendas.com/cdn/shop/files/Procesador-Intel-Core-i7-14700K-20-Nucleos-3_4GHz-30MB-cache-Socket-LGA1700_1.jpg?v=1738682164&width=600"),
        Map.entry(key("Intel Core Ultra 5 245K"), "https://tlggaming.b-cdn.net/image/cache/catalog/products/Processors/Intel/Intel%20core%20ultra%20series%20all%20sku/intel%20core%20ultra%20245k%20tlg-500x500.jpg.webp"),
        Map.entry(key("Intel Core Ultra 7 265K"), "https://www.vmodtech.com/main/wp-content/plugins/fresh-page/files_flutter/1736069031IntelCoreUltra7265KPROCESSORREVIEW.jpg"),
        Map.entry(key("Intel Core Ultra 9 285K"), "https://media.ldlc.com/r705/ld/products/00/06/17/52/LD0006175263.jpg"),
        Map.entry(key("MSI B550M Pro-VDH WiFi"), "https://www.pccasegear.com/images/B550M-PRO-VDH-WIFI-ftr1x.jpg"),
        Map.entry(key("ASUS TUF Gaming B550-Plus"), "https://reset.net.co/wp-content/uploads/2021/08/Board_Asus_TUF_B550_Plus_Gaming_WIFI_Ryzen_DDR4_AM4-600x600.webp"),
        Map.entry(key("ASUS Prime B560M-A"), "https://p1.akcdn.net/mid/790650081.asus-prime-b560m-a.jpg"),
        Map.entry(key("MSI PRO B660M-A DDR4"), "https://storage-asset.msi.com/global/picture/image/feature/mb/PRO-B660M/A-DDR4/story.png"),
        Map.entry(key("ASUS TUF Gaming B760M-Plus WiFi D4"), "https://product.hstatic.net/200000079075/product/44289_mainboard_asus_tuf_gaming_b760m_plus_wifi_ddr4_b4763c472edb4587a0abcd2a5e9062b3_small.jpg"),
        Map.entry(key("Gigabyte Z790 AORUS Elite AX"), "https://static.gigabyte.com/StaticFile/Image/Global/83bf6ff72c82f5fb5b1b6838596339f8/ProductRemoveBg/34197"),
        Map.entry(key("MSI PRO B650M-A WiFi"), "https://storage-asset.msi.com/global/picture/image/feature/mb/B650M/PRO-B650M-A-WIFI/kv.png"),
        Map.entry(key("ASUS TUF Gaming B650-Plus WiFi"), "https://sutechngames.com/wp-content/uploads/2024/01/ASUS-TUF-Gaming-B650-Plus-WIFI-ATX-Motherboard-price-in-Pakistan-at-SU-Tech-600x600.webp"),
        Map.entry(key("Gigabyte B650E AORUS Elite X AX ICE"), "https://www.cdiscount.com/pdt2/6/3/9/1/550x550/gig1708131787639/rw/gigabyte-b650e-a-elite-x-ice.jpg"),
        Map.entry(key("ASUS ROG Strix X870E-E Gaming WiFi"), "https://media.ldlc.com/r705/ld/products/00/06/16/57/LD0006165767.jpg"),
        Map.entry(key("MSI MAG X870 Tomahawk WiFi"), "https://media.ldlc.com/r705/ld/products/00/06/16/98/LD0006169895.jpg"),
        Map.entry(key("ASUS ROG Strix Z890-F Gaming WiFi"), "https://media.ldlc.com/r705/ld/products/00/06/17/65/LD0006176515.jpg"),
        Map.entry(key("MSI MAG B860 Tomahawk WiFi"), "https://img.overclockers.co.uk/images/MOT-MSI-04080/f0050f2c04c5e4adba6d8671db33599b.jpg"),
        Map.entry(key("Kingston Fury Beast 16GB DDR4 3200"), "https://ecommerce.datablitz.com.ph/cdn/shop/files/0740617319880.jpg?v=1732178403"),
        Map.entry(key("Corsair Vengeance LPX 32GB DDR4 3600"), "https://c1.neweggimages.com/productimage/nb640/20-236-537-V01.jpg"),
        Map.entry(key("G.Skill Trident Z Neo 32GB DDR4 3600"), "https://cdn.mwave.com.au/images/65/AC25720.jpg"),
        Map.entry(key("Kingston Fury Beast 32GB DDR5 5200"), "https://images4.kabum.com.br/produtos/fotos/sync_mirakl/697874/xlarge/Memoria-32gb-Ddr5-5200-Cl40-1-25v-Fury-Beast-Gaming-KF552C40BB-32-Kingston_1781013543.jpg"),
        Map.entry(key("Kingston Fury Beast 32GB DDR5 6000"), "https://mdcomputers.in/image/catalog/memory/kinsgton/kf560c30bbe-32/kf560c30bbe-32-image-main.webp"),
        Map.entry(key("G.Skill Trident Z5 Neo RGB 32GB DDR5 6000"), "https://www.gskill.com/_upload/images/2208251640157.png"),
        Map.entry(key("Corsair Vengeance RGB 64GB DDR5 6400"), "https://imagecdn.jw.com.au/media/catalog/product/c/m/cmh64gx5m2b6400c32_1.jpg?height=514&image-type=image&store=default&width=514"),
        Map.entry(key("TeamGroup T-Create Expert 96GB DDR5 6000"), "https://images.teamgroupinc.com/products/memory/u-dimm/ddr5/expert/black/dual_01.jpg"),
        Map.entry(key("NVIDIA GeForce RTX 3060 Ti"), "https://www.nvidia.com/content/dam/en-zz/Solutions/geforce/ampere/rtx-3060-ti/geforce-rtx-3060-ti-product-gallery-thumbnail-267-1-bl.jpg"),
        Map.entry(key("NVIDIA GeForce RTX 3070"), "https://www.nvidia.com/content/dam/en-zz/Solutions/geforce/ampere/rtx-3070/geforce-rtx-3070-product-gallery-thumbnail-267-1.jpg"),
        Map.entry(key("NVIDIA GeForce RTX 3080 10GB"), "https://htlmart.com/wp-content/uploads/2025/01/Htlmart-NVIDIA-GeForce-RTX-3080-GRAPHICS-CARD-10GB-GDDR6X-1-600x600.jpg"),
        Map.entry(key("NVIDIA GeForce RTX 3090"), "https://www.oficinadanet.com.br/media/obj_item/752/381/3090-pronta.png"),
        Map.entry(key("NVIDIA GeForce RTX 3060 12GB"), "https://lastbestprice.com/wp-content/uploads/2021/09/Lenovo-Nvidia-GeForce-RTX-3060-12GB-Graphics-Card.jpg"),
        Map.entry(key("AMD Radeon RX 6600"), "https://www.amd.com/content/dam/amd/en/images/products/graphics/2648997-amd-radeon-6600.jpg"),
        Map.entry(key("AMD Radeon RX 6700 XT"), "https://www.amd.com/content/dam/amd/en/images/products/graphics/2648997-amd-radeon-6700xt.jpg"),
        Map.entry(key("NVIDIA GeForce RTX 4090"), "https://booster-theme-gadget.myshopify.com/cdn/shop/products/16531651_1280x.jpg?v=1670992159"),
        Map.entry(key("AMD Radeon RX 7900 XTX"), "https://images.hothardware.com/contentimages/article/3265/content/small_radeon-rx-7900xtx-style.jpg"),
        Map.entry(key("NVIDIA GeForce RTX 4060"), "https://img.etimg.com/thumb/msid-101347857%2Cwidth-300%2Cheight-225%2Cimgsize-31484%2Cresizemode-75/nvidia-geforce-rtx-4060-to-release-on-june-29-know-price-performance-and-other-details.jpg"),
        Map.entry(key("NVIDIA GeForce RTX 4060 Ti 16GB"), "https://cdn.mos.cms.futurecdn.net/K8W6wbcUEQxv8uf3QaTU2o.jpg"),
        Map.entry(key("AMD Radeon RX 7600"), "https://www.slashgear.com/img/gallery/amd-radeon-rx-7600-challenges-rtx-4060-ti-for-budget-gpu-dominance/amd-wins-the-price-war-1684953212.jpg"),
        Map.entry(key("AMD Radeon RX 7700 XT"), "https://cdn.mos.cms.futurecdn.net/tBQNbE9f388n6s2F7XM3Be.jpg"),
        Map.entry(key("AMD Radeon RX 7800 XT"), "https://cdn.lesnumeriques.com/optim/product/71/71693/f89e8cd4-radeon-rx-7800-xt__400_400.jpg"),
        Map.entry(key("NVIDIA GeForce RTX 4070 SUPER"), "https://static.trackalacker.com/cdn-cgi/image/fit%3Dpad%2Cwidth%3D300%2Cheight%3D300%2Cquality%3D85%2Cformat%3Dauto/uploads/products/photo_item/photo/122966/nvidia-geforce-rtx-4070-super.jpg"),
        Map.entry(key("NVIDIA GeForce RTX 4070 Ti SUPER"), "https://www.tomshardware.fr/content/uploads/sites/3/2024/01/nvidia-geforce-rtx-4070-ti-super-box-msi-ventus-3X.jpg"),
        Map.entry(key("NVIDIA GeForce RTX 4080 SUPER"), "https://cdn.mos.cms.futurecdn.net/puvrWaJyk3sinh4PvEvkLo.jpg"),
        Map.entry(key("AMD Radeon RX 7900 GRE"), "https://i5.walmartimages.com/seo/SAPPHIRE-PURE-Radeon-RX-7900-GRE-16GB-GDDR6-PCI-Express-4-0-x16-ATX-Video-Cards-11325-03-20G_a9b302b4-01d8-4ea3-844e-a99ca0c02b18.144b04fadb488e2a0c69782ceafebdec.jpeg?odnBg=FFFFFF&odnHeight=800&odnWidth=800"),
        Map.entry(key("NVIDIA GeForce RTX 5060"), "https://my-store.msi.com/cdn/shop/files/WebGeForceRTX_50608GVentus2XOC1.jpg?v=1748317216&width=1200"),
        Map.entry(key("NVIDIA GeForce RTX 5060 Ti"), "https://asset-us-store.msi.com/image/cache/catalog/Pd_page/VGA/2026/5060TI16GV2XOCBP/5060TI16GV2XOCBP-8-1024x1024.png"),
        Map.entry(key("NVIDIA GeForce RTX 5070"), "https://pisces.bbystatic.com/image2/BestBuy_US/images/products/4094e258-3af3-4835-b786-9db66e10789f.jpg;maxHeight=1000;maxWidth=1000;format=webp"),
        Map.entry(key("NVIDIA GeForce RTX 5070 Ti"), "https://ugc.kryptex.com/wtm/5070.webp"),
        Map.entry(key("NVIDIA GeForce RTX 5080"), "https://i.ebayimg.com/images/g/RZ8AAOSwMvtnnRgs/s-l500.jpg"),
        Map.entry(key("NVIDIA GeForce RTX 5090"), "https://www.nvidia.com/content/nvidiaGDC/us/en_US/geforce/graphics-cards/50-series/rtx-5090/_jcr_content/root/responsivegrid/nv_modalcontainer_609051001/nv_image.coreimg.jpeg/1739962972157/geforce-rtx-5090-gallery-shot-1-ari.jpeg"),
        Map.entry(key("AMD Radeon RX 9060 XT"), "https://www.amd.com/content/dam/amd/en/images/products/graphics/3558400-radeon-9060xt-9070-transparent.png"),
        Map.entry(key("AMD Radeon RX 9070 XT"), "https://cdn.mos.cms.futurecdn.net/GGqxJVbAb4eJQMWPV6rdDa.jpg"),
        Map.entry(key("Kingston NV2 1TB"), "https://media.kingston.com/kingston/press/ktc-press-fe-nv2-ssd.jpg"),
        Map.entry(key("Samsung 970 EVO Plus 1TB"), "https://i5.walmartimages.com/seo/SAMSUNG-SSD-970-EVO-Plus-Series-1TB-PCIe-NVMe-M-2-Internal-SSD-MZ-V7S1T0B-AM_5922eb14-ff9a-48ed-9617-ac39caaa3385.404bedae7f1c02264183a05d652aa0f4.jpeg?odnBg=FFFFFF&odnHeight=800&odnWidth=800"),
        Map.entry(key("WD Blue SN570 1TB"), "https://media2.nbb-cdn.de/images/products/WD_Blue-SN570-SSD_1TB_Front_a4e1.jpg?size=390"),
        Map.entry(key("Lexar NM790 1TB"), "https://dl.bs365.uz/storage/products/26883/WQHhCqUvoejs0mls19A9-medium_default.jpg"),
        Map.entry(key("Samsung 990 Pro 2TB"), "https://elnstore.com/cdn/shop/products/MZ-V9P2T0BW.jpg?v=1681442588&width=1445"),
        Map.entry(key("WD Black SN850X 2TB"), "https://multimedia.bbycastatic.ca/multimedia/products/500x500/163/16373/16373379.jpg"),
        Map.entry(key("Crucial T700 2TB PCIe 5.0"), "https://cdn.mwave.com.au/images/65/crucial_t700_2tb_pcie_50_nvme_m2_2280_ssd_with_heatsink_ct2000t700ssd5_ac66676_44476.jpg"),
        Map.entry(key("Crucial T705 2TB PCIe 5.0"), "https://cdn.mos.cms.futurecdn.net/URztg9Grku7wXBYgf4QMSc.jpg"),
        Map.entry(key("Kingston Fury Renegade G5 2TB"), "https://eplehuset.no/cdn/shop/files/SFYR2S_4096GB_angle-zm-lg.jpg?v=1759301745&width=1445"),
        Map.entry(key("Cooler Master MWE Bronze 650 V2"), "https://product.hstatic.net/200000790315/product/nguon-may-tinh-cooler-master-mwe-bronze-650w-v2-full-range-1_093aa1b934f04ae6b69357fd1eb74386_medium.jpg"),
        Map.entry(key("Corsair CV750 80 Plus Bronze"), "https://bizweb.dktcdn.net/thumb/grande/100/329/122/products/nguon-may-tinh-corsair-cv750-80-plus-bronze-cp-9020237-na.png?v=1743638194423"),
        Map.entry(key("MSI MAG A650BN"), "https://media.materiel.net/r250/products/MN0005896886_1.jpg"),
        Map.entry(key("MSI MAG A750GL PCIE5"), "https://cdn.inet.se/product/688x386/6906806_4.png"),
        Map.entry(key("Cooler Master MWE Gold 850 V2 ATX 3.0"), "https://www.coolermaster.com/on/demandware.static/-/Sites-cooler-master-main/default/dwdb004678/Assets/mwe-gold-850-v2-atx3-ready-white-edition/large/mwe-gold-850-v2-atx3-white-gallery-01.png"),
        Map.entry(key("Corsair RM850x Shift ATX 3.1"), "https://anphat.com.vn/media/product/250_49546_ngu___n_m__y_t__nh_corsair_rm850x_shift_white__1_.jpg"),
        Map.entry(key("Seasonic Vertex GX-1000 ATX 3.0"), "https://media.ldlc.com/r705/ld/products/00/06/08/33/LD0006083315.jpg"),
        Map.entry(key("ASUS ROG Thor 1200P2 Gaming"), "https://dlcdnwebimgs.asus.com/files/media/64384862-AFB5-4AFE-8874-2F2A976055EF/v1/img/kv.jpg"),
        Map.entry(key("DeepCool AK400"), "https://media.ldlc.com/r705/ld/products/00/05/95/85/LD0005958511.jpg"),
        Map.entry(key("Thermalright Peerless Assassin 120 SE"), "https://zahcomputers.pk/wp-content/uploads/2024/11/Thermalright-Peerless-Assassin-120-SE-ARGB-White-CPU-Air-Cooler-Price-in-Pakistan.jpg"),
        Map.entry(key("DeepCool AK620 Digital"), "https://media.ldlc.com/r705/ld/products/00/06/07/83/LD0006078337.jpg"),
        Map.entry(key("Thermalright Phantom Spirit 120 EVO"), "https://cdn11.bigcommerce.com/s-ms4wz8cgi2/images/stencil/608x608/products/128587/38699/Phantom_Spirit_120_Digital_EVO__80037.1759202631.jpg?c=1"),
        Map.entry(key("Arctic Liquid Freezer III 360 ARGB"), "https://media.ldlc.com/r705/ld/products/00/06/11/37/LD0006113780.jpg"),
        Map.entry(key("Corsair iCUE Link H150i LCD 360"), "https://media.ldlc.com/r705/ld/products/00/06/08/14/LD0006081433.jpg"),
        Map.entry(key("Noctua NH-D15 G2"), "https://cdn.noctua.at/media/nh_d15_g2_hbc_4.jpg"),
        Map.entry(key("Montech Air 100 ARGB"), "https://www.alternate.nl/p/160x160/6/3/Montech_Air_100_ARGB_mini_tower_behuizing%40%401915736_30.jpg"),
        Map.entry(key("NZXT H510 Flow"), "https://http2.mlstatic.com/D_Q_NP_628834-MLU70536469813_072023-R.webp"),
        Map.entry(key("Lian Li Lancool 216 RGB"), "https://www.pcstudio.in/wp-content/uploads/2025/01/Lian-Li-Lancool-216-Rgb-E-ATX-Mid-Tower-Cabinet-With-Tempered-Glass-Side-Panel-White-1-1.webp"),
        Map.entry(key("NZXT H6 Flow RGB"), "https://doc.smarty.sk/pic/X6EA000N01-600-600.webp"),
        Map.entry(key("Lian Li O11 Vision Compact"), "https://computerarenakh.com/image/cache/catalog/001-NEW%20PRODUCT%202025%20/001-CASE%20FAN%202025/vision%20compact%20loop%20by%20cac%20cambodia-468x278.jpg"),
        Map.entry(key("Corsair 3500X ARGB"), "https://images-na.ssl-images-amazon.com/images/I/71MMoud1NzL.jpg"),
        Map.entry(key("Hyte Y70 Touch Infinite"), "https://media.ldlc.com/r705/ld/products/00/06/24/50/LD0006245075.jpg"),
        Map.entry(key("AOC 24G2SP 24 inch"), "https://www.buyerspk.com/cdn/shop/files/AOC-24G2SP-24-Frameless-Gaming-Monitor-Price-in-Pakistan.jpg?crop=center&height=580&v=1701508423&width=580"),
        Map.entry(key("LG UltraGear 27GP850 27 inch"), "https://media.us.lg.com/transform/ecomm-PDPGalleryThumbnail-350x350/0d1e62da-95ec-40e6-b2a3-0ef94b03aea3/md08000370-Z-01-jpg?io=transform%3Afill%2Cwidth%3A900"),
        Map.entry(key("LG UltraGear 27GS95QE OLED 27 inch"), "https://media.us.lg.com/transform/ecomm-PDPGalleryThumbnail-350x350/ebb82051-a91a-4bf6-a95b-567fd58abc32/Monitor-27GS95QE-B-OLED-Gallery_3000x3000?io=transform%3Afill%2Cwidth%3A900"),
        Map.entry(key("Gigabyte M27Q X 27 inch"), "https://static.gigabyte.com/StaticFile/Image/Global/1491123be306fc888d90facf8d05899a/ProductRemoveBg/36349"),
        Map.entry(key("Dell UltraSharp U2724D 27 inch"), "https://www.tpstech.in/cdn/shop/files/1-Dell_U2724D_UltraSharp_27_QHD_IPS_120Hz_Eye_Comfort_Monitor-tpstech.webp?v=1757916770&width=1946"),
        Map.entry(key("AOC Q27G3XMN Mini LED 27 inch"), "https://www.tradeinn.com/f/14158/141582692/aoc-monitor-gaming-q27g3xmn-bk-27-qhd-mini-led-180hz.webp"),
        Map.entry(key("MSI MPG 321URX QD-OLED 32 inch"), "https://storage-asset.msi.com/global/picture/image/feature/monitor/MPG-321URX-QD-OLED/images/kv-xs.jpg"),
        Map.entry(key("ASUS ROG Swift PG32UCDM OLED 32 inch"), "https://speedcom.co.th/cdn/shop/files/Asus-ROG-Swift-OLED-PG32UCDM-32.0_-OLED-240Hz-_-SpeedCom-442758262.webp?v=1782578231&width=1200"),
        Map.entry(key("Logitech G Pro X Superlight"), "https://tech4gamers.com/wp-content/uploads/2023/07/Logitech-G-Pro-X-Superlight-Box-Contents-Image-By-Tech4Gamers-1024x775.jpg"),
        Map.entry(key("Logitech MX Master 3S"), "https://cdn.cs.1worldsync.com/f6/cb/f6cb0bea-9633-4f43-b211-db9b21d57edf.jpg"),
        Map.entry(key("SteelSeries Apex Pro TKL Wireless"), "https://cdn.mos.cms.futurecdn.net/jx46gmyprd6q7CUUvmXgwh.jpg"),
        Map.entry(key("Logitech G Pro X Superlight 2"), "https://www.pcwelt.de/wp-content/uploads/2024/08/Logitech-G-Pro-X-Superlight-2-im-Test.jpg?quality=50&strip=all&w=1024"),
        Map.entry(key("Keychron Q1 HE Magnetic Switch"), "https://www.keychron.com/cdn/shop/files/Keychron-Q1--HE--8K-Full-Size-Keychron-Ultra-fast-Lime-Magnetic-Switch-Keyboard-1.jpg?v=1761655109&width=1200"),
        Map.entry(key("Razer BlackShark V2 Pro 2024"), "https://c1-ebgames.eb-cdn.com.au/merchandising/images/packshots/083ab3c114d74d83ae6ba80126745c8a_Large.jpg"),
        Map.entry(key("Elgato Facecam Pro"), "https://attach.mobile01.com/attach/202408/mobile01-69f8d2cb32d3d9db294a0992a40209d1.png")
    );

    private static final Map<String, List<String>> PRODUCT_IMAGE_FALLBACKS = Map.ofEntries(
        Map.entry(key("Intel Core i5-11400F"), List.of(
            "https://sp-one.vn/Content/uploads/2023/05/cpu-intel-core-i5-gen11-500x500-1.webp",
            "https://img.danuri.io/catalog-image/394/753/013/d96031de0e11482cb8a24f6bfa504ec0.jpg")),
        Map.entry(key("NVIDIA GeForce RTX 5060 Ti"), List.of(
            "https://asset-us-store.msi.com/image/cache/catalog/Pd_page/VGA/2026/5060TI16GV2XOCBP/5060TI16GV2XOCBP-8-1024x1024.png",
            "https://api.technodom.kz/f3/api/v1/images/1000/1000/291120_1.jpg")),
        Map.entry(key("G.Skill Trident Z5 Neo RGB 32GB DDR5 6000"), List.of(
            "https://www.gskill.com/_upload/images/2208251640157.png",
            "https://www.gskill.com/img/overview/tz5n-rgb/01-trident-z5-neo-rgb-main-expo.jpg")),
        Map.entry(key("TeamGroup T-Create Expert 96GB DDR5 6000"), List.of(
            "https://images.teamgroupinc.com/products/memory/u-dimm/ddr5/expert/black/dual_01.jpg",
            "https://img.pccasegear.com/tr%3An-gallery/images/CTCED596G6400HC32ADC01-thumb.jpg")),
        Map.entry(key("Kingston NV2 1TB"), List.of(
            "https://media.kingston.com/kingston/press/ktc-press-fe-nv2-ssd.jpg",
            "https://www.discoazul.com/uploads/media/images/396x396/memoria-ssd-kingston-nv2-1tb-m-2-2280-pcie-nvme-11.jpg")),
        Map.entry(key("ASUS ROG Thor 1200P2 Gaming"), List.of(
            "https://dlcdnwebimgs.asus.com/files/media/64384862-AFB5-4AFE-8874-2F2A976055EF/v1/img/kv.jpg",
            "https://media.pichau.com.br/media/catalog/product/cache/2f958555330323e505eba7ce930bdf27/r/o/rog-thor-1200p2-gaming1.jpg")),
        Map.entry(key("LG UltraGear 27GP850 27 inch"), List.of(
            "https://media.us.lg.com/transform/ecomm-PDPGalleryThumbnail-350x350/0d1e62da-95ec-40e6-b2a3-0ef94b03aea3/md08000370-Z-01-jpg?io=transform%3Afill%2Cwidth%3A900",
            "https://elbadrgroupeg.store/image/cache/catalog/LG/t5duSqJkRfix6hoNmYeURR1JQQo4FvSI-550x550w.jpg")),
        Map.entry(key("LG UltraGear 27GS95QE OLED 27 inch"), List.of(
            "https://media.us.lg.com/transform/ecomm-PDPGalleryThumbnail-350x350/ebb82051-a91a-4bf6-a95b-567fd58abc32/Monitor-27GS95QE-B-OLED-Gallery_3000x3000?io=transform%3Afill%2Cwidth%3A900")),
        Map.entry(key("Gigabyte M27Q X 27 inch"), List.of(
            "https://static.gigabyte.com/StaticFile/Image/Global/1491123be306fc888d90facf8d05899a/ProductRemoveBg/36349",
            "https://prod-api.mediaexpert.pl/api/images/gallery_500_500/thumbnails/images/35/3523853/Monitor-GIGABYTE-M27Q-X-27-2560x1440px-IPS-240Hz-1-ms-GTG-front.jpg")),
        Map.entry(key("MSI MPG 321URX QD-OLED 32 inch"), List.of(
            "https://storage-asset.msi.com/global/picture/image/feature/monitor/MPG-321URX-QD-OLED/images/kv-xs.jpg"))
    );

    private static final Map<String, List<ProductSpec>> VERIFIED = Map.ofEntries(
        Map.entry(key("AMD Ryzen 7 9800X3D"), List.of(
            spec("Kiến trúc", "Zen 5 với AMD 3D V-Cache thế hệ 2"),
            spec("Số nhân / luồng", "8 nhân / 16 luồng"),
            spec("Xung nhịp", "Cơ bản 4,7 GHz, boost tối đa 5,2 GHz"),
            spec("Bộ nhớ đệm", "L2 8 MB, L3 96 MB"),
            spec("Socket", "AM5"), spec("Bộ nhớ hỗ trợ", "DDR5"),
            spec("Chuẩn PCIe", "PCIe 5.0"), spec("TDP mặc định", "120 W"),
            spec("Ngày ra mắt", "07/11/2024"))),
        Map.entry(key("NVIDIA GeForce RTX 5090"), List.of(
            spec("Kiến trúc", "NVIDIA Blackwell"), spec("CUDA Cores", "21.760"),
            spec("Bộ nhớ đồ họa", "32 GB GDDR7"), spec("Giao tiếp bộ nhớ", "512-bit"),
            spec("Công nghệ", "Ray Tracing thế hệ 4, DLSS 4, Reflex 2"),
            spec("Công suất card", "575 W"), spec("Nguồn đề nghị", "1.000 W"),
            spec("Ngày ra mắt", "30/01/2025"))),
        Map.entry(key("Noctua NH-D15 G2"), List.of(
            spec("Kiểu tản nhiệt", "Tháp đôi hiệu năng cao"),
            spec("Quạt đi kèm", "2 quạt NF-A14x25r G2 PWM 140 mm"),
            spec("Chiều cao", "168 mm"), spec("Độ ồn tối đa", "24,8 dB(A)"),
            spec("Bảo hành", "6 năm"), spec("Năm ra mắt", "2024"))),
        Map.entry(key("Lian Li O11 Vision Compact"), List.of(
            spec("Kiểu case", "Mid-tower hai khoang"),
            spec("Hỗ trợ bo mạch chủ", "E-ATX / ATX / Micro-ATX / Mini-ITX"),
            spec("Mặt kính", "Kính cường lực toàn cảnh"),
            spec("Hỗ trợ tản nhiệt", "Radiator tối đa 360 mm"),
            spec("Thiết kế", "Có thể chuyển đổi giữa kính nóc và lưới thông gió"),
            spec("Năm ra mắt", "2024")))
    );

    private ProductPresentation() {}

    public static Product enrich(Product product) {
        if (product == null) return null;
        BrandProfile brand = profileFor(product.getBrand());
        OfficialProduct official = OFFICIAL.get(key(product.getProductName()));
        product.setOfficialSourceName(brand.displayName() + " chính hãng");
        product.setOfficialUrl(official == null ? brand.homepage() : official.pageUrl());
        String curatedImage = PRODUCT_IMAGES.get(key(product.getProductName()));
        if (!blank(curatedImage)) product.setImageUrl(curatedImage);
        product.setSpecificationItems(specifications(product));
        return product;
    }

    public static List<String> imageCandidates(Product product) {
        if (product == null) return List.of();
        Set<String> urls = new LinkedHashSet<>();
        addImageUrl(urls, product.getImageUrl());
        addImageUrl(urls, PRODUCT_IMAGES.get(key(product.getProductName())));
        List<String> fallbacks = PRODUCT_IMAGE_FALLBACKS.get(key(product.getProductName()));
        if (fallbacks != null) fallbacks.forEach(url -> addImageUrl(urls, url));
        OfficialProduct official = OFFICIAL.get(key(product.getProductName()));
        if (official != null) addImageUrl(urls, official.imageUrl());
        return List.copyOf(urls);
    }

    private static void addImageUrl(Set<String> urls, String url) {
        if (!blank(url) && (url.startsWith("https://") || url.startsWith("http://"))) {
            urls.add(url.trim());
        }
    }

    private static List<ProductSpec> specifications(Product product) {
        List<ProductSpec> rows = new ArrayList<>();
        Set<String> labels = new LinkedHashSet<>();
        Set<String> values = new LinkedHashSet<>();
        add(rows, labels, values, "Tên sản phẩm", product.getProductName());
        add(rows, labels, values, "Hãng sản xuất", product.getBrand());
        add(rows, labels, values, "Model", product.getModel());
        add(rows, labels, values, "Danh mục", product.getCatName());
        add(rows, labels, values, "Mô tả", product.getDescription());

        List<ProductSpec> verified = VERIFIED.get(key(product.getProductName()));
        if (verified != null) {
            for (ProductSpec row : verified) add(rows, labels, values, row.getLabel(), row.getValue());
        }

        if (!blank(product.getSpecs())) {
            String[] tokens = product.getSpecs().split("\\s*,\\s*");
            for (int i = 0; i < tokens.length; i++) {
                add(rows, labels, values, labelFor(product.getCatId(), tokens[i], i), tokens[i]);
            }
        }
        add(rows, labels, values, "Tình trạng", product.getStockStatus());
        return List.copyOf(rows);
    }

    private static void add(List<ProductSpec> rows, Set<String> labels, Set<String> values,
            String label, String value) {
        if (blank(label) || blank(value) || values.contains(key(value))) return;
        String unique = label;
        for (int index = 2; labels.contains(key(unique)); index++) unique = label + " " + index;
        labels.add(key(unique));
        values.add(key(value));
        rows.add(spec(unique, value.trim()));
    }

    private static String labelFor(int category, String raw, int index) {
        String v = key(raw);
        if (v.matches("20\\d{2}")) return "Năm ra mắt";
        if (category == 1) {
            if (has(v, "nhan", "luong")) return "Số nhân / luồng";
            if (has(v, "am4", "am5", "lga")) return "Socket";
            if (v.contains("ddr")) return "Bộ nhớ hỗ trợ";
            if (v.contains("pcie")) return "Chuẩn PCIe";
            if (v.contains("tdp")) return "TDP";
            if (v.contains("cache")) return "Bộ nhớ đệm";
            if (has(v, "zen", "arrow", "alder")) return "Kiến trúc";
            if (has(v, "npu", "igpu")) return "Đồ họa / NPU";
            return "Thông số CPU";
        }
        if (category == 2) {
            if (has(v, "am4", "am5", "lga")) return "Socket";
            if (v.matches(".*(b550|b560|b650|b650e|b660|b760|b860|x870|x870e|z790|z890).*")) return "Chipset";
            if (v.contains("ddr")) return "Loại RAM";
            if (v.contains("pcie")) return "Khe PCIe / M.2";
            if (has(v, "wifi", "lan")) return "Kết nối mạng";
            if (has(v, "matx", "atx")) return "Kích thước";
            if (v.contains("usb")) return "Cổng kết nối";
            return "Tính năng bo mạch";
        }
        if (category == 3) {
            if (v.contains("gb") && (!v.contains("ddr") || v.contains("x"))) return "Dung lượng / Kit";
            if (v.contains("ddr")) return "Chuẩn và tốc độ";
            if (v.matches(".*cl\\d+.*")) return "Độ trễ";
            if (has(v, "xmp", "expo")) return "Hồ sơ ép xung";
            if (v.contains("rgb")) return "Đèn LED";
            return "Thông số RAM";
        }
        if (category == 4) {
            if (v.contains("gb") && v.contains("gddr")) return "Bộ nhớ đồ họa";
            if (has(v, "rdna", "blackwell", "ada")) return "Kiến trúc";
            if (has(v, "dlss", "fsr")) return "Công nghệ nâng hình";
            if (v.contains("ray tracing")) return "Ray Tracing";
            if (v.contains("av1")) return "Mã hóa video";
            if (v.contains("bit")) return "Bus bộ nhớ";
            return "Thông số GPU";
        }
        if (category == 5) {
            if (v.matches(".*\\b(1tb|2tb|4tb|500gb|512gb)\\b.*")) return "Dung lượng";
            if (v.contains("m.2")) return "Kích thước";
            if (has(v, "nvme", "pcie")) return "Giao tiếp";
            if (has(v, "mb/s", "doc")) return "Tốc độ đọc";
            return "Thông số lưu trữ";
        }
        if (category == 6) {
            if (v.matches(".*\\b\\d{3,4}w\\b.*")) return "Công suất";
            if (has(v, "plus", "gold", "platinum", "bronze")) return "Chứng nhận hiệu suất";
            if (v.contains("atx")) return "Chuẩn nguồn";
            if (has(v, "pcie", "12vhpwr")) return "Cáp GPU";
            if (v.contains("modular")) return "Kiểu dây";
            if (v.contains("oled")) return "Màn hình";
            return "Thông số nguồn";
        }
        if (category == 7) {
            if (has(v, "aio", "air cooler", "tower")) return "Loại tản nhiệt";
            if (v.contains("heatpipe")) return "Ống dẫn nhiệt";
            if (v.contains("fan")) return "Quạt đi kèm";
            if (v.matches(".*(240mm|280mm|360mm|420mm).*")) return "Kích thước radiator";
            if (has(v, "rgb", "argb")) return "Đèn LED";
            if (has(v, "display", "lcd")) return "Màn hình";
            return "Thông số tản nhiệt";
        }
        if (category == 8) {
            if (has(v, "atx", "matx", "mini-itx")) return "Bo mạch chủ hỗ trợ";
            if (v.contains("fan")) return "Quạt đi kèm";
            if (v.contains("glass")) return "Mặt kính";
            if (has(v, "airflow", "chamber", "panoramic")) return "Thiết kế";
            if (has(v, "display", "touch")) return "Màn hình";
            return "Thông số case";
        }
        if (category == 9) {
            if (v.contains("inch")) return "Kích thước";
            if (has(v, "full hd", "qhd", "4k")) return "Độ phân giải";
            if (has(v, "ips", "oled", "mini led")) return "Tấm nền";
            if (v.contains("hz")) return "Tần số quét";
            if (v.contains("hdr")) return "HDR";
            return "Thông số màn hình";
        }
        if (category == 10) {
            if (has(v, "wireless", "usb", "bluetooth")) return "Kết nối";
            if (has(v, "mouse", "tkl", "75 percent", "webcam")) return "Kiểu sản phẩm";
            if (has(v, "4k", "hero", "rapid", "actuation")) return "Hiệu năng";
            if (has(v, "mic", "pin", "silent", "magspeed")) return "Tính năng";
            return "Thông số phụ kiện";
        }
        return index == 0 ? "Năm ra mắt" : "Thông số kỹ thuật";
    }

    private static boolean has(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }

    private static BrandProfile profileFor(String value) {
        BrandProfile found = BRANDS.get(key(value));
        if (found != null) return found;
        String name = blank(value) ? "Nhà sản xuất" : value.trim();
        return new BrandProfile(name, "bing.com",
            "https://www.bing.com/search?q=" + encode(name + " official products"));
    }

    private static Map.Entry<String, BrandProfile> brand(String key, String name, String domain, String homepage) {
        return Map.entry(key, new BrandProfile(name, domain, homepage));
    }

    private static ProductSpec spec(String label, String value) { return new ProductSpec(label, value); }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }

    private static String key(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }

    private record BrandProfile(String displayName, String domain, String homepage) {}
    private record OfficialProduct(String pageUrl, String imageUrl) {}
}
