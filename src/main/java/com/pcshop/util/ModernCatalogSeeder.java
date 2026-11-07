package com.pcshop.util;

import java.math.BigDecimal;
import java.io.ByteArrayOutputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ModernCatalogSeeder {
    private static final Object LOCK = new Object();
    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static volatile boolean checked;

    private ModernCatalogSeeder() {
    }

    public static void ensureSeeded() {
        if (checked || DBConnection.isDemo()) return;
        synchronized (LOCK) {
            if (checked) return;
            try (Connection c = DBConnection.getConnection()) {
                for (SeedProduct product : PRODUCTS) {
                    seedProduct(c, product);
                }
                normalizeVietnameseText(c);
                checked = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void seedProduct(Connection c, SeedProduct p) throws SQLException {
        String sql = """
                IF NOT EXISTS (SELECT 1 FROM Products WHERE model=?)
                BEGIN
                    INSERT INTO Products(cat_id, product_name, brand, model, description, price, stock_qty, specs, image_url, created_at, is_active)
                    VALUES(?,?,?,?,?,?,?,?,?,?,1)
                END
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.model());
            ps.setInt(2, p.catId());
            ps.setString(3, p.name());
            ps.setString(4, p.brand());
            ps.setString(5, p.model());
            ps.setString(6, p.description());
            ps.setBigDecimal(7, new BigDecimal(p.price()));
            ps.setInt(8, p.stock());
            ps.setString(9, p.specs());
            ps.setNull(10, Types.NVARCHAR);
            ps.setTimestamp(11, Timestamp.valueOf(p.createdAt() + " 09:00:00"));
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement("""
                UPDATE Products
                SET cat_id=?, product_name=?, brand=?, is_active=1
                WHERE model=?
                """)) {
            ps.setInt(1, p.catId());
            ps.setString(2, p.name());
            ps.setString(3, p.brand());
            ps.setString(4, p.model());
            ps.executeUpdate();
        }
    }

    private static void normalizeVietnameseText(Connection c) throws SQLException {
        repairMojibakeText(c);

        update(c, "UPDATE Users SET full_name=? WHERE username='admin'", "Quản trị viên");
        update(c, "UPDATE Users SET full_name=? WHERE username='staff1'", "Nguyễn Văn Nam");
        update(c, "UPDATE Users SET full_name=? WHERE username='staff2'", "Trần Thị Lan");

        update(c, "UPDATE Categories SET cat_name=?, description=? WHERE icon='CPU'",
                "CPU / Bộ xử lý", "Intel Core Ultra, Core i, AMD Ryzen 5000/7000/9000");
        update(c, "UPDATE Categories SET cat_name=?, description=? WHERE icon='MB'",
                "Mainboard", "Bo mạch chủ Intel và AMD đời mới");
        update(c, "UPDATE Categories SET cat_name=?, description=? WHERE icon='RAM'",
                "RAM", "RAM DDR4/DDR5 cho gaming, workstation, AI PC");
        update(c, "UPDATE Categories SET cat_name=?, description=? WHERE icon='GPU'",
                "GPU / Card đồ họa", "NVIDIA GeForce RTX, AMD Radeon RX");
        update(c, "UPDATE Categories SET cat_name=?, description=? WHERE icon='SSD'",
                "SSD / Ổ cứng", "SSD NVMe PCIe 3.0/4.0/5.0 và ổ lưu trữ");
        update(c, "UPDATE Categories SET cat_name=?, description=? WHERE icon='PSU'",
                "Nguồn (PSU)", "Nguồn ATX 3.0/3.1, 80 Plus, PCIe 5.1");
        update(c, "UPDATE Categories SET cat_name=?, description=? WHERE icon='COOL'",
                "Tản nhiệt", "Tản khí, AIO 240/360/420mm");
        update(c, "UPDATE Categories SET cat_name=?, description=? WHERE icon='CASE'",
                "Case / Vỏ máy", "Case ATX, mATX, airflow, kính cường lực");
        update(c, "UPDATE Categories SET cat_name=?, description=? WHERE icon='MON'",
                "Màn hình", "Màn hình gaming, đồ họa, OLED, Mini LED");
        update(c, "UPDATE Categories SET cat_name=?, description=? WHERE icon='ACC'",
                "Phụ kiện", "Bàn phím, chuột, tai nghe, webcam, hub");

    }

    private static void update(Connection c, String sql, String... values) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                ps.setString(i + 1, values[i]);
            }
            ps.executeUpdate();
        }
    }

    private static void repairMojibakeText(Connection c) throws SQLException {
        repairTable(c, "Users", "user_id", "full_name", "address");
        repairTable(c, "Categories", "cat_id", "cat_name", "description");
        repairTable(c, "Products", "product_id", "product_name", "description", "specs");
        repairTable(c, "Orders", "order_id", "customer_name", "shipping_addr", "notes");
        repairTable(c, "StockImports", "import_id", "supplier", "note");
    }

    private static void repairTable(Connection c, String table, String idColumn, String... textColumns) throws SQLException {
        StringBuilder select = new StringBuilder("SELECT ").append(idColumn);
        for (String column : textColumns) select.append(", ").append(column);
        select.append(" FROM ").append(table);

        List<RowRepair> pending = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(select.toString()); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String[] repaired = new String[textColumns.length];
                boolean changed = false;
                for (int i = 0; i < textColumns.length; i++) {
                    String value = rs.getString(textColumns[i]);
                    repaired[i] = repairText(value);
                    if (!Objects.equals(value, repaired[i])) changed = true;
                }
                if (changed) pending.add(new RowRepair(rs.getInt(idColumn), repaired));
            }
        }
        for (RowRepair repair : pending) {
            updateRow(c, table, idColumn, repair.id(), textColumns, repair.values());
        }
    }

    private static void updateRow(Connection c, String table, String idColumn, int id, String[] columns, String[] values) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) sql.append(", ");
            sql.append(columns[i]).append("=?");
        }
        sql.append(" WHERE ").append(idColumn).append("=?");

        try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.length; i++) ps.setString(i + 1, values[i]);
            ps.setInt(values.length + 1, id);
            ps.executeUpdate();
        }
    }

    private static String repairText(String value) {
        if (value == null || !looksMojibake(value)) return value;
        String current = value;
        for (int i = 0; i < 3 && looksMojibake(current); i++) {
            String decoded = decodeOnce(current);
            if (decoded == null || decoded.equals(current)) break;
            current = decoded;
        }
        return current;
    }

    private static String decodeOnce(String value) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(value.length());
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (ch <= 0xFF) {
                    bytes.write(ch);
                    continue;
                }
                byte[] encoded = String.valueOf(ch).getBytes(WINDOWS_1252);
                if (encoded.length != 1 || encoded[0] == '?') return value;
                bytes.write(encoded[0]);
            }
            return StandardCharsets.UTF_8.newDecoder()
                    .decode(java.nio.ByteBuffer.wrap(bytes.toByteArray()))
                    .toString();
        } catch (CharacterCodingException | IllegalArgumentException e) {
            return value;
        }
    }
    private static boolean looksMojibake(String value) {
        return value.contains("Ãƒ")
                || value.contains("Ã‚")
                || value.contains("Ã„")
                || value.contains("Ã†")
                || value.contains("â€")
                || value.contains("áº")
                || value.contains("á»")
                || value.contains("Ã…");
    }

    private record RowRepair(int id, String[] values) {
    }

    private record SeedProduct(
            int catId,
            String name,
            String brand,
            String model,
            String description,
            String price,
            int stock,
            String specs,
            String createdAt
    ) {
    }

    private static SeedProduct p(int catId, String name, String brand, String model, String description,
                                 String price, int stock, String specs, String createdAt) {
        return new SeedProduct(catId, name, brand, model, description, price, stock, specs, createdAt);
    }

    private static final SeedProduct[] PRODUCTS = {
            p(1, "AMD Ryzen 5 5600X", "AMD", "Ryzen 5 5600X", "CPU AM4 pho bien cho gaming gia tot.", "3290000", 22, "2020, 6 nhan 12 luong, AM4, DDR4, PCIe 4.0", "2020-11-05"),
            p(1, "AMD Ryzen 7 5800X", "AMD", "Ryzen 7 5800X", "CPU 8 nhan cho gaming va lam viec.", "4590000", 14, "2020, 8 nhan 16 luong, AM4, DDR4", "2020-11-05"),
            p(1, "AMD Ryzen 9 5900X", "AMD", "Ryzen 9 5900X", "CPU nhieu nhan cho render va workstation AM4.", "6990000", 9, "2020, 12 nhan 24 luong, AM4, DDR4", "2020-11-05"),
            p(1, "AMD Ryzen 7 5800X3D", "AMD", "Ryzen 7 5800X3D", "CPU AM4 gaming manh voi 3D V-Cache.", "6990000", 12, "2022, 8 nhan 16 luong, 3D V-Cache, AM4", "2022-04-20"),
            p(1, "AMD Ryzen 5 7500F", "AMD", "Ryzen 5 7500F", "CPU AM5 gia tot cho gaming pho thong.", "3690000", 25, "2023, 6 nhan 12 luong, AM5, DDR5, khong iGPU", "2023-07-24"),
            p(1, "AMD Ryzen 5 7600", "AMD", "Ryzen 5 7600", "CPU AM5 tiet kiem dien, de nang cap.", "4790000", 20, "2023, 6 nhan 12 luong, AM5, DDR5, PCIe 5.0", "2023-01-10"),
            p(1, "AMD Ryzen 7 7700", "AMD", "Ryzen 7 7700", "CPU 8 nhan can bang cho game va lam viec.", "6990000", 16, "2023, 8 nhan 16 luong, AM5, DDR5, TDP 65W", "2023-01-10"),
            p(1, "AMD Ryzen 7 7800X3D", "AMD", "Ryzen 7 7800X3D", "CPU gaming rat manh voi 3D V-Cache.", "9990000", 12, "2023, 8 nhan 16 luong, 3D V-Cache, AM5", "2023-04-06"),
            p(1, "AMD Ryzen 9 7900", "AMD", "Ryzen 9 7900", "CPU 12 nhan tiet kiem dien cho creator.", "8990000", 10, "2023, 12 nhan 24 luong, AM5, DDR5", "2023-01-10"),
            p(1, "AMD Ryzen 5 9600X", "AMD", "Ryzen 5 9600X", "CPU Zen 5 cho gaming pho thong cao cap.", "7490000", 18, "2024, 6 nhan 12 luong, AM5, DDR5, PCIe 5.0", "2024-08-08"),
            p(1, "AMD Ryzen 7 9700X", "AMD", "Ryzen 7 9700X", "CPU Zen 5 can tot gaming va lam viec.", "9990000", 14, "2024, 8 nhan 16 luong, AM5, TDP 65W, PCIe 5.0", "2024-08-08"),
            p(1, "AMD Ryzen 7 9800X3D", "AMD", "Ryzen 7 9800X3D", "CPU gaming cao cap voi 3D V-Cache.", "14990000", 10, "2024, 8 nhan 16 luong, 3D V-Cache, AM5", "2024-11-07"),
            p(1, "AMD Ryzen 9 9950X", "AMD", "Ryzen 9 9950X", "CPU workstation va creator manh.", "18990000", 8, "2024, 16 nhan 32 luong, Zen 5, AM5, PCIe 5.0", "2024-08-15"),
            p(1, "Intel Core i5-10400F", "Intel", "i5-10400F", "CPU 6 nhan gia re cho may pho thong DDR4.", "1990000", 20, "2020, 6 nhan 12 luong, LGA1200, DDR4, khong iGPU", "2020-05-20"),
            p(1, "Intel Core i5-11400F", "Intel", "i5-11400F", "CPU pho thong tot cho gaming 1080p.", "2390000", 18, "2021, 6 nhan 12 luong, LGA1200, DDR4", "2021-03-30"),
            p(1, "Intel Core i5-12400F", "Intel", "i5-12400F", "CPU Alder Lake gia tot, hop build gaming.", "3290000", 24, "2022, 6 nhan 12 luong, LGA1700, DDR4/DDR5", "2022-01-04"),
            p(1, "Intel Core i5-12600K", "Intel", "i5-12600K", "CPU hybrid dau tien cho game va stream.", "4490000", 15, "2021, 10 nhan 16 luong, LGA1700, DDR4/DDR5", "2021-11-04"),
            p(1, "Intel Core i5-13600K", "Intel", "i5-13600K", "CPU tam trung manh cho gaming va render nhe.", "5890000", 14, "2022, 14 nhan 20 luong, LGA1700, DDR4/DDR5", "2022-10-20"),
            p(1, "Intel Core i7-13700K", "Intel", "i7-13700K", "CPU manh cho gaming, stream va edit video.", "7990000", 10, "2022, 16 nhan 24 luong, LGA1700, DDR4/DDR5", "2022-10-20"),
            p(1, "Intel Core i5-14600K", "Intel", "i5-14600K", "CPU gaming va lam viec tam trung cao.", "6890000", 18, "2023, 14 nhan 20 luong, LGA1700, DDR4/DDR5", "2023-10-17"),
            p(1, "Intel Core i7-14700K", "Intel", "i7-14700K", "CPU manh cho gaming, stream va render.", "9990000", 14, "2023, 20 nhan 28 luong, LGA1700, DDR4/DDR5", "2023-10-17"),
            p(1, "Intel Core Ultra 5 245K", "Intel", "Core Ultra 5 245K", "CPU desktop Arrow Lake cho nen tang LGA1851.", "8190000", 16, "2024, 14 nhan, LGA1851, DDR5, NPU", "2024-10-24"),
            p(1, "Intel Core Ultra 7 265K", "Intel", "Core Ultra 7 265K", "CPU hieu nang cao cho gaming va lam viec.", "11990000", 12, "2024, 20 nhan, LGA1851, DDR5, NPU", "2024-10-24"),
            p(1, "Intel Core Ultra 9 285K", "Intel", "Core Ultra 9 285K", "CPU cao cap nen tang Arrow Lake.", "16990000", 7, "2024, 24 nhan, LGA1851, DDR5, NPU", "2024-10-24"),

            p(2, "MSI B550M Pro-VDH WiFi", "MSI", "B550M Pro-VDH WiFi", "Mainboard AM4 tiet kiem cho Ryzen 5000.", "2290000", 18, "2020, AM4, B550, DDR4, WiFi, mATX", "2020-06-16"),
            p(2, "ASUS TUF Gaming B550-Plus", "ASUS", "TUF B550-Plus", "Mainboard AM4 ben bi cho gaming.", "3190000", 12, "2020, AM4, B550, DDR4, PCIe 4.0", "2020-06-16"),
            p(2, "ASUS Prime B560M-A", "ASUS", "Prime B560M-A", "Mainboard Intel LGA1200 cho CPU gen 10/11.", "2190000", 10, "2021, LGA1200, B560, DDR4, mATX", "2021-03-30"),
            p(2, "MSI PRO B660M-A DDR4", "MSI", "PRO B660M-A DDR4", "Mainboard LGA1700 DDR4 pho thong.", "2990000", 18, "2022, LGA1700, B660, DDR4, mATX", "2022-01-04"),
            p(2, "ASUS TUF Gaming B760M-Plus WiFi D4", "ASUS", "TUF B760M-Plus WiFi D4", "Mainboard Intel DDR4 cho gen 12/13/14.", "3990000", 16, "2023, LGA1700, B760, DDR4, WiFi", "2023-01-03"),
            p(2, "Gigabyte Z790 AORUS Elite AX", "Gigabyte", "Z790 AORUS Elite AX", "Mainboard Intel LGA1700 cao cap.", "7490000", 10, "2022, LGA1700, Z790, DDR5, WiFi 6E", "2022-10-20"),
            p(2, "MSI PRO B650M-A WiFi", "MSI", "PRO B650M-A WiFi", "Mainboard AM5 mATX gia tot.", "4090000", 20, "2022, AM5, B650, DDR5, WiFi, mATX", "2022-09-27"),
            p(2, "ASUS TUF Gaming B650-Plus WiFi", "ASUS", "TUF B650-Plus WiFi", "Mainboard AM5 ben bi cho gaming.", "5690000", 18, "2022, AM5, B650, DDR5, PCIe 5.0 M.2, WiFi", "2022-09-27"),
            p(2, "Gigabyte B650E AORUS Elite X AX ICE", "Gigabyte", "B650E AORUS Elite X", "Mainboard AM5 DDR5 mau trang cho build hien dai.", "6890000", 15, "2024, AM5, B650E, DDR5, PCIe 5.0, WiFi 6E", "2024-03-01"),
            p(2, "ASUS ROG Strix X870E-E Gaming WiFi", "ASUS", "X870E-E", "Mainboard cao cap cho Ryzen 9000.", "13990000", 7, "2024, AM5, X870E, DDR5, PCIe 5.0, WiFi 7, USB4", "2024-09-30"),
            p(2, "MSI MAG X870 Tomahawk WiFi", "MSI", "X870 Tomahawk", "Mainboard AM5 can bang gia va hieu nang.", "8990000", 12, "2024, AM5, X870, DDR5, PCIe 5.0, WiFi 7", "2024-09-30"),
            p(2, "ASUS ROG Strix Z890-F Gaming WiFi", "ASUS", "Z890-F", "Mainboard Intel LGA1851 cao cap.", "11990000", 9, "2024, LGA1851, Z890, DDR5, PCIe 5.0, WiFi 7", "2024-10-24"),
            p(2, "MSI MAG B860 Tomahawk WiFi", "MSI", "B860 Tomahawk", "Mainboard Intel LGA1851 tam trung.", "6490000", 12, "2025, LGA1851, B860, DDR5, PCIe 5.0, WiFi", "2025-01-15"),

            p(3, "Kingston Fury Beast 16GB DDR4 3200", "Kingston", "Fury Beast DDR4 16GB", "RAM DDR4 pho thong cho AM4 va LGA1200/1700.", "890000", 40, "2020, 16GB, DDR4-3200, 1x16GB", "2020-01-10"),
            p(3, "Corsair Vengeance LPX 32GB DDR4 3600", "Corsair", "Vengeance LPX 32GB DDR4", "RAM DDR4 32GB cho gaming gia tot.", "1690000", 26, "2020, 32GB (2x16GB), DDR4-3600", "2020-03-01"),
            p(3, "G.Skill Trident Z Neo 32GB DDR4 3600", "G.Skill", "Trident Z Neo DDR4", "RAM DDR4 RGB hop Ryzen 5000.", "2290000", 15, "2021, 32GB (2x16GB), DDR4-3600, RGB", "2021-02-01"),
            p(3, "Kingston Fury Beast 32GB DDR5 5200", "Kingston", "Fury Beast DDR5 5200", "RAM DDR5 doi dau cho LGA1700.", "2190000", 20, "2022, 32GB (2x16GB), DDR5-5200", "2022-01-04"),
            p(3, "Kingston Fury Beast 32GB DDR5 6000", "Kingston", "Fury Beast DDR5", "RAM DDR5 on dinh cho gaming.", "2890000", 30, "2024, 32GB (2x16GB), DDR5-6000, CL36", "2024-01-20"),
            p(3, "G.Skill Trident Z5 Neo RGB 32GB DDR5 6000", "G.Skill", "F5-6000J3038F16GX2-TZ5NR", "Kit RAM EXPO pho bien cho AMD AM5.", "3290000", 24, "2024, 32GB (2x16GB), DDR5-6000, CL30, RGB", "2024-02-01"),
            p(3, "Corsair Vengeance RGB 64GB DDR5 6400", "Corsair", "Vengeance RGB 64GB", "RAM dung luong cao cho creator va workstation.", "5990000", 16, "2024, 64GB (2x32GB), DDR5-6400, XMP, RGB", "2024-05-01"),
            p(3, "TeamGroup T-Create Expert 96GB DDR5 6000", "TeamGroup", "T-Create Expert", "RAM lon cho AI, render va may tram.", "8990000", 8, "2024, 96GB (2x48GB), DDR5-6000, CL34", "2024-06-01"),

            p(4, "NVIDIA GeForce RTX 3060 Ti", "NVIDIA", "RTX 3060 Ti", "VGA 1080p/1440p rat tot tu RTX 30 series.", "6990000", 10, "2020, 8GB GDDR6, ray tracing, DLSS", "2020-12-02"),
            p(4, "NVIDIA GeForce RTX 3070", "NVIDIA", "RTX 3070", "VGA 1440p manh, phu hop build cu gia tot.", "7990000", 8, "2020, 8GB GDDR6, ray tracing, DLSS", "2020-10-29"),
            p(4, "NVIDIA GeForce RTX 3080 10GB", "NVIDIA", "RTX 3080 10GB", "VGA cao cap doi RTX 30 cho 1440p/4K.", "10990000", 6, "2020, 10GB GDDR6X, ray tracing, DLSS", "2020-09-17"),
            p(4, "NVIDIA GeForce RTX 3090", "NVIDIA", "RTX 3090", "VGA VRAM lon cho AI va render cu.", "18990000", 4, "2020, 24GB GDDR6X, phu hop AI nhe va workstation", "2020-09-24"),
            p(4, "NVIDIA GeForce RTX 3060 12GB", "NVIDIA", "RTX 3060 12GB", "VGA 1080p co VRAM 12GB.", "5990000", 18, "2021, 12GB GDDR6, DLSS, ray tracing", "2021-02-25"),
            p(4, "AMD Radeon RX 6600", "AMD", "RX 6600", "VGA 1080p tiet kiem dien.", "4290000", 15, "2021, 8GB GDDR6, RDNA 2, FSR", "2021-10-13"),
            p(4, "AMD Radeon RX 6700 XT", "AMD", "RX 6700 XT", "VGA 1440p gia tot voi 12GB VRAM.", "7290000", 12, "2021, 12GB GDDR6, RDNA 2, FSR", "2021-03-18"),
            p(4, "NVIDIA GeForce RTX 4090", "NVIDIA", "RTX 4090", "VGA flagship Ada cho 4K, AI va render.", "52990000", 3, "2022, 24GB GDDR6X, DLSS 3, ray tracing", "2022-10-12"),
            p(4, "AMD Radeon RX 7900 XTX", "AMD", "RX 7900 XTX", "VGA AMD cao cap cho 4K.", "24990000", 5, "2022, 24GB GDDR6, RDNA 3, FSR", "2022-12-13"),
            p(4, "NVIDIA GeForce RTX 4060", "NVIDIA", "RTX 4060", "GPU 1080p tiet kiem dien.", "7990000", 20, "2023, 8GB GDDR6, DLSS 3, AV1", "2023-06-29"),
            p(4, "NVIDIA GeForce RTX 4060 Ti 16GB", "NVIDIA", "RTX 4060 Ti 16GB", "VGA tiet kiem dien cho 1080p/1440p.", "12990000", 12, "2023, 16GB GDDR6, DLSS 3, phu hop gaming va AI nhe", "2023-07-18"),
            p(4, "AMD Radeon RX 7600", "AMD", "RX 7600", "VGA 1080p doi RDNA 3.", "6790000", 14, "2023, 8GB GDDR6, RDNA 3, FSR", "2023-05-25"),
            p(4, "AMD Radeon RX 7700 XT", "AMD", "RX 7700 XT", "GPU 1440p gia tot.", "11990000", 12, "2023, 12GB GDDR6, RDNA 3, FSR", "2023-09-06"),
            p(4, "AMD Radeon RX 7800 XT", "AMD", "RX 7800 XT", "VGA hieu nang tot cho 1440p.", "14990000", 10, "2023, 16GB GDDR6, 256-bit, FSR, RDNA 3", "2023-09-06"),
            p(4, "NVIDIA GeForce RTX 4070 SUPER", "NVIDIA", "RTX 4070 SUPER", "VGA gaming 1440p rat manh.", "18990000", 10, "2024, 12GB GDDR6X, DLSS 3, ray tracing", "2024-01-17"),
            p(4, "NVIDIA GeForce RTX 4070 Ti SUPER", "NVIDIA", "RTX 4070 Ti SUPER", "VGA cao cap cho 1440p/4K.", "25990000", 8, "2024, 16GB GDDR6X, DLSS 3", "2024-01-24"),
            p(4, "NVIDIA GeForce RTX 4080 SUPER", "NVIDIA", "RTX 4080 SUPER", "VGA 4K cao cap.", "33990000", 5, "2024, 16GB GDDR6X, DLSS 3", "2024-01-31"),
            p(4, "AMD Radeon RX 7900 GRE", "AMD", "RX 7900 GRE", "VGA 1440p/4K can bang gia.", "16990000", 8, "2024, 16GB GDDR6, RDNA 3", "2024-02-27"),
            p(4, "NVIDIA GeForce RTX 5060", "NVIDIA", "RTX 5060", "GPU doi moi cho gaming 1080p/1440p.", "9990000", 10, "2025, GeForce RTX 50 Series, DLSS 4", "2025-05-01"),
            p(4, "NVIDIA GeForce RTX 5060 Ti", "NVIDIA", "RTX 5060 Ti", "GPU doi moi cho gaming pho thong cao.", "12990000", 8, "2025, GeForce RTX 50 Series, DLSS 4", "2025-04-01"),
            p(4, "NVIDIA GeForce RTX 5070", "NVIDIA", "RTX 5070", "GPU Blackwell cho gaming 1440p.", "19990000", 6, "2025, GeForce RTX 50 Series, DLSS 4", "2025-03-01"),
            p(4, "NVIDIA GeForce RTX 5070 Ti", "NVIDIA", "RTX 5070 Ti", "GPU 1440p/4K cao cap.", "26990000", 5, "2025, GeForce RTX 50 Series, DLSS 4", "2025-03-01"),
            p(4, "NVIDIA GeForce RTX 5080", "NVIDIA", "RTX 5080", "GPU Blackwell cao cap cho 4K.", "36990000", 4, "2025, GeForce RTX 50 Series, DLSS 4", "2025-01-30"),
            p(4, "NVIDIA GeForce RTX 5090", "NVIDIA", "RTX 5090", "GPU flagship cho 4K, AI va workstation ca nhan.", "69990000", 2, "2025, GeForce RTX 50 Series, VRAM lon, DLSS 4", "2025-01-30"),
            p(4, "AMD Radeon RX 9060 XT", "AMD", "RX 9060 XT", "GPU RDNA 4 tam trung.", "10990000", 8, "2025, RDNA 4, FSR 4", "2025-06-01"),
            p(4, "AMD Radeon RX 9070 XT", "AMD", "RX 9070 XT", "VGA RDNA 4 doi moi.", "21990000", 6, "2025, 16GB GDDR6, FSR 4, ray tracing cai thien", "2025-03-06"),

            p(5, "Kingston NV2 1TB", "Kingston", "NV2 1TB", "SSD NVMe gia re cho may pho thong.", "1190000", 35, "2022, M.2 NVMe, PCIe 4.0, 1TB", "2022-09-01"),
            p(5, "Samsung 970 EVO Plus 1TB", "Samsung", "970 EVO Plus 1TB", "SSD PCIe 3.0 ben bi cho may cu.", "1690000", 20, "2020, M.2 NVMe, PCIe 3.0, 1TB", "2020-01-01"),
            p(5, "WD Blue SN570 1TB", "WD", "SN570 1TB", "SSD NVMe can bang gia va do ben.", "1390000", 22, "2021, M.2 NVMe, PCIe 3.0, 1TB", "2021-10-01"),
            p(5, "Lexar NM790 1TB", "Lexar", "NM790 1TB", "SSD NVMe gia tot.", "1590000", 28, "2023, M.2 NVMe, PCIe 4.0, 1TB", "2023-04-01"),
            p(5, "Samsung 990 Pro 2TB", "Samsung", "990 Pro 2TB", "SSD NVMe cao cap PCIe 4.0.", "4290000", 22, "2023, M.2 NVMe, PCIe 4.0, doc den 7450MB/s", "2023-01-01"),
            p(5, "WD Black SN850X 2TB", "WD", "SN850X 2TB", "SSD gaming toc do cao.", "3990000", 18, "2022, M.2 NVMe, PCIe 4.0, doc den 7300MB/s", "2022-05-01"),
            p(5, "Crucial T700 2TB PCIe 5.0", "Crucial", "T700 2TB", "SSD PCIe 5.0 doi dau cho workstation.", "6490000", 8, "2023, M.2 NVMe, PCIe 5.0, 2TB", "2023-05-30"),
            p(5, "Crucial T705 2TB PCIe 5.0", "Crucial", "T705 2TB", "SSD PCIe 5.0 rat nhanh cho workstation.", "7490000", 10, "2024, M.2 NVMe, PCIe 5.0, doc den 14500MB/s", "2024-02-20"),
            p(5, "Kingston Fury Renegade G5 2TB", "Kingston", "Fury Renegade G5", "SSD PCIe 5.0 doi moi.", "6990000", 8, "2025, M.2 NVMe, PCIe 5.0, hieu nang cao", "2025-04-01"),

            p(6, "Cooler Master MWE Bronze 650 V2", "Cooler Master", "MWE Bronze 650 V2", "Nguon 650W gia tot cho build pho thong.", "1290000", 25, "2020, 650W, 80 Plus Bronze", "2020-01-01"),
            p(6, "Corsair CV750 80 Plus Bronze", "Corsair", "CV750", "Nguon 750W cho PC tam trung.", "1690000", 18, "2021, 750W, 80 Plus Bronze", "2021-01-01"),
            p(6, "MSI MAG A650BN", "MSI", "MAG A650BN", "Nguon pho thong cho gaming 1080p.", "1190000", 30, "2022, 650W, 80 Plus Bronze", "2022-01-01"),
            p(6, "MSI MAG A750GL PCIE5", "MSI", "A750GL PCIE5", "Nguon ATX 3.0 gia tot.", "2390000", 22, "2023, 750W, 80 Plus Gold, PCIe 5.0", "2023-06-01"),
            p(6, "Cooler Master MWE Gold 850 V2 ATX 3.0", "Cooler Master", "MWE Gold 850 V2", "Nguon 850W gia tot.", "2790000", 20, "2024, 850W, 80 Plus Gold, ATX 3.0", "2024-01-01"),
            p(6, "Corsair RM850x Shift ATX 3.1", "Corsair", "RM850x Shift", "Nguon cao cap cho GPU doi moi.", "3590000", 14, "2024, 850W, 80 Plus Gold, ATX 3.1, PCIe 5.1", "2024-06-01"),
            p(6, "Seasonic Vertex GX-1000 ATX 3.0", "Seasonic", "Vertex GX-1000", "Nguon 1000W cho build RTX cao cap.", "5490000", 8, "2024, 1000W, 80 Plus Gold, ATX 3.0, 12VHPWR", "2024-01-01"),
            p(6, "ASUS ROG Thor 1200P2 Gaming", "ASUS", "ROG Thor 1200P2", "Nguon flagship co man OLED.", "8990000", 5, "2023, 1200W, Platinum, OLED display, modular", "2023-05-01"),

            p(7, "DeepCool AK400", "DeepCool", "AK400", "Tan khi don tower gia tot.", "690000", 30, "2022, air cooler, 4 heatpipe, 120mm fan", "2022-01-01"),
            p(7, "Thermalright Peerless Assassin 120 SE", "Thermalright", "Peerless Assassin 120 SE", "Tan khi hieu nang cao gia tot.", "990000", 25, "2021, dual tower, 6 heatpipe, 2 fan", "2021-01-01"),
            p(7, "DeepCool AK620 Digital", "DeepCool", "AK620 Digital", "Tan khi manh co man hien thi.", "1890000", 18, "2023, dual tower, display, 2 fan 120mm", "2023-05-01"),
            p(7, "Thermalright Phantom Spirit 120 EVO", "Thermalright", "Phantom Spirit 120 EVO", "Tan khi hieu nang/gia rat tot.", "1390000", 20, "2024, dual tower, 7 heatpipe, 2 fan", "2024-01-10"),
            p(7, "Arctic Liquid Freezer III 360 ARGB", "Arctic", "Liquid Freezer III 360", "AIO hieu nang cao gia tot.", "3790000", 12, "2024, 360mm AIO, ARGB, radiator day", "2024-02-01"),
            p(7, "Corsair iCUE Link H150i LCD 360", "Corsair", "H150i LCD", "AIO 360mm co man hinh LCD.", "6490000", 8, "2024, 360mm AIO, LCD, iCUE Link, RGB", "2024-01-01"),
            p(7, "Noctua NH-D15 G2", "Noctua", "NH-D15 G2", "Tan khi flagship doi moi.", "3990000", 9, "2024, dual tower, 2 fan 140mm", "2024-07-01"),

            p(8, "Montech Air 100 ARGB", "Montech", "Air 100 ARGB", "Case mATX airflow gia tot.", "1090000", 24, "2021, mATX, airflow, 4 fan ARGB", "2021-01-01"),
            p(8, "NZXT H510 Flow", "NZXT", "H510 Flow", "Case gon gang, airflow tot hon H510 cu.", "1990000", 12, "2021, ATX, mesh front, tempered glass", "2021-10-01"),
            p(8, "Lian Li Lancool 216 RGB", "Lian Li", "Lancool 216 RGB", "Case airflow tot cho build gaming.", "2590000", 16, "2022, ATX, airflow, 2 fan 160mm", "2022-11-01"),
            p(8, "NZXT H6 Flow RGB", "NZXT", "H6 Flow RGB", "Case airflow hien dai.", "3290000", 14, "2023, ATX, dual chamber, RGB, tempered glass", "2023-10-01"),
            p(8, "Lian Li O11 Vision Compact", "Lian Li", "O11 Vision Compact", "Case kinh dep cho build trung bay.", "3690000", 10, "2024, ATX, tempered glass, airflow layout", "2024-09-01"),
            p(8, "Corsair 3500X ARGB", "Corsair", "3500X ARGB", "Case kinh rong, ho tro tan lon.", "2890000", 14, "2024, ATX, panoramic glass, ARGB", "2024-06-01"),
            p(8, "Hyte Y70 Touch Infinite", "HYTE", "Y70 Touch Infinite", "Case flagship co man hinh cam ung.", "11990000", 4, "2024, dual chamber, touch display, panoramic glass", "2024-08-01"),

            p(9, "AOC 24G2SP 24 inch", "AOC", "24G2SP", "Man gaming FHD pho thong.", "3490000", 18, "2021, 24 inch, Full HD, IPS, 165Hz", "2021-01-01"),
            p(9, "LG UltraGear 27GP850 27 inch", "LG", "27GP850", "Man QHD 165Hz rat pho bien cho gaming.", "6990000", 14, "2021, 27 inch, QHD, Nano IPS, 165Hz", "2021-06-01"),
            p(9, "Gigabyte M27Q X 27 inch", "Gigabyte", "M27Q X", "Man gaming QHD toc do cao.", "10990000", 8, "2022, 27 inch, QHD, IPS, 240Hz", "2022-01-01"),
            p(9, "Dell UltraSharp U2724D 27 inch", "Dell", "U2724D", "Man van phong/do hoa mau tot.", "8990000", 8, "2023, 27 inch, QHD, IPS Black, 120Hz", "2023-11-01"),
            p(9, "AOC Q27G3XMN Mini LED 27 inch", "AOC", "Q27G3XMN", "Man Mini LED HDR gia tot.", "8990000", 10, "2024, 27 inch, QHD, Mini LED, 180Hz, HDR1000", "2024-02-01"),
            p(9, "MSI MPG 321URX QD-OLED 32 inch", "MSI", "MPG 321URX", "Man OLED 4K gaming cao cap.", "29990000", 5, "2024, 32 inch, 4K, QD-OLED, 240Hz", "2024-03-01"),
            p(9, "ASUS ROG Swift PG32UCDM OLED 32 inch", "ASUS", "PG32UCDM", "Man OLED 4K cao cap cho gaming.", "36990000", 4, "2024, 32 inch, 4K, QD-OLED, 240Hz", "2024-01-01"),

            p(10, "Logitech G Pro X Superlight", "Logitech", "G Pro X Superlight", "Chuot gaming khong day sieu nhe.", "2490000", 20, "2020, wireless, eSports mouse", "2020-12-01"),
            p(10, "Logitech MX Master 3S", "Logitech", "MX Master 3S", "Chuot lam viec cao cap.", "2390000", 18, "2022, wireless, silent click, MagSpeed", "2022-05-24"),
            p(10, "SteelSeries Apex Pro TKL Wireless", "SteelSeries", "Apex Pro TKL Wireless", "Ban phim gaming Hall Effect.", "6290000", 8, "2023, TKL, wireless, adjustable actuation", "2023-01-01"),
            p(10, "Logitech G Pro X Superlight 2", "Logitech", "Superlight 2", "Chuot gaming khong day eSports.", "3490000", 18, "2023, wireless, HERO 2, USB-C", "2023-09-05"),
            p(10, "Keychron Q1 HE Magnetic Switch", "Keychron", "Q1 HE", "Ban phim co Hall Effect cao cap.", "5990000", 8, "2024, 75 percent, magnetic switch, rapid trigger", "2024-06-01"),
            p(10, "Razer BlackShark V2 Pro 2024", "Razer", "BlackShark V2 Pro", "Tai nghe gaming khong day.", "4490000", 10, "2024, wireless, mic HyperClear, pin lau", "2024-01-01"),
            p(10, "Elgato Facecam Pro", "Elgato", "Facecam Pro", "Webcam 4K cho streamer.", "6990000", 6, "2023, 4K60, USB-C, fixed focus pro lens", "2023-01-01")
    };
}
