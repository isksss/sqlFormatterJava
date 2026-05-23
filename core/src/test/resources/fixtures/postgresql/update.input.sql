update inventory set quantity=quantity-:count,updated_at=now() where sku=$1 and quantity>=:count returning sku,quantity;
