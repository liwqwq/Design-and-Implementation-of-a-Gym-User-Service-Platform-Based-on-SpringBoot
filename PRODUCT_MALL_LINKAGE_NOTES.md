# Product Mall Linkage Notes

This update makes the administrator product page manage the same data source used by the user points mall.

## Fixed issues

1. Admin product management previously used the `products` table, while the user points mall used `points_products`.
2. Adding or editing products in the admin panel did not reliably change the user points mall.
3. Admin product names did not switch language because products only had one name field.

## Main changes

- `admin-products.vue` now calls `/api/points/admin/products`.
- `PointsProduct` now supports `nameEn` and `descriptionEn`.
- The admin add/edit form includes an English product name and English description.
- User mall and admin product cards display `nameEn` / `descriptionEn` when the current language is English.
- Existing demo points products receive English names/descriptions at startup.
- Existing legacy records from the old `products` table are copied into `points_products` when missing, so old admin-created products can appear in the points mall.

## Database note

`spring.jpa.hibernate.ddl-auto=update` will automatically add the new columns:

- `points_products.name_en`
- `points_products.description_en`
- `points_exchange.product_name_en`
